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
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;

import java.util.List;
import java.util.stream.Collectors;

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

    public void testIsSqliCareExpression() {
        PsiMethod method;
        method = factory.createMethodFromText(
                "public void foo() {" +
                "  String a = \"fooa\";" +
                "  String b = \"foob\" + a;" +
                "  StringBuilder sb = new StringBuilder();" +
                "  sb.append(\"foo\");" +
                "}"
                , null
        );
        assert method.getBody() != null;
        PsiStatement[] statements = method.getBody().getStatements();

        assert statements[0] instanceof PsiDeclarationStatement;
        PsiLocalVariable var = (PsiLocalVariable)((PsiDeclarationStatement) statements[0]).getDeclaredElements()[0];
        assert var.getInitializer() instanceof PsiLiteralExpression;
        Assert.assertTrue(MoExpressionUtils.isSqliCareExpression(var.getInitializer()));

        assert statements[1] instanceof PsiDeclarationStatement;
        PsiLocalVariable var2 = (PsiLocalVariable)((PsiDeclarationStatement) statements[1]).getDeclaredElements()[0];
        assert var2.getInitializer() instanceof PsiPolyadicExpression;
        Assert.assertTrue(MoExpressionUtils.isSqliCareExpression(var2.getInitializer()));

        assert statements[3] instanceof PsiExpressionStatement;
        PsiMethodCallExpression call = (PsiMethodCallExpression)((PsiExpressionStatement) statements[3]).getExpression();
        Assert.assertTrue(MoExpressionUtils.isSqliCareExpression(call));
    }

    public void testIsSqliCareExpressionOnFile() {
        String stringUtils = "utils/MoExpressionUtils/StringUtils.java";
        String testFile = "utils/MoExpressionUtils/TestIsSqliCareExpression.java";
        myFixture.copyFileToProject(stringUtils);
        myFixture.copyFileToProject(testFile);
        VirtualFile vf = myFixture.findFileInTempDir(testFile);
        PsiJavaFile file = (PsiJavaFile)getPsiManager().findFile(vf);

        assert file != null;
        PsiClass aClass = file.getClasses()[0];
        PsiMethod method = aClass.getMethods()[0];
        assert method.getBody() != null;
        PsiStatement[] statements = method.getBody().getStatements();
        assert statements[2] instanceof PsiExpressionStatement;
        Assert.assertTrue(
                MoExpressionUtils.isSqliCareExpression(
                        ((PsiExpressionStatement) statements[2]).getExpression()));
    }

    public void testDeconPolyadicExpression() {
        String testFile = "utils/MoExpressionUtils/TestDeconPolyadicExpression.java";
        String actual;

        actual = getLastPolyadicString(testFile, "polyadicWithField");
        Assert.assertEquals("select username, password, host from User  where type = UserType.OFFICE", actual);

        actual = getLastPolyadicString(testFile, "polyadicWithArgs");
        Assert.assertEquals("select * from T where id = <not Literal>", actual);

        actual = getLastPolyadicString(testFile, "polyadicWithLiteral");
        Assert.assertEquals("select * from T where id = id", actual);

        actual = getLastPolyadicString(testFile, "polyadicWithMultiLayerLiteral");
        Assert.assertEquals("select * from T where", actual);

        actual = getLastPolyadicString(testFile, "polyadicWithMultiLayerVar");
        Assert.assertEquals("select * from T <not Literal>", actual);

        actual = getLastPolyadicString(testFile, "polyadicWithStringBuilder");
        Assert.assertEquals("select * from T where id in <not Literal>", actual);

        actual = getLastPolyadicString(testFile, "ignore");
        Assert.assertEquals("select * from T where", actual);
    }

    private String getLastPolyadicString(String filename, String methodname) {
        myFixture.copyFileToProject(filename);
        VirtualFile vf = myFixture.findFileInTempDir(filename);
        PsiJavaFile file = (PsiJavaFile)getPsiManager().findFile(vf);

        assert file != null;
        PsiClass aClass = file.getClasses()[0];

        PsiMethod method = null;
        for(PsiMethod m : aClass.getMethods()) {
            if (m.getName().equals(methodname)) {
                method = m;
                break;
            }
        }
        assert method != null;
        assert method.getBody() != null;
        PsiStatement[] statements = method.getBody().getStatements();
        assert statements[statements.length-1] instanceof PsiDeclarationStatement;

        PsiDeclarationStatement sqlDeclaration = (PsiDeclarationStatement)statements[statements.length-1];
        assert sqlDeclaration.getDeclaredElements()[0] instanceof PsiLocalVariable;
        PsiLocalVariable sqlLocalVariable = (PsiLocalVariable)sqlDeclaration.getDeclaredElements()[0];

        assert sqlLocalVariable.getInitializer() instanceof PsiPolyadicExpression;
        List<PsiExpression> derefExps = MoExpressionUtils.deconPolyadicExpression((PsiPolyadicExpression)sqlLocalVariable.getInitializer());

        return StringUtils.join(derefExps.stream().map(item -> {
            if (item instanceof PsiLiteralExpression) {
                return ((PsiLiteralExpression)item).getValue();
            }
            return "<not Literal>";
        }).collect(Collectors.toList()), "");
    }
}
