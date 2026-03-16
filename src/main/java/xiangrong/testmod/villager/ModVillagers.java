package xiangrong.testmod.villager;

import com.google.common.collect.ImmutableSet;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import xiangrong.testmod.TestMod;
import xiangrong.testmod.block.ModBlocks;

public class ModVillagers {
    public static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(ForgeRegistries.POI_TYPES, TestMod.MODID);
    public static final DeferredRegister<VillagerProfession> VILLAGER_PROFESSIONS = DeferredRegister.create(ForgeRegistries.VILLAGER_PROFESSIONS, TestMod.MODID);

    public static final RegistryObject<PoiType> DIRT_WORKER_POI = POI_TYPES.register("dirt_worker_poi",
            ()->new PoiType(ImmutableSet.copyOf(ModBlocks.COMPRESSED_DIRT_BLOCK.get().getStateDefinition().getPossibleStates()),1,1));
    public static final RegistryObject<VillagerProfession> DIRT_WORKER = VILLAGER_PROFESSIONS.register("dirt_worker",
            ()->new VillagerProfession("dirt_worker",holder->holder.value()==DIRT_WORKER_POI.get(),
                    holder->holder.value()==DIRT_WORKER_POI.get(),ImmutableSet.of(), ImmutableSet.of(), SoundEvents.ROOTED_DIRT_BREAK));

    public static final RegistryObject<PoiType> DIRT_PORTAL_POI = POI_TYPES.register("dirt_portal_poi",
            () -> new PoiType(
                    ImmutableSet.copyOf(ModBlocks.DIRT_PORTAL_BLOCK.get().getStateDefinition().getPossibleStates()),
                    1, 1
            )
    );
}
