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
package com.immomo.momosec.lang.java.utils;

import com.immomo.momosec.lang.java.MomoJavaCodeInsightFixtureTestCase;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiMethod;
import org.junit.Assert;

public class MoExpressionUtilsTest extends MomoJavaCodeInsightFixtureTestCase {

    Project project;
    PsiElementFactory factory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.project = myFixture.getProject();
        this.factory = JavaPsiFacade.getElementFactory(this.project);
    }

    public void testGetMethodFQName() {
        PsiMethod method;
        method = factory.createMethodFromText("public void foo(String one, Integer two)", null);
        Assert.assertEquals("null void foo(java.lang.String one, java.lang.Integer two)", MoExpressionUtils.getMethodFQName(method));

        method = factory.createMethodFromText("public static Map<String, Object> getFoo(Object bean)", null);
        Assert.assertEquals("null Map<String,Object> getFoo(java.lang.Object bean)", MoExpressionUtils.getMethodFQName(method));
    }

    public void testGetElementFQName() {
        PsiClass aClass = factory.createClassFromText("" +
                "private String foo;" +
                "public void method_one(Integer id){}", null);

        Assert.assertEquals("_Dummy_ void method_one(java.lang.Integer id)", MoExpressionUtils.getElementFQName(aClass.getMethods()[0]));
        Assert.assertEquals("_Dummy_", MoExpressionUtils.getElementFQName(aClass.getFields()[0]));
    }
}
