package me.celestialfault.sweatbridge

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.mojang.logging.LogUtils
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.text.MutableText
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.slf4j.Logger

object SweatBridge : ClientModInitializer {
	val VERSION: String by lazy {
		val container = FabricLoader.getInstance().getModContainer("sweatbridge").orElseThrow()
		val metadataVersion = container.metadata.version.friendlyString
		if(metadataVersion.contains('+')) metadataVersion.substring(metadataVersion.indexOf('+')) else metadataVersion
	}
	val MINECRAFT_VERSION: String by lazy {
		FabricLoader.getInstance().getModContainer("minecraft").orElseThrow().metadata.version.friendlyString
	}

	val LOGGER: Logger = LogUtils.getLogger()
	@JvmStatic var sendInChat: Boolean = false

	override fun onInitializeClient() {
		Config.load()
		ClientPlayConnectionEvents.JOIN.register(ClientPlayConnectionEvents.Join { _, _, _ ->
			// guard against this event being fired multiple times when "joining" different servers on a server network
			if(!ChatConnection.isConnected) {
				ChatConnection.attemptConnection()
			}
		})
		ClientPlayConnectionEvents.DISCONNECT.register(ClientPlayConnectionEvents.Disconnect { _, _ -> ChatConnection.disconnect() })
		ClientCommandRegistrationCallback.EVENT.register(ClientCommandRegistrationCallback { dispatcher: CommandDispatcher<FabricClientCommandSource>, _ ->
			SweatBridgeCommand.register(dispatcher)
		})
	}

	@JvmStatic
	fun isOnHypixel(): Boolean {
		val client = MinecraftClient.getInstance()
		if(client.isInSingleplayer || client.networkHandler == null) {
			return false
		}
		val serverInfo = client.networkHandler!!.serverInfo ?: return false
		return serverInfo.address.endsWith(".hypixel.net")
	}

	@JvmStatic
	fun getPrefix(): MutableText {
		return Text.empty()
			.append(Text.literal("Sweat").formatted(Formatting.BOLD).rgbColor(Config.Colors.prefix))
			.append(Text.literal(" > ").rgbColor(Config.Colors.arrow))
			.append(Text.empty().formatted(Formatting.RESET))
	}

	@JvmStatic
	fun send(component: Text?) {
		val client = MinecraftClient.getInstance()
		client.inGameHud.chatHud.addMessage(getPrefix().append(component))
	}

	@JvmStatic
	fun requireKey(ctx: CommandContext<FabricClientCommandSource>, hasKey: Runnable) {
		if(Config.token.isNullOrEmpty()) {
			ctx.source.sendFeedback(getPrefix().append(Text.literal("You do not have an API key set!").formatted(Formatting.RED)))
			return
		}
		hasKey.run()
	}

	@JvmStatic
	fun requireConnected(ctx: CommandContext<FabricClientCommandSource>, ifConnected: Runnable) {
		requireKey(ctx) {
			if(!Config.enabled) {
				ctx.source.sendFeedback(getPrefix().append(Text.literal("You have chat toggled off!").formatted(Formatting.RED)))
				return@requireKey
			} else if(!ChatConnection.isConnected) {
				ctx.source.sendFeedback(getPrefix().append(Text.literal("You are not connected to chat!").formatted(Formatting.RED)))
				return@requireKey
			}
			ifConnected.run()
		}
	}
}
