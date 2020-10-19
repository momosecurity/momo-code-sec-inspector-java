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
package com.immomo.momosec.lang.java;

import com.intellij.codeInspection.InspectionProfileEntry;
import com.intellij.openapi.roots.LanguageLevelProjectExtension;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.testFramework.MockProblemDescriptor;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;

import java.util.Locale;


public abstract class MomoJavaCodeInsightFixtureTestCase extends JavaCodeInsightFixtureTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/testData/lang/java/";
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        LanguageLevelProjectExtension.getInstance(getProject()).setLanguageLevel(LanguageLevel.JDK_1_7);
        Locale.setDefault(Locale.CHINESE);
    }

    protected void doTest(@NotNull InspectionProfileEntry inspection, @NotNull String... names) {
        myFixture.enableInspections(inspection);
        myFixture.testHighlightingAllFiles(true, false, false, names);
    }

    protected void testQuickFixInClassInitializer(String classInner, String[] expectAddStats, LocalQuickFix quickFix) {
        Project project = myFixture.getProject();
        PsiClass aClass = JavaPsiFacade.getElementFactory(project).createClassFromText(classInner, null);

        assert aClass.getFields().length == 1;
        PsiField field = aClass.getFields()[0];
        PsiExpression rExp = field.getInitializer();
        assert rExp != null;

        MockProblemDescriptor descriptor =
                new MockProblemDescriptor(rExp, "", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
        quickFix.applyFix(project, descriptor);

        PsiClassInitializer initializer = aClass.getInitializers()[0];
        PsiStatement firstStat = initializer.getBody().getStatements()[0];
        PsiExpressionStatement expStat;
        if (firstStat instanceof PsiTryStatement) {
            PsiTryStatement tryStatement = (PsiTryStatement)initializer.getBody().getStatements()[0];
            assert tryStatement.getTryBlock() != null;
            expStat = (PsiExpressionStatement)tryStatement.getTryBlock().getStatements()[0];
        } else {
            expStat = (PsiExpressionStatement)firstStat;
        }
        assert expStat.getExpression() instanceof PsiMethodCallExpression;

        String[] actual = new String[expectAddStats.length];
        PsiElement currStat = expStat;
        for (int i=0; i<expectAddStats.length && currStat != null; i++) {
            actual[i] = ((PsiExpressionStatement)currStat).getExpression().getText();
            do {
                currStat = currStat.getNextSibling();
            } while(i < expectAddStats.length - 1 && currStat != null && !(currStat instanceof PsiStatement));
        }
        Assert.assertArrayEquals(expectAddStats, actual);
    }

    protected void testQuickFixEntityInLocalVariable(String methodInner, String[] expectAddStats, LocalQuickFix quickFix) {
        Project project = myFixture.getProject();
        PsiMethod method = JavaPsiFacade.getElementFactory(project).createMethodFromText(
                "public foo () {\n" +
                        methodInner + "\n" +
                        "}", null);

        assert method.getBody() != null;
        PsiStatement[] methodStats = method.getBody().getStatements();
        assert methodStats.length > 0;

        assert methodStats[0] instanceof PsiDeclarationStatement;
        PsiElement[] declaredElements = ((PsiDeclarationStatement) methodStats[0]).getDeclaredElements();
        assert declaredElements[0] instanceof PsiLocalVariable;
        PsiExpression rExp = ((PsiLocalVariable)declaredElements[0]).getInitializer();
        if (rExp instanceof PsiTypeCastExpression) {
            rExp = ((PsiTypeCastExpression) rExp).getOperand();
        }
        assert rExp != null;
        MockProblemDescriptor descriptor =
                new MockProblemDescriptor(rExp, "", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
        quickFix.applyFix(project, descriptor);

        assert method.getBody().getStatements()[1] instanceof PsiExpressionStatement;
        PsiExpressionStatement expStat = (PsiExpressionStatement)method.getBody().getStatements()[1];
        assert expStat.getExpression() instanceof PsiMethodCallExpression;

        String[] actual = new String[expectAddStats.length];
        PsiElement currStat = expStat;
        for (int i=0; i<expectAddStats.length && currStat != null; i++) {
            actual[i] = ((PsiExpressionStatement)currStat).getExpression().getText();
            do {
                currStat = currStat.getNextSibling();
            } while(i < expectAddStats.length - 1 && currStat != null && !(currStat instanceof PsiStatement));
        }
        Assert.assertArrayEquals(expectAddStats, actual);
    }

    protected void testQuickFixEntityInMethodAssignment(String classInner, String[] expectAddStats, LocalQuickFix quickFix) {
        Project project = myFixture.getProject();
        PsiClass aClass = JavaPsiFacade.getElementFactory(project).createClassFromText(
                classInner, null);

        assert aClass.getMethods().length == 1;
        PsiMethod method = aClass.getMethods()[0];
        assert method.getBody() != null;
        PsiStatement[] methodStats = method.getBody().getStatements();
        assert methodStats.length > 0;

        assert methodStats[0] instanceof PsiExpressionStatement;
        PsiExpression innerExp = ((PsiExpressionStatement)methodStats[0]).getExpression();
        assert innerExp instanceof PsiAssignmentExpression;

        PsiExpression rExp = ((PsiAssignmentExpression)innerExp).getRExpression();
        if (rExp instanceof PsiTypeCastExpression) {
            rExp = ((PsiTypeCastExpression)rExp).getOperand();
        }
        assert rExp != null;
        MockProblemDescriptor descriptor =
                new MockProblemDescriptor(rExp, "", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
        quickFix.applyFix(project, descriptor);

        assert method.getBody().getStatements()[1] instanceof PsiExpressionStatement;
        PsiExpressionStatement expStat = (PsiExpressionStatement)method.getBody().getStatements()[1];
        assert expStat.getExpression() instanceof PsiMethodCallExpression;

        String[] actual = new String[expectAddStats.length];
        PsiElement currStat = expStat;
        for (int i=0; i<expectAddStats.length && currStat != null; i++) {
            actual[i] = ((PsiExpressionStatement)currStat).getExpression().getText();
            do {
                currStat = currStat.getNextSibling();
            } while(i < expectAddStats.length - 1 && currStat != null && !(currStat instanceof PsiStatement));
        }
        Assert.assertArrayEquals(expectAddStats, actual);
    }
}
