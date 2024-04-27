package me.celestialfault.sweatbridge

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.TypeAdapter
import net.minecraft.client.MinecraftClient
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Util
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.ConnectException
import java.net.URI
import java.util.regex.Pattern
import kotlin.concurrent.thread

private val ADAPTER: TypeAdapter<JsonObject> = Gson().getAdapter(JsonObject::class.java)
private val CLIENT_USERNAME: String = MinecraftClient.getInstance().session.username
// TODO use kotlin Regex() instead?
private val USERNAME_REGEX = Pattern.compile("(\\b)(${CLIENT_USERNAME})(\\b)")
private val FORMATTING_CODE_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]")
private const val HOST = "wss://sweatbridge.odinair.xyz"
private val BACKOFF = ExponentialBackoff()
private var INSTANCE: ChatConnection? = null

class ChatConnection private constructor() : WebSocketClient(uri) {
	private var reconnecting = false

	init {
		addHeader("Api-Version", "1")
		addHeader("Minecraft-Version", SweatBridge.MINECRAFT_VERSION)
		addHeader("Mod-Version", SweatBridge.VERSION)
	}

	fun send(data: JsonObject) = send(ADAPTER.toJson(data))

	@Synchronized
	private fun delayedReconnect() {
		if(reconnecting) return
		reconnecting = true
		Util.getMainWorkerExecutor().submit {
			try {
				val delay = BACKOFF.delay
				SweatBridge.LOGGER.info("Attempting to reconnect in {}s", delay / 1000.0)
				Thread.sleep(delay)
			} catch(e: InterruptedException) {
				throw RuntimeException(e)
			}
			reconnecting = false
			reconnect()
		}
	}

	override fun onOpen(handshake: ServerHandshake) {
		SweatBridge.send(Text.literal("Connected to chat").formatted(Formatting.GRAY))
	}

	override fun onMessage(message: String) {
		if(this !== INSTANCE) {
			SweatBridge.LOGGER.warn("Closing orphaned connection!")
			close()
			return
		}

		val data: JsonObject = runCatching {
			ADAPTER.fromJson(message)
		}.getOrNull() ?: return run {
			SweatBridge.LOGGER.warn("Failed to decode message {}", message)
		}
		// TODO does fabric/vanilla have a similar parse links util to the one in ForgeHooks?
		SweatBridge.send(format(data))

		val client = MinecraftClient.getInstance()
		if(client.player != null && shouldPing(data) && USERNAME_REGEX.matcher(data["message"].asString).find()) {
			client.player!!.playSound(SoundEvents.ENTITY_ARROW_HIT_PLAYER, 1f, 1f)
		}
	}

	override fun onClose(code: Int, reason: String?, remote: Boolean) {
		if(reason != null && reason.contains("403 Forbidden")) {
			SweatBridge.send(
				Text.empty()
					.append(Text.literal("Chat key is invalid! ").formatted(Formatting.RED))
					.append("Set a new one with ")
					.append(Text.literal("/sweat key").formatted(Formatting.YELLOW)))
			SweatBridge.LOGGER.warn("Token is invalid, resetting in config")
			Config.token = null
			Config.save()
			INSTANCE = null
		} else if(code != 1000) {
			if(BACKOFF.count == 0) {
				SweatBridge.send(Text.literal("Disconnected from chat, attempting to reconnect...").formatted(Formatting.GRAY))
			}
			SweatBridge.LOGGER.warn("Disconnected from chat ({}: {})", code, reason)
			delayedReconnect()
		}
	}

	override fun onError(ex: Exception) {
		if (ex is ConnectException) {
			delayedReconnect()
		} else {
			SweatBridge.LOGGER.error("Encountered unhandled websocket error", ex)
		}
	}

	companion object {
		val uri: URI
			get() = URI.create(HOST + "/ws/" + CLIENT_USERNAME + '/' + Config.token!!)

		@JvmStatic
		val isConnected: Boolean
			get() = INSTANCE != null && INSTANCE!!.isOpen

		val instance: ChatConnection?
			get() {
				if(Config.token.isNullOrEmpty() || !Config.enabled) return null
				return INSTANCE ?: ChatConnection().also { INSTANCE = it }
			}

		fun attemptConnection() {
			disconnect()
			instance?.connect()
		}

		fun disconnect() {
			INSTANCE?.close()?.also { INSTANCE = null }
		}

		fun requestOnline() {
			INSTANCE?.send(JsonObject().apply {
				addProperty("type", "request_online")
			})
		}

		@JvmStatic
		fun sendMessage(message: String?) {
			INSTANCE?.send(JsonObject().apply {
				addProperty("type", "send")
				addProperty("data", message)
			})
		}

		private fun shouldPing(data: JsonObject): Boolean {
			return data["pings"]?.asBoolean != true && data["system"]?.asBoolean != true
				&& (!data.has("author") || !data["author"].asString.equals(CLIENT_USERNAME, ignoreCase = true))
		}

		private fun format(data: JsonObject): Text {
			var message = data["message"].asString
			if(data["system"]?.asBoolean == true) {
				return Text.literal(message)
			}
			message = FORMATTING_CODE_PATTERN.matcher(message).replaceAll("")
			val author = data["author"].asString
			val usernameColor: Int = if (author.startsWith("[DISCORD]")) Config.Colors.discord
			else Config.Colors.username

			if(shouldPing(data)) {
				message = USERNAME_REGEX.matcher(message).replaceAll("$1§e$2§r$3")
			}
			return Text.empty()
				.append(Text.literal(author).rgbColor(usernameColor))
				.append(Text.literal(": "))
				.append(Text.literal(message))
		}
	}
}
