package me.celestialfault.sweatbridge

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import dev.isxander.yacl3.api.*
import dev.isxander.yacl3.api.controller.ColorControllerBuilder
import dev.isxander.yacl3.api.controller.StringControllerBuilder
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.awt.Color

@Suppress("unused", "UnstableApiUsage")
object ModMenuImpl : ModMenuApi {
	private val Text.asDescription get() = OptionDescription.of(this)
	private val String.text get() = Text.literal(this)
	private fun String.formatting(vararg formatting: Formatting) = text.formatted(*formatting)

	private fun colorController(option: Option<Color>) =
		ColorControllerBuilder.create(option).allowAlpha(false).build()

	override fun getModConfigScreenFactory() = ConfigScreenFactory { buildConfigScreen(it) }

	fun buildConfigScreen(parent: Screen?): Screen {
		return YetAnotherConfigLib.createBuilder()
			.title("Sweat Bridge".text)
			.category(main())
			.category(commands())
			.category(colors())
			.category(private())
			.save(Config::save)
			.build().generateScreen(parent)
	}

	private fun private() = ConfigCategory.createBuilder()
		.name("Private".text)
		.group(OptionGroup.createBuilder()
			.option(Option.createBuilder<String>()
				.name("API Key".text)
				.description(Text.empty()
					.append("Your API key used when connecting to bridge chat; this can be obtained by using ")
					.append("/apikey".formatting(Formatting.YELLOW))
					.append(" in Discord.")
					.append("\n\n")
					.append("Don't share this with anyone!".formatting(Formatting.RED))
					.asDescription)
				.customController { StringControllerBuilder.create(it).build() }
				.binding("", { Config.token ?: "" }, { Config.token = it })
				.build())
			.build())
		.build()

	private fun main(): ConfigCategory = ConfigCategory.createBuilder()
		.name("Main".text)
		.group(OptionGroup.createBuilder()
			.option(Option.createBuilder<Boolean>()
				.name("Show Chat".text)
				.description("If disabled, you won't see any bridge chat messages.".text.asDescription)
				.customController { TickBoxControllerBuilder.create(it).build() }
				.binding(true, { Config.enabled }, {
					Config.enabled = it
					if(!it) {
						ChatConnection.disconnect()
						SweatBridge.sendInChat = false
					} else {
						ChatConnection.attemptConnection()
					}
				})
				.instant(true)
				.build())
			.build())
		.build()

	private fun commands(): ConfigCategory = ConfigCategory.createBuilder()
		.name("Commands".text)
		.group(OptionGroup.createBuilder()
			.option(Option.createBuilder<String>()
				.name("Chat".text)
				.description("todo".text.asDescription)
				.customController { StringControllerBuilder.create(it).build() }
				.binding("ssc", { Config.Commands.chat }, { Config.Commands.chat = it })
				.flag(OptionFlag.GAME_RESTART)
				.build())
			.option(Option.createBuilder<String>()
				.name("Config".text)
				.description("todo".text.asDescription)
				.customController { StringControllerBuilder.create(it).build() }
				.binding("sweat", { Config.Commands.config }, { Config.Commands.config = it })
				.flag(OptionFlag.GAME_RESTART)
				.build())
			.build())
		.build()

	private fun colors(): ConfigCategory = ConfigCategory.createBuilder()
		.name("Colors".text)
		.group(OptionGroup.createBuilder()
			.option(Option.createBuilder<Color>()
				.name("Prefix".text)
				.description { Text.empty()
					.append("Preview:\n")
					.append("Sweat".text.rgbColor(it.rgb).formatted(Formatting.BOLD))
					.append(" > ".text.rgbColor(Config.Colors.arrow))
					.append("Username".text.rgbColor(Config.Colors.username))
					.append(": Hello!")
					.asDescription }
				.customController(::colorController)
				.binding(Color(Formatting.YELLOW.colorValue!!), { Color(Config.Colors.prefix) }, { Config.Colors.prefix = it.rgb })
				.build())
			.option(Option.createBuilder<Color>()
				.name("Arrow".text)
				.description { Text.empty()
					.append("Preview:\n")
					.append("Sweat".text.rgbColor(Config.Colors.prefix).formatted(Formatting.BOLD))
					.append(" > ".text.rgbColor(it.rgb))
					.append("Username".text.rgbColor(Config.Colors.username))
					.append(": Hello!")
					.asDescription }
				.customController(::colorController)
				.binding(Color(Formatting.GOLD.colorValue!!), { Color(Config.Colors.arrow) }, { Config.Colors.arrow = it.rgb })
				.build())
			.option(Option.createBuilder<Color>()
				.name("Username".text)
				.description { Text.empty()
					.append("Preview:\n")
					.append("Sweat".text.rgbColor(Config.Colors.prefix).formatted(Formatting.BOLD))
					.append(" > ".text.rgbColor(Config.Colors.arrow))
					.append("Username".text.rgbColor(it.rgb))
					.append(": Hello!")
					.asDescription }
				.customController(::colorController)
				.binding(Color(Formatting.GREEN.colorValue!!), { Color(Config.Colors.username) }, { Config.Colors.username = it.rgb })
				.build())
			.option(Option.createBuilder<Color>()
				.name("Discord".text)
				.description { Text.empty()
					.append("Preview:\n")
					.append("Sweat".text.rgbColor(Config.Colors.prefix).formatted(Formatting.BOLD))
					.append(" > ".text.rgbColor(Config.Colors.arrow))
					.append("[DISCORD] Username".text.rgbColor(it.rgb))
					.append(": Hello!")
					.asDescription }
				.customController(::colorController)
				.binding(Color(Formatting.GREEN.colorValue!!), { Color(Config.Colors.discord) }, { Config.Colors.discord = it.rgb })
				.build())
			.build())
		.build()
}
