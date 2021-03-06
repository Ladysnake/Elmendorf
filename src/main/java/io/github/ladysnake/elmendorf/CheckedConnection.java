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
package io.github.ladysnake.elmendorf;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface CheckedConnection {
    /**
     * Sets whether this connection object should allow empty packet matches.
     *
     * <p>If left to {@code false}, {@code sent} checks will throw when no matching packet is found.
     * Allowing empty matches notably allows for more advanced checks on {@link PacketSequenceChecker}.
     *
     * @param allow whether {@code sent} methods should throw immediately on empty matches
     * @return {@code this}, for chaining
     */
    CheckedConnection allowNoPacketMatch(boolean allow);

    PacketSequenceChecker sent(Class<? extends Packet<?>> packetType);

    <P extends Packet<?>> PacketSequenceChecker sent(Class<P> packetType, Predicate<P> expect);

    PacketSequenceChecker sent(Identifier channelId);

    PacketSequenceChecker sent(Identifier channelId, Consumer<ByteBufChecker> expect);

    PacketSequenceChecker sent(Predicate<Packet<?>> test, String errorMessage);

    /**
     * Checks that this connection object got a Cardinal Components API sync packet sent through it
     *
     * @param synced the entity on which the component update occurred
     * @param key    the key object representing the type of component that got synced
     * @param expect assertions for the content of the buffer
     * @return       a {@link PacketSequenceChecker} to perform advanced checks on matching packets
     */
    PacketSequenceChecker sentEntityComponentUpdate(@Nullable Entity synced, ComponentKey<?> key, Consumer<ByteBufChecker> expect);

    void sentPackets(Consumer<Queue<Packet<?>>> test);

}
