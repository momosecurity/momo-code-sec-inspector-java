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

import com.immomo.momosec.lang.MomoBaseLocalInspectionTool;
import com.immomo.momosec.lang.java.MomoJavaCodeInsightFixtureTestCase;


public class XxeInspectorTest extends MomoJavaCodeInsightFixtureTestCase {

    String dirPrefix = "rule/momosecurity/XxeInspector/";

    public void testIfFindAllFactoriesVulns() {
        myFixture.copyFileToProject(dirPrefix + "stub/DocumentBuilderFactory.java");
        myFixture.copyFileToProject(dirPrefix + "stub/SAXBuilder.java");
        myFixture.copyFileToProject(dirPrefix + "stub/SAXParserFactory.java");
        myFixture.copyFileToProject(dirPrefix + "stub/SAXReader.java");
        myFixture.copyFileToProject(dirPrefix + "stub/SAXTransformerFactory.java");
        myFixture.copyFileToProject(dirPrefix + "stub/Schema.java");
        myFixture.copyFileToProject(dirPrefix + "stub/SchemaFactory.java");
        myFixture.copyFileToProject(dirPrefix + "stub/TransformerFactory.java");
        myFixture.copyFileToProject(dirPrefix + "stub/Validator.java");
        myFixture.copyFileToProject(dirPrefix + "stub/XMLConstants.java");
        myFixture.copyFileToProject(dirPrefix + "stub/XMLInputFactory.java");
        myFixture.copyFileToProject(dirPrefix + "stub/XMLReader.java");
        myFixture.copyFileToProject(dirPrefix + "stub/XMLReaderFactory.java");

        doTest(new XxeInspector(), dirPrefix + "FactoriesVulns.java");
    }

    public void testIfFindAllPositionVulns() {
        myFixture.copyFileToProject(dirPrefix + "stub/DocumentBuilderFactory.java");

        doTest(new XxeInspector(), dirPrefix + "PositionVulns.java");
    }

    public void testMultiFixStatementVuln() {
        myFixture.copyFileToProject(dirPrefix + "stub/SAXTransformerFactory.java");
        myFixture.copyFileToProject(dirPrefix + "stub/XMLConstants.java");

        doTest(new XxeInspector(), dirPrefix + "MultiFixStatementVuln.java");
    }

    public void testDocumentBuilderFactoryLocalVuln() {
        testQuickFixEntityInLocalVariable(
                "DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();",
                new String[]{"dbf.setFeature(\"http://apache.org/xml/features/disallow-doctype-decl\", true)"},
                new XxeInspector.XxeInspectionQuickFix(XxeInspector.XmlFactory.DOCUMENT_BUILDER_FACTORY, MomoBaseLocalInspectionTool.VulnElemType.LOCAL_VARIABLE)
        );
    }

    public void testDocumentBuilderFactoryClassVuln() {
        testQuickFixEntityInMethodAssignment(
                "DocumentBuilderFactory dbf;\n" +
                "public foo () {\n" +
                "   dbf = DocumentBuilderFactory.newInstance();" +
                "}",
                new String[]{"dbf.setFeature(\"http://apache.org/xml/features/disallow-doctype-decl\", true)"},
                new XxeInspector.XxeInspectionQuickFix(XxeInspector.XmlFactory.DOCUMENT_BUILDER_FACTORY, MomoBaseLocalInspectionTool.VulnElemType.ASSIGNMENT_EXPRESSION)
        );
    }

    public void testSAXParserFactoryLocalVuln() {
        testQuickFixEntityInLocalVariable(
                "SAXParserFactory spf = SAXParserFactory.newInstance();",
                new String[]{"spf.setFeature(\"http://apache.org/xml/features/disallow-doctype-decl\", true)"},
                new XxeInspector.XxeInspectionQuickFix(XxeInspector.XmlFactory.SAX_PARSER_FACTORY, MomoBaseLocalInspectionTool.VulnElemType.LOCAL_VARIABLE)
        );
    }

    public void testSAXParserFactoryClassVuln() {
        testQuickFixEntityInMethodAssignment(
                "SAXParserFactory spf;\n" +
                "public foo() {\n" +
                "   spf = SAXParserFactory.newInstance();\n" +
                "}",
                new String[]{"spf.setFeature(\"http://apache.org/xml/features/disallow-doctype-decl\", true)"},
                new XxeInspector.XxeInspectionQuickFix(XxeInspector.XmlFactory.SAX_PARSER_FACTORY, MomoBaseLocalInspectionTool.VulnElemType.ASSIGNMENT_EXPRESSION)
        );
    }

    public void testSAXTransformerFactoryLocalVuln() {
        testQuickFixEntityInLocalVariable(
                "SAXTransformerFactory sf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();",
                new String[]{
                    "sf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, \"\")",
                    "sf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, \"\")"
                },
                new XxeInspector.XxeInspectionQuickFix(XxeInspector.XmlFactory.SAX_TRANSFORMER_FACTORY, MomoBaseLocalInspectionTool.VulnElemType.LOCAL_VARIABLE)
        );
    }

