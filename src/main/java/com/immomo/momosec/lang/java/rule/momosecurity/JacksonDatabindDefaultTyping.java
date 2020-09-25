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

import com.immomo.momosec.fix.DeleteElementQuickFix;
import com.immomo.momosec.lang.MomoBaseFixElementWalkingVisitor;
import com.immomo.momosec.lang.MomoBaseLocalInspectionTool;
import com.immomo.momosec.lang.java.utils.MoExpressionUtils;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * 1006: Jackson反序列化风险
 *
 * com.fasterxml.jackson.core:jackson-databind 在开启DefaultTyping时，存在反序列化风险
 *
 * 开启方式
 * (1) ObjectMapper.enableDefaultTyping();  enableDefaultTyping空参数与有参数方法，均受影响
 * (2) Annotation: @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
 * (3) Annotation: @JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS)
 */
public class JacksonDatabindDefaultTyping extends MomoBaseLocalInspectionTool {
    public static final String MESSAGE = "MomoSec: 发现Jackson反序列化风险";
    private static final String DEFAULT_TYPING_FIX_NAME = "!Fix: 移除 enableDefaultTyping";
    private static final String ANNOTATION_FIX_NAME = "!Fix: replace with JsonTypeInfo.Id.NAME";

    private final AnnotationQuickFix annotationQuickFix = new AnnotationQuickFix();

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                if (MoExpressionUtils.hasFullQualifiedName(expression, "com.fasterxml.jackson.databind.ObjectMapper", "enableDefaultTyping")) {
                    PsiExpression varExp = expression.getMethodExpression().getQualifierExpression();
                    if (varExp != null &&  varExp.getReference() != null) {
                        PsiElement var = varExp.getReference().resolve();
                        if (var != null) {
                            UseToJackson2JsonRedisSerializerVisitor visitor = new UseToJackson2JsonRedisSerializerVisitor(var);
                            if (checkVariableUseFix(var, null, visitor)) {
                                return ;
                            }
                        }
                    }

                    holder.registerProblem(
                            expression,
                            MESSAGE,
                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                            new DeleteElementQuickFix(expression, DEFAULT_TYPING_FIX_NAME)
                    );
                }
            }

            @Override
            public void visitAnnotation(PsiAnnotation annotation) {
                if ("com.fasterxml.jackson.annotation.JsonTypeInfo".equals(annotation.getQualifiedName())) {
                    PsiAnnotationParameterList psiAnnotationParameterList = annotation.getParameterList();
                    PsiNameValuePair[] nameValuePairs = psiAnnotationParameterList.getAttributes();
                    for (PsiNameValuePair nameValuePair : nameValuePairs) {
                        if ("use".equals(nameValuePair.getName())) {
                            PsiAnnotationMemberValue annotationValue = nameValuePair.getValue();
                            if (annotationValue != null &&
                                (
                                    "JsonTypeInfo.Id.CLASS".equals(annotationValue.getText()) ||
                                    "JsonTypeInfo.Id.MINIMAL_CLASS".equals(annotationValue.getText())
                                )
                            ) {
                                holder.registerProblem(nameValuePair, MESSAGE, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, annotationQuickFix);
                                break;
                            }
                        }
                    }
                }
            }
        };
    }

    public static class UseToJackson2JsonRedisSerializerVisitor extends MomoBaseFixElementWalkingVisitor {
        private final PsiElement refVar;

        public UseToJackson2JsonRedisSerializerVisitor(PsiElement elem) {
            this.refVar = elem;
        }

        @Override
        public void visitElement(PsiElement element) {
            if (element instanceof PsiMethodCallExpression) {
                PsiMethodCallExpression methodCallExpression = (PsiMethodCallExpression)element;
                if (MoExpressionUtils.hasFullQualifiedName(methodCallExpression, "org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer", "setObjectMapper")) {
                    PsiExpressionList args = methodCallExpression.getArgumentList();
                    if (args.getExpressions().length != 1) { return ; }
                    if (args.getExpressions()[0] instanceof PsiReferenceExpression) {
                        PsiReference refElem = args.getExpressions()[0].getReference();
                        if (refElem != null && refVar.isEquivalentTo(refElem.resolve())) {
                            this.setFix(true);
                            this.stopWalking();
                            return  ;
                        }
                    }
                }
            }
            super.visitElement(element);
        }
    }

    public static class AnnotationQuickFix implements LocalQuickFix {

        @Override
        public @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String getFamilyName() {
            return ANNOTATION_FIX_NAME;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            // getPsiElement() must return PsiNameValuePair
            PsiNameValuePair nameValuePair = (PsiNameValuePair)descriptor.getPsiElement();
            PsiAnnotationMemberValue member = nameValuePair.getValue();
            if (member != null) {
                String[] splitText = member.getText().split("\\.");
                if (splitText.length > 1 &&
                    (splitText[splitText.length-1].equals("CLASS") || splitText[splitText.length-1].equals("MINIMAL_CLASS"))
                ) {
                    splitText[splitText.length-1] = "NAME";
                }

                PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);
                member.replace(factory.createExpressionFromText(String.join(".", splitText), null));
            }
        }
    }
}
