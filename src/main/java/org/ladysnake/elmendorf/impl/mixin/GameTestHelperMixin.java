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
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.Vec3;
import org.ladysnake.elmendorf.CheckedConnection;
import org.ladysnake.elmendorf.ConnectionTestConfiguration;
import org.ladysnake.elmendorf.ElmendorfTestContext;
import org.ladysnake.elmendorf.impl.TestableMockClientConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.UUID;
import java.util.function.Consumer;

@Mixin(GameTestHelper.class)
public abstract class GameTestHelperMixin implements ElmendorfTestContext {
    @Shadow public abstract ServerLevel getLevel();

    @Shadow public abstract Vec3 absoluteVec(Vec3 pos);

    @Shadow public abstract GameTestAssertException assertionException(Component message);

    @Override
    public ServerPlayer spawnServerPlayer(double x, double y, double z) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), "test-mock-player");
        var mockPlayer = new ServerPlayer(
                this.getLevel().getServer(),
                this.getLevel(),
                profile,
                ClientInformation.createDefault()
        );
        var connection = new TestableMockClientConnection(PacketFlow.SERVERBOUND, (GameTestHelper) (Object) this);
        mockPlayer.setPos(this.absoluteVec(new Vec3(x, y, z)));
        mockPlayer.connection = new ServerGamePacketListenerImpl(this.getLevel().getServer(), connection, mockPlayer, CommonListenerCookie.createInitial(profile, false));
        this.getLevel().addFreshEntity(mockPlayer);
        return mockPlayer;
    }

    @Override
    public void configureConnection(ServerPlayer player, Consumer<ConnectionTestConfiguration> configurator) {
        configurator.accept(((TestableMockClientConnection) ((ServerPlayNetworkHandlerAccessor) player.connection).elmendorf$getConnection()));
    }

    @Override
    public void verifyConnection(ServerPlayer player, Consumer<CheckedConnection> verifier) {
        verifier.accept(((TestableMockClientConnection) ((ServerPlayNetworkHandlerAccessor) player.connection).elmendorf$getConnection()));
    }

    @Override
    public GameTestAssertException assertionException(String errorMessage) {
        return assertionException(Component.literal(errorMessage));
    }
}
