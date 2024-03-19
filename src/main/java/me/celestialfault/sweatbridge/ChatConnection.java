package me.celestialfault.sweatbridge;

import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;

public class ChatConnection extends WebSocketClient {

	private static ChatConnection INSTANCE;
	private static final String HOST = "ws://localhost:8000";
	private boolean reconnecting = false;

	private ChatConnection() {
		super(getUri());
	}

	public static URI getUri() {
		String uri = HOST + "/ws/" + Minecraft.getMinecraft().getSession().getUsername() + '/' + Config.TOKEN;
		return URI.create(uri);
	}

	public static boolean isConnected() {
		return INSTANCE != null && INSTANCE.isOpen();
	}

	public static @Nullable ChatConnection getInstance() {
		if(Config.TOKEN == null || Config.TOKEN.isEmpty() || !Config.ENABLED) {
			return null;
		}

		if(INSTANCE == null) {
			INSTANCE = new ChatConnection();
		}
		return INSTANCE;
	}

	public static void disconnect() {
		if(INSTANCE != null) {
			INSTANCE.close();
			INSTANCE = null;
		}
	}

	private static String format(JsonObject data) {
		return EnumChatFormatting.GREEN + EnumChatFormatting.getTextWithoutFormattingCodes(data.get("author").getAsString()) + ": "
				+ EnumChatFormatting.RESET + EnumChatFormatting.getTextWithoutFormattingCodes(data.get("message").getAsString());
	}

	private synchronized void delayedReconnect() {
		if(reconnecting) return;
		reconnecting = true;
		new Thread(() -> {
			try {
				Thread.sleep(2000L);
			} catch(InterruptedException e) {
				throw new RuntimeException(e);
			}

			reconnecting = false;
			reconnect();
		}).start();
	}

	@Override
	public void onOpen(ServerHandshake handshake) {
		SweatBridge.send(EnumChatFormatting.GRAY + "Connected to chat");
	}

	@Override
	public void onMessage(String message) {
		JsonObject data;
		try {
			data = Config.ADAPTER.fromJson(message);
		} catch(IOException e) {
			SweatBridge.LOGGER.warn("Failed to decode message {}", message);
			return;
		}
		SweatBridge.send(format(data));
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		if(code == 1002) {
			SweatBridge.send(EnumChatFormatting.RED + "Chat key is invalid!" + EnumChatFormatting.RESET + " Set a new one with /ssckey!");
			SweatBridge.LOGGER.warn("Token is invalid, resetting in config");
			Config.TOKEN = null;
			Config.save();
			INSTANCE = null;
			return;
		}
		if(code != 1000) {
			SweatBridge.LOGGER.warn("Disconnected from chat");
			delayedReconnect();
		}
	}

	@Override
	public void onError(Exception ex) {
		if(ex instanceof ConnectException) {
			delayedReconnect();
		} else {
			SweatBridge.LOGGER.error("Encountered unhandled websocket error", ex);
		}
	}
}
