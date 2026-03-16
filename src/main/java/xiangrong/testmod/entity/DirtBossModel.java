package xiangrong.testmod.entity;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.AnimationState;

public class DirtBossModel extends EntityModel<DirtBossRenderState> {
    private final ModelPart bone4;
    private final ModelPart bone3;
    private final ModelPart bone2;
    private final ModelPart bone;
    final ModelPart head;
    private final AnimationState idleAnimationState = new AnimationState();

    public DirtBossModel(ModelPart root) {
        super(root);
        this.bone4 = root.getChild("bone4");
        this.bone3 = root.getChild("bone3");
        this.bone2 = root.getChild("bone2");
        this.bone = root.getChild("bone");
        this.head = root.getChild("head");
    }

    public static LayerDefinition createBodyLayer(int width, int height) {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bone4 = partdefinition.addOrReplaceChild("bone4", CubeListBuilder.create().texOffs(16, 32).addBox(12.0F, -24.0F, 12.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(16, 48).addBox(-16.0F, -24.0F, -16.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition bone3 = partdefinition.addOrReplaceChild("bone3", CubeListBuilder.create().texOffs(0, 32).addBox(-13.0F, -19.0F, 9.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 48).addBox(9.0F, -19.0F, -13.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition bone2 = partdefinition.addOrReplaceChild("bone2", CubeListBuilder.create().texOffs(0, 40).addBox(-10.0F, -14.0F, -10.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(32, 40).addBox(6.0F, -14.0F, 6.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(32, 32).addBox(3.0F, -9.0F, -7.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(16, 40).addBox(-7.0F, -9.0F, 3.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -40.0F, -8.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, width, height);
    }

    @Override
    public void setupAnim(DirtBossRenderState pRenderState) {
        super.setupAnim(pRenderState);
        this.head.yRot = pRenderState.yRot * (float) (Math.PI / 180.0);
        this.head.xRot = pRenderState.xRot * (float) (Math.PI / 180.0);
        idleAnimationState.animateWhen(true, (int) pRenderState.ageInTicks);
        this.animate(idleAnimationState,DirtBossAnimation.idle, pRenderState.ageInTicks);
    }

    public static class DirtBossAnimation {
        public static final AnimationDefinition idle = AnimationDefinition.Builder.withLength(2.0F).looping()
                .addAnimation("bone", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 1440.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
                ))
                .addAnimation("bone2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 1440.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
                ))
                .addAnimation("bone3", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 1440.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
                ))
                .addAnimation("bone4", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 1440.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
                ))
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

}
