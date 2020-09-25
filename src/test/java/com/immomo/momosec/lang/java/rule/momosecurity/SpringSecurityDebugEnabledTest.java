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
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiNameValuePair;
import com.intellij.testFramework.MockProblemDescriptor;

public class SpringSecurityDebugEnabledTest extends MomoJavaCodeInsightFixtureTestCase {
    String prefix = "rule/momosecurity/SpringSecurityDebugEnabled/";

    public void testIfFindAllVulns() {
        myFixture.copyFileToProject(prefix + "stub/EnableWebSecurity.java");
        doTest(new SpringSecurityDebugEnabled(), prefix + "Vuln.java");
    }

    public void testDebugDisable() {
        Project project = myFixture.getProject();
        PsiAnnotation annotation = (PsiAnnotation)JavaPsiFacade.getElementFactory(project).createAnnotationFromText(
                "@EnableWebSecurity(debug = true)", null);

        PsiNameValuePair[] nameValuePairs = annotation.getParameterList().getAttributes();

        MockProblemDescriptor descriptor =
                new MockProblemDescriptor(nameValuePairs[0], "", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
        SpringSecurityDebugEnabled.SpringSecurityDebugDisable quickFix = new SpringSecurityDebugEnabled.SpringSecurityDebugDisable();
        quickFix.applyFix(project, descriptor);

        PsiNameValuePair[] nameValuePairs1 = annotation.getParameterList().getAttributes();
        assert nameValuePairs1.length == 1;
        assert "false".equals(nameValuePairs1[0].getLiteralValue());
    }
}
