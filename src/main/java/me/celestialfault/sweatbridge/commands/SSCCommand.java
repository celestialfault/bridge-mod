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
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SSCCommand extends CommandBase {
    final List<String> COMMANDS = Arrays.asList("help", "toggle", "key", "online", "color");

    final String HELP_MESSAGE = "§7§m-----------------§r§7[ §" + Config.PREFIX_COLOR + "Sweat Bridge §7]§m-----------------\n" +
            "§e/ssc <message> §8-> §fSend message in SSC.\n" +
            "§e/ssc help §8-> §fSends this help message.\n" +
            "§e/ssc toggle §8-> §fToggle SSC chat on or off.\n" +
            "§e/chat ssc §8-> §fAllows to send messages without having to /ssc\n" + // temp desc
            "§e/ssc key §8-> §fUse this command with a key obtained from /apikey in Discord.\n" +
            "§e/ssc online §8-> §fList current online SSC users.\n" +
            "§e/ssc color §8-> §fModify colors of SSC message.\n" +
            "§7§m-----------------------------------------------";

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		return true;
	}

    @Override
    public String getCommandName() {
        return "ssc";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/ssc help";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		if(args.length == 0) {
			SweatBridge.SEND_IN_CHAT = !SweatBridge.SEND_IN_CHAT;
			if(SweatBridge.SEND_IN_CHAT) {
				SweatBridge.send("Messages sent will now be sent in bridge chat; use this command again to toggle off");
			} else {
				SweatBridge.send("Messages will no longer be sent in bridge chat");
			}
		} else if(!COMMANDS.contains(args[0].toLowerCase())) {
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
            ChatConnection.sendMessage(StringUtils.join(args, " "));
        } else {
            switch(args[0].toLowerCase()) {
                case "help":
                    SweatBridge.send(false, HELP_MESSAGE);
                    break;
                case "key":
                    if(args.length != 2) {
                        SweatBridge.send(EnumChatFormatting.RED + "Use this command with a key obtained from /apikey in Discord!");
                        return;
                    }

                    if(!Config.ENABLED) {
                        SweatBridge.send(EnumChatFormatting.RED + "You have chat toggled off! Toggle it on with '/ssc toggle' before using this command!");
                        return;
                    }

                    if(ChatConnection.isConnected()) {
                        ChatConnection.disconnect();
                    }
                    Config.TOKEN = args[1];
                    Config.save();
                    SweatBridge.send("Key set, attempting to reconnect...");
                    Objects.requireNonNull(ChatConnection.getInstance()).connect();
                    break;
                case "toggle":
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
                    break;
                case "online":
                    ChatConnection.requestOnline();
                    break;
                case "color":
					if(args.length != 3) {
						SweatBridge.send(EnumChatFormatting.RED + "/ssc color <prefix|arrow|username|discord> <0-9|a-f>");
						return;
					}
					if(args[2].length() != 1) {
						SweatBridge.send(EnumChatFormatting.RED + "The provided color must be exactly 1 character!");
						return;
					}
					String type = args[1].toLowerCase();
					char color = args[2].charAt(0);

					modifyPrefixColors(type, color);
                    break;
            }
        }
    }

	public void modifyPrefixColors(String type, char color) {
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

		ChatComponentText hover = new ChatComponentText(EnumChatFormatting.GRAY + "[PREVIEW]");
		hover.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(SweatBridge.getPrefix())));
		ChatComponentText text = new ChatComponentText(SweatBridge.getPrefix() + "Set " + type + " color, ");
		text.appendSibling(hover);
		SweatBridge.send(String.valueOf(text));
	}
}
