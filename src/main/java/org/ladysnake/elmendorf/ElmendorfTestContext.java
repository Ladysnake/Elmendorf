/*
 * Elmendorf
 * Copyright (C) 2021-2024 Ladysnake
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

import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface ElmendorfTestContext {
    default ServerPlayer spawnServerPlayer(double x, double y, double z) {
        throw new UnsupportedOperationException();
    }

    default void configureConnection(ServerPlayer player, Consumer<ConnectionTestConfiguration> configurator) {

    }

    default void verifyConnection(ServerPlayer player, Consumer<CheckedConnection> verifier) {

    }

    GameTestAssertException assertionException(String errorMessage);

    default void assertTrue(String errorMessage, boolean b) {
        if (!b) throw this.assertionException(errorMessage);
    }

    default void assertFalse(String errorMessage, boolean b) {
        if (b) throw this.assertionException(errorMessage);
    }

    default void assertThrows(Class<? extends Throwable> expectedThrowable, ThrowingRunnable runnable) {
        assertThrows(null, expectedThrowable, runnable);
    }

    default void assertThrows(@Nullable String errorMessage, Class<? extends Throwable> expectedThrowable, ThrowingRunnable runnable) {
        try {
            runnable.run();
        } catch (Throwable t) {
            if (expectedThrowable.isInstance(t)) {
                return;
            } else {
                GameTestAssertException err = assertionException((errorMessage == null ? "" : (errorMessage + " ==> ")) +
                        String.format("Unexpected exception type thrown (expected %s but was %s)", expectedThrowable.getName(), t.getClass().getName()));
                err.initCause(t);
                throw err;
            }
        }
        throw assertionException((errorMessage == null ? "" : (errorMessage + " ==> ")) +
                String.format("Expected %s to be thrown, but nothing was thrown.", expectedThrowable.getName()));
    }
}
