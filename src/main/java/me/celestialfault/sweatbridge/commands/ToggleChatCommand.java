package me.celestialfault.sweatbridge.commands;

import me.celestialfault.sweatbridge.ChatConnection;
import me.celestialfault.sweatbridge.Config;
import me.celestialfault.sweatbridge.SweatBridge;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumChatFormatting;

public class ToggleChatCommand extends CommandBase {
	@Override
	public String getCommandName() {
		return "ssctoggle";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/ssctoggle";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		if(Config.ENABLED) {
			Config.ENABLED = false;
			ChatConnection.disconnect();
			SweatBridge.send("Toggled chat " + EnumChatFormatting.RED + "off" + EnumChatFormatting.RESET + ".");
		} else {
			Config.ENABLED = true;
			ChatConnection connection = ChatConnection.getInstance();
			if(connection != null) connection.connect();
			SweatBridge.send("Toggled chat " + EnumChatFormatting.GREEN + "on" + EnumChatFormatting.RESET + ".");
		}
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		return true;
	}
}
