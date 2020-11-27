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
import com.intellij.psi.util.PsiLiteralUtil;
import com.intellij.util.ObjectUtils;
import com.siyeh.ig.psiutils.ExpressionUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Momo 1024: OpenSAML2 认证绕过风险
 *
 * 在不忽略XML注释的情况下，攻击者可以设法更改NameID字段以使用XML注释来标识用户身份。
 *
 * ref: https://rules.sonarsource.com/java/type/Vulnerability/RSPEC-5679
 */
public class OpenSAML2IgnoreComment extends MomoBaseLocalInspectionTool {
    public static final String MESSAGE = InspectionBundle.message("open.saml2.ignore.comment.msg");
    public static final String QUICK_FIX_NAME = InspectionBundle.message("open.saml2.ignore.comment.fix");

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                if (MoExpressionUtils.hasFullQualifiedName(expression, "org.opensaml.xml.parse.StaticBasicParserPool", "setIgnoreComments") ||
                    MoExpressionUtils.hasFullQualifiedName(expression, "org.opensaml.xml.parse.BasicParserPool", "setIgnoreComments")
                ) {
                    PsiExpression[] args = expression.getArgumentList().getExpressions();
                    if (args.length > 0) {
                        PsiLiteralExpression arg0 = ObjectUtils.tryCast(args[0], PsiLiteralExpression.class);
                        if (arg0 == null) return ;
                        if (ExpressionUtils.isLiteral(arg0, Boolean.FALSE)) {
                            holder.registerProblem(
                                    expression,
                                    MESSAGE,
                                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                    new SetBoolArgQuickFix(QUICK_FIX_NAME, true, arg0)
                            );
                        }
                    }
                }
            }
        };
    }
}    
