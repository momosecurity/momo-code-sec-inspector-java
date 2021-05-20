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
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiUtil;
import com.intellij.psi.util.TypeConversionUtil;
import com.intellij.util.ObjectUtils;
import com.siyeh.ig.psiutils.ExpressionUtils;
import com.siyeh.ig.psiutils.MethodCallUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class MoExpressionUtils {

    private static final Set<String> SQLiCareTypeStr = new HashSet<String>() {{
        add("java.lang.String");
        add("java.lang.StringBuilder");
        add("java.lang.StringBuffer");
    }};

    /**
     * 从field使用点溯源到定义点
     * @param expression PsiExpression
     * @return PsiField | null
     */
    @Nullable
    public static PsiField resolveField(@Nullable PsiExpression expression) {
        expression = PsiUtil.skipParenthesizedExprDown(expression);
        PsiReferenceExpression referenceExpression = ObjectUtils.tryCast(expression, PsiReferenceExpression.class);
        return referenceExpression == null ? null : ObjectUtils.tryCast(referenceExpression.resolve(), PsiField.class);
    }

    /**
     * 获取文本节点的内容
     * @param expression PsiExpression
     * @return String | null
     */
    @Nullable
    public static String getLiteralInnerText(@Nullable PsiExpression expression) {
        PsiLiteralExpression literal = ExpressionUtils.getLiteral(expression);
        if (literal != null) {
            Object value = literal.getValue();
            if (value != null) {
                return value.toString();
            }
        }
        return null;
    }

    /**
     * 将表达式尝试转换为文本内容
     * (1) 文本节点解析
     * (2) 基础类型 / 枚举类型
     * (3) field 字段
     * @param expression PsiExpression
     * @return String
     */
    @Nullable
    public static String getText(@Nullable PsiExpression expression) {
        return getText(expression, false);
    }

    /**
     * 将表达式尝试转换为文本内容
     * (1) 文本节点解析
     * (2) 基础类型 / 枚举类型
     * (3) field 字段
     * @param expression PsiExpression
     * @param force boolean             强制转换为表达式字面值
     * @return String
     */
    @Nullable
    public static String getText(@Nullable PsiExpression expression, boolean force) {
        if (expression == null) {
            return null;
        }

        String value = getLiteralInnerText(expression);

        if (value == null && (
            TypeConversionUtil.isPrimitiveAndNotNull(expression.getType()) ||
            PsiUtil.isConstantExpression(expression) &&
            !(expression instanceof PsiPolyadicExpression)
        )) {
            value = expression.getText();
        }

        if (value == null && expression instanceof PsiReferenceExpression) {
            PsiElement resolve = ((PsiReferenceExpression) expression).resolve();
            if (resolve instanceof PsiField) {
                // 对于 field 可不区分force，field值不是Text时，直接用field变量名
                PsiExpression initializer = ((PsiField) resolve).getInitializer();
                if (initializer != null) {
                    value = getText(initializer, false);
                }
                if (value == null) {
                    value = ((PsiField) resolve).getName();
                }
            }
        }

        if (value == null && expression instanceof PsiPolyadicExpression) {
            StringBuilder sb = new StringBuilder();
            for(PsiExpression operand : ((PsiPolyadicExpression) expression).getOperands()) {
                String text = getText(operand, force);
                if (text == null) {
                    sb.setLength(0);
                    break;
                }
                sb.append(text);
            }
            if (sb.length() != 0) {
                value = sb.toString();
            }
        }

        if (force && value == null) {
            value = expression.getText();
        }
        return value;
    }

    public static Boolean isText(@NotNull PsiExpression expression) {
        return getText(expression) != null;
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

    /**
     * 生成一个"?"文本节点
     * @param project Project
     * @return PsiLiteralExpression
     */
    private static PsiLiteralExpression getPlaceholder(Project project) {
        return (PsiLiteralExpression)JavaPsiFacade.getElementFactory(project).createExpressionFromText("\"?\"", null);
    }

    /**
     * 生成一个自定义文本节点
     * @param project Project
     * @return PsiLiteralExpression
     */
    private static PsiLiteralExpression getCustomLiteral(String s, Project project) {
        return (PsiLiteralExpression)JavaPsiFacade.getElementFactory(project).createExpressionFromText("\""+s.replace("\"", "\\\"")+"\"", null);
    }

    public static boolean isSqliCareExpression(PsiExpression psiExpression) {
        PsiType type = psiExpression.getType();
        if (type != null && !SQLiCareTypeStr.contains(type.getCanonicalText())) {
            return false;
        }

        if (psiExpression instanceof PsiMethodCallExpression) {
            if (MoExpressionUtils.hasFullQualifiedName((PsiMethodCallExpression)psiExpression, "java.lang.String", "join")) {
                PsiExpression[] args = ((PsiMethodCallExpression)psiExpression).getArgumentList().getExpressions();
                if (args.length == 2 && args[1].getType() != null) {
                    return args[1].getType().getPresentableText().contains("<String>");
                }
                return false;
            } else if (MoExpressionUtils.hasFullQualifiedName((PsiMethodCallExpression)psiExpression, "org.apache.commons.lang.StringUtils", "join") ||
                MoExpressionUtils.hasFullQualifiedName((PsiMethodCallExpression)psiExpression, "org.apache.commons.lang3.StringUtils", "join")
            ){
                PsiExpression[] args = ((PsiMethodCallExpression)psiExpression).getArgumentList().getExpressions();
                if (args.length >= 1 && args[0].getType() != null) {
                    return args[0].getType().getPresentableText().contains("<String>");
                }
                return false;
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
     * 解构PsiPolyadicExpression
     * 对于拼接的每一部分
     * (1) 判断为常量则输出为常量
     * (2) 判断为 field 则输出为常量
     * (-) 原样输出
     * @param expression PsiPolyadicExpression
     * @return List<PsiExpression>
     */
    public static List<PsiExpression> deconPolyadicExpression(PsiPolyadicExpression expression) {
        List<PsiExpression> expressions = new ArrayList<>();

        if (expression.getOperationTokenType().equals(JavaTokenType.PLUS)) {
            for (PsiExpression operand: expression.getOperands()) {
                PsiLocalVariable localVariable = ExpressionUtils.resolveLocalVariable(operand);
                if (localVariable != null) {
                    PsiType localVariableType = operand.getType();
                    PsiReferenceExpression refOperand = ObjectUtils.tryCast(operand, PsiReferenceExpression.class);
                    if (localVariableType != null && refOperand != null &&
                        "java.lang.String".equals(localVariableType.getCanonicalText()) &&
                        isConstStringConcatToReference(refOperand)
                    ) {
                        expressions.add(getCustomLiteral(operand.getText(), expression.getProject()));
                        continue;
                    }
                    if (localVariableType != null && refOperand != null && (
                        "java.lang.StringBuilder".equals(localVariableType.getCanonicalText()) ||
                        "java.lang.StringBuffer".equals(localVariableType.getCanonicalText())
                        ) &&
                        isConstStringBuilderToReference(refOperand)
                    ) {
                        expressions.add(getCustomLiteral(operand.getText(), expression.getProject()));
                        continue;
                    }

                    expressions.add(operand);
                    continue;
                }

                PsiField field = MoExpressionUtils.resolveField(operand);
                if (field != null) {
                    PsiExpression fieldInitializer = field.getInitializer();
                    if (fieldInitializer instanceof PsiPolyadicExpression) {
                        expressions.addAll(deconPolyadicExpression((PsiPolyadicExpression)fieldInitializer));
                    } else if (fieldInitializer != null && isText(fieldInitializer)) {
                        expressions.add(fieldInitializer);
                    } else {
                        // field 作为文本对待
                        expressions.add(getCustomLiteral(operand.getText(), expression.getProject()));
                    }
                    continue;
                }

                expressions.add(operand);
            }
        }

        return expressions;
    }

    private static boolean isConstStringBuilderToReference(PsiReferenceExpression ref) {
        List<PsiReference> refPoints = getReferenceOnMethodScope(ref, ref.getTextOffset()-1);

        for(PsiReference refPoint : refPoints) {
            PsiReferenceExpression refPointExp =
                    ObjectUtils.tryCast(refPoint, PsiReferenceExpression.class);
            if (refPointExp == null) {
                continue;
            }
            PsiReferenceExpression refParentExp =
                    ObjectUtils.tryCast(refPointExp.getParent(), PsiReferenceExpression.class);
            if (refParentExp == null) {
                continue;
            }
            if ("append".equals(refParentExp.getReferenceName()) || "insert".equals(refParentExp.getReferenceName())) {
                PsiMethodCallExpression methodCall =
                        ObjectUtils.tryCast(refParentExp.getParent(), PsiMethodCallExpression.class);
                if (methodCall == null) {
                    continue;
                }
                PsiExpression[] args = methodCall.getArgumentList().getExpressions();
                boolean isConst = true;
                if ("append".equals(refParentExp.getReferenceName()) && args.length == 1) {
                    isConst = isText(args[0]);
                } else if ("insert".equals(refParentExp.getReferenceName()) && args.length >= 2) {
                    isConst = isText(args[1]);
                }
                if (!isConst) {
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean isConstStringConcatToReference(PsiReferenceExpression ref) {
        List<PsiReference> refPoints = getReferenceOnMethodScope(ref, ref.getTextOffset()-1);

        // 1. 先检查拼接点
        for(PsiReference refPoint : refPoints) {
            PsiReferenceExpression refPointExp =
                    ObjectUtils.tryCast(refPoint, PsiReferenceExpression.class);
            if (refPointExp == null) {
                continue;
            }

            PsiAssignmentExpression assignExp =
                    ObjectUtils.tryCast(refPointExp.getParent(), PsiAssignmentExpression.class);
            if (assignExp == null || !refPoint.equals(assignExp.getLExpression())) {
                continue;
            }

            PsiReferenceExpression lExp =
                    ObjectUtils.tryCast(assignExp.getLExpression(), PsiReferenceExpression.class);
            if (lExp == null) {
                continue;
            }
            String varName = lExp.getReferenceName();
            if (varName == null) {
                continue;
            }

            PsiExpression rExp = assignExp.getRExpression();
            if (rExp == null) {
                continue;
            }

            if (rExp instanceof PsiReferenceExpression &&
                varName.equals(((PsiReferenceExpression) rExp).getReferenceName())) {
                continue;
            }

            if (rExp instanceof PsiPolyadicExpression) {
                // 对于拼接，需要检查是否为  a = a + b 的场景
                for (PsiExpression operand : ((PsiPolyadicExpression) rExp).getOperands()) {
                    if (operand instanceof PsiReferenceExpression &&
                        varName.equals(((PsiReferenceExpression) operand).getReferenceName())
                    ) {
                        continue;
                    }
                    if (!isText(operand)) {
                        return false;
                    }
                }
            } else if (!isText(rExp)) {
                return false;
            }
        }

        // 2. 再检查定义点
        PsiElement origin = ref.resolve();
        if (origin instanceof PsiLocalVariable) {
            PsiExpression initializer = ((PsiLocalVariable) origin).getInitializer();
            if (initializer != null && !isText(initializer)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 通过一个引用节点，获取当前方法内该节点对应变量的所有引用点
     * @param reference PsiReference
     * @param maxOffset int             用偏移代表行号
     * @return List<PsiReference>
     */
    @NotNull
    private static List<PsiReference> getReferenceOnMethodScope(PsiReference reference, int maxOffset) {
        List<PsiReference> refResults = new ArrayList<>();
        PsiElement element = reference.resolve();
        if (element == null) {
            return refResults;
        }

        PsiMethod method = MoExpressionUtils.getParentOfMethod(element);
        if (method == null) {
            return refResults;
        }
        refResults = new ArrayList<>(ReferencesSearch
                .search(element, new LocalSearchScope(new PsiElement[]{method}, null, true))
                .findAll());
        if (maxOffset != -1) {
            refResults = refResults.stream()
                    .filter(item -> item.getElement().getTextOffset() <= maxOffset)
                    .collect(Collectors.toList());
        }
        return refResults;
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
