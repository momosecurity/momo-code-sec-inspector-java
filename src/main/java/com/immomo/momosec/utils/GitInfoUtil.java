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
package com.immomo.momosec.utils;

import com.immomo.momosec.entity.GitInfo;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcs.log.VcsUser;
import git4idea.GitUserRegistry;
import git4idea.GitUtil;
import git4idea.repo.GitRemote;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import org.jetbrains.annotations.Nullable;

public class GitInfoUtil {

    @Nullable
    public static GitInfo getGitInfo(Project project) {
        GitInfo gitInfo = new GitInfo();

        VirtualFile workspaceFile = project.getWorkspaceFile();
        if (workspaceFile == null) {
            return null;
        }

        VirtualFile root = workspaceFile.getParent().getParent();
        VirtualFile virtualFileGit = root.findChild(".git");
        if (virtualFileGit == null) {
            return null;
        }

        GitRepository gitRepository = GitRepositoryManager.getInstance(project).getRepositoryForRoot(root);
        if (gitRepository == null) {
            return null;
        }

        VcsUser user = GitUserRegistry.getInstance(project).getOrReadUser(root);
        if (user != null) {
            gitInfo.setUserName(user.getName());
            gitInfo.setUserEmail(user.getEmail());
        }

        GitRemote gitRemote = GitUtil.getDefaultOrFirstRemote(gitRepository.getRemotes());
        if (gitRemote != null) {
            gitInfo.setAddress(gitRemote.getFirstUrl());
        }
        gitInfo.setBranch(gitRepository.getCurrentBranchName());

        return gitInfo;
    }
}
