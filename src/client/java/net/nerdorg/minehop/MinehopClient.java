package net.nerdorg.minehop;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import net.minecraft.client.render.RenderLayer;
import net.nerdorg.minehop.block.ModBlocks;
import net.nerdorg.minehop.client.SqueedometerHud;
import net.nerdorg.minehop.config.ConfigWrapper;
import net.nerdorg.minehop.data.DataManager;
import net.nerdorg.minehop.discord.DiscordIntegration;
import net.nerdorg.minehop.entity.ModEntities;
import net.nerdorg.minehop.entity.client.*;
import net.nerdorg.minehop.event.JoinEvent;
import net.nerdorg.minehop.event.KeyInputHandler;
import net.nerdorg.minehop.networking.ClientPacketHandler;

import java.util.ArrayList;
import java.util.List;

public class MinehopClient implements ClientModInitializer {
	public static SqueedometerHud squeedometerHud;

	public static int jump_count = 0;
	public static boolean jumping = false;
	public static double last_jump_speed = 0;
	public static double start_jump_speed = 0;
	public static double old_jump_speed = 0;
	public static long last_jump_time = 0;
	public static long old_jump_time = 0;
	public static double last_efficiency;
	public static double gauge;
	public static boolean wasOnGround = false;

	public static boolean hideSelf = false;
	public static boolean hideReplay = false;
	public static boolean hideOthers = false;

	public static long startTime = 0;
	public static float lastSendTime = 0;

	public static List<String> spectatorList = new ArrayList<>();

    @Override
	public void onInitializeClient() {
		MinecraftClient minecraft = MinecraftClient.getInstance();
		minecraft.execute(() -> {
			ServerList serverList = new ServerList(minecraft);
			serverList.loadFile();
			if (!isServerInList(serverList, "mh.nerd-org.com")) {
				serverList.add(new ServerInfo("§c§l§nOfficial Minehop Server", "mh.nerd-org.com", false), false);
				serverList.swapEntries(0, serverList.size() - 1);
				serverList.saveFile();
			}
		});

		ClientPacketHandler.registerReceivers();
		ConfigWrapper.loadConfig();
		squeedometerHud = new SqueedometerHud();

		KeyInputHandler.register();
		JoinEvent.register();

		EntityRendererRegistry.register(ModEntities.RESET_ENTITY, ResetRenderer::new);
		EntityModelLayerRegistry.registerModelLayer(ModModelLayers.RESET_ENTITY, ResetModel::getTexturedModelData);
		EntityRendererRegistry.register(ModEntities.START_ENTITY, StartRenderer::new);
		EntityModelLayerRegistry.registerModelLayer(ModModelLayers.START_ENTITY, ResetModel::getTexturedModelData);
		EntityRendererRegistry.register(ModEntities.END_ENTITY, EndRenderer::new);
		EntityModelLayerRegistry.registerModelLayer(ModModelLayers.END_ENTITY, ResetModel::getTexturedModelData);
		EntityRendererRegistry.register(ModEntities.REPLAY_ENTITY, ReplayRenderer::new);
		EntityModelLayerRegistry.registerModelLayer(ModModelLayers.REPLAY_ENTITY, ReplayModel::getTexturedModelData);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (!client.isInSingleplayer()) {
				Minehop.override_config = true;
			}
			if (client.player != null) {
				if (client.options.jumpKey.isPressed()) {
					jumping = true;
				}
				else {
					jumping = false;
				}
			}
		});

		BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.BOOSTER_BLOCK, RenderLayer.getTranslucent());
		BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.RAMP_BLOCK, RenderLayer.getTranslucent());
	}

	private boolean isServerInList(ServerList serverList, String ip) {
		for (int i = 0; i < serverList.size(); i++) {
			ServerInfo info = serverList.get(i);
			if (info.address.equals(ip)) {
				return true;
			}
		}
		return false;
	}
}