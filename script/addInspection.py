import os
import re

LANGUAGE = ['java', 'xml']

PROJECT_ROOT = os.path.dirname(os.path.split(os.path.realpath(__file__))[0])
SRC_PATH = os.path.join(PROJECT_ROOT, 'src/main/java')
RESOURCES_PATH = os.path.join(PROJECT_ROOT, 'src/main/resources')
TEST_SRC_PATH = os.path.join(PROJECT_ROOT, 'src/test/java')
TEST_DATA_PATH = os.path.join(PROJECT_ROOT, 'src/test/testData')


def main():
    print("choose language: " + str(LANGUAGE))
    choose_lang = input("choose>")
    if choose_lang not in LANGUAGE:
        print("wrong language.")
        exit(-1)

    name = input("Inspection Name>")
    if not re.fullmatch("[a-zA-Z0-9]+", name):
        print("Inspection name only allow [a-zA-Z0-9]")
        exit(-1)

    print("create Source File: ")
    src_file = os.path.join(
        SRC_PATH,
        'com/immomo/momosec/lang',
        choose_lang,
        'rule/momosecurity',
        name + '.java',
    )
    print(src_file[len(PROJECT_ROOT)+1:])
    src_file_content = """/*
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
package com.immomo.momosec.lang.{lang}.rule.momosecurity;

import com.immomo.momosec.lang.InspectionBundle;
import com.immomo.momosec.lang.MomoBaseLocalInspectionTool;
import com.immomo.momosec.lang.java.utils.MoExpressionUtils;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

/**
 * Todo. Inspection Short Description.
 */
public class {InspectionName} extends MomoBaseLocalInspectionTool {{
    public static final String MESSAGE = InspectionBundle.message(\"\"); // Todo. feed Inspection MESSAGE

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {{
        return new JavaElementVisitor() {{
            
        }};
    }}
}}    
""".format(lang=choose_lang, InspectionName=name)
    with open(src_file, 'wb') as outf:
        outf.write(src_file_content.encode("utf-8"))

    print("create Inspection Description HTML: ")
    src_resource_file = os.path.join(
        RESOURCES_PATH,
        'inspectionDescriptions',
        name + '.html'
    )
    print(src_resource_file[len(PROJECT_ROOT)+1:])
    src_resource_file_content = """<html>
<body>
<b>Momo {number}:</b> {Title} <br>
<br>
<p>{Content}</p>
</body>
</html>
"""
    with open(src_resource_file, 'wb') as outf:
        outf.write(src_resource_file_content.encode("utf-8"))

    print("create Test Source File: ")
    test_src_file = os.path.join(
        TEST_SRC_PATH,
        'com/immomo/momosec/lang',
        choose_lang,
        'rule/momosecurity',
        name + 'Test.java'
    )
    print(test_src_file[len(PROJECT_ROOT)+1:])
    test_src_file_content = """/*
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
package com.immomo.momosec.lang.{lang}.rule.momosecurity;

import com.immomo.momosec.lang.{lang}.Momo{capitalize_lang}CodeInsightFixtureTestCase;

public class {name}Test extends Momo{capitalize_lang}CodeInsightFixtureTestCase {{
    String prefix = "rule/momosecurity/{name}/";

    public void testIfFindAllVulns() {{
        doTest(new {name}(), prefix + "Vuln.{lang}");
    }}
}}
""".format(lang=choose_lang, capitalize_lang=choose_lang.capitalize(), name=name)
    with open(test_src_file, 'wb') as outf:
        outf.write(test_src_file_content.encode("utf-8"))

    print("create Test Data Dir: ")
    test_data_dir = os.path.join(
        TEST_DATA_PATH,
        'lang',
        choose_lang,
        'rule/momosecurity',
        name
    )
    print(test_data_dir[len(PROJECT_ROOT)+1:])
    if not os.path.isdir(test_data_dir):
        os.mkdir(test_data_dir)

    print("create Test Data File:")
    test_data_file = os.path.join(test_data_dir, 'Vuln.'+choose_lang)
    print(test_data_file[len(PROJECT_ROOT)+1:])
    test_data_file_content = """
public class Vuln {
    public void foo() {
    
    }
}
""" if choose_lang == 'java' else ""
    with open(test_data_file, 'wb') as outf:
        outf.write(test_data_file_content.encode("utf-8"))

    print("\n[!]Remember add {name} to plugin.xml\n".format(name=name))


if __name__ == '__main__':
    main()
