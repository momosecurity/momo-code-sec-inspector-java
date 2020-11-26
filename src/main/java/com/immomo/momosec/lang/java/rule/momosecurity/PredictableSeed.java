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
import com.immomo.momosec.lang.InspectionBundle;
import com.immomo.momosec.lang.MomoBaseLocalInspectionTool;
import com.immomo.momosec.lang.java.utils.MoExpressionUtils;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.graphInference.PsiPolyExpressionUtil;
import com.intellij.psi.util.PsiLiteralUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * 1009: 固定的随机数种子风险
 *
 * ref: https://rules.sonarsource.com/java/type/Vulnerability/RSPEC-4347
 */
public class PredictableSeed extends MomoBaseLocalInspectionTool {
    public static final String MESSAGE = InspectionBundle.message("predictable.seed.msg");
    private static final String QUICK_FIX_NAME = InspectionBundle.message("predictable.seed.fix");

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                if (MoExpressionUtils.hasFullQualifiedName(expression, "java.security.SecureRandom", "setSeed")) {
                    PsiExpression[] args = expression.getArgumentList().getExpressions();
                    if (args.length != 1) { return ; }
                    PsiExpression arg0 = args[0];
                    if (arg0 instanceof PsiLiteralExpression) {
                        holder.registerProblem(
                                expression,
                                MESSAGE,
                                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                new DeleteElementQuickFix(expression, QUICK_FIX_NAME)
                        );
                    }
                }
            }

            @Override
            public void visitNewExpression(PsiNewExpression expression) {
                if (MoExpressionUtils.hasFullQualifiedName(expression, "java.security.SecureRandom")) {
                    PsiExpressionList expressionList = expression.getArgumentList();
                    if (expressionList != null && expressionList.getExpressions().length != 0) {
                        holder.registerProblem(
                                expression,
                                MESSAGE,
                                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                new PredictableSeedQuickFix(expressionList)
                        );
                    }
                }
            }
        };
    }

    public static class PredictableSeedQuickFix implements LocalQuickFix {

        private final SmartPsiElementPointer<PsiExpressionList> argList;

        public PredictableSeedQuickFix(PsiExpressionList expressionList) {
            this.argList = SmartPointerManager.createPointer(expressionList);
        }

        @Override
        public @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String getFamilyName() {
            return QUICK_FIX_NAME;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiExpressionList args = argList.getElement();
            if (args != null) {
                for(PsiExpression arg : args.getExpressions()) {
                    arg.delete();
                }
            }
        }
    }

}
