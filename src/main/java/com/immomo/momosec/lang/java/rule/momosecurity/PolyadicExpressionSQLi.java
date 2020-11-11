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

import com.immomo.momosec.fix.ShowHelpCommentQuickFix;
import com.immomo.momosec.lang.InspectionBundle;
import com.immomo.momosec.lang.java.utils.MoExpressionUtils;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.immomo.momosec.Constants.SQL_INJECTION_HELP_COMMENT;
import static com.immomo.momosec.lang.java.utils.MoExpressionUtils.isSqliCareExpression;
import static com.immomo.momosec.utils.SQLi.placeholderPattern;

/**
 * 1001: 多项式拼接型SQL注入漏洞
 *
 * eg.
 * (1) "select *" + " from table" + " where id =" + id;
 * (2) "select * from table" + " where id = " + getUserId();
 */
public class PolyadicExpressionSQLi extends BaseSQLi {
    public static final String MESSAGE = InspectionBundle.message("polyadic.expression.sqli.msg");
    private static final String QUICK_FIX_NAME = InspectionBundle.message("polyadic.expression.sqli.fix");

    private final ShowHelpCommentQuickFix showHelpCommentQuickFix = new ShowHelpCommentQuickFix(QUICK_FIX_NAME, SQL_INJECTION_HELP_COMMENT);

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {

            @Override
            public void visitPolyadicExpression(PsiPolyadicExpression expression) {
                List<PsiExpression> exps = MoExpressionUtils.deconPolyadicExpression(expression);
                if (exps.isEmpty()) { return ; }

                String expStr = exps.stream()
                        .map(item -> MoExpressionUtils.getText(item, true))
                        .collect(Collectors.joining());
                if (isSql(expStr)) {
                    List<String> sql_segments = new ArrayList<>();
                    StringBuilder sb = new StringBuilder();

                    boolean hasVar = false;
                    for (PsiExpression exp : exps) {
                        if (isSqliCareExpression(exp)) {
                            String s = MoExpressionUtils.getLiteralInnerText(exp);
                            if ( s == null ) {
                                if ( !sb.toString().isEmpty() ) {
                                    sql_segments.add(sb.toString());
                                    sb.delete(0, sb.length());
                                }

                                if (!MoExpressionUtils.isText(exp)) {
                                    hasVar = true;
                                }
                            } else {
                                sb.append(s);
                            }
                        } else {
                            sb.append("?");
                        }
                    }
                    // 末段要 drop 掉，不用查

                    if (sql_segments.isEmpty() || Boolean.FALSE.equals(hasVar) || !hasEvalAdditive(sql_segments)) {
                        // 对于 "select * from " + getTable() + " where id = %s" 的情况
                        // getTable() 被忽略了，要考虑后面 %s 的问题
                        if (hasPlaceholderProblem(expStr) && !ignoreMethodName(expression)) {
                            holder.registerProblem(expression, PlaceholderStringSQLi.MESSAGE, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, showHelpCommentQuickFix);
                        }
                        return ;
                    }

                    if (!ignoreMethodName(expression)) {
                        holder.registerProblem(expression, MESSAGE, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, showHelpCommentQuickFix);
                    }
                }
            }

            private boolean hasPlaceholderProblem(String content) {
                return placeholderPattern.matcher(content).find() &&
                        isSql(content) &&
                        hasEvalAdditive(content, placeholderPattern);
            }
        };
    }
}
