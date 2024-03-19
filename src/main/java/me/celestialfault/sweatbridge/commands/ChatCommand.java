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

public class ChatCommand extends CommandBase {
	@Override
	public String getCommandName() {
		return "sweatchat";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/sweatchat <message>";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		if(Config.TOKEN == null || Config.TOKEN.isEmpty()) {
			SweatBridge.send(EnumChatFormatting.RED + "You do not have an API key set!");
			return;
		} else if(!Config.ENABLED) {
			SweatBridge.send(EnumChatFormatting.RED + "You have chat toggled off!");
			return;
		} else if(!ChatConnection.isConnected()) {
			SweatBridge.send(EnumChatFormatting.RED + "You are not connected to chat!");
			return;
		}

		if(args.length == 0) {
			SweatBridge.SEND_IN_CHAT = !SweatBridge.SEND_IN_CHAT;
			if(SweatBridge.SEND_IN_CHAT) {
				SweatBridge.send("Messages sent will now be sent in bridge chat; use this command again to toggle off");
			} else {
				SweatBridge.send("Messages will no longer be sent in bridge chat");
			}
		} else {
			ChatConnection.sendMessage(StringUtils.join(args, " "));
		}
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		return true;
	}

	@Override
	public List<String> getCommandAliases() {
		return Collections.singletonList("ssc");
	}
}
