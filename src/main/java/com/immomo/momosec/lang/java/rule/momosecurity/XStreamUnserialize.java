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

import com.immomo.momosec.fix.ShowHelpCommentQuickFix;
import com.immomo.momosec.lang.InspectionBundle;
import com.immomo.momosec.lang.MomoBaseFixElementWalkingVisitor;
import com.immomo.momosec.lang.MomoBaseLocalInspectionTool;
import com.immomo.momosec.lang.java.utils.MoExpressionUtils;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * 1011: XStream 反序列化风险
 *
 * com.thoughtworks.xstream:xstream 默认情况下会存在反序列化风险
 *
 * ref:
 * http://x-stream.github.io/changes.html
 */
public class XStreamUnserialize extends MomoBaseLocalInspectionTool {
    public static final String MESSAGE = InspectionBundle.message("xstream.unserialize.msg");
    private static final String QUICK_FIX_NAME = InspectionBundle.message("xstream.unserialize.fix");
    private static final String SETUP_DEFAULT_SECURITY = "setupDefaultSecurity";

    private final XStreamUnserializeQuickFix xStreamUnserializeQuickFix = new XStreamUnserializeQuickFix();

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitNewExpression(PsiNewExpression expression) {
                if (MoExpressionUtils.hasFullQualifiedName(expression, "com.thoughtworks.xstream.XStream")) {
                    if (expression.getParent() instanceof PsiLocalVariable) {
                        PsiLocalVariable localVariable = (PsiLocalVariable)expression.getParent();
                        SetupDefaultSecurityElementVisitor visitor = new SetupDefaultSecurityElementVisitor(localVariable);
                        if (checkVariableUseFix(localVariable, null, visitor)) {
                            return ;
                        }
                    } else if (expression.getParent() instanceof PsiAssignmentExpression) {
                        PsiAssignmentExpression assignmentExpression = (PsiAssignmentExpression)expression.getParent();
                        PsiElement resolvedElem = ((PsiReferenceExpression) assignmentExpression.getLExpression()).resolve();
                        SetupDefaultSecurityElementVisitor visitor = new SetupDefaultSecurityElementVisitor(resolvedElem);
                        if (checkVariableUseFix(assignmentExpression, resolvedElem, visitor)) {
                            return ;
                        }
                    } else if (expression.getParent() instanceof PsiField) {
                        PsiField field = (PsiField)expression.getParent();
                        SetupDefaultSecurityElementVisitor visitor = new SetupDefaultSecurityElementVisitor(field);
                        if (checkVariableUseFix(null, field, visitor)) {
                            return ;
                        }
                    }
                    holder.registerProblem(expression, MESSAGE, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, xStreamUnserializeQuickFix);
                }
            }
        };
    }

    private static class SetupDefaultSecurityElementVisitor extends MomoBaseFixElementWalkingVisitor {

        private final PsiElement refVar;

        public SetupDefaultSecurityElementVisitor(PsiElement refVar) {
            this.refVar = refVar;
        }

        @Override
        public void visitElement(PsiElement element) {
            if (element instanceof PsiMethodCallExpression) {
                PsiMethodCallExpression methodCallExpression = (PsiMethodCallExpression)element;
                if (MoExpressionUtils.hasFullQualifiedName(methodCallExpression, "com.thoughtworks.xstream.XStream", SETUP_DEFAULT_SECURITY)) {
                    PsiExpressionList args = methodCallExpression.getArgumentList();
                    if (args.getExpressions().length != 1) { return ; }
                    if (args.getExpressions()[0] instanceof PsiReferenceExpression) {
                        PsiReference refElem = args.getExpressions()[0].getReference();
                        if (refElem != null && refVar.isEquivalentTo(refElem.resolve())) {
                            this.setFix(true);
                            this.stopWalking();
                            return  ;
                        }
                    }
                }
            }
            super.visitElement(element);
        }

    }

    public static class XStreamUnserializeQuickFix implements LocalQuickFix {

        @Override
        public @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String getFamilyName() {
            return QUICK_FIX_NAME;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElement newElem = descriptor.getPsiElement();

            String varName = null;
            VulnElemType vulnElemType = null;

            PsiElement parent = newElem.getParent();
            if (parent instanceof PsiAssignmentExpression &&
                ((PsiAssignmentExpression)parent).getLExpression() instanceof PsiReferenceExpression
            ) {
                varName = ((PsiReferenceExpression)((PsiAssignmentExpression)parent).getLExpression()).getReferenceName();
                vulnElemType = VulnElemType.ASSIGNMENT_EXPRESSION;
            } else if (parent instanceof PsiLocalVariable) {
                varName = ((PsiLocalVariable)parent).getName();
                vulnElemType = VulnElemType.LOCAL_VARIABLE;
            } else if (parent instanceof PsiField) {
                varName = ((PsiField)parent).getName();
                vulnElemType = VulnElemType.CLASS_FIELD;
            }

            if (varName == null) { return ; }

            PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);

            if (VulnElemType.LOCAL_VARIABLE.equals(vulnElemType) || VulnElemType.ASSIGNMENT_EXPRESSION.equals(vulnElemType)) {
                PsiStatement setupDefaultSecurity = factory.createStatementFromText("XStream.setupDefaultSecurity("+varName+");", null);
                PsiElement dualParent = parent.getParent();
                dualParent.getParent().addAfter(setupDefaultSecurity, dualParent);
            } else {
                PsiClass aClass = (PsiClass)parent.getParent();

                // new XStream() 类成员定义时初始化
                PsiJavaFile aFile;
                if (((PsiField) parent).hasModifierProperty(PsiModifier.STATIC)) {
                    aFile = (PsiJavaFile)PsiFileFactory.getInstance(project)
                            .createFileFromText(
                                    "_Dummy_." + JavaFileType.INSTANCE.getDefaultExtension(),
                                    JavaFileType.INSTANCE,
                                    "class _Dummy_ { static { XStream.setupDefaultSecurity("+varName+"); } }"
                            );

                } else {
                    aFile = (PsiJavaFile)PsiFileFactory.getInstance(project)
                            .createFileFromText(
                                    "_Dummy_." + JavaFileType.INSTANCE.getDefaultExtension(),
                                    JavaFileType.INSTANCE,
                                    "class _Dummy_ { { XStream.setupDefaultSecurity("+varName+"); } }"
                            );
                }
                PsiClassInitializer classInitializer = aFile.getClasses()[0].getInitializers()[0];

                PsiField[] fields = aClass.getFields();
                PsiField lastField = fields[fields.length - 1];
                aClass.addAfter(classInitializer, lastField);
            }

            new ShowHelpCommentQuickFix("XStream", "// use xstream latest version, please").applyFix(project, descriptor);
        }
    }
}
