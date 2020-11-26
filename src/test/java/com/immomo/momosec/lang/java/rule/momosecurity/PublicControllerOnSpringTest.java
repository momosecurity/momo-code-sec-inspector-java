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
import com.intellij.psi.*;
import com.intellij.testFramework.MockProblemDescriptor;

public class PublicControllerOnSpringTest extends MomoJavaCodeInsightFixtureTestCase {
    String prefix = "rule/momosecurity/PublicControllerOnSpring/";

    public void testIfFindAllVulns() {
        myFixture.copyFileToProject(prefix + "/stub/GetMapping.java");
        myFixture.copyFileToProject(prefix + "/stub/RequestMapping.java");
        myFixture.copyFileToProject(prefix + "/stub/RestController.java");
        doTest(new PublicControllerOnSpring(), prefix + "Vuln.java");
    }

    public void testQuickFix() {
        PsiMethod method = JavaPsiFacade.getElementFactory(myFixture.getProject()).createMethodFromText(
                "@RequestMapping(\"/a\")\n" +
                "private void a() {\n" +
                "    return ;\n" +
                "}",
                null
        );

        MockProblemDescriptor descriptor =
                new MockProblemDescriptor(method.getAnnotations()[0].getParent(), "", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
        PublicControllerOnSpring.PublicControllerOnSpringQuickFix quickFix =
                new PublicControllerOnSpring.PublicControllerOnSpringQuickFix();
        quickFix.applyFix(myFixture.getProject(), descriptor);

        assert method.hasModifierProperty(PsiModifier.PUBLIC);
    }
}
