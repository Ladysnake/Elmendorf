/*
 * Elmendorf
 * Copyright (C) 2021-2023 Ladysnake
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
package org.ladysnake.elmendorf;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.test.GameTestException;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public final class ByteBufChecker {
    private final PacketByteBuf buf;

    public ByteBufChecker(PacketByteBuf buf) {
        this.buf = PacketByteBufs.copy(buf);
    }

    public ByteBufChecker checkIdentifier(@Nullable Identifier expected) {
        return check(Identifier.class, expected, PacketByteBuf::readIdentifier);
    }

    public ByteBufChecker checkBlockPos(@Nullable BlockPos expected) {
        return check(BlockPos.class, expected, PacketByteBuf::readBlockPos);
    }

    public ByteBufChecker checkChunkPos(@Nullable ChunkPos expected) {
        return check(ChunkPos.class, expected, PacketByteBuf::readChunkPos);
    }

    public ByteBufChecker checkChunkSectionPos(@Nullable ChunkSectionPos expected) {
        return check(ChunkSectionPos.class, expected, PacketByteBuf::readChunkSectionPos);
    }

    public ByteBufChecker checkBoolean(@Nullable Boolean expected) {
        return check(boolean.class, expected, PacketByteBuf::readBoolean);
    }

    public ByteBufChecker checkByte(@Nullable Byte expected) {
        return check(byte.class, expected, PacketByteBuf::readByte);
    }

    public ByteBufChecker checkShort(@Nullable Short expected) {
        return check(short.class, expected, PacketByteBuf::readShort);
    }

    public ByteBufChecker checkInt(@Nullable Integer expected) {
        return check(int.class, expected, PacketByteBuf::readInt);
    }

    public ByteBufChecker checkVarInt(@Nullable Integer expected) {
        return check(int.class, expected, PacketByteBuf::readVarInt);
    }

    public ByteBufChecker checkLong(@Nullable Long expected) {
        return check(long.class, expected, PacketByteBuf::readLong);
    }

    public ByteBufChecker checkFloat(@Nullable Float expected) {
        return check(float.class, expected, PacketByteBuf::readFloat);
    }

    public ByteBufChecker checkDouble(@Nullable Double expected) {
        return check(double.class, expected, PacketByteBuf::readDouble);
    }

    public ByteBufChecker checkString(@Nullable String expected) {
        return check(String.class, expected, PacketByteBuf::readString);
    }

    public void noMoreData() {
        if (this.buf.isReadable()) {
            throw new GameTestException("Expected end of buffer");
        }
    }

    public <T> ByteBufChecker check(Class<T> type, @Nullable T expected, Function<PacketByteBuf, T> reader) {
        T value;
        try {
            value = reader.apply(this.buf);
        } catch (IndexOutOfBoundsException e) {
            throw new GameTestException("Expected %s %s but there was nothing left to read".formatted(type.getSimpleName(), str(expected)));
        }
        GameTestUtil.assertTrue("Expected %s %s, got %s".formatted(type.getSimpleName(), str(expected), value), expected == any() || expected.equals(value));
        return this;
    }

    @Contract("-> null")
    public static <T> @Nullable T any() {
        return null;
    }

    private static String str(@Nullable Object obj) {
        return obj == null ? "(any)" : obj.toString();
    }
}
