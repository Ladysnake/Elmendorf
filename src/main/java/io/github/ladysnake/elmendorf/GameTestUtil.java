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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public final class GameTestUtil {
    public static void assertTrue(boolean b, String errorMessage) {
        if (!b) throw new GameTestException(errorMessage);
    }

    public static ServerPlayerEntity spawnPlayer(TestContext ctx, double x, double y, double z) {
        ServerPlayerEntity mockPlayer = new ServerPlayerEntity(ctx.getWorld().getServer(), ctx.getWorld(), new GameProfile(UUID.randomUUID(), "test-mock-player"));
        Vec3d vec3d = ctx.getAbsolute(new Vec3d(x, y, z));
        mockPlayer.refreshPositionAndAngles(vec3d.x, vec3d.y, vec3d.z, mockPlayer.getYaw(), mockPlayer.getPitch());
        ctx.getWorld().spawnEntity(mockPlayer);
        return mockPlayer;
    }
}
