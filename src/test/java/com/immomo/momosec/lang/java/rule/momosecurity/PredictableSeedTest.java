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

public class PredictableSeedTest extends MomoJavaCodeInsightFixtureTestCase {

    String dirPrefix = "rule/momosecurity/PredictableSeed/";

    public void testIfFindsAllVulns() {
        myFixture.copyFileToProject(dirPrefix + "/stub/SecureRandom.java");
        doTest(new PredictableSeed(), dirPrefix + "Vuln.java");
    }

    public void testQuickFix() {
        Project project = myFixture.getProject();
        PsiNewExpression newExpression = (PsiNewExpression) JavaPsiFacade.getElementFactory(project)
                .createExpressionFromText("new SecureRandom(\"hello\".getBytes(\"us-ascii\"))", null);


        MockProblemDescriptor descriptor =
                new MockProblemDescriptor(newExpression, "", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
        PredictableSeed.PredictableSeedQuickFix quickFix = new PredictableSeed.PredictableSeedQuickFix(newExpression.getArgumentList());
        quickFix.applyFix(project, descriptor);

        Assert.assertEquals(0, newExpression.getArgumentList().getExpressions().length);
    }
}
