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
package com.immomo.momosec.lang;

import com.immomo.momosec.lang.java.utils.MoExpressionUtils;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.psi.*;
import org.apache.commons.codec.digest.MurmurHash3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class MomoBaseLocalInspectionTool extends AbstractBaseJavaLocalInspectionTool {

    public enum VulnElemType {
        ASSIGNMENT_EXPRESSION,
        LOCAL_VARIABLE,
        CLASS_FIELD
    }

    /**
     * 本方法针对可利用安全设置修复的漏洞，例如：
     * DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
     * dbf.setFeature(...);
     * 检查变量 (如dbf) 所在 scope 是否满足 visitor 的要求。
     * 若变量定义与使用分离(如dbf定义为类成员，但初始化在某一方法内)，则assignElem指初始化元素，resolvedElem为定义元素
     *
     * (1) 对于变量在方法/静态块/构造块内初始化， scope 为当前方法/静态块/构造块
     * (2) 对于变量是类成员变量，并且在定义时赋值，有两种情况
     * (2.1) 对于 static 成员变量，检查该类的静态块是否满足 visitor 要求
     * (2.2) 对于非 static 成员变量，检查该类的构造块是否满足 visitor 要求
     * @param assignElem  PsiElement
     * @param resolvedElem PsiElement
     * @param visitor PsiElementVisitor
     * @return boolean
     */
    protected boolean checkVariableUseFix(@Nullable PsiElement assignElem, @Nullable PsiElement resolvedElem, @NotNull MomoBaseFixElementWalkingVisitor visitor) {
        PsiMethod method = MoExpressionUtils.getParentOfMethod(assignElem);
        if (method != null) {
            method.accept(visitor);
            return visitor.isFix();
        }

        PsiClassInitializer initializer = MoExpressionUtils.getParentOfClassInitializer(assignElem);
        if (initializer != null) {
            initializer.accept(visitor);
            return visitor.isFix();
        }

        if (resolvedElem instanceof PsiField) {
            PsiField field = (PsiField)resolvedElem;
            if (field.hasModifierProperty(PsiModifier.STATIC)) {
                return checkStaticInitializersHasFix((PsiClass)field.getParent(), visitor);
            } else {
                return checkConstructorHasFix((PsiClass)field.getParent(), visitor);
            }
        }

        return false;
    }

    private boolean checkConstructorHasFix(PsiClass aClass, MomoBaseFixElementWalkingVisitor visitor) {
        PsiClassInitializer[] initializers = aClass.getInitializers();
        for (PsiClassInitializer initializer : initializers) {
            if (!initializer.hasModifierProperty(PsiModifier.STATIC)) {
                initializer.accept(visitor);
                if (visitor.isFix()) {
                    return true;
                }
            }
        }
        
        PsiMethod[] constructors = aClass.getConstructors();
        for(PsiMethod constructor : constructors) {
            constructor.accept(visitor);
            if(visitor.isFix()) {
                return true;
            }
        }
        return false;
    }

    private boolean checkStaticInitializersHasFix(PsiClass aClass, MomoBaseFixElementWalkingVisitor visitor) {
        PsiClassInitializer[] initializers = aClass.getInitializers();
        for(PsiClassInitializer initializer : initializers) {
            if (initializer.hasModifierProperty(PsiModifier.STATIC)) {
                initializer.accept(visitor);
                if (visitor.isFix()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static int getVulnSign(@NotNull PsiElement element) {
        return getVulnSign(
                MoExpressionUtils.getElementFQName(element),
                element.getText()
        );
    }

    public static int getVulnSign(@NotNull String fqname, @NotNull String elementText) {
        return MurmurHash3.hash32(String.format("%s|%s", fqname, elementText));
    }

}
