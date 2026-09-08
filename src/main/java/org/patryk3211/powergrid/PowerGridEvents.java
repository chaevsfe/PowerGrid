package org.patryk3211.powergrid;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.patryk3211.powergrid.circuits.components.ComponentRegistry;
import org.patryk3211.powergrid.collections.ModdedCommands;
import org.patryk3211.powergrid.collections.ModdedConfigs;
import org.patryk3211.powergrid.electricity.GlobalElectricNetworks;
import org.patryk3211.powergrid.electricity.base.ElectricBehaviour;
import org.patryk3211.powergrid.electricity.wire.BaseWireEntity;
import org.patryk3211.powergrid.electricity.wire.EntityWireInteraction;
import org.patryk3211.powergrid.electricity.wire.WireItem;
import org.patryk3211.powergrid.equipment.portablebattery.PortableBatteryItem;
import org.patryk3211.powergrid.collections.ModPackets;
import org.patryk3211.powergrid.collections.ModdedPackets;
import org.patryk3211.powergrid.network.packets.NegotiateSyncC2SPacket;
import org.patryk3211.powergrid.network.packets.ServerConfigS2CPacket;
import org.patryk3211.powergrid.utility.Lang;
import net.minecraft.server.permissions.Permissions;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class PowerGridEvents {
	private record PendingChunkStatus(LevelChunk chunk, FullChunkStatus oldStatus, FullChunkStatus newStatus) { }

	private static final Map<ServerLevel, List<PendingChunkStatus>> PENDING_CHUNK_STATUS = new IdentityHashMap<>();

	public static void register() {
		ServerLifecycleEvents.SERVER_STARTED.register(server -> ComponentRegistry.validateItemEntries(server.registryAccess()));
		ServerTickEvents.START_LEVEL_TICK.register(PowerGridEvents::levelPreTick);
		ServerTickEvents.END_LEVEL_TICK.register(GlobalElectricNetworks::postTick);
		ServerTickEvents.END_SERVER_TICK.register(EntityWireInteraction::postTick);
		ServerLevelEvents.UNLOAD.register((server, level) -> {
			PENDING_CHUNK_STATUS.remove(level);
			GlobalElectricNetworks.unloadWorld(level);
		});
		CommandRegistrationCallback.EVENT.register(ModdedCommands::register);
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> playerJoin(handler.player));
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> playerQuit(handler.player));
		ServerPlayerEvents.COPY_FROM.register(PowerGridEvents::playerCopy);
		ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register((player, origin, destination) ->
				GlobalElectricNetworks.dropTrackers(player, origin.dimension()));
		UseBlockCallback.EVENT.register((player, level, hand, hit) -> WireItem.useOn(player, hand, hit.getBlockPos(), hit.getDirection()));
		UseItemCallback.EVENT.register((player, level, hand) -> WireItem.use(player, hand));
		ServerEntityEvents.ENTITY_UNLOAD.register(BaseWireEntity::entityUnload);
		ServerChunkEvents.CHUNK_LOAD.register((level, chunk, newlyGenerated) -> chunkLoad(level, chunk.getPos()));
		ServerChunkEvents.FULL_CHUNK_STATUS_CHANGE.register(PowerGridEvents::chunkStatusChange);
	}

	private static void chunkStatusChange(ServerLevel level, LevelChunk chunk, FullChunkStatus oldStatus, FullChunkStatus newStatus) {
		PENDING_CHUNK_STATUS.computeIfAbsent(level, key -> new ArrayList<>())
				.add(new PendingChunkStatus(chunk, oldStatus, newStatus));
	}

	private static void drainChunkStatusChanges(ServerLevel level) {
		var pending = PENDING_CHUNK_STATUS.remove(level);
		if (pending == null)
			return;
		for (var entry : pending) {
			var pos = entry.chunk().getPos();
			if (entry.newStatus().isOrAfter(FullChunkStatus.BLOCK_TICKING) && !level.getChunkSource().hasChunk(pos.x(), pos.z()))
				continue;
			ElectricBehaviour.handleTicketChange(entry.chunk(), entry.oldStatus(), entry.newStatus());
		}
	}

	private static void levelPreTick(ServerLevel level) {
		drainChunkStatusChanges(level);
		GlobalElectricNetworks.preTick(level);
		for (ServerPlayer player : level.players())
			playerPre(player);
	}

	private static void chunkLoad(ServerLevel level, net.minecraft.world.level.ChunkPos pos) {
		var global = GlobalElectricNetworks.getWorldNetworks((LevelAccessor) level);
		if (global == null)
			return;
		global.chunkLoaded(pos);
	}

	private static void playerCopy(ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean alive) {
		Boolean syncType = NegotiateSyncC2SPacket.SYNC_TYPES.remove(oldPlayer);
		if (syncType != null)
			NegotiateSyncC2SPacket.SYNC_TYPES.put(newPlayer, syncType);
	}

	private static void playerQuit(ServerPlayer player) {
		NegotiateSyncC2SPacket.SYNC_TYPES.remove(player);
		GlobalElectricNetworks.dropTrackers(player, player.level().dimension());
	}

	private static void playerJoin(ServerPlayer player) {
		ModPackets.PACKETS.onPlayerJoin(player);
		ModdedPackets.sendToClient(new ServerConfigS2CPacket(), player);

		if(player.permissions().hasPermission(Permissions.COMMANDS_ADMIN) && !ModdedConfigs.server().isUpToDate()) {
			player.sendSystemMessage(Lang.translateDirect("message.outdated_configs"));
			player.sendSystemMessage(Component.empty()
					.append(Lang.translateDirect("message.reset_configs")
							.withStyle(Style.EMPTY.withColor(ChatFormatting.BLUE)
									.withClickEvent(new ClickEvent.RunCommand("/powergrid reset_configs")))
					).append(Component.literal(" "))
					.append(Lang.translateDirect("message.disable_config_message")
							.withStyle(Style.EMPTY.withColor(ChatFormatting.BLUE)
									.withClickEvent(new ClickEvent.RunCommand("/powergrid ignore_configs")))
					)
			);
		}
	}

	private static void playerPre(Player player) {
		if (player.level().isClientSide())
			return;

		ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
		if (!(chestStack.getItem() instanceof PortableBatteryItem battery))
			return;

		battery.onWornTick(chestStack, player);
	}
}
