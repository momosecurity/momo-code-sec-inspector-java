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
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import static com.immomo.momosec.Constants.HTTP_TIMEOUT;

/**
 * 漏洞签名白名单服务
 * Application Level Service
 *
 * 利用漏洞签名排除部分误报
 * 实现了StartupActivity接口，会在Project开启时拉取一次白名单，并覆盖已存在的白名单内容
 */
@State(name = "VulnSignWhiteList", storages = @Storage("MomoInspector.xml"))
public class VulnSignWhiteListService implements PersistentStateComponent<VulnSignWhiteListService.State>, StartupActivity {
    private static State state = new State();
    private final Type type = new TypeToken<Set<Integer>>(){}.getType();

    public static class State {
        public final Set<Integer> vulnSigns = new HashSet<Integer>() {{
            add(-844466204);
            add(1887135609);
            add(-1599760505);
        }};
    }

    public static VulnSignWhiteListService getInstance() {
        return ApplicationManager.getApplication().getComponent(VulnSignWhiteListService.class);
    }

    public State getState() {
        return state;
    }

    public void loadState(@NotNull State state) {
        VulnSignWhiteListService.state = state;
    }

    public boolean isInWhiteList(int sign) {
        return state.vulnSigns.contains(sign);
    }

    @Override
    public void runActivity(@NotNull Project project) {
        new Thread(this::reloadVulnSigns).start();
    }

    private void reloadVulnSigns() {
        if (Constants.VULN_SIGN_WHITE_LIST_ENDPOINT == null || "".equals(Constants.VULN_SIGN_WHITE_LIST_ENDPOINT)) { return ; }

        HttpGet request = new HttpGet(Constants.VULN_SIGN_WHITE_LIST_ENDPOINT);
        request.addHeader("Content-Type", "application/json; charset=UTF-8");
        HttpClient client = HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(HTTP_TIMEOUT)
                        .setConnectionRequestTimeout(HTTP_TIMEOUT)
                        .setSocketTimeout(HTTP_TIMEOUT)
                        .build())
                .setRedirectStrategy(new LaxRedirectStrategy())
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .build();
        try {
            HttpResponse response = client.execute(request);
            if (response.getStatusLine().getStatusCode() != 200) {
                return ;
            }

            JsonParser parser = new JsonParser();
            JsonObject object = parser.parse(
                    new BufferedReader(new InputStreamReader(response.getEntity().getContent()))).getAsJsonObject();
            state.vulnSigns.clear();
            state.vulnSigns.addAll(
                    (new Gson()).fromJson(object.getAsJsonArray("vulnSignWhiteList"), type));

        } catch (Exception ignore) {}
    }
}
