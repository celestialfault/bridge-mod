package me.celestialfault.sweatbridge.commands;

import me.celestialfault.sweatbridge.ChatConnection;
import me.celestialfault.sweatbridge.Config;
import me.celestialfault.sweatbridge.SweatBridge;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumChatFormatting;

import java.util.Objects;

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
		if(args.length != 1) {
			SweatBridge.send(EnumChatFormatting.RED + "");
			return;
		}

		if(ChatConnection.isConnected()) {
			ChatConnection.disconnect();
		}
		Config.TOKEN = args[0];
		Config.save();
		SweatBridge.send("Key set, attempting to reconnect...");
		Objects.requireNonNull(ChatConnection.getInstance()).connect();
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		return true;
	}
}
