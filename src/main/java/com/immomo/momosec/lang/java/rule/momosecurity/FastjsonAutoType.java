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
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

/**
 * 1007: Fastjson反序列化风险
 *
 * com.fasterxml.jackson.core:jackson-databind 在开启DefaultTyping时，存在反序列化风险
 *
 * 程序内开启方法
 * (1) ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
 * (2) parseConfLocalVar.setAutoTypeSupport(true);
 * JVM开启方法
 * -Dfastjson.parser.autoTypeSupport=true
 */
public class FastjsonAutoType extends MomoBaseLocalInspectionTool {
    public static final String MESSAGE = InspectionBundle.message("fastjson.auto.type.msg");
    private static final String QUICK_FIX_NAME = InspectionBundle.message("fastjson.auto.type.fix");

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                if (MoExpressionUtils.hasFullQualifiedName(expression, "com.alibaba.fastjson.parser.ParserConfig", "setAutoTypeSupport")) {
                    PsiExpression[] args = expression.getArgumentList().getExpressions();
                    if (args.length == 1 &&
                        args[0] instanceof PsiLiteralExpression &&
                        Boolean.TRUE.equals(((PsiLiteralExpression)args[0]).getValue())
                    ) {
                        holder.registerProblem(
                                expression,
                                MESSAGE,
                                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                new DeleteElementQuickFix(expression, QUICK_FIX_NAME)
                        );
                    }
                }
            }
        };
    }

}
