/*
 * Copyright (C) 2021 Ladysnake
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
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.ladysnake.elmendorf;

import io.github.ladysnake.elmendorf.impl.MockClientConnection;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;
import org.junit.function.ThrowingRunnable;

import java.util.function.Consumer;

public final class GameTestUtil {
    public static void assertTrue(String errorMessage, boolean b) {
        if (!b) throw new GameTestException(errorMessage);
    }

    public static void assertFalse(String errorMessage, boolean b) {
        if (b) throw new GameTestException(errorMessage);
    }

    public static void assertThrows(Class<? extends Throwable> expectedThrowable, ThrowingRunnable runnable) {
        assertThrows(null, expectedThrowable, runnable);
    }

    public static void assertThrows(@Nullable String errorMessage, Class<? extends Throwable> expectedThrowable, ThrowingRunnable runnable) {
        try {
            Assert.assertThrows(errorMessage, expectedThrowable, runnable);
        } catch (AssertionError e) {
            throw (GameTestException) new GameTestException(e.getMessage()).initCause(e.getCause());
        }
    }

    /**
     * @deprecated use {@link ElmendorfTestContext#spawnServerPlayer(double, double, double)}
     */
    @Deprecated(forRemoval = true)
    public static ServerPlayerEntity spawnPlayer(TestContext ctx, double x, double y, double z) {
        return ctx.spawnServerPlayer(x, y, z);
    }

    /**
     * @deprecated use {@link ElmendorfTestContext#configureConnection(ServerPlayerEntity, Consumer)}
     */
    @Deprecated(forRemoval = true)
    public static ConnectionTestConfiguration configureConnection(ServerPlayerEntity player) {
        return ((MockClientConnection) player.networkHandler.connection);
    }

    /**
     * @deprecated use {@link ElmendorfTestContext#verifyConnection(ServerPlayerEntity, Consumer)}
     */
    @Deprecated(forRemoval = true)
    public static CheckedConnection verifyConnection(ServerPlayerEntity player) {
        return ((MockClientConnection) player.networkHandler.connection);
    }
}
