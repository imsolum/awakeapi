package sol.awakeapi.networking;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;
import sol.awakeapi.AwakeApi;
import sol.awakeapi.networking.packets.*;

public class AwakeApiPackets {

    public static final Identifier CLIENTBOUND_UPDATE_AI_PARAMS = new Identifier(AwakeApi.MOD_ID, "clientbound_update_ai_params");
    public static final Identifier CLIENTBOUND_API_GET_RESPONSE = new Identifier(AwakeApi.MOD_ID, "clientbound_api_get_response");
    public static final Identifier CLIENTBOUND_QUERY_AI = new Identifier(AwakeApi.MOD_ID, "clientbound_query_ai");


    public static final Identifier SERVERBOUND_HANDLE_CONVERSATION = new Identifier(AwakeApi.MOD_ID, "serverbound_handle_conversation");
    public static final Identifier SERVERBOUND_ADD_MESSAGE = new Identifier(AwakeApi.MOD_ID, "serverbound_add_message");
    public static final Identifier SERVERBOUND_EXECUTE_AI_FUNCTION = new Identifier(AwakeApi.MOD_ID, "serverbound_execute_ai_function");
    public static final Identifier SERVERBOUND_QUERY_AI = new Identifier(AwakeApi.MOD_ID, "serverbound_query_ai");
    public static final Identifier SERVERBOUND_DISPLAY_MESSAGES = new Identifier(AwakeApi.MOD_ID, "serverbound_display_messages");

    public static void registerC2SPackets() {
        ServerPlayNetworking.registerGlobalReceiver(SERVERBOUND_HANDLE_CONVERSATION, ServerboundHandleConversationPacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(SERVERBOUND_ADD_MESSAGE, ServerboundAddMessage::receive);
        ServerPlayNetworking.registerGlobalReceiver(SERVERBOUND_EXECUTE_AI_FUNCTION, ServerboundExecuteAIFunction::receive);
        ServerPlayNetworking.registerGlobalReceiver(SERVERBOUND_QUERY_AI, ServerboundQueryAI::receive);
        ServerPlayNetworking.registerGlobalReceiver(SERVERBOUND_DISPLAY_MESSAGES, ServerboundDisplayMessages::receive);
    }

    public static void registerS2CPackets() {
        ClientPlayNetworking.registerGlobalReceiver(CLIENTBOUND_UPDATE_AI_PARAMS, ClientboundUpdateAiParams::receive);
        ClientPlayNetworking.registerGlobalReceiver(CLIENTBOUND_API_GET_RESPONSE, ClientboundApiGetResponse::receive);
        ClientPlayNetworking.registerGlobalReceiver(CLIENTBOUND_QUERY_AI, ClientboundQueryAI::receive);
    }
}
