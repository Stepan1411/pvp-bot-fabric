package org.stepan1411.pvp_bot.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.*;

public class SettingsPayloads {
    public static final Identifier REQUEST_ID = Identifier.of("pvp_bot_gui", "settings_request");
    public static final Identifier RESPONSE_ID = Identifier.of("pvp_bot_gui", "settings_response");
    public static final Identifier UPDATE_ID = Identifier.of("pvp_bot_gui", "settings_update");

    public record SettingsRequestPayload() implements CustomPayload {
        public static final CustomPayload.Id<SettingsRequestPayload> ID = new CustomPayload.Id<>(REQUEST_ID);
        public static final PacketCodec<PacketByteBuf, SettingsRequestPayload> CODEC = PacketCodec.unit(new SettingsRequestPayload());

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record SettingsResponsePayload(Map<String, String> settings) implements CustomPayload {
        public static final CustomPayload.Id<SettingsResponsePayload> ID = new CustomPayload.Id<>(RESPONSE_ID);

        public static final PacketCodec<PacketByteBuf, SettingsResponsePayload> CODEC = PacketCodec.of(
            (value, buf) -> {
                buf.writeVarInt(value.settings.size());
                for (Map.Entry<String, String> e : value.settings.entrySet()) {
                    buf.writeString(e.getKey());
                    buf.writeString(e.getValue());
                }
            },
            buf -> {
                int size = buf.readVarInt();
                Map<String, String> map = new LinkedHashMap<>();
                for (int i = 0; i < size; i++) {
                    map.put(buf.readString(), buf.readString());
                }
                return new SettingsResponsePayload(Collections.unmodifiableMap(map));
            }
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record SettingsUpdatePayload(String key, String value) implements CustomPayload {
        public static final CustomPayload.Id<SettingsUpdatePayload> ID = new CustomPayload.Id<>(UPDATE_ID);

        public static final PacketCodec<PacketByteBuf, SettingsUpdatePayload> CODEC = PacketCodec.of(
            (p, buf) -> {
                buf.writeString(p.key);
                buf.writeString(p.value);
            },
            buf -> new SettingsUpdatePayload(buf.readString(), buf.readString())
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
}
