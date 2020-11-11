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
import com.immomo.momosec.lang.java.utils.MoExpressionUtils;
import com.immomo.momosec.utils.SQLi;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.util.PsiTreeUtil;
import com.siyeh.ig.psiutils.MethodCallUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public abstract class BaseSQLi extends MomoBaseLocalInspectionTool {
    private static final Pattern sqlPattern =
            Pattern.compile("^\\s*(select|delete|update|insert)\\s+.*?(from|into|set)\\s+.*?where.*", Pattern.CASE_INSENSITIVE);

    /**
     * 判断指定字符串是否为SQL语句
     * @param str String
     * @return boolean
     */
    protected boolean isSql(String str) {
        return sqlPattern.matcher(str).find();
    }

    /**
     * 按 needle 拆分字符串后，判断拆分数组是否有拼接SQL注入风险
     * @param content String
     * @param needle String
     * @return boolean
     */
    protected boolean hasEvalAdditive(String content, String needle) {
        List<String> list = new ArrayList<>(Arrays.asList(content.split(needle)));
        if (content.endsWith(list.get(list.size() - 1))) {
            list.remove(list.size() - 1);
        }
        return hasEvalAdditive(list);
    }

    /**
     * 按 needle 拆分字符串后，判断拆分数组是否有拼接SQL注入风险
     * @param content String
     * @param pattern Pattern
     * @return boolean
     */
    protected boolean hasEvalAdditive(String content, Pattern pattern) {
        List<String> list = new ArrayList<>(Arrays.asList(pattern.split(content)));
        if (content.endsWith(list.get(list.size() - 1))) {
            list.remove(list.size() - 1);
        }
        return hasEvalAdditive(list);
    }

    /**
     * 判断数组是否有拼接SQL注入风险
     * @param fragments List<String>
     * @return boolean
     */
    protected boolean hasEvalAdditive(List<String> fragments) {
        return SQLi.hasVulOnAdditiveFragments(fragments);
    }

    protected boolean ignoreMethodName(PsiExpression expression) {
        PsiMethodCallExpression methodCallExpression = PsiTreeUtil.getParentOfType(expression, PsiMethodCallExpression.class);
        if (methodCallExpression != null &&
            MoExpressionUtils.hasFullQualifiedName(methodCallExpression, "java.lang.String", "format")) {
            methodCallExpression = PsiTreeUtil.getParentOfType(methodCallExpression, PsiMethodCallExpression.class);
        }
        if (methodCallExpression == null) {
            return false;
        }
        String methodName = MethodCallUtils.getMethodName(methodCallExpression);
        if (methodName != null) { // 排除日志打印语句
            String lowerMethodName = methodName.toLowerCase();
            return lowerMethodName.contains("log") ||
                    lowerMethodName.contains("trace") ||
                    lowerMethodName.contains("debug") ||
                    lowerMethodName.contains("info") ||
                    lowerMethodName.contains("alarm") ||
                    lowerMethodName.contains("warn") ||
                    lowerMethodName.contains("error") ||
                    lowerMethodName.contains("fatal") ||
                    lowerMethodName.contains("ok") ||
                    lowerMethodName.contains("succ") ||
                    lowerMethodName.contains("fail") ||
                    lowerMethodName.contains("print");
        }
        return false;
    }
}
