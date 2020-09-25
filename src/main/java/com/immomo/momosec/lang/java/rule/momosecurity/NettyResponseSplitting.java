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
 * 1008: Netty响应拆分攻击
 *
 * ref:
 * (1) https://github.com/github/codeql/blob/main/java/ql/src/Security/CWE/CWE-113/NettyResponseSplitting.java
 * (2) http://www.infosecwriters.com/Papers/DCrab_HTTP_Response.pdf
 */
public class NettyResponseSplitting extends MomoBaseLocalInspectionTool {
    public static final String MESSAGE = "MomoSec: Netty响应拆分攻击";
    private static final String QUICK_FIX_NAME = "!Fix: 开启验证";

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitNewExpression(PsiNewExpression expression) {
                if (MoExpressionUtils.hasFullQualifiedName(expression, "io.netty.handler.codec.http.DefaultHttpHeaders")) {
                    if (expression.getArgumentList() != null) {
                        PsiExpression[] args = expression.getArgumentList().getExpressions();
                        if (args.length > 0 && args[0] instanceof PsiLiteralExpression &&
                            Boolean.FALSE.equals(((PsiLiteralExpression)args[0]).getValue())
                        ) {
                            holder.registerProblem(
                                    expression,
                                    MESSAGE,
                                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                    new NettyResponseSplittingQuickFix(0)
                            );
                        }
                    }
                }
                if (MoExpressionUtils.hasFullQualifiedName(expression, "io.netty.handler.codec.http.DefaultHttpResponse")) {
                    if (expression.getArgumentList() != null) {
                        PsiExpression[] args = expression.getArgumentList().getExpressions();

                        // DefaultHttpResponse 第2位参数会有问题 (0位算起)
                        if (args.length > 2 && args[2] instanceof PsiLiteralExpression &&
                            Boolean.FALSE.equals(((PsiLiteralExpression)args[2]).getValue())
                        ) {
                            holder.registerProblem(
                                    expression,
                                    MESSAGE,
                                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                    new NettyResponseSplittingQuickFix(2)
                            );
                        }
                    }
                }
            }
        };
    }

    public static class NettyResponseSplittingQuickFix implements LocalQuickFix {

        private int fixArgIndex;

        public NettyResponseSplittingQuickFix(int fixArgIndex) {
            this.fixArgIndex = fixArgIndex;
        }

        @Override
        public @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String getFamilyName() {
            return QUICK_FIX_NAME;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiNewExpression expression = (PsiNewExpression) descriptor.getPsiElement();
            if (expression.getArgumentList() != null) {
                PsiExpression[] args = expression.getArgumentList().getExpressions();
                if (args.length > fixArgIndex && args[fixArgIndex] instanceof PsiLiteralExpression) {
                    PsiLiteralExpression problemExpression = (PsiLiteralExpression)args[fixArgIndex];
                    PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);
                    problemExpression.replace(factory.createExpressionFromText("true", null));
                }
            }
        }
    }
}
