package me.celestialfault.sweatbridge;

import me.celestialfault.sweatbridge.commands.ChatCommand;
import me.celestialfault.sweatbridge.commands.ColorCommand;
import me.celestialfault.sweatbridge.commands.SetKeyCommand;
import me.celestialfault.sweatbridge.commands.ToggleChatCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(modid = "sweatbridge", useMetadata = true)
public class SweatBridge {

    private static final char FORMAT_CODE = '§';
    public static final Logger LOGGER = LoggerFactory.getLogger(SweatBridge.class);

    @SuppressWarnings("unused")
    @Mod.EventHandler
    public void init(FMLInitializationEvent ignored) {
        MinecraftForge.EVENT_BUS.register(this);
        Config.load();
        ClientCommandHandler.instance.registerCommand(new ChatCommand());
        ClientCommandHandler.instance.registerCommand(new SetKeyCommand());
        ClientCommandHandler.instance.registerCommand(new ToggleChatCommand());
        ClientCommandHandler.instance.registerCommand(new ColorCommand());
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

    public static void send(String message) {
        Minecraft client = Minecraft.getMinecraft();
        if(client.thePlayer == null) {
            return;
        }
        client.thePlayer.addChatMessage(new ChatComponentText(getPrefix() + message));
    }
}
