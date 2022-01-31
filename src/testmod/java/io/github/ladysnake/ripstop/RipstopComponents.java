package io.github.ladysnake.ripstop;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.TransientComponent;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class RipstopComponents implements EntityComponentInitializer {
    public static final ComponentKey<Component> TEST = ComponentRegistry.getOrCreate(new Identifier("ripstop", "test-component"), Component.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        class Impl extends TransientComponent.SimpleImpl implements AutoSyncedComponent {
            @Override
            public void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity recipient) {

            }

            @Override
            public void applySyncPacket(PacketByteBuf buf) {

            }
        }
        registry.registerFor(AxolotlEntity.class, TEST, a -> new Impl());
    }
}
