package xiangrong.testmod.entity;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import xiangrong.testmod.TestMod;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, TestMod.MODID);

    public static final RegistryObject<EntityType<EntityDirtBall>> DIRT_BALL = ENTITIES.register("dirt_ball",
            () -> EntityType.Builder.<EntityDirtBall>of(EntityDirtBall::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build(ResourceKey.create(ForgeRegistries.ENTITY_TYPES.getRegistryKey(), ResourceLocation.fromNamespaceAndPath(TestMod.MODID, "dirt_ball")))
    );
    public static final RegistryObject<EntityType<EntityDirtGolem>> DIRT_GOLEM = ENTITIES.register("dirt_golem",
            ()->EntityType.Builder.of(EntityDirtGolem::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(8)
                    .build(ResourceKey.create(ForgeRegistries.ENTITY_TYPES.getRegistryKey(), ResourceLocation.fromNamespaceAndPath(TestMod.MODID, "dirt_golem")))
    );
    public static final RegistryObject<EntityType<EntityDirtBossSkull>> DIRT_BOSS_SKULL = ENTITIES.register("dirt_boss_skull",
            ()->EntityType.Builder.<EntityDirtBossSkull>of(EntityDirtBossSkull::new, MobCategory.MISC)
                    .sized(0.3125F,0.3125F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build(ResourceKey.create(ForgeRegistries.ENTITY_TYPES.getRegistryKey(),ResourceLocation.fromNamespaceAndPath(TestMod.MODID,"dirt_boss_skull")))
    );
    public static final RegistryObject<EntityType<EntityDirtBoss>> DIRT_BOSS = ENTITIES.register("dirt_boss",
            ()->EntityType.Builder.of(EntityDirtBoss::new, MobCategory.MONSTER)
                    .sized(1.0F, 2.5F)
                    .clientTrackingRange(8)
                    .build(ResourceKey.create(ForgeRegistries.ENTITY_TYPES.getRegistryKey(), ResourceLocation.fromNamespaceAndPath(TestMod.MODID, "dirt_boss")))
    );

    public static final RegistryObject<EntityType<EntityAbsorbedBlock>> ABSORBED_BLOCK = ENTITIES.register("absorbed_block",
            () -> EntityType.Builder.of(
                            EntityAbsorbedBlock::new, MobCategory.MISC)
                    .sized(1.0F, 1.0F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .noSummon()
                    .build(ResourceKey.create(ForgeRegistries.ENTITY_TYPES.getRegistryKey(), ResourceLocation.fromNamespaceAndPath(TestMod.MODID, "absorbed_block")))
    );
}
