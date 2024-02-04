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
package org.ladysnake.elmendorf.impl;

import net.minecraft.entity.Entity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.test.GameTestException;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.internal.entity.CardinalComponentsEntity;
import org.ladysnake.elmendorf.ByteBufChecker;
import org.ladysnake.elmendorf.CheckedConnection;
import org.ladysnake.elmendorf.ConnectionTestConfiguration;
import org.ladysnake.elmendorf.GameTestUtil;
import org.ladysnake.elmendorf.PacketSequenceChecker;
import org.ladysnake.elmendorf.impl.mixin.ClientConnectionAccessor;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class MockClientConnection extends ClientConnection implements CheckedConnection, ConnectionTestConfiguration {
    private final List<SentPacket> packetQueue = new ArrayList<>();
    private boolean allowNoPacketMatch;
    private boolean flushEachTick;
    private int ticks;

    public MockClientConnection(NetworkSide side) {
        super(side);
    }

    @Override
    public void toFlushPacketsEachTick(boolean flush) {
        this.flushEachTick = flush;
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
    public void setInitialPacketListener(PacketListener packetListener) {
        // NO-OP
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
    public PacketSequenceChecker sent(CustomPayload.Id<?> channelId) {
        return sent(packet -> packet instanceof CustomPayloadS2CPacket p && Objects.equals(p.payload().getId(), channelId), "Expected packet for channel " + channelId);
    }

    @Override
    public <T extends CustomPayload> PacketSequenceChecker sent(CustomPayload.Id<T> channelId, Consumer<T> expect) {
        List<GameTestException> suppressed = new ArrayList<>();
        try {
            return sent(createCheckerTest(channelId, expect, suppressed), "Expected packet for channel " + channelId);
        } catch (GameTestException e) {
            suppressed.forEach(e::addSuppressed);
            throw e;
        }
    }

    @NotNull
    private static <T extends CustomPayload> Predicate<Packet<?>> createCheckerTest(CustomPayload.Id<T> channelId, Consumer<T> expect, List<GameTestException> suppressed) {
        return packet -> {
            if (packet instanceof CustomPayloadS2CPacket p && Objects.equals(p.payload().getId(), channelId)) {
                try {
                    // The id is right, so the type must be right
                    @SuppressWarnings("unchecked") T payload = (T) p.payload();
                    expect.accept(payload);
                    return true;
                } catch (GameTestException e) {
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
        if (!this.allowNoPacketMatch) GameTestUtil.assertFalse(errorMessage, packets.isEmpty());
        return new PacketSequenceCheckerImpl(errorMessage, packets);
    }

    // Don't access internal API at home kids
    @SuppressWarnings("UnstableApiUsage")
    @Override
    public PacketSequenceChecker sentEntityComponentUpdate(@Nullable Entity synced, ComponentKey<?> key, Consumer<ByteBufChecker> expect) {
        if (synced != null) GameTestUtil.assertTrue("Expected " + synced + " to provide component " + key.getId(), key.isProvidedBy(synced));
        List<GameTestException> suppressed = new ArrayList<>();
        try {
            return sent(
                    createCheckerTest(CardinalComponentsEntity.PACKET_ID, payload -> {
                        GameTestUtil.assertTrue("Expected component update to target entity " + synced, synced == null || payload.targetData() == synced.getId());
                        expect.accept(new ByteBufChecker(payload.buf()));
                    }, suppressed),
                    "Expected sync packet for component " + key.getId()
            );
        } catch (GameTestException e) {
            suppressed.forEach(e::addSuppressed);
            throw e;
        }
    }

    @Override
    public void sentPackets(Consumer<Queue<Packet<?>>> test) {
        test.accept(this.packetQueue.stream().map(p -> p.packet).collect(Collectors.toCollection(ArrayDeque::new)));
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public void send(Packet<?> packet, @Nullable PacketCallbacks callback, boolean flush) {
        this.packetQueue.add(new SentPacket(packet, this.ticks));
        if (callback != null) callback.onSuccess();
    }

    @Override
    public void tryDisableAutoRead() {
        // NO-OP
    }

    @Override
    public void disconnect(Text disconnectReason) {
        //noinspection ConstantConditions
        ((ClientConnectionAccessor)(Object)this).setDisconnectReason(disconnectReason);
    }

    public record PacketSequenceCheckerImpl(String defaultErrorMessage, List<SentPacket> packets) implements PacketSequenceChecker {

        @Override
        public PacketSequenceChecker atLeast(int times) {
            GameTestUtil.assertTrue("%s to be sent at least %d times, was %d".formatted(defaultErrorMessage, times, this.packets.size()), this.packets.size() >= times);
            return this;
        }

        @Override
        public PacketSequenceChecker atLeast(String errorMessage, int times) {
            GameTestUtil.assertTrue(errorMessage, this.packets.size() >= times);
            return this;
        }

        @Override
        public PacketSequenceChecker exactly(int times) {
            GameTestUtil.assertTrue("%s to be sent %d times, was %d".formatted(defaultErrorMessage, times, this.packets.size()), this.packets.size() == times);
            return this;
        }

        @Override
        public PacketSequenceChecker exactly(String errorMessage, int times) {
            GameTestUtil.assertTrue(errorMessage, this.packets.size() == times);
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
        public PacketSequenceChecker thenSent(Delay delay, CustomPayload.Id<?> channelId) {
            return thenSent(delay, packet -> packet instanceof CustomPayloadS2CPacket p && Objects.equals(p.payload().getId(), channelId), "Expected packet for channel " + channelId);
        }

        @Override
        public <T extends CustomPayload> PacketSequenceChecker thenSent(Delay delay, CustomPayload.Id<T> channelId, Consumer<T> expect) {
            List<GameTestException> suppressed = new ArrayList<>();
            try {
                return this.thenSent(delay, createCheckerTest(channelId, expect, suppressed), "Expected packet for channel " + channelId);
            } catch (GameTestException e) {
                suppressed.forEach(e::addSuppressed);
                throw e;
            }
        }

        // Don't access internal API at home kids
        @SuppressWarnings("UnstableApiUsage")
        @Override
        public PacketSequenceChecker thenSentComponentUpdate(Delay delay, @Nullable Entity synced, ComponentKey<?> key, Consumer<ByteBufChecker> expect) {
            if (synced != null) GameTestUtil.assertTrue("Expected " + synced + " to provide component " + key.getId(), key.isProvidedBy(synced));
            List<GameTestException> suppressed = new ArrayList<>();
            try {
                return thenSent(
                        delay,
                        createCheckerTest(CardinalComponentsEntity.PACKET_ID, payload -> {
                            GameTestUtil.assertTrue("Expected component update to target entity " + synced, synced == null || payload.targetData() == synced.getId());
                            expect.accept(new ByteBufChecker(payload.buf()));
                        }, suppressed),
                        "Expected sync packet for component " + key.getId()
                );
            } catch (GameTestException e) {
                suppressed.forEach(e::addSuppressed);
                throw e;
            }
        }

        @Override
        public PacketSequenceChecker thenSent(Delay delay, Predicate<Packet<?>> test, String errorMessage) {
            var packets = this.packets().stream().map(p -> p.next(delay, test)).filter(Objects::nonNull).toList();
            GameTestUtil.assertFalse(errorMessage, packets.isEmpty());
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
