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

public class SpringSessionFixProtectionTest extends MomoJavaCodeInsightFixtureTestCase {
    String prefix = "rule/momosecurity/SpringSessionFixProtection/";

    public void testIfFindAllVulns() {
        myFixture.copyFileToProject(prefix + "stub/HttpSecurity.java");
        myFixture.copyFileToProject(prefix + "stub/SessionManagementConfigurer.java");
        myFixture.copyFileToProject(prefix + "stub/WebSecurityConfigurerAdapter.java");
        doTest(new SpringSessionFixProtection(), prefix + "Vuln.java");
    }

    public void testQuickFix() {
        Project project = myFixture.getProject();
        PsiMethodCallExpression compileMethodCall = (PsiMethodCallExpression) JavaPsiFacade.getElementFactory(project)
                .createExpressionFromText("http.sessionManagement().sessionFixation().none()", null);

        MockProblemDescriptor descriptor =
                new MockProblemDescriptor(compileMethodCall, "", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
        SpringSessionFixProtection.SpringSessionFixProtectionQuickFix springSessionFixProtectionQuickFix =
                new SpringSessionFixProtection.SpringSessionFixProtectionQuickFix();
        springSessionFixProtectionQuickFix.applyFix(project, descriptor);

        Assert.assertEquals("http.sessionManagement().sessionFixation().migrateSession()", compileMethodCall.getText());
    }
}
