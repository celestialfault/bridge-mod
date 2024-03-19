package me.celestialfault.sweatbridge.mixin;

import me.celestialfault.sweatbridge.ChatConnection;
import me.celestialfault.sweatbridge.SweatBridge;
import net.minecraft.client.entity.EntityPlayerSP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerSP.class)
public abstract class ClientPlayerMixin {
	@Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
	public void sweatbridge$interceptSentMessage(String message, CallbackInfo ci) {
		if(!message.startsWith("/") && SweatBridge.SEND_IN_CHAT && ChatConnection.isConnected()) {
			ci.cancel();
			ChatConnection.sendMessage(message);
		}
	}
}
