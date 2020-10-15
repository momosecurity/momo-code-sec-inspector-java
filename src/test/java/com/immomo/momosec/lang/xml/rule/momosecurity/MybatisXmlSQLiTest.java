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
package com.immomo.momosec.lang.xml.rule.momosecurity;


import com.immomo.momosec.lang.xml.MomoXmlCodeInsightFixtureTestCase;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.XmlElementFactory;
import com.intellij.psi.impl.source.xml.XmlTagImpl;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlText;
import com.intellij.testFramework.MockProblemDescriptor;
import org.junit.Assert;

public class MybatisXmlSQLiTest extends MomoXmlCodeInsightFixtureTestCase {

    public void testIfFindsAllVulns() {
        myFixture.copyFileToProject("rule/momosecurity/MybatisXmlSQLi/mybatis-3-mapper.dtd");
        doTest(new MybatisXmlSQLi(), "rule/momosecurity/MybatisXmlSQLi/Vuln.xml");
    }

    public void testSingleVar() {
        commonHashReplaceTest(
                "where id = ${id}",
                "where id = #{id}"
        );
    }

    public void testSingleVarWithType() {
        commonHashReplaceTest(
                "where id = ${id,jdbcType=VARCHAR}",
                "where id = #{id,jdbcType=VARCHAR}"
        );

    }

    public void testNotHandleFieldVar() {
        commonHashReplaceTest(
                "where ${field} = #{value}",
                "where ${field} = #{value}"
        );
    }

    public void testNotHandleTableVar() {
        commonHashReplaceTest(
                "select * from ${table}",
                "select * from ${table}"
        );
    }

    public void testWhereInQuickFix() {
        commonWhereInReplaceTest(
                "where id in ${ids}",

                "where id in \n" +
                "<foreach collection=\"ids\" item=\"idsItem\" open=\"(\" separator=\",\" close=\")\">\n" +
                "#{idsItem}\n" +
                "</foreach>\n"
        );
    }

    public void testDoubleWhereInQuickFix() {
        commonWhereInReplaceTest(
                "and (createdBy in ${userNameList} or projectId IN ${id})",

                "and (createdBy in \n" +
                "<foreach collection=\"userNameList\" item=\"userNameListItem\" open=\"(\" separator=\",\" close=\")\">\n" +
                "#{userNameListItem}\n" +
                "</foreach>\n" +
                " or projectId IN \n" +
                "<foreach collection=\"id\" item=\"idItem\" open=\"(\" separator=\",\" close=\")\">\n" +
                "#{idItem}\n" +
                "</foreach>\n" +
                ")"
        );

    }

    public void testWhereInWithQuoteQuickFix() {
        commonWhereInReplaceTest(
                "where id in (${ids})",
                "where id in \n" +
                "<foreach collection=\"ids\" item=\"idsItem\" open=\"(\" separator=\",\" close=\")\">\n" +
                "#{idsItem}\n" +
                "</foreach>\n"
        );
    }

    public void testLikeQuickFix() {
        commonHashReplaceTest(
                "where id = \"${id}\" and name like '%${name}%'",
                "where id = #{id} and name like CONCAT('%', #{name}, '%') "
        );
    }

    public void testLikeWithWhiteSpaceQuickFix() {
        commonHashReplaceTest(
                "and name like\n" +
                "    '%${name}%'",
                "and name like CONCAT('%', #{name}, '%') "
        );
    }

    public void commonHashReplaceTest(String origin, String expect) {
        Project project = myFixture.getProject();
        XmlTag tagFromText = XmlElementFactory.getInstance(project)
                .createTagFromText("<a>" + origin + "</a>");
        XmlText[] textElements = tagFromText.getValue().getTextElements();
        XmlText text;
        assert textElements.length != 0: "XmlTag Element not created";
        text = textElements[0];

        MockProblemDescriptor descriptor = new MockProblemDescriptor(text, "", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
        MybatisXmlSQLi.MybatisXmlSQLiQuickFix quickFix = new MybatisXmlSQLi.MybatisXmlSQLiQuickFix();
        quickFix.applyFix(project, descriptor);

        Assert.assertEquals(expect, text.getText());
    }

    public void commonWhereInReplaceTest(String origin, String expect) {
        Project project = myFixture.getProject();
        XmlText text = XmlElementFactory.getInstance(project).createDisplayText(origin);

        MockProblemDescriptor descriptor = new MockProblemDescriptor(text, "", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
        MybatisXmlSQLi.MybatisXmlSQLiQuickFix quickFix = new MybatisXmlSQLi.MybatisXmlSQLiQuickFix();
        quickFix.applyFix(project, descriptor);

        Assert.assertEquals(expect, ((XmlTagImpl) text.getParent()).getValue().getText());
    }
}
