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
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 *  1019: SpringSecurity关闭Debug模式
 *
 *  ref: https://rules.sonarsource.com/java/type/Security%20Hotspot/RSPEC-4507
 */
public class SpringSecurityDebugEnabled extends MomoBaseLocalInspectionTool {
    public static final String MESSAGE = InspectionBundle.message("spring.security.debug.enabled.msg");
    private static final String QUICK_FIX_NAME = InspectionBundle.message("spring.security.debug.enabled.fix");
    private final SpringSecurityDebugDisable springSecurityDebugDisable = new SpringSecurityDebugDisable();

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitAnnotation(PsiAnnotation annotation) {
                if ("org.springframework.security.config.annotation.web.configuration.EnableWebSecurity".equals(annotation.getQualifiedName())) {
                    PsiAnnotationParameterList annotationParameterList = annotation.getParameterList();
                    PsiNameValuePair[] nameValuePairs = annotationParameterList.getAttributes();
                    for(PsiNameValuePair nameValuePair : nameValuePairs) {
                        if ("debug".equals(nameValuePair.getName()) &&
                            "true".equals(nameValuePair.getLiteralValue())
                        ) {
                            holder.registerProblem(
                                    nameValuePair,
                                    MESSAGE,
                                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                    springSecurityDebugDisable
                            );
                        }
                    }
                }
            }
        };
    }

    public static class SpringSecurityDebugDisable implements LocalQuickFix {

        @Override
        public @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String getFamilyName() {
            return QUICK_FIX_NAME;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiNameValuePair nameValuePair = (PsiNameValuePair)descriptor.getPsiElement();
            if ("debug".equals(nameValuePair.getName())) {
                PsiAnnotationMemberValue member = nameValuePair.getValue();
                PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);
                if (member != null) {
                    member.replace(factory.createExpressionFromText("false", null));
                }
            }

        }
    }
}
