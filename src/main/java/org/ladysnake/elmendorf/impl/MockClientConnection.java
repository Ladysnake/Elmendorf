package org.ladysnake.elmendorf.impl;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.PacketListener;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.elmendorf.impl.mixin.ConnectionAccessor;

import java.util.concurrent.TimeUnit;

public class MockClientConnection extends Connection {
    public MockClientConnection(PacketFlow side) {
        super(side);
    }

    @Override
    public void setListenerForServerboundHandshake(PacketListener packetListener) {
        // NO-OP
    }

    @Override
    public boolean isConnected() {
        return super.isConnected();
    }

    @Override
    public void send(Packet<?> packet, @Nullable ChannelFutureListener callback, boolean flush) {
        if (callback != null) {
            try {
                callback.operationComplete(new ChannelFuture() {
                    @Override
                    public Channel channel() {
                        return null;
                    }

                    @Override
                    public ChannelFuture addListener(GenericFutureListener<? extends Future<? super Void>> listener) {
                        return null;
                    }

                    @Override
                    public ChannelFuture addListeners(GenericFutureListener<? extends Future<? super Void>>[] listeners) {
                        return null;
                    }

                    @Override
                    public ChannelFuture removeListener(GenericFutureListener<? extends Future<? super Void>> listener) {
                        return null;
                    }

                    @Override
                    public ChannelFuture removeListeners(GenericFutureListener<? extends Future<? super Void>>[] listeners) {
                        return null;
                    }

                    @Override
                    public ChannelFuture sync() {
                        return null;
                    }

                    @Override
                    public ChannelFuture syncUninterruptibly() {
                        return null;
                    }

                    @Override
                    public ChannelFuture await() {
                        return null;
                    }

                    @Override
                    public ChannelFuture awaitUninterruptibly() {
                        return null;
                    }

                    @Override
                    public boolean isVoid() {
                        return true;
                    }

                    @Override
                    public boolean isSuccess() {
                        return true;
                    }

                    @Override
                    public boolean isCancellable() {
                        return false;
                    }

                    @Override
                    public Throwable cause() {
                        return null;
                    }

                    @Override
                    public boolean await(long l, TimeUnit timeUnit) {
                        return false;
                    }

                    @Override
                    public boolean await(long l) {
                        return false;
                    }

                    @Override
                    public boolean awaitUninterruptibly(long l, TimeUnit timeUnit) {
                        return false;
                    }

                    @Override
                    public boolean awaitUninterruptibly(long l) {
                        return false;
                    }

                    @Override
                    public Void getNow() {
                        return null;
                    }

                    @Override
                    public boolean cancel(boolean b) {
                        return false;
                    }

                    @Override
                    public boolean isCancelled() {
                        return false;
                    }

                    @Override
                    public boolean isDone() {
                        return true;
                    }

                    @Override
                    public Void get() {
                        return null;
                    }

                    @Override
                    public Void get(long timeout, @NotNull TimeUnit unit) {
                        return null;
                    }
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void flushChannel() {
        // NO-OP
    }

    @Override
    public void setReadOnly() {
        // NO-OP
    }

    @Override
    public void disconnect(DisconnectionDetails disconnectReason) {
        //noinspection ConstantConditions
        ((ConnectionAccessor) this).setDisconnectionDetails(disconnectReason);
    }

    @Override
    public <T extends PacketListener> void setupInboundProtocol(ProtocolInfo<T> state, T packetListener) {
        // NO-OP
    }
}
