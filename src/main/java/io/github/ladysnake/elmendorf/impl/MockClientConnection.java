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
package io.github.ladysnake.elmendorf.impl;

import io.github.ladysnake.elmendorf.ConnectionChecker;
import io.github.ladysnake.elmendorf.ConnectionTestConfiguration;
import io.github.ladysnake.elmendorf.GameTestUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class MockClientConnection extends ClientConnection implements ConnectionChecker, ConnectionTestConfiguration {
    private final Queue<Packet<?>> packetQueue = new ArrayDeque<>();
    private boolean flushEachTick;

    public MockClientConnection(NetworkSide side) {
        super(side);
    }

    @Override
    public void toFlushPacketsEachTick() {
        this.flushEachTick = true;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.flushEachTick) {
            this.packetQueue.clear();
        }
    }

    @Override
    public PacketChecker sent(Class<? extends Packet<?>> packetType) {
        return sent(packetType::isInstance, "Expected packet of type " + packetType.getTypeName());
    }

    @Override
    public PacketChecker sent(Identifier channelId) {
        return sent(packet -> packet instanceof CustomPayloadS2CPacket p && Objects.equals(p.getChannel(), channelId), "Expected packet for channel " + channelId);
    }

    @Override
    public PacketChecker sent(Predicate<Packet<?>> test, String errorMessage) {
        List<Packet<?>> packets = this.packetQueue.stream().filter(test).toList();
        GameTestUtil.assertFalse(errorMessage, packets.isEmpty());
        return new PacketChecker(packets, errorMessage);
    }

    @Override
    public void sentPackets(Consumer<Queue<Packet<?>>> test) {
        test.accept(this.packetQueue);
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public void send(Packet<?> packet, @Nullable GenericFutureListener<? extends Future<? super Void>> callback) {
        this.packetQueue.add(packet);
    }

    public Queue<Packet<?>> getPacketQueue() {
        return packetQueue;
    }

}
