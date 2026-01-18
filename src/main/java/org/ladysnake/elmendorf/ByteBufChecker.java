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
package org.ladysnake.elmendorf;

import net.fabricmc.fabric.api.networking.v1.FriendlyByteBufs;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public final class ByteBufChecker {
    private final FriendlyByteBuf buf;
    private final GameTestHelper ctx;

    public ByteBufChecker(FriendlyByteBuf buf, GameTestHelper ctx) {
        this.buf = FriendlyByteBufs.copy(buf);
        this.ctx = ctx;
    }

    public ByteBufChecker checkIdentifier(@Nullable Identifier expected) {
        return check(Identifier.class, expected, FriendlyByteBuf::readIdentifier);
    }

    public ByteBufChecker checkBlockPos(@Nullable BlockPos expected) {
        return check(BlockPos.class, expected, buf -> FriendlyByteBuf.readBlockPos(buf));
    }

    public ByteBufChecker checkChunkPos(@Nullable ChunkPos expected) {
        return check(ChunkPos.class, expected, b -> b.readChunkPos());
    }

    public ByteBufChecker checkBoolean(@Nullable Boolean expected) {
        return check(boolean.class, expected, FriendlyByteBuf::readBoolean);
    }

    public ByteBufChecker checkByte(@Nullable Byte expected) {
        return check(byte.class, expected, FriendlyByteBuf::readByte);
    }

    public ByteBufChecker checkShort(@Nullable Short expected) {
        return check(short.class, expected, FriendlyByteBuf::readShort);
    }

    public ByteBufChecker checkInt(@Nullable Integer expected) {
        return check(int.class, expected, FriendlyByteBuf::readInt);
    }

    public ByteBufChecker checkVarInt(@Nullable Integer expected) {
        return check(int.class, expected, FriendlyByteBuf::readVarInt);
    }

    public ByteBufChecker checkLong(@Nullable Long expected) {
        return check(long.class, expected, FriendlyByteBuf::readLong);
    }

    public ByteBufChecker checkFloat(@Nullable Float expected) {
        return check(float.class, expected, FriendlyByteBuf::readFloat);
    }

    public ByteBufChecker checkDouble(@Nullable Double expected) {
        return check(double.class, expected, FriendlyByteBuf::readDouble);
    }

    public ByteBufChecker checkUtf(@Nullable String expected) {
        return check(String.class, expected, FriendlyByteBuf::readUtf);
    }

    public void noMoreData() {
        if (this.buf.isReadable()) {
            throw ctx.assertionException(Component.literal("Expected end of buffer"));
        }
    }

    public <T> ByteBufChecker check(Class<T> type, @Nullable T expected, Function<FriendlyByteBuf, T> reader) {
        T value;
        try {
            value = reader.apply(this.buf);
        } catch (IndexOutOfBoundsException e) {
            throw ctx.assertionException(Component.literal("Expected %s %s but there was nothing left to read".formatted(type.getSimpleName(), str(expected))));
        }
        ctx.assertTrue(expected == any() || expected.equals(value), "Expected %s %s, got %s".formatted(type.getSimpleName(), str(expected), value));
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
