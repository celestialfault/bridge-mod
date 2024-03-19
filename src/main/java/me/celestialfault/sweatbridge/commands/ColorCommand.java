package me.celestialfault.sweatbridge.commands;

import me.celestialfault.sweatbridge.Config;
import me.celestialfault.sweatbridge.SweatBridge;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumChatFormatting;

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
		if(args.length != 2) {
			SweatBridge.send(EnumChatFormatting.RED + getCommandUsage(sender));
			return;
		}
		if(args[1].length() != 1) {
			SweatBridge.send(EnumChatFormatting.RED + "The provided color must be exactly 1 character!");
			return;
		}
		String type = args[0].toLowerCase();
		char color = args[1].charAt(0);

		switch(type) {
			case "prefix":
				Config.PREFIX_COLOR = color;
				break;
			case "arrow":
				Config.ARROW_COLOR = color;
				break;
			case "name":
			case "username":
				Config.USERNAME_COLOR = color;
				break;
			case "discord":
				Config.DISCORD_USERNAME_COLOR = color;
				break;
			default:
				SweatBridge.send(EnumChatFormatting.RED + "Expected one of either prefix, arrow, username, or discord; instead got " + type);
				return;
		}
		Config.save();
		SweatBridge.send("Set " + type + " color");
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		return true;
	}
}
