package me.celestialfault.sweatbridge;

import me.celestialfault.celestialconfig.AbstractConfig;
import me.celestialfault.celestialconfig.variables.BooleanVariable;
import me.celestialfault.celestialconfig.variables.CharVariable;
import me.celestialfault.celestialconfig.variables.StringVariable;
import net.minecraft.client.Minecraft;

import java.nio.file.Path;

public final class Config extends AbstractConfig {
	private static final Path PATH = Minecraft.getMinecraft().mcDataDir.toPath().toAbsolutePath()
			.resolve("config").resolve("sweat-bridge.json");

	private Config() {
		super(PATH);
	}

	public static final Config INSTANCE = new Config();

	public final StringVariable token = new StringVariable("token", null);
	public final BooleanVariable enabled = new BooleanVariable("enabled", true);
	public final CharVariable prefix = new CharVariable("prefix", 'e');
	public final CharVariable arrow = new CharVariable("arrow", '6');
	public final CharVariable username = new CharVariable("username", 'a');
	public final CharVariable discord = new CharVariable("discord_username", 'a');
}
