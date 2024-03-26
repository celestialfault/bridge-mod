package me.celestialfault.sweatbridge;

import com.mojang.logging.LogUtils;
import me.celestialfault.sweatbridge.commands.SSCCommand;
import me.celestialfault.sweatbridge.commands.SweatBridgeCommand;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;

import java.io.IOException;

public class SweatBridge implements ClientModInitializer {

	public static final Logger LOGGER = LogUtils.getLogger();
	public static boolean SEND_IN_CHAT = false;

	@Override
	public void onInitializeClient() {
		try {
			Config.INSTANCE.load();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> ChatConnection.attemptConnection());
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ChatConnection.disconnect());
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			SweatBridgeCommand.register(dispatcher);
			SSCCommand.register(dispatcher);
		});
	}

	public static boolean isOnHypixel() {
		MinecraftClient client = MinecraftClient.getInstance();
		if(client.isInSingleplayer() || client.getNetworkHandler() == null) {
			return false;
		}
		ServerInfo serverInfo = client.getNetworkHandler().getServerInfo();
		if(serverInfo == null) {
			return false;
		}
		return serverInfo.address.endsWith(".hypixel.net");
	}

	public static MutableText getPrefix() {
		return Text.empty()
			.append(Text.literal("Sweat").formatted(Config.INSTANCE.colors.prefix.get()))
			.append(Text.literal(" > ").formatted(Config.INSTANCE.colors.arrow.get()))
			.append(Text.empty().formatted(Formatting.RESET));
	}

	public static void send(Text component) {
		MinecraftClient client = MinecraftClient.getInstance();
		client.inGameHud.getChatHud().addMessage(getPrefix().append(component));
	}
}
