/*
 * Elmendorf
 * Copyright (C) 2021-2026 Ladysnake
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
package org.ladysnake.elmendorf.impl;

import io.netty.channel.ChannelFutureListener;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.elmendorf.ByteBufChecker;
import org.ladysnake.elmendorf.CheckedConnection;
import org.ladysnake.elmendorf.ConnectionTestConfiguration;
import org.ladysnake.elmendorf.PacketSequenceChecker;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class TestableMockClientConnection extends MockClientConnection implements CheckedConnection, ConnectionTestConfiguration {
    private final List<SentPacket> packetQueue = new ArrayList<>();
    private final GameTestHelper ctx;
    private boolean allowNoPacketMatch;
    private boolean flushEachTick;
    private int ticks;

    public TestableMockClientConnection(PacketFlow side, GameTestHelper ctx) {
        super(side);
        this.ctx = ctx;
    }

    @Override
    public void send(Packet<?> packet, @Nullable ChannelFutureListener callback, boolean flush) {
        this.packetQueue.add(new TestableMockClientConnection.SentPacket(packet, this.ticks));
        super.send(packet, callback, flush);
    }

    @Override
    public void tick() {
        super.tick();
        this.ticks++;
        if (this.flushEachTick) {
            this.packetQueue.clear();
        }
    }

    @Override
    public void toFlushPacketsEachTick(boolean flush) {
        this.flushEachTick = flush;
    }

    @Override
    public CheckedConnection allowNoPacketMatch(boolean allow) {
        this.allowNoPacketMatch = allow;
        return this;
    }

    @Override
    public PacketSequenceChecker sent(Class<? extends Packet<?>> packetType) {
        return sent(packetType::isInstance, "Expected packet of type " + packetType.getTypeName());
    }

    @Override
    public <P extends Packet<?>> PacketSequenceChecker sent(Class<P> packetType, Predicate<P> expect) {
        return sent(packet -> packetType.isInstance(packet) && expect.test(packetType.cast(packet)), "Expected packet of type " + packetType.getTypeName());
    }

    @Override
    public PacketSequenceChecker sent(CustomPacketPayload.Type<?> channelId) {
        return sent(packet -> packet instanceof ClientboundCustomPayloadPacket(CustomPacketPayload payload) && Objects.equals(payload.type(), channelId), "Expected packet for channel " + channelId);
    }

    @Override
    public <T extends CustomPacketPayload> PacketSequenceChecker sent(CustomPacketPayload.Type<T> channelId, Consumer<T> expect) {
        List<GameTestAssertException> suppressed = new ArrayList<>();
        try {
            return sent(createCheckerTest(channelId, expect, suppressed), "Expected packet for channel " + channelId);
        } catch (GameTestAssertException e) {
            suppressed.forEach(e::addSuppressed);
            throw e;
        }
    }

    @Override
    public void checkByteBuf(FriendlyByteBuf buf, Consumer<ByteBufChecker> expect) {
        expect.accept(new ByteBufChecker(buf, ctx));
    }

    @NotNull
    private static <T extends CustomPacketPayload> Predicate<Packet<?>> createCheckerTest(CustomPacketPayload.Type<T> channelId, Consumer<T> expect, List<GameTestAssertException> suppressed) {
        return packet -> {
            if (packet instanceof ClientboundCustomPayloadPacket(CustomPacketPayload p) && Objects.equals(p.type(), channelId)) {
                try {
                    // The id is right, so the type must be right
                    @SuppressWarnings("unchecked") T payload = (T) p;
                    expect.accept(payload);
                    return true;
                } catch (GameTestAssertException e) {
                    suppressed.add(e);
                    return false;
                }
            }
            return false;
        };
    }

    @Override
    public PacketSequenceChecker sent(Predicate<Packet<?>> test, String errorMessage) {
        var packets = this.packetQueue.stream().filter(p -> test.test(p.packet)).toList();
        if (!this.allowNoPacketMatch) ctx.assertFalse(packets.isEmpty(), errorMessage);
        return new PacketSequenceCheckerImpl(errorMessage, packets);
    }

    @Override
    public void sentPackets(Consumer<Queue<Packet<?>>> test) {
        test.accept(this.packetQueue.stream().map(p -> p.packet).collect(Collectors.toCollection(ArrayDeque::new)));
    }

    public class PacketSequenceCheckerImpl implements PacketSequenceChecker {
        private final String defaultErrorMessage;
        private final List<SentPacket> packets;

        public PacketSequenceCheckerImpl(String defaultErrorMessage, List<SentPacket> packets) {
            this.defaultErrorMessage = defaultErrorMessage;
            this.packets = packets;
        }

        @Override
        public PacketSequenceChecker atLeast(int times) {
            ctx.assertTrue(this.packets.size() >= times, "%s to be sent at least %d times, was %d".formatted(defaultErrorMessage, times, this.packets.size()));
            return this;
        }

        @Override
        public PacketSequenceChecker atLeast(String errorMessage, int times) {
            ctx.assertTrue(this.packets.size() >= times, errorMessage);
            return this;
        }

        @Override
        public PacketSequenceChecker exactly(int times) {
            ctx.assertTrue(this.packets.size() == times, "%s to be sent %d times, was %d".formatted(defaultErrorMessage, times, this.packets.size()));
            return this;
        }

        @Override
        public PacketSequenceChecker exactly(String errorMessage, int times) {
            ctx.assertTrue(this.packets.size() == times, errorMessage);
            return this;
        }

        @Override
        public PacketSequenceChecker thenSent(Delay delay, Class<? extends Packet<?>> packetType) {
            return thenSent(delay, packetType::isInstance, "Expected packet of type " + packetType.getTypeName());
        }

        @Override
        public <P extends Packet<?>> PacketSequenceChecker thenSent(Delay delay, Class<P> packetType, Predicate<P> expect) {
            return thenSent(delay, packet -> packetType.isInstance(packet) && expect.test(packetType.cast(packet)), "Expected packet of type " + packetType.getTypeName());
        }

        @Override
        public PacketSequenceChecker thenSent(Delay delay, CustomPacketPayload.Type<?> channelId) {
            return thenSent(delay, packet -> packet instanceof ClientboundCustomPayloadPacket(
                    CustomPacketPayload payload)
                    && Objects.equals(payload.type(), channelId), "Expected packet for channel " + channelId);
        }

        @Override
        public <T extends CustomPacketPayload> PacketSequenceChecker thenSent(Delay delay, CustomPacketPayload.Type<T> channelId, Consumer<T> expect) {
            List<GameTestAssertException> suppressed = new ArrayList<>();
            try {
                return this.thenSent(delay, createCheckerTest(channelId, expect, suppressed), "Expected packet for channel " + channelId);
            } catch (GameTestAssertException e) {
                suppressed.forEach(e::addSuppressed);
                throw e;
            }
        }

        @Override
        public PacketSequenceChecker thenSent(Delay delay, Predicate<Packet<?>> test, String errorMessage) {
            var packets = this.packets.stream().map(p -> p.next(delay, test)).filter(Objects::nonNull).toList();
            ctx.assertFalse(packets.isEmpty(), errorMessage);
            return new PacketSequenceCheckerImpl(errorMessage, packets);
        }
    }

    public class SentPacket {
        final Packet<?> packet;
        final int tick;

        public SentPacket(Packet<?> packet, int tick) {
            this.packet = packet;
            this.tick = tick;
        }

        public @Nullable SentPacket next(PacketSequenceChecker.Delay delay, Predicate<Packet<?>> test) {
            SentPacket successor;
            for (var it = packetQueue.listIterator(packetQueue.indexOf(this) + 1); it.hasNext();) {
                successor = it.next();

                if (switch (delay) {
                    case IMMEDIATELY, SAME_TICK -> successor.tick == this.tick;
                    case LATER -> true;
                } && test.test(successor.packet)) {
                    return successor;
                }

                if (switch (delay) {
                    case IMMEDIATELY -> true;
                    case SAME_TICK -> successor.tick != this.tick;
                    case LATER -> false;
                }) break;
            }

            return null;
        }
    }
}
