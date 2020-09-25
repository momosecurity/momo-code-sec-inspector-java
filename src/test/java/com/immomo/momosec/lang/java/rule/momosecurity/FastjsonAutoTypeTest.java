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

public class FastjsonAutoTypeTest extends MomoJavaCodeInsightFixtureTestCase {
    String dirPrefix = "rule/momosecurity/FastjsonAutoType/";

    public void testIfFindsAllVulns() {
        myFixture.copyFileToProject(dirPrefix + "stub/ParserConfig.java");
        doTest(new FastjsonAutoType(), dirPrefix + "Vuln.java");
    }
}