    public void testSAXTransformerFactoryClassVuln() {
        testQuickFixEntityInMethodAssignment(
                "SAXTransformerFactory sf;\n" +
                "public foo() {\n" +
                "   sf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();\n" +
                "}",
                new String[]{
                    "sf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, \"\")",
                    "sf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, \"\")"
                },
                new XxeInspector.XxeInspectionQuickFix(XxeInspector.XmlFactory.SAX_TRANSFORMER_FACTORY, MomoBaseLocalInspectionTool.VulnElemType.ASSIGNMENT_EXPRESSION)
        );
    }

    public void testSAXBuilderLocalVuln() {
        testQuickFixEntityInLocalVariable(
                "SAXBuilder saxBuilder = new SAXBuilder();",
                new String[]{"saxBuilder.setFeature(\"http://apache.org/xml/features/disallow-doctype-decl\", true)"},
                new XxeInspector.XxeInspectionQuickFix(XxeInspector.XmlFactory.SAX_BUILDER, MomoBaseLocalInspectionTool.VulnElemType.LOCAL_VARIABLE)
        );
    }

    public void testSAXBuilderClassVuln() {
        testQuickFixEntityInMethodAssignment(
                "SAXBuilder saxBuilder;\n" +
                "public foo() {\n" +
                "  saxBuilder = new SAXBuilder();\n" +
                "}",
                new String[]{"saxBuilder.setFeature(\"http://apache.org/xml/features/disallow-doctype-decl\", true)"},
                new XxeInspector.XxeInspectionQuickFix(XxeInspector.XmlFactory.SAX_BUILDER, MomoBaseLocalInspectionTool.VulnElemType.ASSIGNMENT_EXPRESSION)
        );
    }

    public void testSAXReaderLocalVuln() {
        testQuickFixEntityInLocalVariable(
                "SAXReader saxReader = new SAXReader();",
                new String[]{"saxReader.setFeature(\"http://apache.org/xml/features/disallow-doctype-decl\", true)"},
                new XxeInspector.XxeInspectionQuickFix(XxeInspector.XmlFactory.SAX_READER, MomoBaseLocalInspectionTool.VulnElemType.LOCAL_VARIABLE)
        );
    }

    public void testSAXReaderClassVuln() {
        testQuickFixEntityInMethodAssignment(
                "SAXReader saxReader;\n" +
                "public foo() {\n" +
                "  saxReader = new SAXReader();\n" +
                "}",
                new String[]{"saxReader.setFeature(\"http://apache.org/xml/features/disallow-doctype-decl\", true)"},
                new XxeInspector.XxeInspectionQuickFix(XxeInspector.XmlFactory.SAX_READER, MomoBaseLocalInspectionTool.VulnElemType.ASSIGNMENT_EXPRESSION)
        );
    }

    public void testXMLReaderFactoryLocalVuln() {
        testQuickFixEntityInLocalVariable(
                "XMLReader reader = XMLReaderFactory.createXMLReader();",
                new String[]{"reader.setFeature(\"http://apache.org/xml/features/disallow-doctype-decl\", true)"},
                new XxeInspector.XxeInspectionQuickFix(XxeInspector.XmlFactory.XML_READER_FACTORY, MomoBaseLocalInspectionTool.VulnElemType.LOCAL_VARIABLE)
        );
    }

    public void testXMLReaderFactoryClassVuln() {
        testQuickFixEntityInMethodAssignment(
                "XMLReader reader;\n" +
                "public foo() {\n" +
                "  reader = XMLReaderFactory.createXMLReader();\n" +
                "}",
                new String[]{"reader.setFeature(\"http://apache.org/xml/features/disallow-doctype-decl\", true)"},
                new XxeInspector.XxeInspectionQuickFix(XxeInspector.XmlFactory.XML_READER_FACTORY, MomoBaseLocalInspectionTool.VulnElemType.ASSIGNMENT_EXPRESSION)
        );
    }

    public void testSchemaFactoryLocalVuln() {
        testQuickFixEntityInLocalVariable(
                "SchemaFactory factory = SchemaFactory.newInstance(\"http://www.w3.org/2001/XMLSchema\");",
                new String[] {
                    "factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, \"\")",
                    "factory.setProperty(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, \"\")"
                },
                new XxeInspector.XxeInspectionQuickFix(XxeInspector.XmlFactory.SCHEMA_FACTORY, MomoBaseLocalInspectionTool.VulnElemType.LOCAL_VARIABLE)
        );
    }

    public void testSchemaFactoryClassVuln() {
        testQuickFixEntityInMethodAssignment(
                "SchemaFactory factory;\n" +
                "public foo() {\n" +
                "  factory = SchemaFactory.newInstance(\"http://www.w3.org/2001/XMLSchema\");\n" +
                "}",
                new String[] {
                    "factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, \"\")",
                    "factory.setProperty(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, \"\")"
                },
                new XxeInspector.XxeInspectionQuickFix(XxeInspector.XmlFactory.SCHEMA_FACTORY, MomoBaseLocalInspectionTool.VulnElemType.ASSIGNMENT_EXPRESSION)
        );
    }

