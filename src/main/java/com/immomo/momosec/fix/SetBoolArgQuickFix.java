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

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class SetBoolArgQuickFix implements LocalQuickFix {
    private final String QUICK_FIX_NAME;
    private final Boolean b;
    private SmartPsiElementPointer<PsiLiteralExpression> argPointer;

    public SetBoolArgQuickFix(String name, Boolean b) {
        this.QUICK_FIX_NAME = name;
        this.b = b;
        this.argPointer = null;
    }

    public SetBoolArgQuickFix(String name, Boolean b, PsiLiteralExpression arg) {
        this.QUICK_FIX_NAME = name;
        this.b = b;
        this.argPointer = SmartPointerManager.createPointer(arg);
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String getFamilyName() {
        return QUICK_FIX_NAME;
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        if (this.argPointer == null) {
            PsiLiteralExpression arg = ObjectUtils.tryCast(descriptor.getPsiElement(), PsiLiteralExpression.class);
            if (arg == null) return ;
            this.argPointer = SmartPointerManager.createPointer(arg);
        }

        PsiLiteralExpression targetArg = this.argPointer.getElement();
        if (targetArg == null) return ;
        PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);
        PsiLiteralExpression newArg = (PsiLiteralExpression)factory.createExpressionFromText(b.toString(), null);
        targetArg.replace(newArg);
    }
}
