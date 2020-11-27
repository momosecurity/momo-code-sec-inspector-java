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
import com.intellij.util.ObjectUtils;
import org.junit.Assert;

public class ReplacePseudorandomGeneratorTest extends MomoJavaCodeInsightFixtureTestCase {
    String prefix = "rule/momosecurity/ReplacePseudorandomGenerator/";

    public void testIfFindAllVulns() {
        doTest(new ReplacePseudorandomGenerator(), prefix + "Vuln.java");
    }

    public void testReplaceOnLocalVariableInit() {
        Project project = myFixture.getProject();
        PsiDeclarationStatement declar = (PsiDeclarationStatement)JavaPsiFacade.getElementFactory(project)
                .createStatementFromText("Random random = new Random();", null);
        assert declar.getDeclaredElements().length == 1;
        PsiLocalVariable localVariable = ObjectUtils.tryCast(declar.getDeclaredElements()[0], PsiLocalVariable.class);
        assert localVariable != null;
        PsiNewExpression newExpression = ObjectUtils.tryCast(localVariable.getInitializer(), PsiNewExpression.class);
        assert newExpression != null;

        MockProblemDescriptor descriptor =
                new MockProblemDescriptor(newExpression, "", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
        ReplacePseudorandomGenerator.ReplacePseudorandomGeneratorQuickFix quickFix =
                new ReplacePseudorandomGenerator.ReplacePseudorandomGeneratorQuickFix();
        quickFix.applyFix(project, descriptor);

        Assert.assertEquals("SecureRandom random = new SecureRandom();", declar.getText());
    }

    public void testReplaceOnSeparateInit() {
        Project project = myFixture.getProject();
        PsiMethod method = JavaPsiFacade.getElementFactory(project)
                .createMethodFromText("public foo() {\n" +
                        "Random random;\n" +
                        "random = new Random();" +
                        "}", null);
        assert method.getBody() != null;
        PsiStatement[] statements = method.getBody().getStatements();

        PsiExpressionStatement expressionStatement = ObjectUtils.tryCast(statements[1], PsiExpressionStatement.class);
        assert expressionStatement != null;
        PsiAssignmentExpression assign = ObjectUtils.tryCast(expressionStatement.getExpression(), PsiAssignmentExpression.class);
        assert assign != null;
        assert assign.getRExpression() != null;

        MockProblemDescriptor descriptor =
                new MockProblemDescriptor(assign.getRExpression(), "", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
        ReplacePseudorandomGenerator.ReplacePseudorandomGeneratorQuickFix quickFix =
                new ReplacePseudorandomGenerator.ReplacePseudorandomGeneratorQuickFix();
        quickFix.applyFix(project, descriptor);

        PsiStatement[] newStatements = method.getBody().getStatements();
        Assert.assertEquals("SecureRandom random;", newStatements[0].getText());
        Assert.assertEquals("random = new SecureRandom();", newStatements[1].getText());
    }

}
