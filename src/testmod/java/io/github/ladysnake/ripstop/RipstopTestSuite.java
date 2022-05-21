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
package io.github.ladysnake.ripstop;

import io.github.ladysnake.elmendorf.ByteBufChecker;
import io.github.ladysnake.elmendorf.GameTestUtil;
import io.github.ladysnake.elmendorf.PacketSequenceChecker;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EntityType;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.network.packet.s2c.play.ClearTitleS2CPacket;
import net.minecraft.test.GameTest;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import static io.github.ladysnake.elmendorf.ByteBufChecker.any;

public class RipstopTestSuite implements FabricGameTest {
    @GameTest(structureName = EMPTY_STRUCTURE)
    public void testPacketChecks(TestContext ctx) {
        var player = ctx.spawnServerPlayer(5, 5, 5);
        player.networkHandler.sendPacket(new ClearTitleS2CPacket(true));
        player.networkHandler.sendPacket(new ClearTitleS2CPacket(false));
        ctx.verifyConnection(player, conn -> conn.sent(ClearTitleS2CPacket.class).atLeast(2));
        GameTestUtil.assertThrows(GameTestException.class,
                () -> ctx.verifyConnection(player, conn -> conn.sent(new Identifier("ribbit"))));
        var buf = PacketByteBufs.create();
        buf.writeBlockPos(BlockPos.ORIGIN);
        buf.writeString("test");
        player.networkHandler.sendPacket(ServerPlayNetworking.createS2CPacket(new Identifier("a"), buf));
        ctx.verifyConnection(player, conn -> conn.sent(new Identifier("a"), c -> c.checkBlockPos(any()).checkString("test").noMoreData()));
        GameTestUtil.assertThrows(GameTestException.class,
                () -> ctx.verifyConnection(player, conn -> conn.sent(new Identifier("a"), c -> c.checkBoolean(false).noMoreData())));
        ctx.complete();
    }

    @GameTest(structureName = EMPTY_STRUCTURE)
    public void testPacketSequenceChecks(TestContext ctx) {
        var player = ctx.spawnServerPlayer(5, 5, 5);
        player.networkHandler.sendPacket(new BlockBreakingProgressS2CPacket(1, BlockPos.ORIGIN, 3));
        player.networkHandler.sendPacket(new BlockBreakingProgressS2CPacket(1, BlockPos.ORIGIN, 4));
        player.networkHandler.sendPacket(new BlockBreakingProgressS2CPacket(1, BlockPos.ORIGIN, 5));
        var buf1 = PacketByteBufs.create();
        buf1.writeBoolean(true);
        player.networkHandler.sendPacket(ServerPlayNetworking.createS2CPacket(new Identifier("a"), buf1));
        ctx.verifyConnection(player, conn ->
                conn.sent(BlockBreakingProgressS2CPacket.class, packet -> packet.getEntityId() == 1)
                // 3 matching packets
                .thenSent(PacketSequenceChecker.Delay.IMMEDIATELY, BlockBreakingProgressS2CPacket.class)
                // 2 matching packets: the last BlockBreaking packet is logically not followed by another one
                .thenSent(PacketSequenceChecker.Delay.SAME_TICK, new Identifier("a"), c -> c.checkBoolean(true).noMoreData())
                // still 2 matching packets: all BlockBreaking packets are followed by the custom packet in the same tick
                .exactly(2));
        ctx.complete();
    }

    @GameTest(structureName = EMPTY_STRUCTURE)
    public void testComponentSyncChecks(TestContext ctx) {
        var player = ctx.spawnServerPlayer(5, 0, 5);
        var key = RipstopComponents.TEST;
        var entity = ctx.spawnEntity(EntityType.AXOLOTL, 1, 0, 1);
        key.sync(entity);
        GameTestUtil.assertThrows("Expected " + player + " to provide component ripstop:test-component", GameTestException.class, () -> ctx.verifyConnection(player, conn -> conn.sentEntityComponentUpdate(player, key, ByteBufChecker::noMoreData)));
        ctx.verifyConnection(player, conn -> conn.sentEntityComponentUpdate(entity, key, ByteBufChecker::noMoreData));
        ctx.complete();
    }
}
