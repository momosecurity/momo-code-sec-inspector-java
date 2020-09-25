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
package com.immomo.momosec.fix;

import com.immomo.momosec.lang.java.utils.MoExpressionUtils;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class ShowHelpCommentQuickFix implements LocalQuickFix {
    private final String QUICK_FIX_NAME;
    private final String COMMENT;


    public ShowHelpCommentQuickFix(String name, String comment) {
        this.QUICK_FIX_NAME = name;
        this.COMMENT = comment;
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String getFamilyName() {
        return QUICK_FIX_NAME;
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);
        PsiComment comment = factory.createCommentFromText(COMMENT, null);

        PsiElement currElem = MoExpressionUtils.getParentOfStatement(descriptor.getPsiElement());
        if (currElem == null) {
            currElem = MoExpressionUtils.getParentOfField(descriptor.getPsiElement());
        }
        if (currElem == null) {
            return ;
        }
        PsiElement prev = currElem.getPrevSibling();

        PsiElement prevParent;
        if (prev == null) {
            prevParent = currElem.getParent();
        } else {
            prevParent = prev.getParent();
        }
        prevParent.addAfter(comment, prev);
    }
}
