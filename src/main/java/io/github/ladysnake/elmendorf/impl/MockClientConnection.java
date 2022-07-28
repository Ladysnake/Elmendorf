/*
 * Elmendorf
 * Copyright (C) 2021-2022 Ladysnake
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
package io.github.ladysnake.elmendorf.impl;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.internal.entity.CardinalComponentsEntity;
import io.github.ladysnake.elmendorf.ByteBufChecker;
import io.github.ladysnake.elmendorf.CheckedConnection;
import io.github.ladysnake.elmendorf.ConnectionTestConfiguration;
import io.github.ladysnake.elmendorf.GameTestUtil;
import io.github.ladysnake.elmendorf.PacketSequenceChecker;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.class_7648;
import net.minecraft.entity.Entity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.test.GameTestException;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    public PacketSequenceChecker sent(Identifier channelId) {
        return sent(packet -> packet instanceof CustomPayloadS2CPacket p && Objects.equals(p.getChannel(), channelId), "Expected packet for channel " + channelId);
    }

    @Override
    public PacketSequenceChecker sent(Identifier channelId, Consumer<ByteBufChecker> expect) {
        List<GameTestException> suppressed = new ArrayList<>();
        try {
            return sent(createCheckerTest(channelId, expect, suppressed), "Expected packet for channel " + channelId);
        } catch (GameTestException e) {
            suppressed.forEach(e::addSuppressed);
            throw e;
        }
    }

    @NotNull
    private static Predicate<Packet<?>> createCheckerTest(Identifier channelId, Consumer<ByteBufChecker> expect, List<GameTestException> suppressed) {
        return packet -> {
            if (packet instanceof CustomPayloadS2CPacket p && Objects.equals(p.getChannel(), channelId)) {
                var checker = new ByteBufChecker(p.getData());
                try {
                    expect.accept(checker);
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

    @Override
    public PacketSequenceChecker sentEntityComponentUpdate(@Nullable Entity synced, ComponentKey<?> key, Consumer<ByteBufChecker> expect) {
        if (synced != null) GameTestUtil.assertTrue("Expected " + synced + " to provide component " + key.getId(), key.isProvidedBy(synced));
        List<GameTestException> suppressed = new ArrayList<>();
        try {
            // Don't access internal API at home kids
            //noinspection UnstableApiUsage
            return sent(
                    createCheckerTest(CardinalComponentsEntity.PACKET_ID, ((Consumer<ByteBufChecker>) c -> c.checkInt(synced == null ? ByteBufChecker.any() : synced.getId()).checkIdentifier(key.getId())).andThen(expect), suppressed),
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
    public void send(Packet<?> packet, @Nullable class_7648 callback) {
        this.packetQueue.add(new SentPacket(packet, this.ticks));
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
        public PacketSequenceChecker thenSent(Delay delay, Identifier channelId) {
            return thenSent(delay, packet -> packet instanceof CustomPayloadS2CPacket p && Objects.equals(p.getChannel(), channelId), "Expected packet for channel " + channelId);
        }

        @Override
        public PacketSequenceChecker thenSent(Delay delay, Identifier channelId, Consumer<ByteBufChecker> expect) {
            List<GameTestException> suppressed = new ArrayList<>();
            try {
                return this.thenSent(delay, createCheckerTest(channelId, expect, suppressed), "Expected packet for channel " + channelId);
            } catch (GameTestException e) {
                suppressed.forEach(e::addSuppressed);
                throw e;
            }
        }

        @Override
        public PacketSequenceChecker thenSentComponentUpdate(Delay delay, @Nullable Entity synced, ComponentKey<?> key, Consumer<ByteBufChecker> expect) {
            if (synced != null) GameTestUtil.assertTrue("Expected " + synced + " to provide component " + key.getId(), key.isProvidedBy(synced));
            List<GameTestException> suppressed = new ArrayList<>();
            try {
                // Don't access internal API at home kids
                //noinspection UnstableApiUsage
                return thenSent(delay,
                        createCheckerTest(CardinalComponentsEntity.PACKET_ID, ((Consumer<ByteBufChecker>) c -> c.checkInt(synced == null ? ByteBufChecker.any() : synced.getId()).checkIdentifier(key.getId())).andThen(expect), suppressed),
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
