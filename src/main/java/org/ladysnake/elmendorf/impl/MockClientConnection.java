package org.ladysnake.elmendorf.impl;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.DisconnectionInfo;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.state.NetworkState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.elmendorf.impl.mixin.ClientConnectionAccessor;

import java.util.concurrent.TimeUnit;

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
