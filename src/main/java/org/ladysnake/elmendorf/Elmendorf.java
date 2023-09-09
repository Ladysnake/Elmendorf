/*
 * Elmendorf
 * Copyright (C) 2021-2023 Ladysnake
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.ladysnake.elmendorf;

import net.fabricmc.fabric.impl.gametest.FabricGameTestModInitializer;
import net.minecraft.test.TestFunctions;

import java.lang.reflect.Field;
import java.util.Map;

public final class Elmendorf {
    /**
     * Registers a class as a test container. This method should be called during test mod initialization.
     * @param testClass a class containing test methods, which was not already registered through other means
     * @param modId the id for the mod to which the class belongs
     * @throws IllegalStateException if the class already got registered through this method or through the entrypoint
     * @deprecated use QuiltGameTest#registerTests
     */
    @Deprecated
    public static void registerTestClass(Class<?> testClass, String modId) {
        try {
            // Use reflection to avoid hard dependency on fabric-gametest-api
            Field field = FabricGameTestModInitializer.class.getDeclaredField("GAME_TEST_IDS");
            field.setAccessible(true);
            @SuppressWarnings("unchecked") Map<Class<?>, String> testOwners = (Map<Class<?>, String>) field.get(null);
            if (testOwners.containsKey(testClass)) throw new IllegalStateException(testClass + " got registered twice");
            testOwners.put(testClass, modId);
            TestFunctions.register(testClass);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
