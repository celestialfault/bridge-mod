package me.celestialfault.sweatbridge.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.CommandNode;
import me.celestialfault.sweatbridge.ChatConnection;
import me.celestialfault.sweatbridge.Config;
import me.celestialfault.sweatbridge.SweatBridge;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandSource;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class SweatBridgeCommand {
	@SuppressWarnings("CodeBlock2Expr")
	private static final SuggestionProvider<FabricClientCommandSource> MESSAGE_PARTS = (context, builder) -> {
		return CommandSource.suggestMatching(List.of("prefix", "arrow", "username", "discord"), builder);
	};
	private static final SuggestionProvider<FabricClientCommandSource> COLORS = (context, builder) -> {
		Stream<String> names = Arrays.stream(Formatting.values())
			.filter(Formatting::isColor)
			.map(x -> x.name().toLowerCase());
		return CommandSource.suggestMatching(names, builder);
	};

	public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
		CommandNode<FabricClientCommandSource> toggle = ClientCommandManager.literal("toggle")
			.executes(SweatBridgeCommand::toggle)
			.build();

		CommandNode<FabricClientCommandSource> online = ClientCommandManager.literal("online")
			.executes(SweatBridgeCommand::online)
			.build();

		CommandNode<FabricClientCommandSource> key = ClientCommandManager.literal("key")
			.then(ClientCommandManager.argument("key", StringArgumentType.string())
				.executes(SweatBridgeCommand::setKey))
			.build();

		CommandNode<FabricClientCommandSource> color = ClientCommandManager.literal("color")
			.then(ClientCommandManager.argument("part", StringArgumentType.string())
				.suggests(MESSAGE_PARTS)
				.then(ClientCommandManager.argument("color", StringArgumentType.string())
					.suggests(COLORS)
					.executes(SweatBridgeCommand::setColor)))
			.build();

		CommandNode<FabricClientCommandSource> root = dispatcher.register(ClientCommandManager.literal("sweatbridge")
			.then(key)
			.then(toggle)
			.then(online)
			.then(color));
		dispatcher.register(ClientCommandManager.literal("sweat").redirect(root));
	}

	private static int online(CommandContext<FabricClientCommandSource> ctx) {
		SSCCommand.requireConnected(ctx, ChatConnection::requestOnline);
		return 0;
	}

	@SuppressWarnings("DataFlowIssue")
	private static int toggle(CommandContext<FabricClientCommandSource> ctx) {
		if(Config.INSTANCE.enabled.get()) {
			Config.INSTANCE.enabled.set(false);
			SweatBridge.SEND_IN_CHAT = false;
			ChatConnection.disconnect();
			ctx.getSource().sendFeedback(SweatBridge.getPrefix()
				.append("Toggled chat ")
				.append("off").formatted(Formatting.RED)
				.append("."));
		} else {
			Config.INSTANCE.enabled.set(true);
			ChatConnection connection = ChatConnection.getInstance();
			if(connection != null) connection.connect();
			ctx.getSource().sendFeedback(SweatBridge.getPrefix()
				.append("Toggled chat ")
				.append("on").formatted(Formatting.GREEN)
				.append("."));
		}
		try {
			Config.INSTANCE.save();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		return 0;
	}

	private static int setKey(CommandContext<FabricClientCommandSource> ctx) {
		Config.INSTANCE.token.set(StringArgumentType.getString(ctx, "key"));
		try {
			Config.INSTANCE.save();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		ctx.getSource().sendFeedback(SweatBridge.getPrefix().append("Key set!"));
		ChatConnection.attemptConnection();
		return 0;
	}

	@SuppressWarnings("SameReturnValue")
	private static int setColor(CommandContext<FabricClientCommandSource> ctx) {
		String type = StringArgumentType.getString(ctx, "part");
		String colorName = StringArgumentType.getString(ctx, "color");
		Formatting color = Arrays.stream(Formatting.values())
			.filter(x -> x.name().equalsIgnoreCase(colorName))
			.findFirst()
			.orElse(null);
		if(color == null) {
			ctx.getSource().sendError(Text.literal(colorName + " is not a valid color"));
			return 0;
		}

		switch(type.toLowerCase()) {
			case "prefix":
				Config.INSTANCE.colors.prefix.set(color);
				break;
			case "arrow":
				Config.INSTANCE.colors.arrow.set(color);
				break;
			case "name":
			case "username":
				Config.INSTANCE.colors.username.set(color);
				break;
			case "discord":
				Config.INSTANCE.colors.discord.set(color);
				break;
			default:
				ctx.getSource().sendError(Text.literal(type + " is not a valid message part"));
				return 0;
		}
		try {
			Config.INSTANCE.save();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}

		Formatting previewUsername = type.equals("discord") ? Config.INSTANCE.colors.discord.get() : Config.INSTANCE.colors.username.get();
		Text preview = Text.empty()
			.append(SweatBridge.getPrefix())
			.append(Text.empty()
				.append(type.equals("discord") ? "[DISCORD] " : "").formatted(previewUsername)
				.append("Example").formatted(previewUsername))
			.append(": Hello!!");

		Text text = SweatBridge.getPrefix().append("Set ").append(type).append(" color ")
			.append(Text.literal("[PREVIEW]")
				.setStyle(Style.EMPTY
					.withBold(true)
					.withColor(Formatting.YELLOW)
					.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, preview))));

		ctx.getSource().sendFeedback(text);
		return 0;
	}
}
