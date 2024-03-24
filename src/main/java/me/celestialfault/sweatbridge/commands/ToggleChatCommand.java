package me.celestialfault.sweatbridge.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

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
		SSCCommand.requireKey(SweatBridgeCommand::toggle);
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		return true;
	}
}
