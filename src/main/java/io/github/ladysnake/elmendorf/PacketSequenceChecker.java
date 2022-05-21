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
package io.github.ladysnake.elmendorf;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface PacketSequenceChecker {
    /**
     * @throws net.minecraft.test.GameTestException if the current packet sequence has not occurred at least {@code times} times
     */
    PacketSequenceChecker atLeast(int times);

    /**
     * @throws net.minecraft.test.GameTestException if the current packet sequence has not occurred at least {@code times} times
     */
    PacketSequenceChecker atLeast(String errorMessage, int times);

    /**
     * @throws net.minecraft.test.GameTestException if the current packet sequence has not occurred exactly {@code times} times
     */
    PacketSequenceChecker exactly(int times);

    /**
     * @throws net.minecraft.test.GameTestException if the current packet sequence has not occurred exactly {@code times} times
     */
    PacketSequenceChecker exactly(String errorMessage, int times);

    /**
     * Creates a packet sequence checker that looks for a matching packet sent after this packet within the given {@code delay}
     */
    PacketSequenceChecker thenSent(Delay delay, Class<? extends Packet<?>> packetType);

    /**
     * Creates a packet sequence checker that looks for a matching packet sent after this packet within the given {@code delay}
     */
    <P extends Packet<?>> PacketSequenceChecker thenSent(Delay delay, Class<P> packetType, Predicate<P> expect);

    /**
     * Creates a packet sequence checker that looks for a matching packet sent after this packet within the given {@code delay}
     */
    PacketSequenceChecker thenSent(Delay delay, Identifier channelId);

    /**
     * Creates a packet sequence checker that looks for a matching packet sent after this packet within the given {@code delay}
     */
    PacketSequenceChecker thenSent(Delay delay, Identifier channelId, Consumer<ByteBufChecker> expect);

    /**
     * Creates a packet sequence checker that looks for a matching Cardinal Components Entity sync packet sent after this packet within the given {@code delay}
     */
    PacketSequenceChecker thenSentComponentUpdate(Delay delay, @Nullable Entity synced, ComponentKey<?> key, Consumer<ByteBufChecker> expect);

    /**
     * Creates a packet sequence checker that looks for a matching packet sent after this packet within the given {@code delay}
     */
    PacketSequenceChecker thenSent(Delay delay, Predicate<Packet<?>> test, String errorMessage);

    enum Delay {
        /**The packet must be the one sent right after, and in the same tick*/
        IMMEDIATELY,
        /**The packet must have been sent in the same tick*/
        SAME_TICK,
        /**The packet must have been sent at some point in the same or in a later tick*/
        LATER,
    }
}
