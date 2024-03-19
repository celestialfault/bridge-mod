package me.celestialfault.sweatbridge;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonWriter;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.function.Supplier;

public final class Config {
	private Config() {
		throw new AssertionError("Config is intended to be used in a static context");
	}

	static final TypeAdapter<JsonObject> ADAPTER = new Gson().getAdapter(JsonObject.class);
	private static final Path PATH = Minecraft.getMinecraft().mcDataDir.toPath().toAbsolutePath()
			.resolve("config").resolve("sweat-bridge.json");

	public static String TOKEN;
	public static boolean ENABLED = true;
	public static char PREFIX_COLOR = 'e';
	public static char ARROW_COLOR = '6';
	public static char USERNAME_COLOR = 'a';
	public static char DISCORD_USERNAME_COLOR = 'a';

	public static void load() {
		File configFile = PATH.toFile();
		if(!configFile.exists()) {
			save();
			return;
		}
		try(FileReader reader = new FileReader(configFile)) {
			JsonObject obj = ADAPTER.fromJson(reader);
			TOKEN = read(obj, "token", JsonElement::getAsString, null);
			ENABLED = read(obj, "enabled", JsonElement::getAsBoolean, true);
			PREFIX_COLOR = read(obj, "prefix", JsonElement::getAsCharacter, 'e');
			ARROW_COLOR = read(obj, "arrow", JsonElement::getAsCharacter, '6');
			USERNAME_COLOR = read(obj, "username", JsonElement::getAsCharacter, 'a');
			DISCORD_USERNAME_COLOR = read(obj, "discord_username", JsonElement::getAsCharacter, 'a');
		} catch(IOException e) {
			SweatBridge.LOGGER.error("Failed to read config file", e);
		}
	}

	public static void save() {
		File configFile = PATH.toFile();
		try(FileWriter writer = new FileWriter(configFile); JsonWriter jsonWriter = new JsonWriter(writer)) {
			jsonWriter.setIndent("\t");
			JsonObject obj = new JsonObject();
			obj.addProperty("token", TOKEN != null ? TOKEN : "");
			obj.addProperty("enabled", ENABLED);
			obj.addProperty("prefix", PREFIX_COLOR);
			obj.addProperty("arrow", ARROW_COLOR);
			obj.addProperty("username", USERNAME_COLOR);
			obj.addProperty("discord_username", DISCORD_USERNAME_COLOR);
			ADAPTER.write(jsonWriter, obj);
		} catch(IOException e) {
			SweatBridge.LOGGER.error("Failed to save config file", e);
		}
	}

	private static <T> T read(JsonObject obj, String key, Function<JsonElement, T> reader, T defaultValue) {
		if(obj.has(key)) return reader.apply(obj.get(key));
		return defaultValue;
	}
}
