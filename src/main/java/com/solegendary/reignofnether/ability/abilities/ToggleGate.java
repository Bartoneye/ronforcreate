package com.solegendary.reignofnether.ability.abilities;

import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.building.*;
import com.solegendary.reignofnether.building.buildings.monsters.HauntedGate;
import com.solegendary.reignofnether.building.buildings.villagers.StoneGate;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.unit.UnitAction;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

import static com.solegendary.reignofnether.building.Building.GateType.GATE_OPEN;
import static com.solegendary.reignofnether.building.Building.GateType.GATE_SHUT;
import static com.solegendary.reignofnether.building.buildings.villagers.StoneGate.*;


public class ToggleGate extends Ability {
    private final Building building;


    private static final int TOGGLE_GATE_COOLDOWN = 3;

    public ToggleGate(Building building) {
        super(
                UnitAction.TOGGLE_GATE,
                building.getLevel(),
                TOGGLE_GATE_COOLDOWN * ResourceCost.TICKS_PER_SECOND,
                0,
                0,
                false
        );
        this.building = building;
    }

    @Override
    public AbilityButton getButton(Keybinding hotkey) {
        return new AbilityButton(
                "Toggle Gate",
                new ResourceLocation("minecraft", "textures/item/spruce_door.png"),
                hotkey,
                () -> false,
                () -> false,
                () -> true,
                () -> {
                    Building.GateType gateType = null;

                    if (building instanceof HauntedGate hauntedGate) {
                        gateType = hauntedGate.getGateType();
                    } else if (building instanceof StoneGate stoneGate) {
                        gateType = stoneGate.getGateType();
                    }

                    if (gateType == Building.GateType.GATE_OPEN) {
                                BuildingServerboundPacket.gateToggle(building.originPos, String.valueOf(GATE_SHUT));

                            } else if (gateType == Building.GateType.GATE_SHUT) {
                                BuildingServerboundPacket.gateToggle(building.originPos, String.valueOf(GATE_OPEN));
                            }

                },
                null,
                List.of(
                        FormattedCharSequence.forward(I18n.get("abilities.reignofnether.toggle_gate"), Style.EMPTY.withBold(true)),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward(I18n.get("abilities.reignofnether.toggle_gate.tooltip1"), Style.EMPTY)
                ),
                this
        );
    }
}

    //Debug Comments
    //HudClientEvents.showTemporaryMessage(I18n.get("abilities.reignofnether.toggle_gate.debug1")); - Opening
    //HudClientEvents.showTemporaryMessage(I18n.get("abilities.reignofnether.toggle_gate.debug2")); - Closing
    //HudClientEvents.showTemporaryMessage(I18n.get("abilities.reignofnether.toggle_gate.error1")) - Nothing Happened

