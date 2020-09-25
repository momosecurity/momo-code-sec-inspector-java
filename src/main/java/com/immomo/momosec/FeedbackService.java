/*
 * Copyright 2020 momosecurity.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.immomo.momosec;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.immomo.momosec.entity.GitInfo;
import com.immomo.momosec.lang.java.utils.MoExpressionUtils;
import com.immomo.momosec.utils.GitInfoUtil;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.psi.PsiElement;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static com.immomo.momosec.Constants.*;
import static com.immomo.momosec.lang.MomoBaseLocalInspectionTool.getVulnSign;

/**
 * 漏洞上报服务
 * Project Level Service
 *
 * 实现了Disposable接口，会在Project关闭或Application关闭时执行一次上报。
 * 正常情况下，在记录满足POST_DATA_MIN_NUM数量时会执行一次上报。
 */
@SuppressWarnings(value = {"unchecked"})
public class FeedbackService implements Disposable {
    private static final Logger LOG = Logger.getInstance(FeedbackService.class);
    private static final int POST_DATA_MIN_NUM = 30;
    private final Project project;
    private GitInfo gitInfo;
    private final HashMap<Integer, JsonObject> vulns = new HashMap<>();

    public FeedbackService(Project project) {
        this.project = project;
        this.loadGitInfo();

        LocalFileSystem.getInstance().addVirtualFileListener(new FeedbackFileListener(project));
    }

    public void loadGitInfo() {
        this.gitInfo = GitInfoUtil.getGitInfo(project);
    }

    public void markupVuln(PsiElement element, String message) {
        String elementText = element.getText();
        String fqname = MoExpressionUtils.getElementFQName(element);
        int sign = getVulnSign(fqname, elementText);

        if (vulns.containsKey(sign)) { return ; }
        if (vulns.size() > POST_DATA_MIN_NUM) {
            feedbackMarkupVulns();
        }

        LOG.info(String.format("markupVuln fqname:[%s] sign:[%d] text:[%s]", fqname, sign, elementText));

        JsonObject vuln = new JsonObject();
        vuln.addProperty("projectName", project.getName());
        vuln.addProperty("fqname", fqname);
        vuln.addProperty("message", message);
        vuln.addProperty("elemText", elementText);
        vulns.put(sign, vuln);
    }

    public void markupVulnFix(PsiElement element) {
        int sign = getVulnSign(MoExpressionUtils.getElementFQName(element), element.getText());
        if (vulns.containsKey(sign)) {
            vulns.get(sign).addProperty("status", 1);
        }
    }

    public void feedbackMarkupVulns() {
        HashMap<Integer, JsonObject> vulnsClone = (HashMap<Integer, JsonObject>) vulns.clone();
        vulns.clear();
        new Thread(() -> {
            feedbackMarkupVulns(vulnsClone, gitInfo);
            loadGitInfo();
        }).start();
    }

    private void feedbackMarkupVulns(Map<Integer, JsonObject> vulns, GitInfo gitInfo) {
        if (vulns.size() == 0) { return ; }

        String gitInfoString = "null (null)";
        String user = "null<null>";
        if (gitInfo != null) {
            gitInfoString = gitInfo.getGitAddrWithBranch();
            user = gitInfo.getUserWithEmail();
        }

        Gson gson = new Gson();
        JsonObject data = new JsonObject();
        data.addProperty("gitInfo", gitInfoString);
        data.addProperty("user", user);
        data.addProperty("plugin_version", PLUGIN_VERSION);
        data.add("vulns", gson.toJsonTree(vulns));
        try{
            postMarkupVulns(gson.toJson(data));
        } catch (Exception e) {
            LOG.warn("Feedback Post Vulns Failed.");
        } finally {
            vulns.clear();
        }
    }

    private void postMarkupVulns(String data) throws IOException {
        if (!"prod".equals(PLUGIN_ENV) && !"pre".equals(PLUGIN_ENV)) { return ; }
        if (FEEDBACK_ENDPOINT == null || "".equals(FEEDBACK_ENDPOINT)) { return ; }

        HttpPost request = new HttpPost(FEEDBACK_ENDPOINT);
        request.addHeader("content-type", "application/json; charset=UTF-8");
        request.setEntity(new StringEntity(data, StandardCharsets.UTF_8));

        HttpClient client = HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(HTTP_TIMEOUT)
                        .setConnectionRequestTimeout(HTTP_TIMEOUT)
                        .setSocketTimeout(HTTP_TIMEOUT)
                        .build())
                .setRedirectStrategy(new LaxRedirectStrategy())
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .build();
        client.execute(request);
    }

    private String getCurrFilename() {
        FileEditor fileEditor = FileEditorManager.getInstance(project).getSelectedEditor();
        VirtualFile virtualFile;
        String filename = "";
        if (fileEditor != null) {
            virtualFile = fileEditor.getFile();
            if (virtualFile != null && virtualFile.getCanonicalPath() != null) {
                if  (project.getBasePath() != null && virtualFile.getCanonicalPath().startsWith(project.getBasePath())) {
                    filename = virtualFile.getCanonicalPath().substring(project.getBasePath().length());
                } else {
                    filename = virtualFile.getCanonicalPath();
                }
            }
        }
        return filename;
    }

    @Override
    public void dispose() {
        // run on project close or application shutdown
        feedbackMarkupVulns(vulns, gitInfo);
    }


    public static class FeedbackFileListener implements VirtualFileListener {

        private final Project project;

        public  FeedbackFileListener(Project project) {
            this.project = project;
        }

        @Override
        public void beforeContentsChange(@NotNull VirtualFileEvent event) {
            if (project.isDisposed()) {
                return ;
            }

            // post feedback data when git branch changed
            if(event.getFile().getPath().endsWith(".git/HEAD")) {
                FeedbackService feedbackService = ServiceManager.getService(project, FeedbackService.class);
                if (feedbackService != null) {
                    feedbackService.feedbackMarkupVulns();
                }
            }
        }
    }
}
