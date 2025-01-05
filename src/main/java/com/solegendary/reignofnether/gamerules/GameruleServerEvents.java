package com.solegendary.reignofnether.gamerules;

import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.solegendary.reignofnether.gamemode.GameModeClientboundPacket;
import com.solegendary.reignofnether.player.PlayerClientboundPacket;
import com.solegendary.reignofnether.registrars.GameRuleRegistrar;
import com.solegendary.reignofnether.unit.UnitServerEvents;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.Map;

public class GameruleServerEvents {

    @SubscribeEvent
    public static void onCommandUsed(CommandEvent evt) {
        List<ParsedCommandNode<CommandSourceStack>> nodes = evt.getParseResults().getContext().getNodes();
        if (nodes.size() <= 2)
            return;
        if (!nodes.get(0).getNode().getName().equals("gamerule"))
            return;

        if (nodes.get(1).getNode().getName().equals("disallowWaveSurvival")) {
            Map<String, ParsedArgument<CommandSourceStack, ?>> args = evt.getParseResults().getContext().getArguments();
            if (args.containsKey("value")) {
                boolean value = (boolean) args.get("value").getResult();
                if (value)
                    GameModeClientboundPacket.disallowSurvival();
                else
                    GameModeClientboundPacket.allowSurvival();
            }
        } else if (nodes.get(1).getNode().getName().equals("maxPopulation")) {
            Map<String, ParsedArgument<CommandSourceStack, ?>> args = evt.getParseResults().getContext().getArguments();
            if (args.containsKey("value")) {
                UnitServerEvents.maxPopulation = (int) args.get("value").getResult();
                PlayerClientboundPacket.syncMaxPopulation(UnitServerEvents.maxPopulation);
            }
        } else if (nodes.get(1).getNode().getName().equals("groundYLevel")) {
            Map<String, ParsedArgument<CommandSourceStack, ?>> args = evt.getParseResults().getContext().getArguments();
            if (args.containsKey("value")) {
                double groundYLevel = ((Integer) args.get("value").getResult()).doubleValue();
                PlayerClientboundPacket.setOrthoviewMinY((long) groundYLevel + 30);
            }
        } else if (nodes.get(1).getNode().getName().equals("improvedPathfinding")) {
            Map<String, ParsedArgument<CommandSourceStack, ?>> args = evt.getParseResults().getContext().getArguments();
            if (args.containsKey("value")) {
                boolean value = (boolean) args.get("value").getResult();
                for (LivingEntity le : UnitServerEvents.getAllUnits()) {
                    UnitServerEvents.IMPROVED_PATHFINDING = value;
                    AttributeInstance ai = le.getAttribute(Attributes.FOLLOW_RANGE);
                    if (ai != null)
                        ai.setBaseValue(Unit.getFollowRange());
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent evt) {
        MinecraftServer server = evt.getEntity().getLevel().getServer();
        if (server != null) {
            int groundYLevel = server.getGameRules().getRule(GameRuleRegistrar.GROUND_Y_LEVEL).get();
            if (groundYLevel >= 0)
                PlayerClientboundPacket.setOrthoviewMinY(groundYLevel + 30);
        }
    }
}