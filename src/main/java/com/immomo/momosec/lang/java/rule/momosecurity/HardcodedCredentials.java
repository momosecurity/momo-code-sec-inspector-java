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
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import com.intellij.util.ObjectUtils;
import me.gosimple.nbvcxz.Nbvcxz;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * Momo 1020: 硬编码凭证风险
 *
 * ref:
 * (1) https://github.com/securego/gosec/blob/master/rules/hardcoded_credentials.go
 */
public class HardcodedCredentials extends MomoBaseLocalInspectionTool {
    public static final String MESSAGE = InspectionBundle.message("hardcoded.credentials.msg");
    private static final Pattern pattern = Pattern.compile("passwd|pass|password|pwd|secret|token", Pattern.CASE_INSENSITIVE);
    private static final Pattern connPwdPattern = Pattern.compile("password=(.*?)($|&)", Pattern.CASE_INSENSITIVE);
    private static final double entropyThreshold = 50.0;
    private static final int truncate = 16;


    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitLocalVariable(PsiLocalVariable variable) {
                String varname = variable.getName();
                if (varname != null && pattern.matcher(varname).find()) {
                     PsiExpression initializer = variable.getInitializer();
                     if (initializer instanceof PsiLiteralExpression) {
                         String value = MoExpressionUtils.getLiteralInnerText(initializer);
                         if (value != null && isHighEntropyString(value)) {
                             holder.registerProblem(variable, MESSAGE, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                         }
                     }
                }
            }

            @Override
            public void visitAssignmentExpression(PsiAssignmentExpression expression) {
                PsiExpression lexp = expression.getLExpression();
                if (lexp instanceof PsiReferenceExpression) {
                    String varname = ((PsiReferenceExpression) lexp).getQualifiedName();
                    if (pattern.matcher(varname).find()) {
                        PsiExpression rexp = expression.getRExpression();
                        if (rexp instanceof PsiLiteralExpression) {
                            String value = MoExpressionUtils.getLiteralInnerText(rexp);
                            if (value != null && isHighEntropyString(value)) {
                                holder.registerProblem(expression, MESSAGE, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                            }
                        }
                    }
                }
            }

            @Override
            public void visitField(PsiField field) {
                String varname = field.getName();
                if (varname != null && pattern.matcher(varname).find()) {
                    PsiExpression initializer = field.getInitializer();
                    if (initializer instanceof PsiLiteralExpression) {
                        String value = MoExpressionUtils.getLiteralInnerText(initializer);
                        if (value != null && isHighEntropyString(value)) {
                            holder.registerProblem(field, MESSAGE, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                        }
                    }
                }
            }

            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                if (MoExpressionUtils.hasFullQualifiedName(expression, "java.util.Hashtable", "put")) {
                    PsiExpression qualifierExp = expression.getMethodExpression().getQualifierExpression();
                    if (qualifierExp != null &&
                        qualifierExp.getType() != null &&
                        "java.util.Properties".equals(qualifierExp.getType().getCanonicalText())
                    ) {
                        PsiExpression[] args = expression.getArgumentList().getExpressions();
                        if (args.length == 2 && args[1] instanceof PsiLiteralExpression) {
                            String key = MoExpressionUtils.getText(args[0], true);
                            if (key != null && pattern.matcher(key).find()) {
                                String value = MoExpressionUtils.getLiteralInnerText(args[1]);
                                if (value != null && isHighEntropyString(value)) {
                                    holder.registerProblem(expression, MESSAGE, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                                }
                            }
                        }
                    }
                } else if (MoExpressionUtils.hasFullQualifiedName(expression, "java.sql.DriverManager", "getConnection")) {
                    // 检查0位参的连接串，或3位参的password字段
                    PsiExpression[] args = expression.getArgumentList().getExpressions();
                    if (args.length == 1) {
                        String connUrl = MoExpressionUtils.getLiteralInnerText(ObjectUtils.tryCast(args[0], PsiLiteralExpression.class));
                        if (connUrl != null && connPwdPattern.matcher(connUrl).find()) {
                            holder.registerProblem(expression, MESSAGE, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                        }
                    } else if (args.length == 3) {
                        PsiLiteralExpression password = ObjectUtils.tryCast(args[2], PsiLiteralExpression.class);
                        if (password != null) {
                            holder.registerProblem(expression, MESSAGE, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                        }
                    }
                }
            }
        };
    }

    private static boolean isHighEntropyString(String v) {
        if (truncate < v.length()) {
            v = v.substring(0, truncate);
        }
        return new Nbvcxz().estimate(v).getEntropy() > entropyThreshold;
    }
}
