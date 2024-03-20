package me.celestialfault.sweatbridge.commands;

import me.celestialfault.sweatbridge.ChatConnection;
import me.celestialfault.sweatbridge.Config;
import me.celestialfault.sweatbridge.SweatBridge;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumChatFormatting;

import java.util.Objects;

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
		ChatConnection.requestOnline();
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		return true;
	}
}
