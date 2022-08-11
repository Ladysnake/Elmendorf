/*
 * Elmendorf
 * Copyright (C) 2021-2022 Ladysnake
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
package io.github.ladysnake.elmendorf;

import com.google.common.base.Preconditions;
import io.github.ladysnake.elmendorf.impl.mixin.FabricGameTestModInitializerAccessor;
import net.minecraft.test.TestFunctions;

public final class Elmendorf {
    /**
     * Registers a class as a test container. This method should be called during test mod initialization.
     * @param testClass a class containing test methods, which was not already registered through other means
     * @param modId the id for the mod to which the class belongs
     * @throws IllegalStateException if the class already got registered through this method or through the entrypoint
     */
    public static void registerTestClass(Class<?> testClass, String modId) {
        Preconditions.checkState(!FabricGameTestModInitializerAccessor.getGameTestIds().containsKey(testClass));
        FabricGameTestModInitializerAccessor.getGameTestIds().put(testClass, modId);
        TestFunctions.register(testClass);
    }
}
