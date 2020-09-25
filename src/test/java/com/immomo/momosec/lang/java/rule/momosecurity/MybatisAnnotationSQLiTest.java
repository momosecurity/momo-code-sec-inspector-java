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

import com.immomo.momosec.lang.java.MomoJavaCodeInsightFixtureTestCase;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.testFramework.MockProblemDescriptor;
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;

import static com.immomo.momosec.Constants.SQL_INJECTION_HELP_COMMENT;

public class MybatisAnnotationSQLiTest extends MomoJavaCodeInsightFixtureTestCase {
    String dirPrefix = "rule/momosecurity/MybatisAnnotationSQLi/";

    public void testIfFindsAllVulns() {
        myFixture.copyFileToProject(dirPrefix + "stub/Select.java");
        myFixture.copyFileToProject(dirPrefix + "stub/Param.java");
        doTest(new MybatisAnnotationSQLi(), dirPrefix + "Vuln.java");
    }

    public void testLiteralExpressionQuickFix() {
        Project project = myFixture.getProject();
        PsiClass aClass = JavaPsiFacade.getInstance(project).getElementFactory().createClassFromText(
                "@Select(\"select count(1) from T where id = '${id}'\")\n" +
                "public int getNum(@Param(\"id\") String id);",
                null);

        PsiAnnotation annotation = getAnnotation(aClass, "Select");
        assert annotation != null;
        PsiNameValuePair[] valuePairs = annotation.getParameterList().getAttributes();
        assert valuePairs.length > 0;
        assert valuePairs[0].getValue() != null;
        MockProblemDescriptor descriptor = new MockProblemDescriptor(valuePairs[0].getValue(), "", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
        MybatisAnnotationSQLi.MybatisAnnotationSQLiQuickFix quickFix = new MybatisAnnotationSQLi.MybatisAnnotationSQLiQuickFix();
        quickFix.applyFix(project, descriptor);

        PsiNameValuePair[] newValuePairs = annotation.getParameterList().getAttributes();
        assert newValuePairs.length > 0;
        Assert.assertEquals("select count(1) from T where id = #{id}", newValuePairs[0].getLiteralValue());
    }

    public  void testLikeLiteralExpressionQuickFix() {
        Project project = myFixture.getProject();
        PsiClass aClass = JavaPsiFacade.getInstance(project).getElementFactory().createClassFromText(
                "@Select(\"select count(1) from T where id = ${id} and name like '%${like}%'\")\n" +
                "public int getNum(@Param(\"id\") String id);",
                null);

        PsiAnnotation annotation = getAnnotation(aClass, "Select");
        assert annotation != null;
        PsiNameValuePair[] valuePairs = annotation.getParameterList().getAttributes();
        assert valuePairs.length > 0;
        assert valuePairs[0].getValue() != null;
        MockProblemDescriptor descriptor = new MockProblemDescriptor(valuePairs[0].getValue(), "", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
        MybatisAnnotationSQLi.MybatisAnnotationSQLiQuickFix quickFix = new MybatisAnnotationSQLi.MybatisAnnotationSQLiQuickFix();
        quickFix.applyFix(project, descriptor);

        PsiNameValuePair[] newValuePairs = annotation.getParameterList().getAttributes();
        assert newValuePairs.length > 0;
        Assert.assertEquals("select count(1) from T where id = #{id} and name like CONCAT('%', #{like}, '%') ", newValuePairs[0].getLiteralValue());
    }

    public void testShowHelpComment() {
        Project project = myFixture.getProject();
        PsiClass aClass = JavaPsiFacade.getElementFactory(project).createClassFromText(
                "@Select(\"select count(1) from T where id = \" + \"${id}\")\n" +
                "public int getNum(@Param(\"id\") String id);",
                null);

        PsiAnnotation annotation = getAnnotation(aClass, "Select");
        assert annotation != null;
        PsiNameValuePair[] valuePairs = annotation.getParameterList().getAttributes();
        assert valuePairs.length > 0;
        assert valuePairs[0].getValue() != null;
        MockProblemDescriptor descriptor = new MockProblemDescriptor(valuePairs[0].getValue(), "", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
        MybatisAnnotationSQLi.MybatisAnnotationSQLiQuickFix quickFix = new MybatisAnnotationSQLi.MybatisAnnotationSQLiQuickFix();
        quickFix.applyFix(project, descriptor);

        PsiMethod method = (PsiMethod)annotation.getParent().getParent();
        PsiElement firstElem = method.getFirstChild();

        assert firstElem instanceof PsiComment;
        Assert.assertEquals(SQL_INJECTION_HELP_COMMENT, firstElem.getText());

        assert firstElem.getNextSibling() instanceof PsiWhiteSpace;
    }

    public void testWhereInShowHelpComment() {
        Project project = myFixture.getProject();
        PsiClass aClass = JavaPsiFacade.getElementFactory(project).createClassFromText(
                "@Select(\"select count(1) from T where id in (${ids}) and name = ${name}\")\n" +
                "public int getNum(@Param(\"id\") String id);",
                null);

        PsiAnnotation annotation = getAnnotation(aClass, "Select");
        assert annotation != null;
        PsiNameValuePair[] valuePairs = annotation.getParameterList().getAttributes();
        assert valuePairs.length > 0;
        assert valuePairs[0].getValue() != null;
        MockProblemDescriptor descriptor = new MockProblemDescriptor(valuePairs[0].getValue(), "", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
        MybatisAnnotationSQLi.MybatisAnnotationSQLiQuickFix quickFix = new MybatisAnnotationSQLi.MybatisAnnotationSQLiQuickFix();
        quickFix.applyFix(project, descriptor);

        PsiMethod method = (PsiMethod)annotation.getParent().getParent();
        PsiElement firstElem = method.getFirstChild();

        assert firstElem instanceof PsiComment;
        Assert.assertEquals(SQL_INJECTION_HELP_COMMENT, firstElem.getText());

        assert firstElem.getNextSibling() instanceof PsiWhiteSpace;
    }

    @Nullable
    private PsiAnnotation getAnnotation(PsiClass aClass, String annotationName) {
        PsiMethod method = aClass.getMethods()[0];
        PsiAnnotation[] annotations = method.getAnnotations();
        for(PsiAnnotation annotation : annotations) {
            if (annotationName.equals(annotation.getQualifiedName())) {
                return annotation;
            }
        }
        return null;
    }
}
