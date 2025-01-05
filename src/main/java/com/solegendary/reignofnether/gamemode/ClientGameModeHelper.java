package com.solegendary.reignofnether.gamemode;

import com.solegendary.reignofnether.hud.Button;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.survival.SurvivalClientEvents;
import com.solegendary.reignofnether.survival.WaveDifficulty;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class ClientGameModeHelper {

    public static GameMode DEFAULT_GAMEMODE = GameMode.STANDARD;
    public static GameMode gameMode = DEFAULT_GAMEMODE;
    public static boolean gameModeLocked = false; // locked with startRTS() in any gamemode, unlocked with /rts-reset
    public static boolean disallowSurvival = false;

    public static void cycleGameMode() {
        if (gameModeLocked)
            return;
        switch (gameMode) {
            case STANDARD -> {
                if (!disallowSurvival)
                    gameMode = GameMode.SURVIVAL;
            }
            case SURVIVAL -> gameMode = GameMode.STANDARD;
            default -> gameMode = GameMode.STANDARD;
        }
    }

    public static void cycleWaveDifficulty() {
        switch (SurvivalClientEvents.difficulty) {
            case EASY -> SurvivalClientEvents.difficulty = WaveDifficulty.MEDIUM;
            case MEDIUM -> SurvivalClientEvents.difficulty = WaveDifficulty.HARD;
            case HARD -> SurvivalClientEvents.difficulty = WaveDifficulty.EXTREME;
            case EXTREME -> SurvivalClientEvents.difficulty = WaveDifficulty.EASY;
        }
    }

    private static String getLockedString() {
        return gameModeLocked ? " " + I18n.get("hud.gamemode.reignofnether.locked") : "";
    }

    // all gamemodes are controlled by 1 button, cycled with right-click
    // left click provides functionality specific to the gamemode, eg. changing wave survival difficulty
    public static Button getButton() {
        Button button = switch (gameMode) {
            case STANDARD -> new Button(
                    "Standard",
                    Button.itemIconSize,
                    new ResourceLocation("minecraft", "textures/block/grass_block_side.png"),
                    (Keybinding) null,
                    () -> false,
                    () -> false,
                    () -> !gameModeLocked,
                    null,
                    ClientGameModeHelper::cycleGameMode,
                    List.of(
                            FormattedCharSequence.forward(I18n.get("hud.gamemode.reignofnether.standard1") +
                                    getLockedString(), Style.EMPTY.withBold(true)),
                            FormattedCharSequence.forward("", Style.EMPTY),
                            FormattedCharSequence.forward(I18n.get("hud.gamemode.reignofnether.standard2"), Style.EMPTY),
                            FormattedCharSequence.forward(I18n.get("hud.gamemode.reignofnether.standard3"), Style.EMPTY),
                            FormattedCharSequence.forward("", Style.EMPTY),
                            FormattedCharSequence.forward(I18n.get("hud.gamemode.reignofnether.changemode"), Style.EMPTY)
                    )
            );
            case SURVIVAL -> new Button(
                    "Survival",
                    Button.itemIconSize,
                    switch (SurvivalClientEvents.difficulty) {
                        case EASY -> new ResourceLocation("minecraft", "textures/item/wooden_sword.png");
                        case MEDIUM -> new ResourceLocation("minecraft", "textures/item/iron_sword.png");
                        case HARD -> new ResourceLocation("minecraft", "textures/item/diamond_sword.png");
                        case EXTREME -> new ResourceLocation("minecraft", "textures/item/netherite_sword.png");
                    },
                    (Keybinding) null,
                    () -> false,
                    () -> false,
                    () -> !gameModeLocked,
                    ClientGameModeHelper::cycleWaveDifficulty,
                    ClientGameModeHelper::cycleGameMode,
                    List.of(
                            FormattedCharSequence.forward(I18n.get("hud.gamemode.reignofnether.survival1") +
                                    getLockedString(), Style.EMPTY.withBold(true)),
                            FormattedCharSequence.forward(I18n.get("hud.gamemode.reignofnether.survival4",
                                    SurvivalClientEvents.difficulty, SurvivalClientEvents.getMinutesPerDay()), Style.EMPTY),
                            FormattedCharSequence.forward("", Style.EMPTY),
                            FormattedCharSequence.forward(I18n.get("hud.gamemode.reignofnether.survival2"), Style.EMPTY),
                            FormattedCharSequence.forward(I18n.get("hud.gamemode.reignofnether.survival3"), Style.EMPTY),
                            FormattedCharSequence.forward("", Style.EMPTY),
                            FormattedCharSequence.forward(I18n.get("hud.gamemode.reignofnether.survival5"), Style.EMPTY),
                            FormattedCharSequence.forward(I18n.get("hud.gamemode.reignofnether.changemode"), Style.EMPTY)
                    )
            );
            case KOTH -> new Button(
                    "King of the Beacon",
                    Button.itemIconSize,
                    new ResourceLocation("minecraft", "textures/item/nether_star.png"),
                    (Keybinding) null,
                    () -> false,
                    () -> false,
                    () -> !gameModeLocked,
                    () -> {},
                    ClientGameModeHelper::cycleGameMode,
                    List.of(
                            FormattedCharSequence.forward(I18n.get("hud.gamemode.reignofnether.koth1") +
                                    getLockedString(), Style.EMPTY.withBold(true)),
                            FormattedCharSequence.forward("", Style.EMPTY),
                            FormattedCharSequence.forward(I18n.get("hud.gamemode.reignofnether.koth2"), Style.EMPTY),
                            FormattedCharSequence.forward(I18n.get("hud.gamemode.reignofnether.koth3"), Style.EMPTY),
                            FormattedCharSequence.forward("", Style.EMPTY),
                            FormattedCharSequence.forward(I18n.get("hud.gamemode.reignofnether.koth4"), Style.EMPTY),
                            FormattedCharSequence.forward(I18n.get("hud.gamemode.reignofnether.changemode"), Style.EMPTY)
                    )
            );
            default -> null;
        };
        if (button != null)
            button.tooltipOffsetY = 45;
        return button;
    }
}
