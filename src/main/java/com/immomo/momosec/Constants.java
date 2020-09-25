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
package com.immomo.momosec;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Constants {
    public static String PLUGIN_VERSION = null;
    public static String FEEDBACK_ENDPOINT = null;
    public static String VULN_SIGN_WHITE_LIST_ENDPOINT = null;
    public static String PLUGIN_ENV = "dev";
    public static final Integer HTTP_TIMEOUT = 2000;

    public static final String SQL_INJECTION_HELP_COMMENT = "// 请查看示例 https://gist.github.com/retanoj/5fd369524a18ab68a4fe7ac5e0d121e8";

    static {
        Properties properties = new Properties();
        InputStream in = Constants.class.getClassLoader().getResourceAsStream("properties/app.properties");
        try {
            if (in != null && !"true".equals(System.getenv("PLUGIN_BAN_CONST"))) {
                properties.load(in);
                FEEDBACK_ENDPOINT = properties.getProperty("feedback_endpoint", null);
                VULN_SIGN_WHITE_LIST_ENDPOINT = properties.getProperty("vuln_sign_white_list_endpoint", null);
                PLUGIN_VERSION = properties.getProperty("plugin_version", null);
                PLUGIN_ENV = properties.getProperty("plugin_env", "dev");
            }
        } catch (IOException ignored) {

        }
    }
}
