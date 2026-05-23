package org.stepan1411.pvp_bot.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class BotPayloads {
    public static final Identifier LIST_REQUEST_ID = Identifier.of("pvp_bot_gui", "bot_list_request");
    public static final Identifier LIST_RESPONSE_ID = Identifier.of("pvp_bot_gui", "bot_list_response");
    public static final Identifier ACTION_ID = Identifier.of("pvp_bot_gui", "bot_action");

    public record BotListRequestPayload() implements CustomPayload {
        public static final CustomPayload.Id<BotListRequestPayload> ID = new CustomPayload.Id<>(LIST_REQUEST_ID);
        public static final PacketCodec<PacketByteBuf, BotListRequestPayload> CODEC = PacketCodec.unit(new BotListRequestPayload());
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    public record BotListResponsePayload(List<String> botNames) implements CustomPayload {
        public static final CustomPayload.Id<BotListResponsePayload> ID = new CustomPayload.Id<>(LIST_RESPONSE_ID);
        public static final PacketCodec<PacketByteBuf, BotListResponsePayload> CODEC = PacketCodec.of(
            (p, buf) -> {
                buf.writeVarInt(p.botNames.size());
                for (String name : p.botNames) buf.writeString(name);
            },
            buf -> {
                int size = buf.readVarInt();
                List<String> list = new ArrayList<>();
                for (int i = 0; i < size; i++) list.add(buf.readString());
                return new BotListResponsePayload(list);
            }
        );
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    public record BotActionPayload(String botName, String action) implements CustomPayload {
        public static final CustomPayload.Id<BotActionPayload> ID = new CustomPayload.Id<>(ACTION_ID);
        public static final PacketCodec<PacketByteBuf, BotActionPayload> CODEC = PacketCodec.of(
            (p, buf) -> {
                buf.writeString(p.botName);
                buf.writeString(p.action);
            },
            buf -> new BotActionPayload(buf.readString(), buf.readString())
        );
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }
}
