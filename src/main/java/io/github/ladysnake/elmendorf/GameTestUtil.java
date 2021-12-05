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
