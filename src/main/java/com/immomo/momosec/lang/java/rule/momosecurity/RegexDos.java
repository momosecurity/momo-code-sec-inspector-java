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
import com.siyeh.ig.psiutils.MethodCallUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

/**
 * 1005: RegexDos风险
 *
 * 正则表达式拒绝服务攻击(RegexDos)
 *
 * 当编写校验的正则表达式存在缺陷或者不严谨时, 攻击者可以构造特殊的字符串来大量消耗服务器的系统资源，造成服务器的服务中断或停止。
 * ref: https://cloud.tencent.com/developer/article/1041326
 *
 * check:
 * java.util.regex.Pattern#compile args:0
 * java.util.regex.Pattern#matchers args:0
 *
 * fix:
 * (1) optimize Regular Expressions
 * (2) use com.google.re2j
 *
 * notes:
 * `isExponentialRegex` method copy from CodeQL
 */
public class RegexDos extends MomoBaseLocalInspectionTool {
    public static final String MESSAGE = InspectionBundle.message("regex.dos.msg");
    private static final String QUICK_FIX_NAME = InspectionBundle.message("regex.dos.fix");

    private final RegexDosWithRe2jQuickFix regexDosWithRe2jQuickFix = new RegexDosWithRe2jQuickFix();

    public static boolean isExponentialRegex(String s) {
        return
        // Example: ([a-z]+)+
        Pattern.matches(".*\\([^()*+\\]]+\\]?(\\*|\\+)\\)(\\*|\\+).*", s) ||
        // Example: (([a-z])?([a-z]+))+
        Pattern.matches(".*\\((\\([^()]+\\)\\?)?\\([^()*+\\]]+\\]?(\\*|\\+)\\)\\)(\\*|\\+).*", s) ||
        // Example: (([a-z])+)+
        Pattern.matches(".*\\(\\([^()*+\\]]+\\]?\\)(\\*|\\+)\\)(\\*|\\+).*", s) ||
        // Example: (a|aa)+
        Pattern.matches(".*\\(([^()*+\\]]+\\]?)\\|\\1+\\??\\)(\\*|\\+).*", s) ||
        // Example: (.*[a-z]){n} n >= 10
        Pattern.matches(".*\\(\\.\\*[^()*+\\]]+\\]?\\)\\{[1-9][0-9]+,?[0-9]*\\}.*", s);
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                if (MethodCallUtils.isCallToRegexMethod(expression)) { // `isCallToRegexMethod` judges include java.util.regex.Pattern
                    String methodName = MethodCallUtils.getMethodName(expression);
                    if (methodName != null && ( methodName.equals("compile") || methodName.equals("matches") )) {
                        PsiExpression[] expressions = expression.getArgumentList().getExpressions();
                        if (expressions.length > 0) {
                            PsiLiteralExpression literal = getLiteralExpression(expressions[0]);
                            if (literal != null && isExponentialRegex(MoExpressionUtils.getLiteralInnerText(literal))) {
                                holder.registerProblem(expressions[0], MESSAGE, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, regexDosWithRe2jQuickFix);
                            }
                        }
                    }
                }
            }
        };
    }

    @Nullable
    private PsiLiteralExpression getLiteralExpression(PsiExpression expression) {
        if (expression instanceof PsiReferenceExpression) {
            PsiElement elem = ((PsiReferenceExpression) expression).resolve();
            if (elem instanceof PsiVariable) {
                PsiExpression initializer = ((PsiVariable) elem).getInitializer();
                if (initializer instanceof PsiLiteralExpression) {
                    return (PsiLiteralExpression)initializer;
                }
            }
        } else if (expression instanceof PsiLiteralExpression) {
            return (PsiLiteralExpression)expression;
        }
        return null;
    }

    public static class RegexDosWithRe2jQuickFix implements LocalQuickFix {

        @Override
        public @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String getFamilyName() {
            return QUICK_FIX_NAME;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElement problemLiteral = descriptor.getPsiElement();
            PsiElement methodCall = problemLiteral.getParent().getParent();
            if (methodCall instanceof PsiMethodCallExpression) {
                PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);
                PsiExpression newQualifier = factory.createExpressionFromText("com.google.re2j.Pattern", null);
                ((PsiMethodCallExpression) methodCall).getMethodExpression().setQualifierExpression(newQualifier);
            }
        }
    }
}
