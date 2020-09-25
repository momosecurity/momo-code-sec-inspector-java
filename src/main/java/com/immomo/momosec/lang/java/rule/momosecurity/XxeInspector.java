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

import com.immomo.momosec.lang.MomoBaseFixElementWalkingVisitor;
import com.immomo.momosec.lang.MomoBaseLocalInspectionTool;
import com.immomo.momosec.lang.java.utils.MoExpressionUtils;
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

/**
 * 1010: XXE漏洞
 *
 * 检查如下几种XXE工厂:
 * (1) DocumentBuilderFactory
 * (2) SAXParserFactory
 * (3) SAXTransformerFactory
 *
 * ref:
 * (1) https://owasp.org/www-community/vulnerabilities/XML_External_Entity_(XXE)_Processing
 */
public class XxeInspector extends MomoBaseLocalInspectionTool {
    public static final String MESSAGE = "MomoSec: 疑似存在XXE漏洞";
    private static final String QUICK_FIX_NAME = "!Fix: 禁用外部实体";

    public enum XmlFactory {
        DOCUMENT_BUILDER,
        SAX_PARSER_FACTORY,
        SAX_TRANSFORMER_FACTORY
    }

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                if (MoExpressionUtils.hasFullQualifiedName(expression, "javax.xml.parsers.DocumentBuilderFactory", "newInstance")) {
                    if (expression.getParent() instanceof PsiAssignmentExpression) {
                        assignmentExpressionCheck(holder, expression, "setFeature", XmlFactory.DOCUMENT_BUILDER);
                    } else if (expression.getParent() instanceof PsiLocalVariable) {
                        localVariableCheck(holder, expression, "setFeature", XmlFactory.DOCUMENT_BUILDER);
                    } else if (expression.getParent() instanceof PsiField) {
                        classFieldCheck(holder, expression, "setFeature", XmlFactory.DOCUMENT_BUILDER);
                    }
                } else if (MoExpressionUtils.hasFullQualifiedName(expression, "javax.xml.parsers.SAXParserFactory", "newInstance")) {
                    if (expression.getParent() instanceof PsiAssignmentExpression) {
                        assignmentExpressionCheck(holder, expression, "setFeature", XmlFactory.SAX_PARSER_FACTORY);
                    } else if (expression.getParent() instanceof PsiLocalVariable) {
                        localVariableCheck(holder, expression, "setFeature", XmlFactory.SAX_PARSER_FACTORY);
                    } else if (expression.getParent() instanceof PsiField) {
                        classFieldCheck(holder, expression, "setFeature", XmlFactory.SAX_PARSER_FACTORY);
                    }
                } else if (MoExpressionUtils.hasFullQualifiedName(expression, "javax.xml.transform.sax.SAXTransformerFactory", "newInstance")) {
                    if (expression.getParent() instanceof PsiAssignmentExpression ||
                            (expression.getParent() instanceof PsiTypeCastExpression &&
                             expression.getParent().getParent() instanceof PsiAssignmentExpression)
                    ) {
                        assignmentExpressionCheck(holder, expression, "setAttribute", XmlFactory.SAX_TRANSFORMER_FACTORY);
                    } else if (expression.getParent() instanceof PsiLocalVariable ||
                            (expression.getParent() instanceof PsiTypeCastExpression &&
                             expression.getParent().getParent() instanceof PsiLocalVariable)
                    ) {
                        localVariableCheck(holder, expression, "setAttribute", XmlFactory.SAX_TRANSFORMER_FACTORY);
                    } else if (expression.getParent() instanceof PsiField ||
                            (expression.getParent() instanceof PsiTypeCastExpression &&
                             expression.getParent().getProject() instanceof PsiField)
                    ) {
                        classFieldCheck(holder, expression, "setAttribute", XmlFactory.SAX_TRANSFORMER_FACTORY);
                    }
                }
            }
        };
    }

    private void assignmentExpressionCheck(@NotNull ProblemsHolder holder, PsiMethodCallExpression expression, String shouldUsedMethodName, XmlFactory xmlFactory) {
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

    private void localVariableCheck(@NotNull ProblemsHolder holder, PsiMethodCallExpression expression, String shouldUsedMethodName, XmlFactory xmlFactory) {
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

    private void classFieldCheck(@NotNull ProblemsHolder holder, PsiMethodCallExpression expression, String shouldUsedMethodName, XmlFactory xmlFactory) {
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

        public DisableEntityElementVisitor(String shouldUsedMethodName, XmlFactory xmlFactory, PsiElement refVar) {
            this.shouldUsedMethodName = shouldUsedMethodName;
            this.xmlFactory = xmlFactory;
            this.refVar = refVar;
        }

        @Override
        public void visitElement(PsiElement element) {
            if (element instanceof PsiMethodCallExpression) {
                PsiMethodCallExpression methodCallExpression = (PsiMethodCallExpression)element;
                if (shouldUsedMethodName.equals(MethodCallUtils.getMethodName(methodCallExpression))) {
                    PsiExpressionList args = methodCallExpression.getArgumentList();
                    if (!(args.getExpressions().length == 2)) { return ; }
                    // 根据 xmlFactory 类型判断参数值
                    if (xmlFactory.equals(XmlFactory.DOCUMENT_BUILDER) || xmlFactory.equals(XmlFactory.SAX_PARSER_FACTORY)) {
                        if (!(ExpressionUtils.isLiteral(args.getExpressions()[0], "http://apache.org/xml/features/disallow-doctype-decl") &&
                              ExpressionUtils.isLiteral(args.getExpressions()[1], Boolean.TRUE)
                        )) { return ; }
                    } else if (xmlFactory.equals(XmlFactory.SAX_TRANSFORMER_FACTORY)) {
                        if (!(args.getExpressions()[0] instanceof PsiReferenceExpression)) { return ; }
                        if (!(args.getExpressions()[0].textMatches("XMLConstants.ACCESS_EXTERNAL_DTD") &&
                              ExpressionUtils.isEmptyStringLiteral(args.getExpressions()[1])
                        )) { return ; }
                    } else { return ; }

                    PsiExpression refQualifier = methodCallExpression.getMethodExpression().getQualifierExpression();
                    if (refQualifier != null && refQualifier.getReference() != null) {
                        PsiElement resolvedElem = refQualifier.getReference().resolve();
                        if (refVar.isEquivalentTo(resolvedElem)) {
                            this.setFix(true);
                            this.stopWalking();
                            return ;
                        }
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
            String blockContent = null;
            if (xmlFactory.equals(XmlFactory.DOCUMENT_BUILDER) || xmlFactory.equals(XmlFactory.SAX_PARSER_FACTORY)) {
                blockContent = varName + ".setFeature(\"http://apache.org/xml/features/disallow-doctype-decl\", true);";
            } else if (xmlFactory.equals(XmlFactory.SAX_TRANSFORMER_FACTORY)) {
                blockContent = varName+".setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, \"\");";
            }
            if (blockContent == null) { return ; }

            if (VulnElemType.LOCAL_VARIABLE.equals(vulnElemType) ||
                VulnElemType.ASSIGNMENT_EXPRESSION.equals(vulnElemType)
            ) {
                PsiStatement disableEntityStat = factory.createStatementFromText(blockContent, null);

                // parent is expression and dualParent is Statement
                PsiElement dualParent = parent.getParent();
                dualParent.getParent().addAfter(disableEntityStat, dualParent);
                if (MoExpressionUtils.getParentOfMethod(parent) != null) {
                    QuickFixFactory.getInstance()
                            .createAddExceptionToThrowsFix(((PsiExpressionStatement) dualParent.getNextSibling()).getExpression())
                            .invoke(project, null, descriptor.getPsiElement().getContainingFile());
                } else {
                    // scope in initializer block
                    addTryCatchFix(project, descriptor, dualParent.getNextSibling());
                }
            } else {  // 类成员定义时就初始化的情况
                PsiField field = (PsiField)parent;
                PsiClass aClass = (PsiClass)field.getParent();
                PsiField[] fields = aClass.getFields();
                PsiField lastField = fields[fields.length - 1];

                PsiJavaFile aFile;
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

                PsiClassInitializer firstInitializer = aClass.getInitializers()[0];
                PsiExpressionStatement addedStat = (PsiExpressionStatement)firstInitializer.getBody().getStatements()[0];
                addTryCatchFix(project, descriptor, addedStat.getExpression());
            }
        }

        private void addTryCatchFix(Project project, ProblemDescriptor descriptor, PsiElement element) {
            Document document = descriptor.getPsiElement().getContainingFile().getViewProvider().getDocument();
            if (document != null) {
                Editor[] editors = EditorFactory.getInstance().getEditors(document);
                Editor oneEditor;
                if (editors.length > 0) {
                    oneEditor = editors[0];
                } else {
                    oneEditor = EditorFactory.getInstance().createEditor(document);
                }
                QuickFixFactory.getInstance()
                        .createSurroundWithTryCatchFix(element)
                        .invoke(project, oneEditor, descriptor.getPsiElement().getContainingFile());
                if (!oneEditor.isDisposed()) {
                    EditorFactory.getInstance().releaseEditor(oneEditor);
                }
            }
        }
    }
}
