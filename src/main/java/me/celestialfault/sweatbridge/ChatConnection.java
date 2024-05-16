package me.celestialfault.sweatbridge;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.common.Loader;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.util.Optional;
import java.util.regex.Pattern;

public class ChatConnection extends WebSocketClient {

	private static final TypeAdapter<JsonObject> ADAPTER = new Gson().getAdapter(JsonObject.class);
	private static final String CLIENT_USERNAME = Minecraft.getMinecraft().getSession().getUsername();
	private static final Pattern USERNAME_REGEX = Pattern.compile(String.format("(\\b)(%s)(\\b)", CLIENT_USERNAME));
	private static final char FORMAT_CODE = '§';
	private static final String HOST = "wss://bridge.celestialfault.dev";
	private static ChatConnection INSTANCE;
	private boolean reconnecting = false;
	private int reconnectAttempts = 0;

	private ChatConnection() {
		super(getUri());
		addHeader("Api-Version", "1");
		addHeader("Minecraft-Version", Loader.MC_VERSION);
		addHeader("Mod-Version", SweatBridge.VERSION);
	}

	public static URI getUri() {
		String uri = HOST + "/ws/" + CLIENT_USERNAME + '/' + Config.INSTANCE.token.get();
		return URI.create(uri);
	}

	public static boolean isConnected() {
		return INSTANCE != null && INSTANCE.isOpen();
	}

	public static void attemptConnection() {
		disconnect();
		Optional.ofNullable(getInstance()).ifPresent(ChatConnection::connect);
	}

	public static @Nullable ChatConnection getInstance() {
		String token = Config.INSTANCE.token.get();
		if(token == null || token.isEmpty() || !Config.INSTANCE.enabled.get()) {
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

	public static void requestOnline() {
		if(INSTANCE != null) {
			JsonObject data = new JsonObject();
			data.addProperty("type", "request_online");
			try {
				INSTANCE.send(ADAPTER.toJson(data));
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static void sendMessage(String message) {
		if(INSTANCE != null) {
			JsonObject data = new JsonObject();
			data.addProperty("type", "send");
			data.addProperty("data", message);
			try {
				INSTANCE.send(ADAPTER.toJson(data));
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private static boolean shouldPing(JsonObject data) {
		if(data.has("pings") && !data.get("pings").getAsBoolean()) {
			return false;
		}
		if(data.has("system") && data.get("system").getAsBoolean()) {
			return false;
		}
		return !data.has("author") || !data.get("author").getAsString().equalsIgnoreCase(CLIENT_USERNAME);
	}

	private static String format(JsonObject data) {
		char usernameColor = data.get("author").getAsString().startsWith("[DISCORD]")
				? Config.INSTANCE.discord.get() : Config.INSTANCE.username.get();

		String message = data.get("message").getAsString();
		if(data.has("system") && data.get("system").getAsBoolean()) {
			return message;
		}

		message = EnumChatFormatting.getTextWithoutFormattingCodes(message);
		if(shouldPing(data)) {
			message = USERNAME_REGEX.matcher(message).replaceAll("$1" + EnumChatFormatting.YELLOW + "$2" + EnumChatFormatting.RESET + "$3");
		}
		return "" + FORMAT_CODE + usernameColor + EnumChatFormatting.getTextWithoutFormattingCodes(data.get("author").getAsString())
				+ EnumChatFormatting.RESET + ": " + message;
	}

	private synchronized void delayedReconnect() {
		if(reconnecting) return;
		reconnecting = true;
		new Thread(() -> {
			try {
				Thread.sleep(500L * (Math.min(reconnectAttempts, 5) ^ 2));
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
		reconnectAttempts = 0;
	}

	@Override
	public void onMessage(String message) {
		if(this != INSTANCE) {
			SweatBridge.LOGGER.warn("Closing orphaned connection!");
			this.close();
			return;
		}

		JsonObject data;
		try {
			data = ADAPTER.fromJson(message);
		} catch(IOException e) {
			SweatBridge.LOGGER.warn("Failed to decode message {}", message);
			return;
		}
		SweatBridge.send(ForgeHooks.newChatWithLinks(format(data)));

		Minecraft client = Minecraft.getMinecraft();
		if(client.thePlayer != null && shouldPing(data) && USERNAME_REGEX.matcher(data.get("message").getAsString()).find()) {
			client.thePlayer.playSound("random.successful_hit", 1f, 1f);
		}
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		if(reason.contains("403 Forbidden")) {
			SweatBridge.send(EnumChatFormatting.RED + "Chat key is invalid!" + EnumChatFormatting.RESET + " Set a new one with /ssckey!");
			SweatBridge.LOGGER.warn("Token is invalid, resetting in config");
			Config.INSTANCE.token.set(null);
			try {
				Config.INSTANCE.save();
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
			INSTANCE = null;
			return;
		}
		if(code != 1000) {
			if(reconnectAttempts++ == 0) {
				SweatBridge.send(EnumChatFormatting.GRAY + "Disconnected from chat, attempting to reconnect...");
			}
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
