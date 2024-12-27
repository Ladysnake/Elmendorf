package org.ladysnake.elmendorf.impl;

import net.minecraft.network.*;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.elmendorf.impl.mixin.ClientConnectionAccessor;

public class MockClientConnection extends ClientConnection {
    public MockClientConnection(NetworkSide side) {
        super(side);
    }

    @Override
    public void setInitialPacketListener(PacketListener packetListener) {
        // NO-OP
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public void send(Packet<?> packet, @Nullable PacketCallbacks callback, boolean flush) {
        if (callback != null) callback.onSuccess();
    }

    @Override
    public void flush() {
        // NO-OP
    }

    @Override
    public void tryDisableAutoRead() {
        // NO-OP
    }

    @Override
    public void disconnect(DisconnectionInfo disconnectReason) {
        //noinspection ConstantConditions
        ((ClientConnectionAccessor) this).setDisconnectionInfo(disconnectReason);
    }

    @Override
    public <T extends PacketListener> void transitionInbound(NetworkState<T> state, T packetListener) {
        // NO-OP
    }
}
