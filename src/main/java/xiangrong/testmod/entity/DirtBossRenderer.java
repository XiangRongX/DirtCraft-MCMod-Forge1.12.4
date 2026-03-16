package xiangrong.testmod.entity;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import xiangrong.testmod.TestMod;

import static xiangrong.testmod.entity.ModModelLayers.DIRT_BOSS;

public class DirtBossRenderer extends MobRenderer<EntityDirtBoss, DirtBossRenderState, DirtBossModel> {
    private static final ResourceLocation DIRT_BOSS_INVULNERABLE_LOCATION = ResourceLocation.fromNamespaceAndPath(TestMod.MODID,"textures/entity/dirt_boss_invulnerable.png");
    private static final ResourceLocation DIRT_BOSS_LOCATION = ResourceLocation.fromNamespaceAndPath(TestMod.MODID,"textures/entity/dirt_boss.png");



    public DirtBossRenderer(EntityRendererProvider.Context context) {
        super(context, new DirtBossModel(context.bakeLayer(DIRT_BOSS)), 1.0F);
        this.addLayer(new DirtBossWindLayer(context, this));
    }

    @Override
    public ResourceLocation getTextureLocation(DirtBossRenderState pRenderState) {
        int i = Mth.floor(pRenderState.invulnerableTicks);
        return i > 0 && (i > 80 || i / 5 % 2 != 1) ? DIRT_BOSS_INVULNERABLE_LOCATION : DIRT_BOSS_LOCATION;
    }

    @Override
    public DirtBossRenderState createRenderState() {
        return new DirtBossRenderState();
    }

    public static DirtBossWindModel enable(DirtBossWindModel pModel, ModelPart... pParts) {
        pModel.wind().visible = false;

        for (ModelPart modelpart : pParts) {
            modelpart.visible = true;
        }

        return pModel;
    }

    public void extractRenderState(EntityDirtBoss entity, DirtBossRenderState state, float partialTick) {
        // 先提取通用状态（位置、旋转、动画tick等）
        super.extractRenderState(entity, state, partialTick);

        // 提取无敌时间（出生阶段用）
        int i = entity.getInvulnerableTicks();
        state.invulnerableTicks = i > 0 ? (float)i - partialTick : 0.0F;
    }
}
