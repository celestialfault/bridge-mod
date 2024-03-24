package me.celestialfault.sweatbridge.mixin;

import me.celestialfault.sweatbridge.ChatConnection;
import me.celestialfault.sweatbridge.SweatBridge;
import me.celestialfault.sweatbridge.commands.SSCCommand;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.EnumChatFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerSP.class)
public abstract class ClientPlayerMixin {
	@Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
	public void sweatbridge$interceptSentMessage(String message, CallbackInfo ci) {
		if(!message.startsWith("/") && SweatBridge.SEND_IN_CHAT) {
			SSCCommand.requireConnected(() -> ChatConnection.sendMessage(message));
			ci.cancel();
		} else if(message.startsWith("/chat ")) {
			if(message.equalsIgnoreCase("/chat ssc")) {
				SweatBridge.SEND_IN_CHAT = true;
				SweatBridge.send("Messages sent will now be sent in bridge chat");
				ci.cancel();
			} else {
				SweatBridge.SEND_IN_CHAT = false;
			}
		}
	}
}
