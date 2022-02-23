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
