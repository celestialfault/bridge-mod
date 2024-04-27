package me.celestialfault.sweatbridge

import me.celestialfault.celestialconfig.AbstractConfig
import me.celestialfault.celestialconfig.Property
import me.celestialfault.celestialconfig.properties.ObjectProperty
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.util.Formatting

object Config : AbstractConfig(FabricLoader.getInstance().configDir.resolve("sweat-bridge.json")) {
	var token by Property.string("token")
	var enabled by Property.boolean("enabled", true).notNullable()

	object Commands : ObjectProperty<Commands>("commands") {
		var chat by Property.string("chat", "ssc").notNullable()
		var config by Property.string("config", "sweat").notNullable()
	}

	object Colors : ObjectProperty<Colors>("colors") {
		var prefix by Property.int("prefix", Formatting.YELLOW.colorValue).notNullable()
		var arrow by Property.int("arrow", Formatting.GOLD.colorValue).notNullable()
		var username by Property.int("username", Formatting.GREEN.colorValue).notNullable()
		var discord by Property.int("discord_username", Formatting.GREEN.colorValue).notNullable()
	}
}
