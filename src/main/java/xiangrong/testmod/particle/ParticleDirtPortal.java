package xiangrong.testmod.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.PortalParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;

public class ParticleDirtPortal extends PortalParticle {
    protected ParticleDirtPortal(ClientLevel p_107551_, double p_107552_, double p_107553_, double p_107554_, double p_107555_, double p_107556_, double p_107557_) {
        super(p_107551_, p_107552_, p_107553_, p_107554_, p_107555_, p_107556_, p_107557_);

        this.rCol = 0.6F;
        this.gCol = 0.4F;
        this.bCol = 0.2F;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double xd, double yd, double zd) {
            ParticleDirtPortal particle = new ParticleDirtPortal(level, x, y, z, xd, yd, zd);
            particle.pickSprite(this.sprites);
            return particle;
        }
    }
}
