package me.celestialfault.sweatbridge

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.brigadier.tree.CommandNode
import me.celestialfault.sweatbridge.ChatConnection.Companion.sendMessage
import me.celestialfault.sweatbridge.SweatBridge.getPrefix
import me.celestialfault.sweatbridge.SweatBridge.requireConnected
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.command.CommandSource
import net.minecraft.text.HoverEvent
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Util
import java.util.*

@Suppress("SameReturnValue")
object SweatBridgeCommand {
	private val MESSAGE_PARTS =
		SuggestionProvider { _: CommandContext<FabricClientCommandSource>, builder: SuggestionsBuilder ->
			CommandSource.suggestMatching(listOf("prefix", "arrow", "username", "discord"), builder)
		}
	private val COLORS =
		SuggestionProvider { _: CommandContext<FabricClientCommandSource>, builder: SuggestionsBuilder ->
			val names = Formatting.entries.toTypedArray()
				.filter { obj: Formatting -> obj.isColor }
				.map { it.name.lowercase() }
			CommandSource.suggestMatching(names, builder)
		}

	fun register(dispatcher: CommandDispatcher<FabricClientCommandSource>) {
		val toggle: CommandNode<FabricClientCommandSource> = ClientCommandManager.literal("toggle")
			.executes(this::toggle)
			.build()

		val online: CommandNode<FabricClientCommandSource> = ClientCommandManager.literal("online")
			.executes(this::online)
			.build()

		val key: CommandNode<FabricClientCommandSource> = ClientCommandManager.literal("key")
			.then(ClientCommandManager.argument("key", StringArgumentType.string())
				.executes(this::setKey))
			.build()

		val color: CommandNode<FabricClientCommandSource> = ClientCommandManager.literal("color")
			.then(ClientCommandManager.argument("part", StringArgumentType.string())
				.suggests(MESSAGE_PARTS)
				.then(ClientCommandManager.argument("color", StringArgumentType.string())
					.suggests(COLORS)
					.executes(this::setColor)))
			.build()

		dispatcher.register(ClientCommandManager.literal(Config.Commands.config)
			.then(key)
			.then(toggle)
			.then(online)
			.then(color)
			.also {
				if(FabricLoader.getInstance().isModLoaded("yet_another_config_lib_v3")) {
					it.executes(this::openConfig)
				}
			}
		)

		dispatcher.register(ClientCommandManager.literal(Config.Commands.chat)
			.then(ClientCommandManager.argument("message", StringArgumentType.greedyString())
				.executes(this::send)
			)
			.executes(this::toggleSend))
	}

	@Suppress("UNUSED_PARAMETER")
	private fun openConfig(ctx: CommandContext<FabricClientCommandSource>): Int {
		val screen = ModMenuImpl.buildConfigScreen(null)
		// why must opening a screen from a chat command be so obnoxious?
		Util.getMainWorkerExecutor().submit {
			// wait for the next tick (or roughly around there, at least)
			Thread.sleep(50L)
			MinecraftClient.getInstance().submit {
				MinecraftClient.getInstance().setScreen(screen)
			}
		}
		return 0
	}

	private fun toggleSend(ctx: CommandContext<FabricClientCommandSource>): Int {
		SweatBridge.sendInChat = !SweatBridge.sendInChat
		if(SweatBridge.sendInChat) {
			ctx.source.sendFeedback(getPrefix().append("Messages will now be sent in bridge chat; use this command again to toggle off"))
		} else {
			ctx.source.sendFeedback(getPrefix().append("Messages will no longer be sent in bridge chat"))
		}
		return 0
	}

	private fun send(ctx: CommandContext<FabricClientCommandSource>): Int {
		val message = StringArgumentType.getString(ctx, "message")
		requireConnected(ctx) { sendMessage(message) }
		return 0
	}

	private fun online(ctx: CommandContext<FabricClientCommandSource>): Int {
		requireConnected(ctx, ChatConnection::requestOnline)
		return 0
	}

	private fun toggle(ctx: CommandContext<FabricClientCommandSource>): Int {
		if(Config.enabled) {
			Config.enabled = false
			SweatBridge.sendInChat = false
			ChatConnection.disconnect()
			ctx.source.sendFeedback(
				getPrefix()
					.append("Toggled chat ")
					.append(Text.literal("off").formatted(Formatting.RED))
					.append(".")
			)
		} else {
			Config.enabled = true
			ChatConnection.instance?.connect()
			ctx.source.sendFeedback(getPrefix()
				.append("Toggled chat ")
				.append(Text.literal("on").formatted(Formatting.GREEN))
				.append("."))
		}
		Config.save()
		return 0
	}

	private fun setKey(ctx: CommandContext<FabricClientCommandSource>): Int {
		Config.token = StringArgumentType.getString(ctx, "key")
		Config.save()
		ctx.source.sendFeedback(getPrefix().append("Key set!"))
		ChatConnection.attemptConnection()
		return 0
	}

	private fun setColor(ctx: CommandContext<FabricClientCommandSource>): Int {
		val type = StringArgumentType.getString(ctx, "part")
		val colorName = StringArgumentType.getString(ctx, "color")
		val color = Formatting.entries.toTypedArray().asSequence().firstOrNull { it.name.equals(colorName, ignoreCase = true) }
		if(color == null) {
			ctx.source.sendError(Text.literal("$colorName is not a valid color"))
			return 0
		}

		when(type.lowercase()) {
			"prefix" -> Config.Colors.prefix = color.colorValue!!
			"arrow" -> Config.Colors.arrow = color.colorValue!!
			"name", "username" -> Config.Colors.username = color.colorValue!!
			"discord" -> Config.Colors.discord = color.colorValue!!
			else -> {
				ctx.source.sendError(Text.literal("$type is not a valid message part"))
				return 0
			}
		}
		Config.save()

		val previewUsername: Int =
			if (type == "discord") Config.Colors.discord else Config.Colors.username
		val preview: Text = Text.empty()
			.append(getPrefix())
			.append(
				Text.empty()
					.append(if (type == "discord") "[DISCORD] " else "").rgbColor(previewUsername)
					.append("Example").rgbColor(previewUsername))
			.append(": Hello!!")

		val text: Text = getPrefix().append("Set ").append(type).append(" color ").append(
			Text.literal("[PREVIEW]").setStyle(
				Style.EMPTY
					.withBold(true)
					.withColor(Formatting.YELLOW)
					.withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, preview))))

		ctx.source.sendFeedback(text)
		return 0
	}
}
