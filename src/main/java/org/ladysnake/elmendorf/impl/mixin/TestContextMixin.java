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
package org.ladysnake.elmendorf.impl.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.Vec3d;
import org.ladysnake.elmendorf.CheckedConnection;
import org.ladysnake.elmendorf.ConnectionTestConfiguration;
import org.ladysnake.elmendorf.ElmendorfTestContext;
import org.ladysnake.elmendorf.impl.MockClientConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.UUID;
import java.util.function.Consumer;

@Mixin(TestContext.class)
public abstract class TestContextMixin implements ElmendorfTestContext {
    @Shadow public abstract ServerWorld getWorld();

    @Shadow public abstract Vec3d getAbsolute(Vec3d pos);

    @Override
    public ServerPlayerEntity spawnServerPlayer(double x, double y, double z) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), "test-mock-player");
        var mockPlayer = new ServerPlayerEntity(
                this.getWorld().getServer(),
                this.getWorld(),
                profile,
                SyncedClientOptions.createDefault()
        );
        var connection = new MockClientConnection(NetworkSide.SERVERBOUND);
        mockPlayer.setPosition(this.getAbsolute(new Vec3d(x, y, z)));
        mockPlayer.networkHandler = new ServerPlayNetworkHandler(this.getWorld().getServer(), connection, mockPlayer, ConnectedClientData.createDefault(profile, false));
        this.getWorld().spawnEntity(mockPlayer);
        return mockPlayer;
    }

    @Override
    public void configureConnection(ServerPlayerEntity player, Consumer<ConnectionTestConfiguration> configurator) {
        configurator.accept(((MockClientConnection) ((ServerPlayNetworkHandlerAccessor) player.networkHandler).elmendorf$getConnection()));
    }

    @Override
    public void verifyConnection(ServerPlayerEntity player, Consumer<CheckedConnection> verifier) {
        verifier.accept(((MockClientConnection) ((ServerPlayNetworkHandlerAccessor) player.networkHandler).elmendorf$getConnection()));
    }
}
