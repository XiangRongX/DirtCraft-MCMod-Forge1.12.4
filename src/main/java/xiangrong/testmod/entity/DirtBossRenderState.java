package xiangrong.testmod.entity;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DirtBossRenderState extends LivingEntityRenderState {
    public float invulnerableTicks;
}
