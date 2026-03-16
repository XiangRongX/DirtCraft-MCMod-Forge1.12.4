package xiangrong.testmod.entity;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.AnimationState;

public class DirtBossWindModel extends EntityModel<DirtBossRenderState> {
    private final ModelPart wind;
    private final ModelPart windTop;
    private final ModelPart windMid;
    private final ModelPart windBottom;
    private final AnimationState idleAnimationState = new AnimationState();

    public DirtBossWindModel(ModelPart root) {
        super(root, RenderType::entityTranslucent);
        this.wind = root.getChild("wind_body");
        this.windBottom = this.wind.getChild("wind_bottom");
        this.windMid = this.windBottom.getChild("wind_mid");
        this.windTop = this.windMid.getChild("wind_top");
    }

    public static LayerDefinition createWindLayer(int texWidth, int texHeight) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition wind_body = root.addOrReplaceChild("wind_body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition bottom = wind_body.addOrReplaceChild(
                "wind_bottom",
                CubeListBuilder.create().texOffs(1, 83).addBox(-2.5F, -7.0F, -2.5F, 5.0F, 7.0F, 5.0F),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition mid = bottom.addOrReplaceChild(
                "wind_mid",
                CubeListBuilder.create()
                        .texOffs(74, 28).addBox(-6.0F, -6.0F, -6.0F, 12.0F, 6.0F, 12.0F)
                        .texOffs(78, 32).addBox(-4.0F, -6.0F, -4.0F, 8.0F, 6.0F, 8.0F)
                        .texOffs(49, 71).addBox(-2.5F, -6.0F, -2.5F, 5.0F, 6.0F, 5.0F),
                PartPose.offset(0.0F, -7.0F, 0.0F));
        mid.addOrReplaceChild("wind_top",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-9.0F, -8.0F, -9.0F, 18.0F, 8.0F, 18.0F)
                        .texOffs(6, 6).addBox(-6.0F, -8.0F, -6.0F, 12.0F, 8.0F, 12.0F)
                        .texOffs(105, 57).addBox(-2.5F, -8.0F, -2.5F, 5.0F, 8.0F, 5.0F),
                PartPose.offset(0.0F, -6.0F, 0.0F));

        return LayerDefinition.create(mesh, texWidth, texHeight);
    }

    public static class DirtBossWindAnimation {
        public static final AnimationDefinition idle = AnimationDefinition.Builder.withLength(2.0F).looping()
                .addAnimation(
                        "wind_top",
                        new AnimationChannel(
                                AnimationChannel.Targets.POSITION,
                                new Keyframe(0.0F, KeyframeAnimations.posVec(0.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                new Keyframe(0.25F, KeyframeAnimations.posVec(0.5F, 0.0F, -0.5F), AnimationChannel.Interpolations.LINEAR),
                                new Keyframe(0.75F, KeyframeAnimations.posVec(-0.5F, 0.0F, -0.5F), AnimationChannel.Interpolations.LINEAR),
                                new Keyframe(1.25F, KeyframeAnimations.posVec(-0.5F, 0.0F, 0.5F), AnimationChannel.Interpolations.LINEAR),
                                new Keyframe(1.75F, KeyframeAnimations.posVec(0.5F, 0.0F, 0.5F), AnimationChannel.Interpolations.LINEAR),
                                new Keyframe(2.0F, KeyframeAnimations.posVec(0.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
                        )
                )
                .addAnimation(
                        "wind_mid",
                        new AnimationChannel(
                                AnimationChannel.Targets.POSITION,
                                new Keyframe(0.0F, KeyframeAnimations.posVec(0.5F, 0.0F, -0.5F), AnimationChannel.Interpolations.LINEAR),
                                new Keyframe(0.5F, KeyframeAnimations.posVec(-0.5F, 0.0F, -0.5F), AnimationChannel.Interpolations.LINEAR),
                                new Keyframe(1.0F, KeyframeAnimations.posVec(-0.5F, 0.0F, 0.5F), AnimationChannel.Interpolations.LINEAR),
                                new Keyframe(1.5F, KeyframeAnimations.posVec(0.5F, 0.0F, 0.5F), AnimationChannel.Interpolations.LINEAR),
                                new Keyframe(2.0F, KeyframeAnimations.posVec(0.5F, 0.0F, -0.5F), AnimationChannel.Interpolations.LINEAR)
                        )
                )
                .build();
    }

    @Override
    public void setupAnim(DirtBossRenderState state) {
        super.setupAnim(state);
        // 让风也能跟随动画移动
        this.animate(idleAnimationState, DirtBossModel.DirtBossAnimation.idle, state.ageInTicks);
    }

    public ModelPart wind() {
        return this.wind;
    }

}
