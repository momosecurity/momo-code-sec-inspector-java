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
package com.immomo.momosec.lang.java.utils;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.ObjectUtils;
import com.siyeh.ig.psiutils.ExpressionUtils;
import com.siyeh.ig.psiutils.MethodCallUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MoExpressionUtils {

    private static final Set<String> SQLiCareTypeStr = new HashSet<String>() {{
        add("java.lang.String");
    }};

    @Nullable
    public static PsiField resolveField(@Nullable PsiExpression expression) {
        expression = PsiUtil.skipParenthesizedExprDown(expression);
        PsiReferenceExpression referenceExpression = ObjectUtils.tryCast(expression, PsiReferenceExpression.class);
        return referenceExpression == null ? null : ObjectUtils.tryCast(referenceExpression.resolve(), PsiField.class);
    }

    @Nullable
    public static String getLiteralInnerText(@NotNull PsiExpression expression) {
        PsiLiteralExpression literal = ExpressionUtils.getLiteral(expression);
        if (literal != null) {
            Object value = literal.getValue();
            if (value != null) {
                return value.toString();
            }
        }
        return null;
    }

    @Nullable
    public static String getText(@NotNull PsiExpression expression) {
        String s;

        s = getLiteralInnerText(expression);
        if (s == null && expression instanceof PsiPolyadicExpression) {
            s = derefPolyadicExpression((PsiPolyadicExpression)expression).stream()
                    .map(MoExpressionUtils::getText)
                    .collect(Collectors.joining());
        }

        return s == null ? expression.getText() : s;
    }

    /**
     * 判断PsiExpression是否为变量表达式
     * @param expression PsiExpression
     * @return boolean
     */
    public static Boolean isVariable(@NotNull PsiExpression expression) {
        if (expression instanceof PsiReferenceExpression) {
            PsiElement element = ((PsiReferenceExpression) expression).resolve();
            return element instanceof PsiVariable;
        }
        return false;
    }

    /**
     * 向上查找直到出现PsiStatement
     * @param element PsiElement
     * @return PsiStatement | null
     */
    @Nullable
    public static PsiStatement getParentOfStatement(PsiElement element) {
        while (!(element instanceof PsiStatement)) {
            if (element == null || element instanceof PsiMethod) {
                return null;
            }
            element = element.getParent();
        }
        return (PsiStatement)element;
    }

    /**
     * 向上查找直到出现PsiField
     * @param element PsiElement
     * @return PsiField | null
     */
    @Nullable
    public static PsiField getParentOfField(PsiElement element) {
        while (!(element instanceof PsiField)) {
            if (element == null || element instanceof PsiClass) {
                return null;
            }
            element = element.getParent();
        }
        return (PsiField)element;
    }

    /**
     * 向上查找到当前的Method
     * @param element PsiElement
     * @return PsiMethod | null
     */
    @Nullable
    public static PsiMethod getParentOfMethod(PsiElement element) {
        while (!(element instanceof PsiMethod)) {
            if (element == null) {
                return null;
            }
            element = element.getParent();
        }
        return (PsiMethod)element;
    }

    /**
     * 向上查找直到出现PsiClass
     * @param element PsiElement
     * @return PsiClass | null
     */
    @Nullable
    public static PsiClass getParentOfClass(PsiElement element) {
        while (!(element instanceof PsiClass)) {
            if (element == null) {
                return null;
            }
            element  = element.getParent();
        }
        return (PsiClass)element;
    }

    /**
     * 向上查找到当前的ClassInitializer
     * @param element PsiElement
     * @return PsiClassInitializer | null
     */
    @Nullable
    public static PsiClassInitializer getParentOfClassInitializer(PsiElement element) {
        while(!(element instanceof PsiClassInitializer)) {
            if (element == null) {
                return null;
            }
            element = element.getParent();
        }
        return (PsiClassInitializer)element;
    }

    private static PsiLiteralExpression getPlaceholder(Project project) {
        return (PsiLiteralExpression)JavaPsiFacade.getElementFactory(project).createExpressionFromText("\"?\"", null);
    }

    public static boolean isSqliCareExpression(PsiExpression psiExpression) {
        PsiType type = psiExpression.getType();
        if (type != null && !SQLiCareTypeStr.contains(type.getCanonicalText())) {
            return false;
        }

        if (psiExpression instanceof PsiMethodCallExpression) {
            if ("join".equals(MethodCallUtils.getMethodName((PsiMethodCallExpression)psiExpression))) {
                PsiExpression[] args = ((PsiMethodCallExpression)psiExpression).getArgumentList().getExpressions();
                return args.length <= 0 ||
                        args[0].getType() == null ||
                        args[0].getType().getPresentableText().contains("<String>");
            } else {
                PsiExpression qualifierExp = ((PsiMethodCallExpression) psiExpression).getMethodExpression().getQualifierExpression();
                if (qualifierExp != null && qualifierExp.getReference() != null) {
                    PsiElement targetElem = qualifierExp.getReference().resolve();
                    return !(targetElem instanceof PsiClass) || !((PsiClass) targetElem).isEnum();
                }
            }
        }
        return true;
    }

    /**
     * 解析PsiPolyadicExpression中引用的常量
     * @param expression PsiPolyadicExpression
     * @return List<PsiExpression>
     */
    public static List<PsiExpression> derefPolyadicExpression(PsiPolyadicExpression expression) {
        List<PsiExpression> expressions = new ArrayList<>();

        if (expression.getOperationTokenType().equals(JavaTokenType.PLUS)) {
            for (PsiExpression psiExpression: expression.getOperands()) {
                if (!isSqliCareExpression(psiExpression)){
                    expressions.add(getPlaceholder(psiExpression.getProject()));
                    continue;
                }

                if (ExpressionUtils.isLiteral(psiExpression)) {
                    expressions.add(psiExpression);
                    continue;
                }

                PsiLocalVariable localVariable = ExpressionUtils.resolveLocalVariable(psiExpression);
                if (localVariable != null) {
                    PsiExpression localVariableInitializer = localVariable.getInitializer();
                    if (ExpressionUtils.isLiteral(localVariableInitializer)) {
                        expressions.add(localVariable.getInitializer());
                        continue;
                    } else if (localVariableInitializer instanceof PsiPolyadicExpression) {
                        expressions.addAll(derefPolyadicExpression((PsiPolyadicExpression)localVariableInitializer));
                        continue;
                    }
                }

                PsiField field = MoExpressionUtils.resolveField(psiExpression);
                if (field != null) {
                    PsiExpression fieldInitializer = field.getInitializer();
                    if (ExpressionUtils.isLiteral(fieldInitializer)) {
                        expressions.add(field.getInitializer());
                        continue;
                    } else if (fieldInitializer instanceof PsiPolyadicExpression) {
                        expressions.addAll(derefPolyadicExpression((PsiPolyadicExpression)fieldInitializer));
                        continue;
                    }
                }

                expressions.add(psiExpression);
            }
        }

        return expressions;
    }

    public static boolean hasFullQualifiedName(PsiMethodCallExpression methodCall, String qualifiedName, String methodName) {
        String methodCallName = MethodCallUtils.getMethodName(methodCall);
        if (!methodName.equals(methodCallName)) {
            return false;
        }

        PsiMethod method = methodCall.resolveMethod();
        if (method == null) { return false; }

        PsiClass containingClass = method.getContainingClass();
        if (containingClass == null) { return false; }

        return qualifiedName.equals(containingClass.getQualifiedName());
    }

    public static boolean hasFullQualifiedName(PsiNewExpression newExpression, String qualifiedName) {
        if (newExpression.getClassReference() == null) {
            return false;
        }
        return qualifiedName.equals(newExpression.getClassReference().getQualifiedName());
    }

    /**
     * get FQName of a PsiMethod
     * fqname construct with <class QualifiedName> <method return Type> <method name>(<param_type> <param_name>, ...)
     * @param method PsiMethod
     * @return String
     */
    public static String getMethodFQName(PsiMethod method) {
        StringBuilder fqname = new StringBuilder();
        PsiClass aClass = method.getContainingClass();
        fqname.append(aClass != null ? aClass.getQualifiedName() : "null");
        fqname.append(" ");

        PsiType methodReturnType = method.getReturnType();
        fqname.append(methodReturnType != null ? methodReturnType.getCanonicalText() : "null");
        fqname.append(" ");

        fqname.append(method.getName());
        fqname.append("(");

        PsiParameterList parameterList = method.getParameterList();
        PsiParameter[] parameters = parameterList.getParameters();
        for(PsiParameter parameter : parameters) {
            fqname.append(parameter.getType().getCanonicalText());
            fqname.append(" ");
            fqname.append(parameter.getName());
            fqname.append(", ");
        }

        if (parameters.length != 0) {
            fqname.delete(fqname.length()-2, fqname.length());
        }

        fqname.append(")");
        return fqname.toString();
    }

    public static String getElementFQName(PsiElement element) {
        PsiMethod method = getParentOfMethod(element);
        if (method != null) {
            return getMethodFQName(method);
        } else {
            PsiClass aClass = getParentOfClass(element);
            if (aClass != null) {
                return aClass.getQualifiedName();
            }
        }
        return "null";
    }
}
