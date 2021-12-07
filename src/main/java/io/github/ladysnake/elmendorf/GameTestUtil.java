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

import com.mojang.authlib.GameProfile;
import io.github.ladysnake.elmendorf.impl.MockClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;
import org.junit.function.ThrowingRunnable;

import java.util.UUID;

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
            throw new GameTestException(e.getMessage());
        }
    }

    public static ServerPlayerEntity spawnPlayer(TestContext ctx, double x, double y, double z) {
        var mockPlayer = new ServerPlayerEntity(ctx.getWorld().getServer(), ctx.getWorld(), new GameProfile(UUID.randomUUID(), "test-mock-player"));
        var connection = new MockClientConnection(NetworkSide.CLIENTBOUND);
        mockPlayer.setPosition(ctx.getAbsolute(new Vec3d(x, y, z)));
        mockPlayer.networkHandler = new ServerPlayNetworkHandler(ctx.getWorld().getServer(), connection, mockPlayer);
        ctx.getWorld().spawnEntity(mockPlayer);
        return mockPlayer;
    }

    public static ConnectionTestConfiguration configureConnection(ServerPlayerEntity player) {
        return ((MockClientConnection) player.networkHandler.connection);
    }

    public static ConnectionChecker verifyConnection(ServerPlayerEntity player) {
        return ((MockClientConnection) player.networkHandler.connection);
    }
}
