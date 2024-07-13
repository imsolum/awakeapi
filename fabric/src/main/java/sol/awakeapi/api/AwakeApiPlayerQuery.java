package sol.awakeapi.api;

import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sol.awakeapi.AwakeApi;
import sol.awakeapi.api.api_data.AIData;
import sol.awakeapi.api.api_data.Message;
import sol.awakeapi.interfaces.IPlayerEntity;
import sol.awakeapi.util.ConverseStatus;
import java.util.*;

/**
 * This class provides utility functions for data pertaining to the player.
 * All methods in this class are server-side only.
 *
 * <p>This class includes methods to interact with AI and conversation management,
 * including querying which mob the player is conversing with, adding messages, and
 * retrieving AI data.</p>
 * <p>For other API interfaces, see also:</p>
 * @see AwakeApiImpl
 * @see AwakeApiQueryReg
 */
public class AwakeApiPlayerQuery {

    private static final String SIMPLE_NAME = AwakeApiPlayerQuery.class.getSimpleName();

    /**
     * Gets the mob the player is currently speaking to.
     * Returns {@code null} if the player is not engaged in a conversation.
     *
     * @param serverPlayer The player whose conversation status is to be checked
     * @return The mob entity the player is conversing with, or {@code null} if not engaged in a conversation
     */
    public static @Nullable MobEntity getMobConversingWith(ServerPlayerEntity serverPlayer) {
        return ((IPlayerEntity) serverPlayer).getMobConversingWith();
    }

    /**
     * Adds a message to the player's conversation history. The message object should contain the
     * UUID of the mob to which the message is related. Additionally, {@code msg.isUser()} denotes
     * whether the message is sent by the player or the mob.
     * @implNote Messages are automatically added (and saved) between
     * a player and a mob. This method should <b>only</b> be used
     * if you want to add <b>additional</b> or unspoken data.
     * @param serverPlayer The player to whom the message is to be added
     * @param msg The message object containing the details of the conversation
     * @return {@code true} if a previous existing conversation was found, {@code false} if no
     * conversation was found, and thus a new one was created with the new message.
     */
    public boolean addMessageToPlayer(ServerPlayerEntity serverPlayer, Message msg) {
        return ((IPlayerEntity) serverPlayer).addMessage(serverPlayer, msg);
    }

    /**
     * Gets the conversation history between a player and a mob. If no conversation history exists,
     * returns an empty list. It is advisable to check the list before using it.
     *
     * @param serverPlayer The player whose conversation history is to be retrieved
     * @param mobUuid The UUID of the mob in the conversation
     * @return A list of messages representing the conversation history
     */
    public List<Message> getMessagesFromPlayer(ServerPlayerEntity serverPlayer, UUID mobUuid) {
        return ((IPlayerEntity) serverPlayer).getMessages(mobUuid);
    }

    /**
     * Interacts with a mob by attempting to start or end a conversation. If a conversation is already
     * ongoing, it will end the conversation. If the mob is different from the current conversation mob,
     * it will replace the existing conversation.
     *
     * @param serverPlayer The player initiating the conversation
     * @param mob The mob entity to converse with
     * @return The status of the conversation after the interaction
     * @see ConverseStatus
     */
    public ConverseStatus converse(ServerPlayerEntity serverPlayer, MobEntity mob) {
        return ((IPlayerEntity) serverPlayer).converse(serverPlayer, mob);
    }

    /**
     * Creates an AIData object of the mob the player is currently speaking to. If the player is not
     * speaking to any mob, returns {@code null}.
     *
     * @param serverPlayer The player whose AI data is to be retrieved
     * @return An AIData object containing the conversation details, or {@code null} if not conversing
     */
    public @Nullable AIData getPlayerAIData(ServerPlayerEntity serverPlayer) {
        return ((IPlayerEntity) serverPlayer).getAIData();
    }

    /**
     * Creates an AIData object of the mob the player is currently speaking to, including a system message
     * and an image flag. If the player is not speaking to any mob, returns {@code null}.
     *
     * @param serverPlayer The player whose AI data is to be retrieved
     * @param systemMessage An optional system message to be included
     * @param isImage A flag indicating whether the system message involves an image
     * @return An AIData object containing the conversation details, or {@code null} if not conversing
     */
    public AIData getPlayerAIData(ServerPlayerEntity serverPlayer, String systemMessage, boolean isImage) {
        return ((IPlayerEntity) serverPlayer).getAIData(systemMessage, isImage);
    }

    /**
     * Creates an AIData object for a specified mob, including a system message and an image flag.
     * If the mob does not have any data stored, this method is essentially pointless, but will
     * still return a valid AIData object.
     *
     * @param serverPlayer The player whose AI data is to be retrieved
     * @param mob The mob entity whose data is to be retrieved
     * @param systemMessage An optional system message to be included
     * @param isImage A flag indicating whether the system message involves an image
     * @return An AIData object containing the conversation details with the specified mob
     */
    public AIData getPlayerAIData(ServerPlayerEntity serverPlayer, MobEntity mob, String systemMessage, boolean isImage) {
        return ((IPlayerEntity) serverPlayer).getAIData(mob, systemMessage, isImage);
    }

