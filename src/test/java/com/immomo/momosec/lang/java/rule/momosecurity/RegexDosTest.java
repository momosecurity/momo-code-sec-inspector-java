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
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.testFramework.MockProblemDescriptor;
import org.junit.Assert;


public class RegexDosTest extends MomoJavaCodeInsightFixtureTestCase {

    public void testIfFindsAllVulns() {
        myFixture.copyFileToProject("rule/momosecurity/RegexDos/stub/Pattern.java");
        doTest(new RegexDos(), "rule/momosecurity/RegexDos/Vuln.java");
    }

    public void testReplaceWithRE2JQuickFix() {
        Project project = myFixture.getProject();
        PsiMethodCallExpression compileMethodCall = (PsiMethodCallExpression) JavaPsiFacade.getElementFactory(project)
                .createExpressionFromText("Pattern.compile(\"foo\")", null);


        MockProblemDescriptor descriptor =
                new MockProblemDescriptor(compileMethodCall.getArgumentList().getExpressions()[0],
                        "", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
        RegexDos.RegexDosWithRe2jQuickFix quickFix = new RegexDos.RegexDosWithRe2jQuickFix();
        quickFix.applyFix(project, descriptor);

        assert compileMethodCall.getMethodExpression().getQualifier() != null;
        Assert.assertEquals("com.google.re2j.Pattern", compileMethodCall.getMethodExpression().getQualifier().getText());
    }
}
