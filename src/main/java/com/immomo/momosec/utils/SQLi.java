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
package com.immomo.momosec.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SQLi {

    public static final Pattern whereInEndPattern          = Pattern.compile("(where|and|or)\\s+\\S+?\\s+in\\s*\\(?$", Pattern.CASE_INSENSITIVE);
    public static final Pattern likeEndPatterh             = Pattern.compile("\\S+?\\s+like\\s+('|\")%?$", Pattern.CASE_INSENSITIVE);

    public static final Pattern placeholderPattern         = Pattern.compile("%(\\d\\$\\d{0,5})?s", Pattern.CASE_INSENSITIVE);
    public static final Pattern dollarVarPattern           = Pattern.compile("\\$\\{(\\S+?)\\}");


    /**
     * 判断SQL拼接点的字符串集合是否有SQL注入风险
     * @param SQLJoinStrList List<String>
     * @return boolean
     */
    public static boolean hasVulOnSQLJoinStrList(@NotNull List<List<String>> SQLJoinStrList) {
        for(List<String> sqlJoinStr : SQLJoinStrList) {
            if (sqlJoinStr.size() < 2 || sqlJoinStr.size() > 3) {
                continue;
            }
            if (sqlJoinStr.size() == 2 && hasVulOnSQLJoinStr(sqlJoinStr.get(0), sqlJoinStr.get(1), null)) {
                return true;
            }
            if (sqlJoinStr.size() == 3 && hasVulOnSQLJoinStr(sqlJoinStr.get(0), sqlJoinStr.get(1), sqlJoinStr.get(2))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断SQL拼接点的字符串是否有SQL注入风险
     * @param prefix String
     * @param var String
     * @param suffix String
     * @return boolean
     */
    public static boolean hasVulOnSQLJoinStr(@NotNull String prefix, @Nullable String var, @Nullable String suffix) {
        List<String> fragments = Arrays.stream(prefix.split("[\\s|(]+"))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .collect(Collectors.toList());
        int seg_size = fragments.size();
        if (seg_size == 0 ||
            fragments.get(seg_size - 1).trim().endsWith("=") ||
            fragments.get(seg_size - 1).trim().endsWith(">=") ||
            fragments.get(seg_size - 1).trim().endsWith("<=")
        ) {
            return true;
        }

        for(int i = seg_size - 1; i >= 0; i--) {
            String frag = fragments.get(i);
            if (frag.equals("limit") ||
                frag.equals("by") ||
                frag.equals("having")
            ) {
                continue;
            }

            if (frag.equals("where") || frag.equals("set")) {
                if (suffix != null && (
                    suffix.trim().startsWith("=") ||
                    suffix.trim().startsWith(">") ||
                    suffix.trim().startsWith("<")
                )) {
                    return false;
                }
                return true;
            }

            if (frag.equals("values")) {
                return true;
            }

            if (frag.equals("from") ||
                frag.equals("into") ||
                frag.equals("join") ||
                frag.equals("select") ||
                frag.equals("update")
            ) {
                return false;
            }
        }
        return true;
    }
}
