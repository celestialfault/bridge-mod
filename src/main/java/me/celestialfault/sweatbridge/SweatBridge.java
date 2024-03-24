package me.celestialfault.sweatbridge;

import me.celestialfault.sweatbridge.commands.*;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = "sweatbridge", useMetadata = true)
public class SweatBridge {

    public static final char FORMAT_CODE = '§';
    public static final Logger LOGGER = LogManager.getLogger();
    public static boolean SEND_IN_CHAT = false;

    @SuppressWarnings("unused")
    @Mod.EventHandler
    public void init(FMLInitializationEvent ignored) {
        MinecraftForge.EVENT_BUS.register(this);
        Config.load();
		ClientCommandHandler.instance.registerCommand(new SSCCommand());
		// legacy stub commands
        ClientCommandHandler.instance.registerCommand(new SetKeyCommand());
        ClientCommandHandler.instance.registerCommand(new ToggleChatCommand());
        ClientCommandHandler.instance.registerCommand(new ColorCommand());
        ClientCommandHandler.instance.registerCommand(new OnlineCommand());
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public void onJoin(FMLNetworkEvent.ClientConnectedToServerEvent ignored) {
        ChatConnection connection = ChatConnection.getInstance();
        if(connection != null) connection.connect();
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public void onLeave(FMLNetworkEvent.ClientDisconnectionFromServerEvent ignored) {
        ChatConnection.disconnect();
    }

    public static String getPrefix() {
        return "" + FORMAT_CODE + Config.PREFIX_COLOR + EnumChatFormatting.BOLD + "Sweat"
                + FORMAT_CODE + Config.ARROW_COLOR + " > "
                + EnumChatFormatting.RESET;
    }

	public static void send(boolean prefix, ChatComponentText component) {
		Minecraft client = Minecraft.getMinecraft();
		if(client.thePlayer == null) {
			return;
		}
		if(prefix) {
			client.thePlayer.addChatMessage(new ChatComponentText(getPrefix()).appendSibling(component));
		} else {
			client.thePlayer.addChatMessage(component);
		}
	}

	public static void send(boolean prefix, String message) {
		send(prefix, new ChatComponentText(message));
	}

	public static void send(ChatComponentText component) {
		send(true, component);
	}

    public static void send(String message) {
        send(true, message);
    }
}
