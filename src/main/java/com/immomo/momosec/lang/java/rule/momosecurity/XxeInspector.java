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

import com.immomo.momosec.lang.InspectionBundle;
import com.immomo.momosec.lang.MomoBaseFixElementWalkingVisitor;
import com.immomo.momosec.lang.MomoBaseLocalInspectionTool;
import com.immomo.momosec.lang.java.utils.MoExpressionUtils;
import com.intellij.codeInsight.daemon.impl.quickfix.ImportClassFix;
import com.intellij.codeInsight.intention.QuickFixFactory;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.siyeh.ig.psiutils.ExpressionUtils;
import com.siyeh.ig.psiutils.MethodCallUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 1010: XXE漏洞
 *
 * 检查如下几种XXE工厂:
 * (1) DocumentBuilderFactory
 * (2) SAXParserFactory
 * (3) SAXTransformerFactory
 * (4) SAXBuilder
 * (5) SAXReader
 * (6) XMLReaderFactory
 * (7) SchemaFactory
 * (8) XMLInputFactory
 * (9) TransformerFactory
 * (10) Validator
 *
 * ref:
 * (1) https://owasp.org/www-community/vulnerabilities/XML_External_Entity_(XXE)_Processing
 */
public class XxeInspector extends MomoBaseLocalInspectionTool {
    public static final String MESSAGE = InspectionBundle.message("xxe.inspector.msg");
    private static final String QUICK_FIX_NAME = InspectionBundle.message("xxe.inspector.fix");

