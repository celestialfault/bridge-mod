package me.celestialfault.sweatbridge.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class ColorCommand extends CommandBase {
	@Override
	public String getCommandName() {
		return "ssccolor";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/ssccolor <prefix|arrow|username|discord> <0-9|a-f>";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		SSCCommand.setColor(args);
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		return true;
	}
}
