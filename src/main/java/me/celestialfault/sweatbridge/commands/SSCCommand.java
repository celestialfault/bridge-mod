package me.celestialfault.sweatbridge.commands;

import me.celestialfault.sweatbridge.ChatConnection;
import me.celestialfault.sweatbridge.Config;
import me.celestialfault.sweatbridge.SweatBridge;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumChatFormatting;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;

public class SSCCommand extends CommandBase {

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		return true;
	}

    @Override
    public String getCommandName() {
        return "sweatchat";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return SweatBridgeCommand.getHelpMessage();
    }

	@SuppressWarnings("DataFlowIssue")
	public static void requireKey(Runnable hasKey) {
		if(Config.INSTANCE.token.get() == null || Config.INSTANCE.token.get().isEmpty()) {
			SweatBridge.send(EnumChatFormatting.RED + "You do not have an API key set!");
			return;
		}
		hasKey.run();
	}

	@SuppressWarnings("DataFlowIssue")
	public static void requireConnected(Runnable ifConnected) {
		requireKey(() -> {
			if(!Config.INSTANCE.enabled.get()) {
				SweatBridge.send(EnumChatFormatting.RED + "You have chat toggled off!");
				return;
			} else if(!ChatConnection.isConnected()) {
				SweatBridge.send(EnumChatFormatting.RED + "You are not connected to chat!");
				return;
			}
			ifConnected.run();
		});
	}

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		if(args.length == 0) {
			SweatBridge.SEND_IN_CHAT = !SweatBridge.SEND_IN_CHAT;
			if(SweatBridge.SEND_IN_CHAT) {
				SweatBridge.send("Messages sent will now be sent in bridge chat; use this command again to toggle off");
			} else {
				SweatBridge.send("Messages will no longer be sent in bridge chat");
			}
			return;
		}
	    requireConnected(() -> ChatConnection.sendMessage(StringUtils.join(args, " ")));
    }

	@Override
	public List<String> getCommandAliases() {
		return Collections.singletonList("ssc");
	}
}
