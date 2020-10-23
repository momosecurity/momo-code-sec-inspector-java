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
package com.immomo.momosec.lang;

import com.intellij.BundleBase;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class InspectionBundle {
    private static Reference<ResourceBundle> ourBundle;
    @NonNls
    public static final String BUNDLE = "com.immomo.momosec.bundle.InspectionBundle";

    private InspectionBundle() {

    }

    private static ResourceBundle getBundle() {
        ResourceBundle bundle = com.intellij.reference.SoftReference.dereference(ourBundle);
        if (bundle == null) {
            try {
                bundle = ResourceBundle.getBundle(BUNDLE);
            } catch (MissingResourceException e) {
                bundle = ResourceBundle.getBundle(BUNDLE, Locale.ROOT);
            }
            ourBundle = new SoftReference<>(bundle);
        }

        return bundle;
    }

    @NotNull
    public static String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, @NotNull Object... params) {
        return BundleBase.message(getBundle(), key, params);
    }
}
