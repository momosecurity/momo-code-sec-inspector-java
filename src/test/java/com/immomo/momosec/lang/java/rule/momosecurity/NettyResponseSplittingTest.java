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
import org.junit.Assert;

public class NettyResponseSplittingTest extends MomoJavaCodeInsightFixtureTestCase {
    String dirPrefix = "rule/momosecurity/NettyResponseSplitting/";

    public void testIfFindsAllVulns() {
        myFixture.copyFileToProject(dirPrefix + "stub/DefaultHttpHeaders.java");
        myFixture.copyFileToProject(dirPrefix + "stub/DefaultHttpResponse.java");
        doTest(new NettyResponseSplitting(), dirPrefix + "Vuln.java");
    }

    public void testDefaultHttpHeadersQuickFix() {
        commonNewExpressionTest("new DefaultHttpHeaders(false)", 0);
    }

    public void testDefaulthttpResponseQuickFix() {
        commonNewExpressionTest("new DefaultHttpResponse(version, httpResponseStatus, false)", 2);
    }

    public void testDefaulthttpResponseNotVuln() {
        commonNewExpressionTest("new DefaultHttpResponse(version, httpResponseStatus)", 2);
    }

    public void commonNewExpressionTest(String newExpressionStr, int fixArgIdx) {
        Project project = myFixture.getProject();
        PsiNewExpression newExpression = (PsiNewExpression) JavaPsiFacade.getElementFactory(project)
                .createExpressionFromText(newExpressionStr, null);

        MockProblemDescriptor descriptor =
                new MockProblemDescriptor(newExpression, "", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
        NettyResponseSplitting.NettyResponseSplittingQuickFix quickFix = new NettyResponseSplitting.NettyResponseSplittingQuickFix(fixArgIdx);
        quickFix.applyFix(project, descriptor);

        PsiExpressionList expressionList = newExpression.getArgumentList();
        assert expressionList != null;
        PsiExpression[] expressions = expressionList.getExpressions();
        if (expressions.length > fixArgIdx) {
            PsiLiteralExpression arg0 = (PsiLiteralExpression)expressions[fixArgIdx];
            Assert.assertEquals(Boolean.TRUE, arg0.getValue());
        }
    }
}
