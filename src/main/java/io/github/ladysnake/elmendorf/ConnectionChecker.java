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

import net.minecraft.network.Packet;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface ConnectionChecker {
    PacketChecker sent(Class<? extends Packet<?>> packetType);

    PacketChecker sent(Identifier channelId);

    PacketChecker sent(Predicate<Packet<?>> test, String errorMessage);

    void sentPackets(Consumer<Queue<Packet<?>>> test);

    final class PacketChecker {
        private final List<Packet<?>> packets;
        private final String defaultErrorMessage;

        public PacketChecker(List<Packet<?>> packets, String defaultErrorMessage) {
            this.packets = packets;
            this.defaultErrorMessage = defaultErrorMessage;
        }

        public void atLeast(int times) {
            GameTestUtil.assertTrue("%s to be sent at least %d times, was %d".formatted(defaultErrorMessage, times, this.packets.size()), this.packets.size() >= times);
        }

        public void atLeast(String errorMessage, int times) {
            GameTestUtil.assertTrue(errorMessage, this.packets.size() >= times);
        }

        public void exactly(int times) {
            GameTestUtil.assertTrue("%s to be sent %d times, was %d".formatted(defaultErrorMessage, times, this.packets.size()), this.packets.size() == times);
        }

        public void exactly(int times, String errorMessage) {
            GameTestUtil.assertTrue(errorMessage, this.packets.size() == times);
        }
    }
}
