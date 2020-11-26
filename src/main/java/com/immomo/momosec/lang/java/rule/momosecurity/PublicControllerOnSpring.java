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

import java.util.HashSet;
import java.util.Set;

/**
 * 1021: "@RequestMapping" 方法应当为 "public"
 *
 * SpringBoot中，以@RequestMapping等注解的方法，尽管使用private描述符也可被api映射。
 * 因此，对api使用private是个迷惑的写法，应一直使用public。
 *
 * 该Inspector会检查如下注解
 * \@RequestMapping
 * \@GetMapping
 * \@PostMapping
 * \@PutMapping
 * \@DeleteMapping
 * \@PatchMapping
 *
 * ref: https://rules.sonarsource.com/java/type/Vulnerability/RSPEC-3751
 */
public class PublicControllerOnSpring extends MomoBaseLocalInspectionTool {
    public static final String MESSAGE = InspectionBundle.message("public.controller.on.spring.msg");
    private static final String QUICK_FIX_NAME = InspectionBundle.message("public.controller.on.spring.fix");

    private static final Set<String> requestMappingAnnotations = new HashSet<String>() {{
        add("org.springframework.web.bind.annotation.RequestMapping");
        add("org.springframework.web.bind.annotation.GetMapping");
        add("org.springframework.web.bind.annotation.PostMapping");
        add("org.springframework.web.bind.annotation.PutMapping");
        add("org.springframework.web.bind.annotation.DeleteMapping");
        add("org.springframework.web.bind.annotation.PatchMapping");
    }};

    private final PublicControllerOnSpringQuickFix publicControllerOnSpringQuickFix = new PublicControllerOnSpringQuickFix();

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitAnnotation(PsiAnnotation annotation) {
                if (Boolean.FALSE.equals(requestMappingAnnotations.contains(annotation.getQualifiedName()))) {
                    return ;
                }

                PsiModifierList modifierList = ObjectUtils.tryCast(annotation.getParent(), PsiModifierList.class);
                if (modifierList == null) {
                    return ;
                }

                if (!modifierList.hasModifierProperty(PsiModifier.PUBLIC)) {
                    holder.registerProblem(
                            modifierList,
                            MESSAGE,
                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                            publicControllerOnSpringQuickFix
                    );
                }
            }
        };
    }

    public static class PublicControllerOnSpringQuickFix implements LocalQuickFix {

        @Override
        public @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String getFamilyName() {
            return QUICK_FIX_NAME;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElement elem = descriptor.getPsiElement();
            PsiModifierList modifierList = ObjectUtils.tryCast(elem, PsiModifierList.class);
            if (modifierList == null) {
                return ;
            }

            for (PsiElement child : modifierList.getChildren()) {
                if (child instanceof PsiKeyword &&
                    (child.textMatches(PsiKeyword.PRIVATE) || child.textMatches(PsiKeyword.PROTECTED))
                ) {
                    child.delete();
                    break;
                }
            }

            PsiElement lastElement = modifierList.getLastChild();
            if (lastElement instanceof PsiKeyword) {
                modifierList.addBefore(JavaPsiFacade.getElementFactory(project).createKeyword(PsiKeyword.PUBLIC), lastElement);
            } else {
                modifierList.add(JavaPsiFacade.getElementFactory(project).createKeyword(PsiKeyword.PUBLIC));
            }

        }

    }
}    
