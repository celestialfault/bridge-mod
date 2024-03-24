package me.celestialfault.sweatbridge.commands;

import me.celestialfault.sweatbridge.ChatConnection;
import me.celestialfault.sweatbridge.Config;
import me.celestialfault.sweatbridge.SweatBridge;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.*;

public class SweatBridgeCommand extends CommandBase {
	private static final Map<String, String> COMMAND_HELP = new LinkedHashMap<>();

	static {
		COMMAND_HELP.put("/ssc", "Toggle sending messages in bridge chat");
		COMMAND_HELP.put("/ssc <message>", "Send a message in bridge chat");
		COMMAND_HELP.put("toggle", "Toggle if bridge chat should be visible");
		COMMAND_HELP.put("online", "List all players currently connected");
		COMMAND_HELP.put("color", "Set the color for a given chat component");
		COMMAND_HELP.put("key", "Set your bridge API key");
	}

	static String getHelpMessage() {
		StringBuilder builder = new StringBuilder();
		builder.append("§7§m-----------------§r§7[ §")
			.append(Config.PREFIX_COLOR)
			.append("Sweat Bridge §7]§m-----------------")
			.append('\n');

		for(Map.Entry<String, String> entry : COMMAND_HELP.entrySet()) {
			builder.append(SweatBridge.FORMAT_CODE)
				.append(Config.PREFIX_COLOR);
			if(!entry.getKey().isEmpty() && !entry.getKey().startsWith("/")) {
				builder.append("/sweat ");
			}
			if(!entry.getKey().isEmpty()) {
				builder.append(entry.getKey());
			}
			builder.append(" ")
				.append(SweatBridge.FORMAT_CODE)
				.append("7»")
				.append(EnumChatFormatting.RESET)
				.append(" ")
				.append(entry.getValue())
				.append('\n');
		}

		builder.append("§7§m-----------------------------------------------");
		return builder.toString();
	}

	static void toggle() {
		if(Config.ENABLED) {
			Config.ENABLED = false;
			SweatBridge.SEND_IN_CHAT = false;
			ChatConnection.disconnect();
			SweatBridge.send("Toggled chat " + EnumChatFormatting.RED + "off" + EnumChatFormatting.RESET + ".");
		} else {
			Config.ENABLED = true;
			ChatConnection connection = ChatConnection.getInstance();
			if(connection != null) connection.connect();
			SweatBridge.send("Toggled chat " + EnumChatFormatting.GREEN + "on" + EnumChatFormatting.RESET + ".");
		}
		Config.save();
	}

	static void setKey(String[] args) {
		if(args.length != 1) {
			SweatBridge.send("Usage: " + EnumChatFormatting.YELLOW + "/sweat key <key>");
			SweatBridge.send("Get an API key with /apikey in Discord!");
			return;
		}

		Config.TOKEN = args[0];
		Config.save();
		SweatBridge.send("Key set, attempting to reconnect...");
		ChatConnection.attemptConnection();
	}

	static void setColor(String[] args) {
		if(args.length != 2) {
			SweatBridge.send("Usage: " + EnumChatFormatting.YELLOW + "/sweat color <prefix/arrow/username/discord> <0-9/a-f>");
			SweatBridge.send("Example: " + EnumChatFormatting.YELLOW + "/sweat color prefix e" + EnumChatFormatting.RESET + " - sets the 'Sweat' prefix to yellow");
			return;
		}
		if(args[1].length() != 1) {
			SweatBridge.send("The provided color must be a single character color code, such as " + EnumChatFormatting.GREEN + "a");
			return;
		}

		String type = args[0].toLowerCase();
		char color = args[1].charAt(0);
		modifyPrefixColors(type, color);
	}

	private static void modifyPrefixColors(String type, char color) {
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

		@SuppressWarnings("StringBufferReplaceableByString")
		StringBuilder preview = new StringBuilder()
			.append(SweatBridge.getPrefix())
			.append(SweatBridge.FORMAT_CODE).append(type.equals("discord") ? Config.DISCORD_USERNAME_COLOR : Config.USERNAME_COLOR)
			.append(type.equals("discord") ? "[DISCORD] " : "")
			.append("Example")
			.append(EnumChatFormatting.RESET).append(": ")
			.append("Hello!!");

		ChatComponentText hover = new ChatComponentText("" + EnumChatFormatting.YELLOW + EnumChatFormatting.BOLD + "[PREVIEW]");
		hover.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(preview.toString())));
		ChatComponentText text = new ChatComponentText("Set " + type + " color ");
		text.appendSibling(hover);
		SweatBridge.send(text);
	}

	@Override
	public String getCommandName() {
		return "sweatbridge";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return getHelpMessage();
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		if(args.length == 0) {
			SweatBridge.send(false, getHelpMessage());
			return;
		}

		switch(args[0].toLowerCase()) {
			case "key":
				setKey(Arrays.copyOfRange(args, 1, args.length));
				break;
			case "toggle":
				SSCCommand.requireKey(SweatBridgeCommand::toggle);
				break;
			case "online":
				SSCCommand.requireConnected(ChatConnection::requestOnline);
				break;
			case "colour":
			case "color":
				setColor(Arrays.copyOfRange(args, 1, args.length));
				break;
			default:
				SweatBridge.send(false, getHelpMessage());
		}
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		return true;
	}

	@Override
	public List<String> getCommandAliases() {
		return Collections.singletonList("sweat");
	}
}
