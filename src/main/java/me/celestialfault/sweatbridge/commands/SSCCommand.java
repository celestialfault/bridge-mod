package me.celestialfault.sweatbridge.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.celestialfault.sweatbridge.ChatConnection;
import me.celestialfault.sweatbridge.Config;
import me.celestialfault.sweatbridge.SweatBridge;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class SSCCommand {
	@SuppressWarnings("DataFlowIssue")
	public static void requireKey(CommandContext<FabricClientCommandSource> ctx, Runnable hasKey) {
		if(Config.INSTANCE.token.get() == null || Config.INSTANCE.token.get().isEmpty()) {
			ctx.getSource().sendFeedback(SweatBridge.getPrefix()
				.append(Text.literal("You do not have an API key set!").formatted(Formatting.RED)));
			return;
		}
		hasKey.run();
	}

	@SuppressWarnings("DataFlowIssue")
	public static void requireConnected(CommandContext<FabricClientCommandSource> ctx, Runnable ifConnected) {
		requireKey(ctx, () -> {
			if(!Config.INSTANCE.enabled.get()) {
				ctx.getSource().sendFeedback(SweatBridge.getPrefix()
					.append(Text.literal("You have chat toggled off!").formatted(Formatting.RED)));
				return;
			} else if(!ChatConnection.isConnected()) {
				ctx.getSource().sendFeedback(SweatBridge.getPrefix()
					.append(Text.literal("You are not connected to chat!").formatted(Formatting.RED)));
				return;
			}
			ifConnected.run();
		});
	}

	public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
		dispatcher.register(ClientCommandManager.literal("ssc")
			.then(ClientCommandManager.argument("message", StringArgumentType.greedyString())
				.executes(ctx -> {
					String message = StringArgumentType.getString(ctx, "message");
					requireConnected(ctx, () -> ChatConnection.sendMessage(message));
					return 0;
				}))
			.executes(ctx -> {
				SweatBridge.SEND_IN_CHAT = !SweatBridge.SEND_IN_CHAT;
				if(SweatBridge.SEND_IN_CHAT) {
					ctx.getSource().sendFeedback(SweatBridge.getPrefix().append("Messages will now be sent in bridge chat; use this command again to toggle off"));
				} else {
					ctx.getSource().sendFeedback(SweatBridge.getPrefix().append("Messages will no longer be sent in bridge chat"));
				}
				return 0;
			}));
	}
}
