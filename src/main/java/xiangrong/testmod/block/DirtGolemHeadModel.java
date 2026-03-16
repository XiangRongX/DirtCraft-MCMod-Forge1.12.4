package xiangrong.testmod.block;

import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import xiangrong.testmod.TestMod;

public class DirtGolemHeadModel extends SkullModelBase {
    public static final ModelLayerLocation DIRT_GOLEM_HEAD = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TestMod.MODID, "dirt_golem_head"), "main");

    private final ModelPart root;

    public DirtGolemHeadModel(ModelPart root) {
        super(root);
        this.root = root;
    }

    private static MeshDefinition createHeadModel() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.ZERO);
        return meshdefinition;
    }

    public static LayerDefinition createSnowGolemHeadLayer() {
        MeshDefinition meshdefinition = createHeadModel();
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(float pMouthAnimation, float pYRot, float pXRot) {
        this.root.yRot = pYRot * ((float)Math.PI / 180F);
        this.root.xRot = pXRot * ((float)Math.PI / 180F);
    }

}
