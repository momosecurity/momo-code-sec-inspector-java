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
package com.immomo.momosec.lang.java.rule.momosecurity;

import com.immomo.momosec.lang.InspectionBundle;
import com.immomo.momosec.lang.MomoBaseLocalInspectionTool;
import com.immomo.momosec.lang.java.utils.MoExpressionUtils;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * Momo 1022: Spring 会话固定攻击风险
 *
 * 默认开启，但使用sessionFixation().none()会关闭
 *
 * ref: https://rules.sonarsource.com/java/type/Vulnerability/RSPEC-5876
 */
public class SpringSessionFixProtection extends MomoBaseLocalInspectionTool {
    public static final String MESSAGE = InspectionBundle.message("spring.session.fix.protection.msg");
    public static final String QUICK_FIX_NAME = InspectionBundle.message("spring.session.fix.protection.fix");

    private final SpringSessionFixProtectionQuickFix springSessionFixProtectionQuickFix = new SpringSessionFixProtectionQuickFix();

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                if (MoExpressionUtils.hasFullQualifiedName(
                        expression,
                        "org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer.SessionFixationConfigurer",
                        "none")
                ) {
                    holder.registerProblem(expression, MESSAGE, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, springSessionFixProtectionQuickFix);
                }
            }
        };
    }

    public static class SpringSessionFixProtectionQuickFix implements LocalQuickFix {
        @Override
        public @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String getFamilyName() {
            return QUICK_FIX_NAME;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiMethodCallExpression methodCallExp = ObjectUtils.tryCast(descriptor.getPsiElement(), PsiMethodCallExpression.class);
            if (methodCallExp == null) {
                return ;
            }

            PsiReferenceExpression referenceExpression = methodCallExp.getMethodExpression();
            if ("none".equals(referenceExpression.getReferenceName())) {
                PsiIdentifier identifier = ObjectUtils.tryCast(referenceExpression.getLastChild(), PsiIdentifier.class);
                if (identifier != null) {
                    identifier.replace(JavaPsiFacade.getElementFactory(project).createIdentifier("migrateSession"));
                }
            }
        }
    }
}
