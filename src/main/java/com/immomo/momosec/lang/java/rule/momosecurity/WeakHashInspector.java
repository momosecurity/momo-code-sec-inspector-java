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
import com.siyeh.ig.psiutils.MethodCallUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * MD2, MD4, MD5 为脆弱的消息摘要算法
 *
 * MD5的安全性受到严重损害。在4核2.6GHz的机器上，碰撞攻击可以在秒级完成。选择前缀碰撞攻击可以在小时级完成。
 *
 * ref:
 * https://en.wikipedia.org/wiki/MD5#Security
 */
public class WeakHashInspector extends MomoBaseLocalInspectionTool {
    public static final String MESSAGE = InspectionBundle.message("weak.hash.inspector.msg");

    private static final Set<String> WeakHashNames = new HashSet<String>() {{
        add("MD2");
        add("MD4");
        add("MD5");
    }};

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                String methodCallName = MethodCallUtils.getMethodName(expression);

                PsiMethod method = expression.resolveMethod();
                if (method == null) { return ; }

                PsiClass containingClass = method.getContainingClass();
                if (containingClass == null) { return ; }

                String methodQualifiedName = containingClass.getQualifiedName();

                if ("java.security.MessageDigest".equals(methodQualifiedName) && "getInstance".equals(methodCallName)) {
                    checkZeroArgs(expression);
                } else if ("org.apache.commons.codec.digest.DigestUtils".equals(methodQualifiedName)) {
                    if ("getDigest".equals(methodCallName)) {
                        checkZeroArgs(expression);
                    } else if (
                            "getMd5Digest".equals(methodCallName) ||
                            "getMd2Digest".equals(methodCallName) ||
                            "md2".equals(methodCallName) ||
                            "md2Hex".equals(methodCallName) ||
                            "md5".equals(methodCallName) ||
                            "md5Hex".equals(methodCallName)
                    ) {
                        registerProblem(expression);
                    }
                }
            }

            @Override
            public void visitNewExpression(PsiNewExpression expression) {
                if (MoExpressionUtils.hasFullQualifiedName(expression, "org.apache.commons.codec.digest.DigestUtils")) {
                    checkZeroArgs(expression);
                }
            }

            private void checkZeroArgs(PsiCallExpression expression) {
                PsiExpressionList argList = expression.getArgumentList();
                if (argList == null) { return ; }
                PsiExpression[] args = argList.getExpressions();
                if (args.length > 0 && args[0] instanceof PsiLiteralExpression) {
                    String mdName = MoExpressionUtils.getLiteralInnerText(args[0]);
                    if (null != mdName && WeakHashNames.contains(mdName.toUpperCase())) {
                        registerProblem(expression);
                    }
                }
            }

            private void registerProblem(PsiExpression expression) {
                holder.registerProblem(expression, MESSAGE, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
            }
        };
    }
}
