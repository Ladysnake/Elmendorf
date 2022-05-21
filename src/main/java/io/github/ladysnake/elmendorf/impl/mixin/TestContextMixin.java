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
package io.github.ladysnake.elmendorf.impl.mixin;

import com.mojang.authlib.GameProfile;
import io.github.ladysnake.elmendorf.CheckedConnection;
import io.github.ladysnake.elmendorf.ConnectionTestConfiguration;
import io.github.ladysnake.elmendorf.ElmendorfTestContext;
import io.github.ladysnake.elmendorf.impl.MockClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.Vec3d;
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
        var mockPlayer = new ServerPlayerEntity(this.getWorld().getServer(), this.getWorld(), new GameProfile(UUID.randomUUID(), "test-mock-player"), null);
        var connection = new MockClientConnection(NetworkSide.CLIENTBOUND);
        mockPlayer.setPosition(this.getAbsolute(new Vec3d(x, y, z)));
        mockPlayer.networkHandler = new ServerPlayNetworkHandler(this.getWorld().getServer(), connection, mockPlayer);
        this.getWorld().spawnEntity(mockPlayer);
        return mockPlayer;
    }

    @Override
    public void configureConnection(ServerPlayerEntity player, Consumer<ConnectionTestConfiguration> configurator) {
        configurator.accept(((MockClientConnection) player.networkHandler.connection));
    }

    @Override
    public void verifyConnection(ServerPlayerEntity player, Consumer<CheckedConnection> verifier) {
        verifier.accept(((MockClientConnection) player.networkHandler.connection));
    }
}
