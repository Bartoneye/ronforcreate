package com.solegendary.reignofnether.ability.abilities;

import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.ability.AbilityClientboundPacket;
import com.solegendary.reignofnether.cursor.CursorClientEvents;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.hud.HudClientEvents;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.research.ResearchClient;
import com.solegendary.reignofnether.research.researchItems.ResearchSpiderWebs;
import com.solegendary.reignofnether.research.researchItems.ResearchWitherClouds;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.unit.UnitAction;
import com.solegendary.reignofnether.unit.UnitServerEvents;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.units.monsters.PoisonSpiderUnit;
import com.solegendary.reignofnether.unit.units.monsters.SpiderUnit;
import com.solegendary.reignofnether.unit.units.monsters.WardenUnit;
import com.solegendary.reignofnether.util.MiscUtil;
import com.solegendary.reignofnether.util.MyMath;
import com.solegendary.reignofnether.util.MyRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.TargetBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.solegendary.reignofnether.unit.UnitClientEvents.sendUnitCommand;

public class SpinWebs extends Ability {

    public static final int CD_MAX_SECONDS = 30;
    public static final int RANGE = 8;
    public static final int DURATION_SECONDS = 5;

    private final Spider spider;

    public SpinWebs(Spider spider) {
        super(
            UnitAction.SPIN_WEBS,
            spider.level,
            CD_MAX_SECONDS * ResourceCost.TICKS_PER_SECOND,
            RANGE,
            0,
            true,
            true
        );
        this.spider = spider;
        this.canAutocast = true;
    }

    @Override
    public AbilityButton getButton(Keybinding hotkey) {
        return new AbilityButton(
                "Spin Webs",
                new ResourceLocation("minecraft", "textures/block/cobweb.png"),
                hotkey,
                () -> CursorClientEvents.getLeftClickAction() == UnitAction.SPIN_WEBS || autocast,
                () -> !ResearchClient.hasResearch(ResearchSpiderWebs.itemName),
                () -> true,
                () -> CursorClientEvents.setLeftClickAction(UnitAction.SPIN_WEBS),
                () -> sendUnitCommand(UnitAction.AUTOCAST),
                List.of(
                        FormattedCharSequence.forward(I18n.get("abilities.reignofnether.spin_webs"), Style.EMPTY.withBold(true)),
                        FormattedCharSequence.forward("\uE004  " + CD_MAX_SECONDS + "s  \uE005  " + RANGE, MyRenderer.iconStyle),
                        FormattedCharSequence.forward(I18n.get("abilities.reignofnether.spin_webs.tooltip1"), Style.EMPTY),
                        FormattedCharSequence.forward(I18n.get("abilities.reignofnether.spin_webs.tooltip2", DURATION_SECONDS), Style.EMPTY),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward(I18n.get("abilities.reignofnether.spin_webs.tooltip4"), Style.EMPTY),
                        FormattedCharSequence.forward(I18n.get("abilities.reignofnether.spin_webs.tooltip3"), Style.EMPTY)
                ),
                this
        );
    }

    @Override
    public void use(Level level, Unit unitUsing, LivingEntity targetEntity) {
        if (!isOffCooldown())
            return;
        if (unitUsing instanceof SpiderUnit spiderUnit) {
            spiderUnit.getWebGoal().setAbility(this);
            spiderUnit.getWebGoal().setTarget(targetEntity);
        }
    }

    @Override
    public void use(Level level, Unit unitUsing, BlockPos targetBp) {
        if (!isOffCooldown())
            return;
        if (unitUsing instanceof SpiderUnit spiderUnit) {
            spiderUnit.getWebGoal().setAbility(this);
            spiderUnit.getWebGoal().setTarget(targetBp);
        }
    }
}
