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
		if(args.length == 0) {
			SweatBridge.send(EnumChatFormatting.RED + "Missing a message to send!");
			return;
		}

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

		ChatConnection connection = ChatConnection.getInstance();
		if(connection != null) {
			connection.send(StringUtils.join(args, " "));
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
