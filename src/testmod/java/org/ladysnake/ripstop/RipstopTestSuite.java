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
package org.ladysnake.ripstop;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.networking.v1.FriendlyByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ClientboundClearTitlesPacket;
import org.ladysnake.elmendorf.ElmendorfTestContext;
import org.ladysnake.elmendorf.PacketSequenceChecker;

import static org.ladysnake.elmendorf.ByteBufChecker.any;

public class RipstopTestSuite {
    @GameTest
    public void testPacketChecks(GameTestHelper helper) {
        ElmendorfTestContext ctx = ((ElmendorfTestContext) helper);
        var player = ctx.spawnServerPlayer(5, 5, 5);
        player.connection.send(new ClientboundClearTitlesPacket(true));
        player.connection.send(new ClientboundClearTitlesPacket(false));
        ctx.verifyConnection(player, conn -> conn.sent(ClientboundClearTitlesPacket.class).atLeast(2));
        ctx.assertThrows(GameTestAssertException.class,
                () -> ctx.verifyConnection(player, conn -> conn.sent(CustomPacketPayload.createType("ribbit"))));
        var buf = FriendlyByteBufs.create();
        buf.writeBlockPos(BlockPos.ZERO);
        buf.writeUtf("test");
        player.connection.send(ServerPlayNetworking.createClientboundPacket(new TestPayload(buf)));
        ctx.verifyConnection(player, conn -> conn.sent(TestPayload.ID, p -> conn.checkByteBuf(p.rawData(), c -> c.checkBlockPos(any()).checkUtf("test").noMoreData())));
        ctx.assertThrows(GameTestAssertException.class,
                () -> ctx.verifyConnection(player, conn -> conn.sent(TestPayload.ID, p -> conn.checkByteBuf(p.rawData(), c -> c.checkBoolean(false).noMoreData()))));
        ((GameTestHelper) ctx).succeed();
    }

    @GameTest
    public void testPacketSequenceChecks(GameTestHelper helper) {
        ElmendorfTestContext ctx = ((ElmendorfTestContext) helper);
        var player = ctx.spawnServerPlayer(5, 5, 5);
        player.connection.send(new ClientboundBlockDestructionPacket(1, BlockPos.ZERO, 3));
        player.connection.send(new ClientboundBlockDestructionPacket(1, BlockPos.ZERO, 4));
        player.connection.send(new ClientboundBlockDestructionPacket(1, BlockPos.ZERO, 5));
        var buf1 = FriendlyByteBufs.create();
        buf1.writeBoolean(true);
        player.connection.send(ServerPlayNetworking.createClientboundPacket(new TestPayload(buf1)));
        ctx.verifyConnection(player, conn ->
                conn.sent(ClientboundBlockDestructionPacket.class, packet -> packet.getId() == 1)
                // 3 matching packets
                .thenSent(PacketSequenceChecker.Delay.IMMEDIATELY, ClientboundBlockDestructionPacket.class)
                // 2 matching packets: the last BlockBreaking packet is logically not followed by another one
                .thenSent(PacketSequenceChecker.Delay.SAME_TICK, TestPayload.ID, p -> conn.checkByteBuf(p.rawData(), c -> c.checkBoolean(true).noMoreData()))
                // still 2 matching packets: all BlockBreaking packets are followed by the custom packet in the same tick
                .exactly(2));
        ((GameTestHelper) ctx).succeed();
    }
}
