package xiangrong.testmod.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import xiangrong.testmod.TestMod;

@OnlyIn(Dist.CLIENT)
public class DirtGolemRenderer extends MobRenderer<EntityDirtGolem, DirtGolemRenderState, DirtGolemModel> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TestMod.MODID, "textures/entity/dirt_golem.png");

    public DirtGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new DirtGolemModel(context.bakeLayer(DirtGolemModel.LAYER_LOCATION)), 0.5f);
        this.addLayer(new DirtGolemHeadLayer(this, context.getBlockRenderDispatcher()));
    }

    @Override
    public DirtGolemRenderState createRenderState() {
        return new DirtGolemRenderState();
    }

    @Override
    public ResourceLocation getTextureLocation(DirtGolemRenderState pRenderState) {
        return TEXTURE;
    }

    public void extractRenderState(EntityDirtGolem p_361607_, DirtGolemRenderState p_378334_, float p_368353_) {
        super.extractRenderState(p_361607_, p_378334_, p_368353_);
        p_378334_.hasPumpkin = p_361607_.hasPumpkin();
    }
}
