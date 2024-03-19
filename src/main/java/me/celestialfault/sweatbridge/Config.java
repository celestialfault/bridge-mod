package me.celestialfault.sweatbridge;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonWriter;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.nio.file.Path;

public final class Config {
	private Config() {
		throw new AssertionError("Config is intended to be used in a static context");
	}

	static final TypeAdapter<JsonObject> ADAPTER = new Gson().getAdapter(JsonObject.class);
	private static final Path PATH = Minecraft.getMinecraft().mcDataDir.toPath().toAbsolutePath()
			.resolve("config").resolve("sweat-bridge.json");

	public static String TOKEN;
	public static boolean ENABLED = true;

	public static void load() {
		File configFile = PATH.toFile();
		if(!configFile.exists()) {
			save();
			return;
		}
		try(FileReader reader = new FileReader(configFile)) {
			JsonObject obj = ADAPTER.fromJson(reader);
			TOKEN = obj.has("token") ? obj.get("token").getAsString() : null;
			ENABLED = !obj.has("enabled") || obj.get("enabled").getAsBoolean();
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
			ADAPTER.write(jsonWriter, obj);
		} catch(IOException e) {
			SweatBridge.LOGGER.error("Failed to save config file", e);
		}
	}
}
