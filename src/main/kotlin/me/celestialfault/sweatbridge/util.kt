package me.celestialfault.sweatbridge

import net.minecraft.text.MutableText

fun MutableText.rgbColor(color: Int): MutableText = this.setStyle(style.withColor(color))