    public void testXMLInputFactoryLocalVuln() {
        testQuickFixEntityInLocalVariable(
                "XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();",
                new String[] {
                    "xmlInputFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, \"\")",
                    "xmlInputFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, \"\")"
                },
                new XxeInspector.XxeInspectionQuickFix(XxeInspector.XmlFactory.XML_INPUT_FACTORY, MomoBaseLocalInspectionTool.VulnElemType.LOCAL_VARIABLE)
        );
    }

    public void testXMLInputFactoryClassVuln() {
        testQuickFixEntityInMethodAssignment(
                "XMLInputFactory xmlInputFactory;\n" +
                "public foo() {\n" +
                "  xmlInputFactory = XMLInputFactory.newFactory();\n" +
                "}",
                new String[] {
                    "xmlInputFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, \"\")",
                    "xmlInputFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, \"\")"
                },
                new XxeInspector.XxeInspectionQuickFix(XxeInspector.XmlFactory.XML_INPUT_FACTORY, MomoBaseLocalInspectionTool.VulnElemType.ASSIGNMENT_EXPRESSION)
        );
    }

    public void testTransformerFactoryLocalVuln() {
        testQuickFixEntityInLocalVariable(
                "TransformerFactory tf = TransformerFactory.newInstance();",
                new String[] {
                    "tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, \"\")",
                    "tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, \"\")"
                },
                new XxeInspector.XxeInspectionQuickFix(XxeInspector.XmlFactory.TRANSFORMER_FACTORY, MomoBaseLocalInspectionTool.VulnElemType.LOCAL_VARIABLE)
        );
    }

    public void testTransformerFactoryClassVuln() {
        testQuickFixEntityInMethodAssignment(
                "TransformerFactory tf;\n" +
                "public foo() {\n" +
                "  tf = TransformerFactory.newInstance();\n" +
                "}",
                new String[] {
                    "tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, \"\")",
                    "tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, \"\")"
                },
                new XxeInspector.XxeInspectionQuickFix(XxeInspector.XmlFactory.TRANSFORMER_FACTORY, MomoBaseLocalInspectionTool.VulnElemType.ASSIGNMENT_EXPRESSION)
        );
    }

    public void testValidatorLocalVuln() {
        testQuickFixEntityInLocalVariable(
                "Validator validator = schema.newValidator();",
                new String[] {
                    "validator.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, \"\")",
                    "validator.setProperty(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, \"\")"
                },
                new XxeInspector.XxeInspectionQuickFix(XxeInspector.XmlFactory.VALIDATOR_OF_SCHEMA, MomoBaseLocalInspectionTool.VulnElemType.LOCAL_VARIABLE)
        );
    }

    public void testValidatorClassVuln() {
        testQuickFixEntityInMethodAssignment(
                "Validator validator;\n" +
                "public foo() {\n" +
                "  validator = schema.newValidator();\n" +
                "}",
                new String[] {
                    "validator.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, \"\")",
                    "validator.setProperty(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, \"\")"
                },
                new XxeInspector.XxeInspectionQuickFix(XxeInspector.XmlFactory.VALIDATOR_OF_SCHEMA, MomoBaseLocalInspectionTool.VulnElemType.ASSIGNMENT_EXPRESSION)
        );
    }

    public void testDocumentBuilderFactoryClassFieldInitVuln()  {
        testQuickFixInClassInitializer(
                "private final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();",
                new String[]{"dbf.setFeature(\"http://apache.org/xml/features/disallow-doctype-decl\", true)"},
                new XxeInspector.XxeInspectionQuickFix(XxeInspector.XmlFactory.DOCUMENT_BUILDER_FACTORY, MomoBaseLocalInspectionTool.VulnElemType.CLASS_FIELD)
        );
    }

    public void testDocumentBuilderFactoryStaticClassFieldInitVuln()  {
        testQuickFixInClassInitializer(
                "private static DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();",
                new String[]{"dbf.setFeature(\"http://apache.org/xml/features/disallow-doctype-decl\", true)"},
                new XxeInspector.XxeInspectionQuickFix(XxeInspector.XmlFactory.DOCUMENT_BUILDER_FACTORY, MomoBaseLocalInspectionTool.VulnElemType.CLASS_FIELD)
        );
    }

    public void testValidatorClassFieldInitVuln() {
        testQuickFixInClassInitializer(
                "private final Validator validator = schema.newValidator();",
                new String[] {
                    "validator.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, \"\")",
                    "validator.setProperty(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, \"\")"
                },
                new XxeInspector.XxeInspectionQuickFix(XxeInspector.XmlFactory.VALIDATOR_OF_SCHEMA, MomoBaseLocalInspectionTool.VulnElemType.CLASS_FIELD)
        );
    }

    public void testValidatorStaticClassFieldInitVuln() {
        testQuickFixInClassInitializer(
                "private static final Validator validator = schema.newValidator();",
                new String[] {
                    "validator.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, \"\")",
                    "validator.setProperty(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, \"\")"
                },
                new XxeInspector.XxeInspectionQuickFix(XxeInspector.XmlFactory.VALIDATOR_OF_SCHEMA, MomoBaseLocalInspectionTool.VulnElemType.CLASS_FIELD)
        );
    }
}
