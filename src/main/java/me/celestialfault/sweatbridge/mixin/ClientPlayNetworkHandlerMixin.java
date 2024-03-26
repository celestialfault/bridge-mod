package me.celestialfault.sweatbridge.mixin;

import me.celestialfault.sweatbridge.ChatConnection;
import me.celestialfault.sweatbridge.SweatBridge;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
	@Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
	public void sweatbridge$interceptSentMessage(String message, CallbackInfo ci) {
		if(SweatBridge.SEND_IN_CHAT) {
			if(ChatConnection.isConnected()) {
				ChatConnection.sendMessage(message);
			} else {
				SweatBridge.send(Text.literal("You are not connected to chat!").formatted(Formatting.RED));
			}
			ci.cancel();
		}
	}

	@Inject(method = "sendChatCommand", at = @At("HEAD"), cancellable = true)
	public void sweatbridge$onSentCommand(String command, CallbackInfo ci) {
		if(!SweatBridge.isOnHypixel()) {
			return;
		}

		String[] parts = command.split(" ");
		if(parts[0].equalsIgnoreCase("chat") && parts.length > 1) {
			if(parts[1].equalsIgnoreCase("ssc")) {
				SweatBridge.SEND_IN_CHAT = true;
				ci.cancel();
			} else {
				SweatBridge.SEND_IN_CHAT = false;
			}
		}
	}
}
