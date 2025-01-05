package com.solegendary.reignofnether.unit.units.modelling;

import com.solegendary.reignofnether.building.GarrisonableBuilding;
import com.solegendary.reignofnether.building.buildings.piglins.BlazeWatchtower;
import com.solegendary.reignofnether.unit.units.piglins.BlazeUnit;
import net.minecraft.client.model.BlazeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BlazeUnitRenderer extends MobRenderer<Blaze, BlazeModel<Blaze>> {
    private static final ResourceLocation SUPER_BLAZE_UNIT = new ResourceLocation("reignofnether", "textures/entities/super_blaze_unit.png");
    private static final ResourceLocation BLAZE_VANILLA = new ResourceLocation("textures/entity/blaze.png");

    public BlazeUnitRenderer(EntityRendererProvider.Context p_174354_) {
        super(p_174354_, new BlazeModel(p_174354_.bakeLayer(ModelLayers.BLAZE)), 1.0f);
    }


    public ResourceLocation getTextureLocation(Blaze blaze) {
        if (blaze instanceof BlazeUnit) {
            GarrisonableBuilding garr = GarrisonableBuilding.getGarrison((BlazeUnit) blaze);

            if (garr != null && garr instanceof BlazeWatchtower) {
                return SUPER_BLAZE_UNIT;
            }
        }
        return BLAZE_VANILLA;
    }

}
