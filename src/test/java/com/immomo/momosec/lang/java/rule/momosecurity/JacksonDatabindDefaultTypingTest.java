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
import org.junit.Assert;

public class JacksonDatabindDefaultTypingTest extends MomoJavaCodeInsightFixtureTestCase {
    String dirPrefix = "rule/momosecurity/JacksonDatabindDefaultTyping/";

    public void testDefaultTypingVulns() {
        myFixture.copyFileToProject(dirPrefix + "stub/ObjectMapper.java");
        myFixture.copyFileToProject(dirPrefix + "stub/Jackson2JsonRedisSerializer.java");
        doTest(new JacksonDatabindDefaultTyping(), dirPrefix + "DefaultTypingVuln.java");
    }

    public void testAnnotationVulns() {
        myFixture.copyFileToProject(dirPrefix + "stub/JsonTypeInfo.java");
        doTest(new JacksonDatabindDefaultTyping(), dirPrefix + "AnnotationVuln.java");
    }

    public void testAnnotationQuickFix() {
        Project project = myFixture.getProject();
        PsiAnnotation annotation = JavaPsiFacade.getElementFactory(project).createAnnotationFromText(
                "@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)", null);

        assert annotation.getParameterList().getAttributes().length > 0;
        PsiNameValuePair nameValuePair = annotation.getParameterList().getAttributes()[0];
        MockProblemDescriptor descriptor = new MockProblemDescriptor(nameValuePair, "", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);

        JacksonDatabindDefaultTyping.AnnotationQuickFix quickFix = new JacksonDatabindDefaultTyping.AnnotationQuickFix();
        quickFix.applyFix(project, descriptor);

        PsiNameValuePair fixNameValuePair = annotation.getParameterList().getAttributes()[0];
        assert fixNameValuePair.getValue() != null;
        Assert.assertEquals("JsonTypeInfo.Id.NAME", fixNameValuePair.getValue().getText());
    }
}
