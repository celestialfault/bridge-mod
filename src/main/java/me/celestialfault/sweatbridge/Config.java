package me.celestialfault.sweatbridge;

import me.celestialfault.celestialconfig.AbstractConfig;
import me.celestialfault.celestialconfig.variables.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Formatting;

import java.nio.file.Path;

public final class Config extends AbstractConfig {
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("sweat-bridge.json");

	private Config() {
		super(PATH);
	}

	public static final Config INSTANCE = new Config();

	public final StringVariable token = new StringVariable("token", null);
	public final BooleanVariable enabled = new BooleanVariable("enabled", true);
	public final ColorCodes colors = new ColorCodes();

	public static class ColorCodes extends VariableMap {
		private ColorCodes() {
			super("colors");
		}

		public final EnumVariable<Formatting> prefix = new EnumVariable<>("prefix", Formatting.class, Formatting.YELLOW);
		public final EnumVariable<Formatting> arrow = new EnumVariable<>("arrow", Formatting.class, Formatting.GOLD);
		public final EnumVariable<Formatting> username = new EnumVariable<>("username", Formatting.class, Formatting.GREEN);
		public final EnumVariable<Formatting> discord = new EnumVariable<>("discord_username", Formatting.class, Formatting.GREEN);
	}
}
