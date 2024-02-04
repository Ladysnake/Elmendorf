package org.ladysnake.ripstop;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;

public record TestPayload(PacketByteBuf rawData) implements CustomPayload {
    public static final CustomPayload.Id<TestPayload> ID = CustomPayload.id("ripstop:test");

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