    /**
     * Creates an AIData object for a specified mob. If the mob does not have any data stored,
     * returns a valid AIData object, albeit redundant.
     *
     * @param serverPlayer The player whose AI data is to be retrieved
     * @param mob The mob entity whose data is to be retrieved
     * @return An AIData object containing the conversation details with the specified mob
     */
    public AIData getPlayerAIData(ServerPlayerEntity serverPlayer, MobEntity mob) {
        return ((IPlayerEntity) serverPlayer).getAIData(mob);
    }

    /**
     * Given a player and a mob UUID, returns a JSON array of their conversation
     * (if any)
     * @param player
     * @param mobUuid
     * @return A {@code Nullable JsonObject}
     */
    public @Nullable JsonObject conversationAsJson(ServerPlayerEntity player, UUID mobUuid) {
        return ((IPlayerEntity) player).asArray(mobUuid);
    }

    /**
     * This method returns the MobEntity a player is speaking with
     * (if any)
     * @param player
     * @return A {@code Nullable MobEntity}
     */
    public @Nullable MobEntity getConversingWith(ServerPlayerEntity player) {
        return ((IPlayerEntity) player).getMobConversingWith();
    }

    /**
     * Utility method to retrieve a team from the scoreboard, ideally
     * useful for storing 'teams'. If the team is not found, optionally
     * create it.
     * @param server The MinecraftServer to locate / create the team from
     * @param teamName The name of the team
     * @param createIfNull A boolean flag to denote whether it should be
     *                     created or not if it isn't found
     * @return A nullable {@code Team} object
     * @implNote Since nullable, ensure response is validated.
     */
    public static @Nullable Team getTeam(@NotNull MinecraftServer server, String teamName, boolean createIfNull) {
        ServerScoreboard scoreboard = server.getScoreboard();
        Team friendlyTeam = scoreboard.getTeam(teamName);

        if (createIfNull) {
            if (friendlyTeam == null) {
                friendlyTeam = scoreboard.addTeam(teamName);
                friendlyTeam.setFriendlyFireAllowed(false);
                friendlyTeam.setShowFriendlyInvisibles(true);
            }
        }

        return friendlyTeam;
    }

    /**
     * Get a list of friendly mob UUIDs by passing a reference to
     * the player in question as well as the team name.
     * @param player
     * @param teamName
     * @return A set of UUID objects.
     * @see #getTeam(MinecraftServer, String, boolean)
     * @see #addToTeam(ServerPlayerEntity, String, Entity)
     */
    public static Set<UUID> getTeamMobUUIDs(@NotNull ServerPlayerEntity player, String teamName) {
        MinecraftServer server = player.getServer();
        if (server != null) {
            Set<UUID> friendlyMobs = new HashSet<>();
            Team friendlyTeam = getTeam(server, teamName, false);
            if (friendlyTeam != null) {
                for (String uuidString : friendlyTeam.getPlayerList()) {
                    try {
                        UUID uuid = UUID.fromString(uuidString);
                        friendlyMobs.add(uuid);
                    } catch (IllegalArgumentException e) {
                        if (!Objects.equals(uuidString, player.getDisplayName().getString())) {
                            AwakeApi.LOGGER.warn("@{}: Invalid UUID string: {}. Skipping...", SIMPLE_NAME, uuidString);
                        }
                    }
                }
            }
            return friendlyMobs;
        }
        AwakeApi.LOGGER.warn("@{}: Could not find any friendly mob UUIDs because the player is not in a server.", SIMPLE_NAME);
        return null;
    }

    /**
     * Utility method to add an entity to a team
     * @param player
     * @param teamName
     * @param entityToAdd
     * @implNote If the team doesn't already exist, the entity will
     * <b>not</b> be added
     * @see #getTeam(MinecraftServer, String, boolean)
     * @see #getTeamMobUUIDs(ServerPlayerEntity, String)
     */
    public static void addToTeam(ServerPlayerEntity player, String teamName, Entity entityToAdd) {
        MinecraftServer server = player.getServer();
        if (server != null) {
            Team friendlyTeam = getTeam(server, teamName, false);
            if (friendlyTeam != null) {
                if (entityToAdd instanceof PlayerEntity) {
                    server.getScoreboard().addPlayerToTeam(entityToAdd.getDisplayName().getString(), friendlyTeam);
                } else {
                    server.getScoreboard().addPlayerToTeam(entityToAdd.getUuidAsString(), friendlyTeam);
                }
            }
        }
    }
}
