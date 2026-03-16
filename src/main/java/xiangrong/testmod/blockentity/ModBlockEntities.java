package xiangrong.testmod.blockentity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import xiangrong.testmod.TestMod;
import xiangrong.testmod.block.ModBlocks;

import java.util.Set;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, TestMod.MODID);

    public static final RegistryObject<BlockEntityType<BlockEntityDirtElementGenerator>> DIRT_ELEMENT_GENERATOR_BE = BLOCK_ENTITIES.register("dirt_element_generator_be",
            ()->new BlockEntityType<>(BlockEntityDirtElementGenerator::new, Set.of(ModBlocks.DIRT_ELEMENT_GENERATOR.get())));
    public static final RegistryObject<BlockEntityType<BlockEntityDirtElementPipe>> DIRT_ELEMENT_PIPE_BE = BLOCK_ENTITIES.register("dirt_element_pipe_be",
            ()->new BlockEntityType<>(BlockEntityDirtElementPipe::new, Set.of(ModBlocks.DIRT_ELEMENT_PIPE.get())));
    public static final RegistryObject<BlockEntityType<BlockEntityDirtElementStorage>> DIRT_ELEMENT_STORAGE_BE = BLOCK_ENTITIES.register("dirt_element_storage_be",
            ()->new BlockEntityType<>(BlockEntityDirtElementStorage::new, Set.of(ModBlocks.DIRT_ELEMENT_STORAGE.get())));
    public static final RegistryObject<BlockEntityType<BlockEntityDirtEssenceExtractor>> DIRT_ESSENCE_EXTRACTOR_BE = BLOCK_ENTITIES.register("dirt_essence_extractor_be",
            ()->new BlockEntityType<>(BlockEntityDirtEssenceExtractor::new, Set.of(ModBlocks.DIRT_ESSENCE_EXTRACTOR.get())));
}
