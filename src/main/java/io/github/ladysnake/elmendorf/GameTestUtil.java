/*
 * Elmendorf
 * Copyright (C) 2021-2022 Ladysnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; If not, see <https://www.gnu.org/licenses>.
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
