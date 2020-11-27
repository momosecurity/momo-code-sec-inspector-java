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

import com.immomo.momosec.fix.SetBoolArgQuickFix;
import com.immomo.momosec.lang.InspectionBundle;
import com.immomo.momosec.lang.MomoBaseLocalInspectionTool;
import com.immomo.momosec.lang.java.utils.MoExpressionUtils;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

/**
 * Momo 1017: LDAP反序列化风险
 *
 * ref: https://rules.sonarsource.com/java/type/Vulnerability/RSPEC-4434
 */
public class LDAPUnserialize extends MomoBaseLocalInspectionTool {
    public static final String MESSAGE = InspectionBundle.message("ldap.unserialize.msg");
    private static final String QUICK_FIX_NAME = InspectionBundle.message("ldap.unserialize.fix");

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitNewExpression(PsiNewExpression expression) {
                if (MoExpressionUtils.hasFullQualifiedName(expression, "javax.naming.directory.SearchControls")) {
                    PsiExpressionList expressionList = expression.getArgumentList();
                    if (expressionList != null && expressionList.getExpressionCount() == 6) {
                        PsiExpression[] args = expressionList.getExpressions();
                        if (args[4] instanceof PsiLiteralExpression &&
                            Boolean.TRUE.equals(((PsiLiteralExpression) args[4]).getValue())
                        ) {
                            holder.registerProblem(
                                    expression,
                                    MESSAGE,
                                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                    new SetBoolArgQuickFix(QUICK_FIX_NAME, false, (PsiLiteralExpression)args[4])
                            );
                        }
                    }
                }
            }

            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                if (MoExpressionUtils.hasFullQualifiedName(expression, "javax.naming.directory.SearchControls", "setReturningObjFlag")) {
                    PsiExpressionList expressionList = expression.getArgumentList();
                    PsiExpression[] args = expressionList.getExpressions();
                    if (args.length == 1 &&
                        args[0] instanceof PsiLiteralExpression &&
                        Boolean.TRUE.equals(((PsiLiteralExpression)args[0]).getValue())
                    ) {
                        holder.registerProblem(
                                expression,
                                MESSAGE,
                                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                new SetBoolArgQuickFix(QUICK_FIX_NAME, false, (PsiLiteralExpression)args[0])
                        );
                    }
                }
            }
        };
    }
}
