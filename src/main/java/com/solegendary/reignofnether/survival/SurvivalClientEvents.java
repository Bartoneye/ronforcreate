package com.solegendary.reignofnether.survival;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.apache.commons.lang3.text.WordUtils;

public class SurvivalClientEvents {

    public static int waveNumber = 1;
    public static boolean isEnabled = false;
    public static WaveDifficulty difficulty = WaveDifficulty.EASY;

    private static Minecraft MC = Minecraft.getInstance();

    public static int getMinutesPerDay() {
        return switch (SurvivalClientEvents.difficulty) {
            case EASY -> 15;
            case MEDIUM -> 12;
            case HARD -> 9;
            case EXTREME -> 6;
        };
    }

    public static void reset() {
        isEnabled = false;
        waveNumber = 1;
    }

    public static void enable(WaveDifficulty diff) {
        if (MC.player == null)
            return;

        difficulty = diff;
        isEnabled = true;

        String diffMsg = I18n.get("hud.gamemode.reignofnether.survival4",
                difficulty, getMinutesPerDay()).toLowerCase();
        diffMsg = diffMsg.substring(0,1).toUpperCase() + diffMsg.substring(1);

        MC.player.sendSystemMessage(Component.literal(""));
        MC.player.sendSystemMessage(Component.translatable(I18n.get("hud.gamemode.reignofnether.survival1"))
                .withStyle(Style.EMPTY.withBold(true)));
        MC.player.sendSystemMessage(Component.translatable(diffMsg));
        MC.player.sendSystemMessage(Component.literal(""));
    }
}
