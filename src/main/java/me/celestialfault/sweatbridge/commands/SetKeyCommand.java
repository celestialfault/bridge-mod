package me.celestialfault.sweatbridge.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class SetKeyCommand extends CommandBase {
	@Override
	public String getCommandName() {
		return "ssckey";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/ssckey <key>";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		SSCCommand.setKey(args);
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		return true;
	}
}
