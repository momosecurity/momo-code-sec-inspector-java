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
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.testFramework.MockProblemDescriptor;

public class PredictableSeedTest extends MomoJavaCodeInsightFixtureTestCase {

    String dirPrefix = "rule/momosecurity/PredictableSeed/";

    public void testIfFindsAllVulns() {
        myFixture.copyFileToProject(dirPrefix + "/stub/SecureRandom.java");
        doTest(new PredictableSeed(), dirPrefix + "Vuln.java");
    }

    public void testQuickFix() {
        Project project = myFixture.getProject();
        PsiMethodCallExpression compileMethodCall = (PsiMethodCallExpression) JavaPsiFacade.getElementFactory(project)
                .createExpressionFromText("prng.setSeed(1L)", null);


        MockProblemDescriptor descriptor =
                new MockProblemDescriptor(compileMethodCall, "", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
        PredictableSeed.PredictableSeedQuickFix quickFix = new PredictableSeed.PredictableSeedQuickFix();
        quickFix.applyFix(project, descriptor);

        PsiExpression[] args = compileMethodCall.getArgumentList().getExpressions();
        assert args.length > 0;
        PsiExpression arg0 = args[0];
        assert !(arg0 instanceof PsiLiteralExpression);
    }
}
