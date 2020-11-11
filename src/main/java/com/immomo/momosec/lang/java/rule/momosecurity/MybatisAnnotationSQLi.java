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
import com.immomo.momosec.utils.SQLi;
import com.immomo.momosec.utils.Str;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiArrayInitializerMemberValueImpl;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static com.immomo.momosec.Constants.SQL_INJECTION_HELP_COMMENT;
import static com.immomo.momosec.utils.SQLi.*;

/**
 * 1003: Mybatis注解SQL注入漏洞
 *
 * Mybatis 注解SQL语句，使用${}方式插入的变量可能存在SQL注入的风险
 *
 * Mybatis可书写SQL语句的注解，接受的是字符串数组。如下均为正确的写法
 *
 * (1) @Select("select * from table")
 * (2) @Select("select * " + " from table")
 * (3) @Select({"select * from table"})
 * (4) @Select({"select", "*", "from", "table"})
 *
 * 当需要使用XML脚本时，需要将SQL语句插入<script></script>标签中
 * (*) @Select({"<script>", "select * from table <if test=\"id != null\">where id = #{id}</if>", "</script>"})
 */
public class MybatisAnnotationSQLi extends MomoBaseLocalInspectionTool {
    public static final String MESSAGE = InspectionBundle.message("mybatis.annotation.sqli.msg");
    private static final String QUICK_FIX_NAME = InspectionBundle.message("mybatis.annotation.sqli.fix");

    // 待检查的注解
    private static final Set<String> checkedAnnotations = new HashSet<>(Arrays.asList(
            "org.apache.ibatis.annotations.Select",
            "org.apache.ibatis.annotations.Delete",
            "org.apache.ibatis.annotations.Update",
            "org.apache.ibatis.annotations.Insert"
    ));
    private final MybatisAnnotationSQLiQuickFix mybatisAnnotationSQLiQuickFix = new MybatisAnnotationSQLiQuickFix();

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitAnnotation(PsiAnnotation annotation) {
                if (Boolean.FALSE.equals(checkedAnnotations.contains(annotation.getQualifiedName()))) {
                    return ;
                }
                PsiAnnotationParameterList psiAnnotationParameterList = annotation.getParameterList();
                PsiNameValuePair[] psiNameValuePairs = psiAnnotationParameterList.getAttributes();
                if (psiNameValuePairs.length != 1) { return ; }
                PsiNameValuePair psiNameValuePair = psiNameValuePairs[0];

                String content;
                content = psiNameValuePair.getLiteralValue();   // 见注释 (1)
                if (content == null) {
                    PsiElement innerElem = psiNameValuePair.getValue();
                    if (innerElem == null) {
                        return ;
                    }

                    if (innerElem instanceof PsiPolyadicExpression) {   // 见注释 (2)
                        content = MoExpressionUtils.getText((PsiPolyadicExpression)innerElem, true);
                    } else if (innerElem instanceof PsiArrayInitializerMemberValue) {   // 见注释 (3) / (4)
                        content = Arrays.stream(((PsiArrayInitializerMemberValueImpl)innerElem).getInitializers())
                                .map(elem -> MoExpressionUtils.getText((PsiExpression)elem, true))
                                .collect(Collectors.joining());
                    }
                }

                if (content != null && !content.isEmpty() && hasSQLi(content) && psiNameValuePair.getValue() != null) {
                    holder.registerProblem(psiNameValuePair.getValue(), MESSAGE, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, mybatisAnnotationSQLiQuickFix);
                }
            }
        };
    }

    private boolean hasSQLi(String content) {
        if (content.startsWith("<script>") && content.endsWith("</script>")) {
            content = content.substring(8, content.length() - 9);
        }

        List<String> fragments = new ArrayList<>(Arrays.asList(dollarVarPattern.split(content)));
        if (fragments.size() == 1 && fragments.get(0).equals(content)) {
            return false;
        }

        if (fragments.size() > 1) {
            fragments.remove(fragments.size() - 1);
        }

        return SQLi.hasVulOnAdditiveFragments(fragments);
    }

    public static class MybatisAnnotationSQLiQuickFix implements LocalQuickFix {

        @Override
        public @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String getFamilyName() {
            return QUICK_FIX_NAME;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElement problemElem = descriptor.getPsiElement();

            boolean isFix = false;

            // 目前仅对单一字符串进行自动替换
            if (problemElem instanceof PsiLiteralExpression) {
                String content = MoExpressionUtils.getLiteralInnerText((PsiLiteralExpression)problemElem);
                if (content != null) {
                    String newContent = replaceDollarWithHashtagOnString(content, 0);
                    PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);
                    problemElem = problemElem.replace(factory.createExpressionFromText("\"" + newContent + "\"", problemElem));
                    isFix = !newContent.contains("$");
                }
            }

            // 未自动修复，则展示help comment
            if (Boolean.FALSE.equals(isFix)) {
                PsiMethod method = MoExpressionUtils.getParentOfMethod(problemElem);
                if (method == null) {
                    return ;
                }
                PsiElement firstChild = method.getFirstChild();

                PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);
                PsiComment comment = factory.createCommentFromText(SQL_INJECTION_HELP_COMMENT, null);

                method.addBefore(comment, firstChild);
            }
        }

        private String replaceDollarWithHashtagOnString(@NotNull String content, int offset) {
            Matcher m = dollarVarPattern.matcher(content);
            if(m.find(offset)) {
                String prefix = content.substring(0, m.start());
                String suffix = content.substring(m.end());
                String inner = m.group();
                if (SQLi.hasVulOnAdditiveFragments(Collections.singletonList(prefix))) {
                    String lowerPrefix = prefix.toLowerCase();
                    if (whereInEndPattern.matcher(lowerPrefix).find()) {
                        // where in 不处理
                        content = replaceDollarWithHashtagOnString(content, m.end());
                    } else if (likeEndPatterh.matcher(lowerPrefix).find()) {
                        // like '%${var} 或 like "%${var} 情况
                        String concat = " CONCAT('%', " + inner.replace('$', '#') + ", '%') ";
                        prefix = StringUtils.stripEnd(prefix, "'\"% ");
                        suffix = StringUtils.stripStart(suffix, "'\"% ");
                        content = replaceDollarWithHashtagOnString(prefix + concat + suffix, prefix.length() + concat.length() - 1);
                    } else {
                        if (Str.rtrim(prefix).endsWith("'") || Str.rtrim(prefix).endsWith("\"")) {
                            prefix = Str.rtrim(prefix).substring(0, prefix.length()-1);
                            suffix = Str.ltrim(suffix).substring(1);
                        }
                        content = replaceDollarWithHashtagOnString(prefix + inner.replace('$', '#') + suffix, prefix.length() + inner.length());
                    }
                }
            }
            return content;
        }
    }

}
