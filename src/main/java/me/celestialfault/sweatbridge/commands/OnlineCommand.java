package me.celestialfault.sweatbridge.commands;

import me.celestialfault.sweatbridge.ChatConnection;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class OnlineCommand extends CommandBase {
	@Override
	public String getCommandName() {
		return "ssconline";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/ssconline";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		SSCCommand.requireConnected(ChatConnection::requestOnline);
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		return true;
	}
}
