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
package com.immomo.momosec.aspect;

import com.immomo.momosec.FeedbackService;
import com.immomo.momosec.VulnSignWhiteListService;
import com.immomo.momosec.lang.MomoBaseLocalInspectionTool;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.psi.PsiElement;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.jetbrains.annotations.NotNull;

@Aspect
public class FeedbackAspect {

    @Before(
        "(" +
            "execution(* com.immomo.momosec.fix.ShowHelpCommentQuickFix.applyFix(..)) ||" +
            "execution(* com.immomo.momosec.fix.SetBoolArgQuickFix.applyFix(..)) ||" +
            "execution(* com.immomo.momosec.lang.java.rule.*.*.*.applyFix(..)) ||" +
            "execution(* com.immomo.momosec.lang.xml.rule.*.*.*.applyFix(..))" +
        ") && args(*, descriptor)"
    )
    public void beforeApplyFix(ProblemDescriptor descriptor) {
        markupVulnFix(descriptor.getPsiElement());
    }

    @Before(
        "(" +
            "execution(* com.immomo.momosec.fix.DeleteElementQuickFix.invoke(..)) ||" +
            "execution(* com.immomo.momosec.fix.RenameClassQuickFix.invoke(..)) " +
        ") && args(*, *, *, element, *)")
    public void beforeApplyExtendsFix(PsiElement element) {
        markupVulnFix(element);
    }

    @Around("call(* com.intellij.codeInspection.ProblemsHolder.registerProblem(..)) && args(element, message, ..)")
    public Object beforeMarkupVuln(ProceedingJoinPoint jp, PsiElement element, String message) throws Throwable {
        if (message != null && message.startsWith("Momo")) {
            if (VulnSignWhiteListService.getInstance().isInWhiteList(MomoBaseLocalInspectionTool.getVulnSign(element))) {
                return null;
            }
            markupVuln(element, message);
        }
        return jp.proceed();
    }


    private void markupVuln(@NotNull PsiElement element, String message) {
        FeedbackService feedbackService = ServiceManager.getService(element.getProject(), FeedbackService.class);
        if (feedbackService != null) {
            feedbackService.markupVuln(element, message);
        }
    }

    private void markupVulnFix(@NotNull PsiElement element) {
        FeedbackService feedbackService = ServiceManager.getService(element.getProject(), FeedbackService.class);
        if (feedbackService != null) {
            feedbackService.markupVulnFix(element);
        }
    }
}