    public enum XmlFactory {
        DOCUMENT_BUILDER_FACTORY,
        SAX_PARSER_FACTORY,
        SAX_TRANSFORMER_FACTORY,
        SAX_BUILDER,
        SAX_READER,
        XML_READER_FACTORY,
        SCHEMA_FACTORY,
        XML_INPUT_FACTORY,
        TRANSFORMER_FACTORY,
        VALIDATOR_OF_SCHEMA
    }

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                if (MoExpressionUtils.hasFullQualifiedName(expression, "javax.xml.parsers.DocumentBuilderFactory", "newInstance")) {
                    commonExpressionCheck(expression, "setFeature", XmlFactory.DOCUMENT_BUILDER_FACTORY, false);
                } else if (MoExpressionUtils.hasFullQualifiedName(expression, "javax.xml.parsers.SAXParserFactory", "newInstance")) {
                    commonExpressionCheck(expression, "setFeature", XmlFactory.SAX_PARSER_FACTORY, false);
                } else if (MoExpressionUtils.hasFullQualifiedName(expression, "javax.xml.transform.sax.SAXTransformerFactory", "newInstance")) {
                    commonExpressionCheck(expression, "setAttribute", XmlFactory.SAX_TRANSFORMER_FACTORY, true);
                } else if (MoExpressionUtils.hasFullQualifiedName(expression, "org.xml.sax.helpers.XMLReaderFactory", "createXMLReader")) {
                    commonExpressionCheck(expression, "setFeature", XmlFactory.XML_READER_FACTORY, false);
                } else if (MoExpressionUtils.hasFullQualifiedName(expression, "javax.xml.validation.SchemaFactory", "newInstance")) {
                    commonExpressionCheck(expression, "setProperty", XmlFactory.SCHEMA_FACTORY, false);
                } else if (MoExpressionUtils.hasFullQualifiedName(expression, "javax.xml.stream.XMLInputFactory", "newFactory")) {
                    commonExpressionCheck(expression, "setProperty", XmlFactory.XML_INPUT_FACTORY, false);
                } else if (MoExpressionUtils.hasFullQualifiedName(expression, "javax.xml.transform.TransformerFactory", "newInstance")) {
                    commonExpressionCheck(expression, "setAttribute", XmlFactory.TRANSFORMER_FACTORY, true);
                } else if (MoExpressionUtils.hasFullQualifiedName(expression, "javax.xml.validation.Schema", "newValidator")) {
                    commonExpressionCheck(expression, "setProperty", XmlFactory.VALIDATOR_OF_SCHEMA, false);
                }
            }

            @Override
            public void visitNewExpression(PsiNewExpression expression) {
                if (MoExpressionUtils.hasFullQualifiedName(expression, "org.jdom.input.SAXBuilder")) {
                    commonExpressionCheck(expression, "setFeature", XmlFactory.SAX_BUILDER, false);
                } else if (MoExpressionUtils.hasFullQualifiedName(expression, "org.dom4j.io.SAXReader")) {
                    commonExpressionCheck(expression, "setFeature", XmlFactory.SAX_READER, false);
                }
            }

            private void commonExpressionCheck(PsiCallExpression expression, String methodName, XmlFactory xmlFactory, boolean withTypeCast) {
                if (expression.getParent() instanceof PsiAssignmentExpression ||
                    (withTypeCast &&
                     expression.getParent() instanceof PsiTypeCastExpression &&
                     expression.getParent().getParent() instanceof PsiAssignmentExpression)
                ) {
                    assignmentExpressionCheck(holder, expression, methodName, xmlFactory);
                } else if (expression.getParent() instanceof PsiLocalVariable ||
                    (withTypeCast &&
                     expression.getParent() instanceof PsiTypeCastExpression &&
                     expression.getParent().getParent() instanceof PsiLocalVariable)
                ) {
                    localVariableCheck(holder, expression, methodName, xmlFactory);
                } else if (expression.getParent() instanceof PsiField ||
                    (withTypeCast &&
                     expression.getParent() instanceof PsiTypeCastExpression &&
                     expression.getParent().getParent() instanceof PsiField)
                ) {
                    classFieldCheck(holder, expression, methodName, xmlFactory);
                }
            }
        };
    }

    private void assignmentExpressionCheck(@NotNull ProblemsHolder holder, PsiCallExpression expression, String shouldUsedMethodName, XmlFactory xmlFactory) {
        PsiElement parent = expression.getParent();
        if (parent instanceof PsiTypeCastExpression) {
            parent = parent.getParent();
        }

        PsiAssignmentExpression assignmentExpression = (PsiAssignmentExpression)parent;
        PsiElement resolvedElem = ((PsiReferenceExpression) assignmentExpression.getLExpression()).resolve();
        DisableEntityElementVisitor visitor = new DisableEntityElementVisitor(shouldUsedMethodName, xmlFactory, resolvedElem);

        if (checkVariableUseFix(assignmentExpression, resolvedElem, visitor)) {
            return ;
        }

        holder.registerProblem(
                expression,
                MESSAGE,
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                new XxeInspectionQuickFix(xmlFactory, VulnElemType.ASSIGNMENT_EXPRESSION)
        );
    }

    private void localVariableCheck(@NotNull ProblemsHolder holder, PsiCallExpression expression, String shouldUsedMethodName, XmlFactory xmlFactory) {
        PsiElement parent = expression.getParent();
        if (parent instanceof PsiTypeCastExpression) {
            parent = parent.getParent();
        }

        PsiLocalVariable localVariable = (PsiLocalVariable)parent;
        DisableEntityElementVisitor visitor = new DisableEntityElementVisitor(shouldUsedMethodName, xmlFactory, localVariable);
        if (checkVariableUseFix(localVariable, null, visitor)) {
            return ;
        }

        holder.registerProblem(
                expression,
                MESSAGE,
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                new XxeInspectionQuickFix(xmlFactory, VulnElemType.LOCAL_VARIABLE)
        );
    }

    private void classFieldCheck(@NotNull ProblemsHolder holder, PsiCallExpression expression, String shouldUsedMethodName, XmlFactory xmlFactory) {
        PsiElement parent  = expression.getParent();
        if (parent instanceof PsiTypeCastExpression) {
            parent = parent.getParent();
        }

        PsiField field = (PsiField) parent;
        DisableEntityElementVisitor visitor = new DisableEntityElementVisitor(shouldUsedMethodName, xmlFactory, field);
        if (checkVariableUseFix(null, field, visitor)) {
            return ;
        }

        holder.registerProblem(
                expression,
                MESSAGE,
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                new XxeInspectionQuickFix(xmlFactory, VulnElemType.CLASS_FIELD)
        );
    }

    private static class DisableEntityElementVisitor extends MomoBaseFixElementWalkingVisitor {

        private final String shouldUsedMethodName;
        private final XmlFactory xmlFactory;
        private final PsiElement refVar;
        private Map<String, Boolean> needSatisfiedRules;

        public DisableEntityElementVisitor(String shouldUsedMethodName, XmlFactory xmlFactory, PsiElement refVar) {
            this.shouldUsedMethodName = shouldUsedMethodName;
            this.xmlFactory = xmlFactory;
            this.refVar = refVar;

            if (xmlFactory.equals(XmlFactory.DOCUMENT_BUILDER_FACTORY) ||
                xmlFactory.equals(XmlFactory.SAX_PARSER_FACTORY) ||
                xmlFactory.equals(XmlFactory.SAX_BUILDER) ||
                xmlFactory.equals(XmlFactory.SAX_READER) ||
                xmlFactory.equals(XmlFactory.XML_READER_FACTORY)
            ) {
                this.needSatisfiedRules = new HashMap<String, Boolean>() {{
                   put("http://apache.org/xml/features/disallow-doctype-decl", false);
                }};
            } else if (xmlFactory.equals(XmlFactory.SAX_TRANSFORMER_FACTORY) ||
                xmlFactory.equals(XmlFactory.SCHEMA_FACTORY) ||
                xmlFactory.equals(XmlFactory.XML_INPUT_FACTORY) ||
                xmlFactory.equals(XmlFactory.TRANSFORMER_FACTORY) ||
                xmlFactory.equals(XmlFactory.VALIDATOR_OF_SCHEMA)
            ) {
                this.needSatisfiedRules = new HashMap<String, Boolean>() {{
                   put("XMLConstants.ACCESS_EXTERNAL_DTD", false);
                   put("XMLConstants.ACCESS_EXTERNAL_STYLESHEET", false);
               }};
            }
        }

        @Override
        public void visitElement(PsiElement element) {
            if (element instanceof PsiMethodCallExpression) {
                PsiMethodCallExpression methodCallExpression = (PsiMethodCallExpression)element;
                if (shouldUsedMethodName.equals(MethodCallUtils.getMethodName(methodCallExpression))) {
                    PsiExpressionList args = methodCallExpression.getArgumentList();
                    if (!(args.getExpressions().length == 2)) { return ; }
                    // 根据 xmlFactory 类型判断参数值
                    if (xmlFactory.equals(XmlFactory.DOCUMENT_BUILDER_FACTORY) ||
                        xmlFactory.equals(XmlFactory.SAX_PARSER_FACTORY) ||
                        xmlFactory.equals(XmlFactory.SAX_BUILDER) ||
                        xmlFactory.equals(XmlFactory.SAX_READER) ||
                        xmlFactory.equals(XmlFactory.XML_READER_FACTORY)
                    ) {
                        if (ExpressionUtils.isLiteral(args.getExpressions()[0], "http://apache.org/xml/features/disallow-doctype-decl") &&
                            ExpressionUtils.isLiteral(args.getExpressions()[1], Boolean.TRUE)
                        ) {
                            this.needSatisfiedRules.replace("http://apache.org/xml/features/disallow-doctype-decl", true);
                        } else {
                            return ;
                        }
                    } else if (xmlFactory.equals(XmlFactory.SAX_TRANSFORMER_FACTORY) ||
                        xmlFactory.equals(XmlFactory.SCHEMA_FACTORY) ||
                        xmlFactory.equals(XmlFactory.XML_INPUT_FACTORY) ||
                        xmlFactory.equals(XmlFactory.TRANSFORMER_FACTORY) ||
                        xmlFactory.equals(XmlFactory.VALIDATOR_OF_SCHEMA)
                    ) {
                        if (!(args.getExpressions()[0] instanceof PsiReferenceExpression)) { return ; }
                        if (args.getExpressions()[0].textMatches("XMLConstants.ACCESS_EXTERNAL_DTD") &&
                            ExpressionUtils.isEmptyStringLiteral(args.getExpressions()[1])
                        ) {
                            this.needSatisfiedRules.replace("XMLConstants.ACCESS_EXTERNAL_DTD", true);
                        } else if (args.getExpressions()[0].textMatches("XMLConstants.ACCESS_EXTERNAL_STYLESHEET") &&
                            ExpressionUtils.isEmptyStringLiteral(args.getExpressions()[1])
                        ) {
                            this.needSatisfiedRules.replace("XMLConstants.ACCESS_EXTERNAL_STYLESHEET", true);
                        } else {
                            return ;
                        }
                    } else { return ; }

                    PsiExpression refQualifier = methodCallExpression.getMethodExpression().getQualifierExpression();
                    if (refQualifier != null &&
                        refQualifier.getReference() != null &&
                        refVar.isEquivalentTo(refQualifier.getReference().resolve()) &&
                        this.needSatisfiedRules.values().stream().allMatch(Boolean::booleanValue)
                    ) {
                        this.setFix(true);
                        this.stopWalking();
                        return;
                    }
                }
                return ; // 停止继续 walking 当前的 methodcall
            }
            super.visitElement(element);
        }
    }

    public static class XxeInspectionQuickFix implements LocalQuickFix {

        private final XmlFactory xmlFactory;
        private final VulnElemType vulnElemType;

        public XxeInspectionQuickFix(XmlFactory xmlFactory, VulnElemType vulnElemType) {
            this.xmlFactory = xmlFactory;
            this.vulnElemType = vulnElemType;
        }

        @Override
        public @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String getFamilyName() {
            return QUICK_FIX_NAME;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElement newInstanceElem = descriptor.getPsiElement();
            PsiElement parent = newInstanceElem.getParent();
            if (parent instanceof PsiTypeCastExpression) {
                parent = parent.getParent();
            }

            // 取到变量名称
            String varName = null;
            if (vulnElemType.equals(VulnElemType.ASSIGNMENT_EXPRESSION)) {
                if (((PsiAssignmentExpression)parent).getLExpression() instanceof PsiReferenceExpression) {
                    varName = ((PsiReferenceExpression)((PsiAssignmentExpression)parent).getLExpression()).getReferenceName();
                }
            } else if (vulnElemType.equals(VulnElemType.LOCAL_VARIABLE)) {
                varName = ((PsiLocalVariable)parent).getName();
            } else if (vulnElemType.equals(VulnElemType.CLASS_FIELD)) {
                varName = ((PsiField)parent).getName();
            }

            if (varName == null) { return ; }

            PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);

            // 生成待插入的语句内容
            List<String> blockTextes = new ArrayList<>();
            if (xmlFactory.equals(XmlFactory.DOCUMENT_BUILDER_FACTORY) ||
                xmlFactory.equals(XmlFactory.SAX_PARSER_FACTORY) ||
                xmlFactory.equals(XmlFactory.SAX_BUILDER) ||
                xmlFactory.equals(XmlFactory.SAX_READER) ||
                xmlFactory.equals(XmlFactory.XML_READER_FACTORY)
            ) {
                blockTextes.add(varName + ".setFeature(\"http://apache.org/xml/features/disallow-doctype-decl\", true);");
            } else if (
                xmlFactory.equals(XmlFactory.SAX_TRANSFORMER_FACTORY) ||
                xmlFactory.equals(XmlFactory.TRANSFORMER_FACTORY)
            ) {
                blockTextes.add(varName+".setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, \"\");");
                blockTextes.add(varName+".setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, \"\");");
            } else if (
                xmlFactory.equals(XmlFactory.SCHEMA_FACTORY) ||
                xmlFactory.equals(XmlFactory.XML_INPUT_FACTORY) ||
                xmlFactory.equals(XmlFactory.VALIDATOR_OF_SCHEMA)
            ) {
                blockTextes.add(varName+".setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, \"\");");
                blockTextes.add(varName+".setProperty(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, \"\");");
            }
            if (blockTextes.size() == 0) { return ; }

            if (VulnElemType.LOCAL_VARIABLE.equals(vulnElemType) ||
                VulnElemType.ASSIGNMENT_EXPRESSION.equals(vulnElemType)
            ) {
                // parent is expression and dualParent is Statement
                PsiElement dualParent = parent.getParent();

                // 1. 先写入一句
                PsiStatement disableEntityStat = factory.createStatementFromText(blockTextes.get(0), null);
                dualParent.getParent().addAfter(disableEntityStat, dualParent);

                // 2. 对于 XXE 的修复方法，如果出现 XMLConstants. 则需要import该类
                if (blockTextes.get(0).contains("XMLConstants.")) {
                    PsiElement next = dualParent.getNextSibling();
                    while(!(next instanceof PsiStatement)) {
                        next = next.getNextSibling();
                    }
                    if (next instanceof PsiExpressionStatement &&
                        ((PsiExpressionStatement) next).getExpression() instanceof PsiMethodCallExpression) {
                        PsiExpressionList expressionList = ((PsiMethodCallExpression) ((PsiExpressionStatement) next).getExpression()).getArgumentList();
                        classImportFix(project, descriptor, (PsiReferenceExpression)expressionList.getExpressions()[0].getFirstChild());
                    }
                }

                // 3. 增加Throws 或 Try Catch
                PsiElement toWriteBlock = dualParent.getParent();
                PsiElement firstStatOnBlock = dualParent;
                if (MoExpressionUtils.getParentOfMethod(parent) != null) {
                    QuickFixFactory.getInstance()
                            .createAddExceptionToThrowsFix(((PsiExpressionStatement) dualParent.getNextSibling()).getExpression())
                            .invoke(project, null, descriptor.getPsiElement().getContainingFile());
                    firstStatOnBlock = dualParent.getNextSibling();
                } else {
                    // scope in initializer block
                    addTryCatchFix(project, descriptor, dualParent.getNextSibling());
                    if (dualParent.getNextSibling() instanceof PsiTryStatement) {
                        toWriteBlock = ((PsiTryStatement)dualParent.getNextSibling()).getTryBlock();
                        if (toWriteBlock != null && ((PsiCodeBlock)toWriteBlock).getStatements().length > 0) {
                            firstStatOnBlock = ((PsiCodeBlock)toWriteBlock).getStatements()[0];
                        }
                    }
                }

                // 4. 如果是多语句修复，写入剩余语句 (倒叙写入保证顺序)
                for (int i=blockTextes.size()-1; i>=1; i--){
                    disableEntityStat = factory.createStatementFromText(blockTextes.get(i), null);
                    if (toWriteBlock != null) {
                        toWriteBlock.addAfter(disableEntityStat, firstStatOnBlock);
                    }
                }
            } else {  // 类成员定义时就初始化的情况
                PsiField field = (PsiField)parent;
                PsiClass aClass = (PsiClass)field.getParent();
                PsiField[] fields = aClass.getFields();
                PsiField lastField = fields[fields.length - 1];

                PsiJavaFile aFile;

                // 1. 先写入一句
                String blockContent = blockTextes.get(0);
                if (field.hasModifierProperty(PsiModifier.STATIC)) {
                    aFile = (PsiJavaFile)PsiFileFactory.getInstance(project)
                            .createFileFromText(
                                    "_Dummy_." + JavaFileType.INSTANCE.getDefaultExtension(),
                                    JavaFileType.INSTANCE,
                                    "class _Dummy_ { static { "+blockContent+" } }"
                            );

                } else {
                    aFile = (PsiJavaFile)PsiFileFactory.getInstance(project)
                            .createFileFromText(
                                    "_Dummy_." + JavaFileType.INSTANCE.getDefaultExtension(),
                                    JavaFileType.INSTANCE,
                                    "class _Dummy_ { { "+blockContent+" } }"
                            );
                }

                PsiClassInitializer classInitializer = aFile.getClasses()[0].getInitializers()[0];
                aClass.addAfter(classInitializer, lastField);

                // 2. 添加 Try Catch
                PsiClassInitializer firstInitializer = aClass.getInitializers()[0];
                PsiExpressionStatement addedStat = (PsiExpressionStatement)firstInitializer.getBody().getStatements()[0];
                addTryCatchFix(project, descriptor, addedStat.getExpression());

                // 3. 对于 XXE 的修复方法，如果出现 XMLConstants. 则需要import该类
                firstInitializer = aClass.getInitializers()[0];
                PsiCodeBlock toWriteBlock = firstInitializer.getBody();
                PsiElement firstStatOnBlock = toWriteBlock.getStatements()[0];
                if (blockTextes.get(0).contains("XMLConstants.")) {
                    if (firstInitializer.getBody().getStatements()[0] instanceof PsiTryStatement) {
                        PsiTryStatement tryStatement = (PsiTryStatement)firstInitializer.getBody().getStatements()[0];
                        toWriteBlock = tryStatement.getTryBlock();
                        if (toWriteBlock != null) {
                            firstStatOnBlock = toWriteBlock.getStatements()[0];
                            if (firstStatOnBlock instanceof PsiExpressionStatement &&
                                ((PsiExpressionStatement) firstStatOnBlock).getExpression() instanceof PsiMethodCallExpression) {
                                PsiExpressionList expressionList = ((PsiMethodCallExpression) ((PsiExpressionStatement) firstStatOnBlock).getExpression()).getArgumentList();
                                classImportFix(project, descriptor, (PsiReferenceExpression)expressionList.getExpressions()[0].getFirstChild());
                            }
                        }
                    }
                }

                // 4. 如果是多语句修复，写入剩余语句 (倒叙写入保证顺序)
                for (int i=blockTextes.size()-1; i>=1; i--){
                    PsiStatement disableEntityStat = factory.createStatementFromText(blockTextes.get(i), null);
                    if (toWriteBlock != null) {
                        toWriteBlock.addAfter(disableEntityStat, firstStatOnBlock);
                    }
                }
            }
        }

        private void addTryCatchFix(Project project, ProblemDescriptor descriptor, PsiElement element) {
            Document document = PsiDocumentManager.getInstance(project).getDocument(element.getContainingFile());
            if (document != null) {
                Editor[] editors = EditorFactory.getInstance().getEditors(document, project);
                QuickFixFactory.getInstance()
                        .createSurroundWithTryCatchFix(element)
                        .invoke(project, editors[0], descriptor.getPsiElement().getContainingFile());
//                Fix: do not close editor
//                if (!editors[0].isDisposed()) {
//                    EditorFactory.getInstance().releaseEditor(editors[0]);
//                }
            }
        }

        private void classImportFix(Project project, ProblemDescriptor descriptor, PsiReferenceExpression element) {
            Document document = PsiDocumentManager.getInstance(project).getDocument(element.getContainingFile());
            if (document != null) {
                Editor[] editors = EditorFactory.getInstance().getEditors(document, project);
                new ImportClassFix(element).invoke(project, editors[0], descriptor.getPsiElement().getContainingFile());
            }
        }
    }
}
