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


public class XxeInspectorTest extends MomoJavaCodeInsightFixtureTestCase {

    String dirPrefix = "rule/momosecurity/XxeInspector/";

    public void testIfFindsAllVulns() {
        myFixture.copyFileToProject(dirPrefix + "stub/DocumentBuilderFactory.java");
        myFixture.copyFileToProject(dirPrefix + "stub/SAXParserFactory.java");
        myFixture.copyFileToProject(dirPrefix + "stub/SAXTransformerFactory.java");
        myFixture.copyFileToProject(dirPrefix + "stub/XMLConstants.java");
        doTest(new XxeInspector(), dirPrefix + "Vuln.java");
    }

    public void testDocumentBuilderFactoryLocalVuln() {
        testQuickFixEntityInLocalVariable(
                "DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();",
                "dbf.setFeature(\"http://apache.org/xml/features/disallow-doctype-decl\", true)",
                new XxeInspector.XxeInspectionQuickFix(XxeInspector.XmlFactory.DOCUMENT_BUILDER, XxeInspector.VulnElemType.LOCAL_VARIABLE)
        );
    }

    public void testDocumentBuilderFactoryClassVuln() {
        testQuickFixEntityInMethodAssignment(
                "DocumentBuilderFactory dbf;\n" +
                "public foo () {\n" +
                "   dbf = DocumentBuilderFactory.newInstance();" +
                "}",
                "dbf.setFeature(\"http://apache.org/xml/features/disallow-doctype-decl\", true)",
                new XxeInspector.XxeInspectionQuickFix(XxeInspector.XmlFactory.DOCUMENT_BUILDER, XxeInspector.VulnElemType.ASSIGNMENT_EXPRESSION)
        );
    }

    public void testSAXParserFactoryLocalVuln() {
        testQuickFixEntityInLocalVariable(
                "SAXParserFactory spf = SAXParserFactory.newInstance();",
                "spf.setFeature(\"http://apache.org/xml/features/disallow-doctype-decl\", true)",
                new XxeInspector.XxeInspectionQuickFix(XxeInspector.XmlFactory.SAX_PARSER_FACTORY, XxeInspector.VulnElemType.LOCAL_VARIABLE)
        );
    }

    public void  testSAXParserFactoryClassVuln() {
        testQuickFixEntityInMethodAssignment(
                "SAXParserFactory spf;\n" +
                "public foo() {\n" +
                "   spf = SAXParserFactory.newInstance();\n" +
                "}",
                "spf.setFeature(\"http://apache.org/xml/features/disallow-doctype-decl\", true)",
                new XxeInspector.XxeInspectionQuickFix(XxeInspector.XmlFactory.SAX_PARSER_FACTORY, XxeInspector.VulnElemType.ASSIGNMENT_EXPRESSION)
        );
    }

    public void testSAXTransformerFactoryLocalVuln() {
        testQuickFixEntityInLocalVariable(
                "SAXTransformerFactory sf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();",
                "sf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, \"\")",
                new XxeInspector.XxeInspectionQuickFix(XxeInspector.XmlFactory.SAX_TRANSFORMER_FACTORY, XxeInspector.VulnElemType.LOCAL_VARIABLE)
        );
    }

    public void  testSAXTransformerFactoryClassVuln() {
        testQuickFixEntityInMethodAssignment(
                "SAXTransformerFactory sf;\n" +
                "public foo() {\n" +
                "   sf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();\n" +
                "}",
                "sf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, \"\")",
                new XxeInspector.XxeInspectionQuickFix(XxeInspector.XmlFactory.SAX_TRANSFORMER_FACTORY, XxeInspector.VulnElemType.ASSIGNMENT_EXPRESSION)
        );
    }

    public void testDocumentBuilderFactoryClassFieldInitVuln()  {
        testQuickFixInClassInitializer(
                "private final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();",
                "dbf.setFeature(\"http://apache.org/xml/features/disallow-doctype-decl\", true)",
                new XxeInspector.XxeInspectionQuickFix(XxeInspector.XmlFactory.DOCUMENT_BUILDER, XxeInspector.VulnElemType.CLASS_FIELD)
        );
    }

    public void testDocumentBuilderFactoryStaticClassFieldInitVuln()  {
        testQuickFixInClassInitializer(
                "private static DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();",
                "dbf.setFeature(\"http://apache.org/xml/features/disallow-doctype-decl\", true)",
                new XxeInspector.XxeInspectionQuickFix(XxeInspector.XmlFactory.DOCUMENT_BUILDER, XxeInspector.VulnElemType.CLASS_FIELD)
        );
    }
}
