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
package com.immomo.momosec.fix;

import com.immomo.momosec.lang.java.MomoJavaCodeInsightFixtureTestCase;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.testFramework.MockProblemDescriptor;
import org.junit.Assert;

import static com.immomo.momosec.Constants.SQL_INJECTION_HELP_COMMENT;

public class ShowHelpCommentQuickFixTest extends MomoJavaCodeInsightFixtureTestCase {

    public void testVariableShowHelpQuickFix() {
        Project project = myFixture.getProject();
        PsiMethod aMethod = JavaPsiFacade.getElementFactory(project).createMethodFromText(
                "public void foo() {\n" +
                "   String foo = \"sql injection\";\n" +
                "}", null
        );

        assert aMethod.getBody() != null;
        PsiStatement[] statements = aMethod.getBody().getStatements();
        assert statements.length > 0;
        PsiDeclarationStatement declarationStatement = (PsiDeclarationStatement)statements[0];

        PsiElement[] declaredElems = declarationStatement.getDeclaredElements();
        assert declaredElems.length > 0;

        PsiLocalVariable var = (PsiLocalVariable)declaredElems[0];
        PsiLiteralExpression literalExpression = (PsiLiteralExpression)var.getInitializer();
        assert literalExpression != null;

        MockProblemDescriptor descriptor = new MockProblemDescriptor(literalExpression, "", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
        ShowHelpCommentQuickFix quickFix = new ShowHelpCommentQuickFix("quickFix", SQL_INJECTION_HELP_COMMENT);
        quickFix.applyFix(project, descriptor);

        PsiElement comment = declarationStatement.getPrevSibling().getPrevSibling();
        assert comment instanceof PsiComment;

        Assert.assertEquals(SQL_INJECTION_HELP_COMMENT, comment.getText());
        assert comment.getPrevSibling() instanceof PsiWhiteSpace;
    }

    public void testFieldShowHelpQuickFix() {
        Project project = myFixture.getProject();
        PsiClass aClass = JavaPsiFacade.getElementFactory(project).createClassFromText(
                "String foo = \"foo\";\n" +
                "String sql = \"select\" + foo;", null
        );

        PsiField[] fields = aClass.getFields();
        assert fields.length == 2;

        PsiElement polyElem = fields[1].getInitializer();
        assert polyElem instanceof PsiPolyadicExpression;

        MockProblemDescriptor descriptor = new MockProblemDescriptor(polyElem, "", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
        ShowHelpCommentQuickFix quickFix = new ShowHelpCommentQuickFix("quickFix", SQL_INJECTION_HELP_COMMENT);
        quickFix.applyFix(project, descriptor);

        assert fields[1].getPrevSibling().getPrevSibling() instanceof PsiComment;
        PsiComment comment = (PsiComment)fields[1].getPrevSibling().getPrevSibling();

        Assert.assertEquals(SQL_INJECTION_HELP_COMMENT, comment.getText());
        assert comment.getPrevSibling() instanceof PsiWhiteSpace;
        assert comment.getNextSibling() instanceof PsiWhiteSpace;
    }
}
