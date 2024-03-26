package me.celestialfault.sweatbridge;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.util.Optional;
import java.util.regex.Pattern;

@SuppressWarnings("DataFlowIssue")
public class ChatConnection extends WebSocketClient {

	private static final TypeAdapter<JsonObject> ADAPTER = new Gson().getAdapter(JsonObject.class);
	private static final String CLIENT_USERNAME = MinecraftClient.getInstance().getSession().getUsername();
	private static final Pattern USERNAME_REGEX = Pattern.compile(String.format("(\\b)(%s)(\\b)", CLIENT_USERNAME));
	private static final String HOST = "wss://sweatbridge.odinair.xyz";
	private static ChatConnection INSTANCE;
	private boolean reconnecting = false;
	private int reconnectAttempts = 0;

	private ChatConnection() {
		super(getUri());
		addHeader("Api-Version", "1");
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
			INSTANCE.send(ADAPTER.toJson(data));
		}
	}

	public static void sendMessage(String message) {
		if(INSTANCE != null) {
			JsonObject data = new JsonObject();
			data.addProperty("type", "send");
			data.addProperty("data", message);
			INSTANCE.send(ADAPTER.toJson(data));
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

	private static Text format(JsonObject data) {
		String message = data.get("message").getAsString();
		if(data.has("system") && data.get("system").getAsBoolean()) {
			return Text.literal(message);
		}
		String author = data.get("author").getAsString();
		Formatting usernameColor = author.startsWith("[DISCORD]") ? Config.INSTANCE.colors.discord.get()
			: Config.INSTANCE.colors.username.get();

		if(shouldPing(data)) {
			message = USERNAME_REGEX.matcher(message).replaceAll("$1§e$2§r$3");
		}
		return Text.empty()
			.append(Text.literal(author).formatted(usernameColor))
			.append(Text.literal(": "))
			.append(Text.literal(message));
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
		SweatBridge.send(Text.literal("Connected to chat").formatted(Formatting.GRAY));
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
		// TODO does fabric/vanilla have a similar parse links util to the one in ForgeHooks?
		SweatBridge.send(format(data));

		MinecraftClient client = MinecraftClient.getInstance();
		if(client.player != null && shouldPing(data) && USERNAME_REGEX.matcher(data.get("message").getAsString()).find()) {
			client.player.playSound(SoundEvents.ENTITY_ARROW_HIT_PLAYER, 1f, 1f);
		}
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		if(reason.contains("403 Forbidden")) {
			SweatBridge.send(Text.empty()
				.append(Text.literal("Chat key is invalid!").formatted(Formatting.RED))
				.append("Set a new one with ")
				.append(Text.literal("/ssckey").formatted(Formatting.YELLOW)));
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
				SweatBridge.send(Text.literal("Disconnected from chat, attempting to reconnect...").formatted(Formatting.GRAY));
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
