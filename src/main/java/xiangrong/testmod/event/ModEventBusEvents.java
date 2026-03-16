package xiangrong.testmod.event;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xiangrong.testmod.TestMod;
import xiangrong.testmod.block.ModBlocks;
import xiangrong.testmod.entity.DirtGolemModel;
import xiangrong.testmod.entity.EntityDirtBoss;
import xiangrong.testmod.entity.EntityDirtGolem;
import xiangrong.testmod.entity.ModEntities;

import java.util.Set;

@Mod.EventBusSubscriber(modid = TestMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventBusEvents {
    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event){
        event.registerLayerDefinition(DirtGolemModel.LAYER_LOCATION, DirtGolemModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event){
        event.put(ModEntities.DIRT_GOLEM.get(), EntityDirtGolem.createAttributes().build());
        event.put(ModEntities.DIRT_BOSS.get(), EntityDirtBoss.createAttributes().build());
    }

    public static void init() {
        Set<Block> skullValidBlocks = new ObjectOpenHashSet<>(BlockEntityType.SKULL.validBlocks);
        skullValidBlocks.addAll(ImmutableSet.of(
                ModBlocks.DIRT_GOLEM_HEAD.get(), ModBlocks.DIRT_GOLEM_WALL_HEAD.get()
        ));
        BlockEntityType.SKULL.validBlocks = skullValidBlocks;
    }
}

