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
