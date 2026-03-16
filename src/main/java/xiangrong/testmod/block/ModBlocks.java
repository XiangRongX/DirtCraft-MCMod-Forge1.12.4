package xiangrong.testmod.block;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;
import xiangrong.testmod.TestMod;

import java.util.function.Supplier;

import static xiangrong.testmod.item.ModItems.ITEMS;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, TestMod.MODID);

    public static final RegistryObject<Block> COMPRESSED_DIRT_BLOCK = registerBlock("compressed_dirt",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DIRT)
                    .setId(BLOCKS.key("compressed_dirt"))
                    .mapColor(MapColor.DIRT)));

    public static final RegistryObject<BlockDirtGolemHead> DIRT_GOLEM_HEAD = BLOCKS.register("dirt_golem_head",
            () -> new BlockDirtGolemHead(BlockDirtGolemHead.Types.DIRT_GOLEM, BlockBehaviour.Properties.ofFullCopy(Blocks.PLAYER_HEAD)
                    .setId(BLOCKS.key("dirt_golem_head"))));
    public static final RegistryObject<BlockDirtGolemWallHead> DIRT_GOLEM_WALL_HEAD = BLOCKS.register("dirt_golem_wall_head",
            () -> new BlockDirtGolemWallHead(BlockDirtGolemHead.Types.DIRT_GOLEM, BlockBehaviour.Properties.ofFullCopy(Blocks.PLAYER_WALL_HEAD)
                    .setId(BLOCKS.key("dirt_golem_head"))));
    public static final RegistryObject<Item> DIRT_GOLEM_HEAD_ITEM = ITEMS.register("dirt_golem_head",
            () -> new StandingAndWallBlockItem(DIRT_GOLEM_HEAD.get(),
                    DIRT_GOLEM_WALL_HEAD.get(),
                    Direction.DOWN, // 默认地面朝向
                    new Item.Properties().setId(ITEMS.key("dirt_golem_head"))) {
                @Override
                public @Nullable EquipmentSlot getEquipmentSlot(ItemStack stack) {
                    return EquipmentSlot.HEAD;
                }
            });

    public static final RegistryObject<Block> DIRT_ESSENCE_ORE = registerBlock("dirt_essence_ore",
            ()->new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)
                    .setId(BLOCKS.key("dirt_essence_ore"))
                    .mapColor(MapColor.GOLD)));
    public static final RegistryObject<Block> DIRT_ESSENCE_BLOCK = registerBlock("dirt_essence_block",
            ()->new Block(BlockBehaviour.Properties.of()
                    .setId(BLOCKS.key("dirt_essence_block"))
                    .strength(1,1)
                    .mapColor(MapColor.GOLD)));

    public static final RegistryObject<Block> DIRT_PORTAL_BLOCK = BLOCKS.register("dirt_portal_block",
            ()->new BlockDirtPortal(BlockBehaviour.Properties.of()
                    .setId(BLOCKS.key("dirt_portal_block"))
                    .noCollission()
                    .randomTicks()
                    .strength(-1.0F)
                    .sound(SoundType.GLASS)
                    .lightLevel(blockState->11)
                    .pushReaction(PushReaction.BLOCK)
            ));

    public static final RegistryObject<Block> DIRT_ELEMENT_GENERATOR = registerBlock("dirt_element_generator",
            ()->new BlockDirtElementGenerator(BlockBehaviour.Properties.of()
                    .setId(BLOCKS.key("dirt_element_generator"))));
    public static final RegistryObject<Block> DIRT_ELEMENT_PIPE = registerBlock("dirt_element_pipe",
            ()->new BlockDirtElementPipe(BlockBehaviour.Properties.of()
                    .setId(BLOCKS.key("dirt_element_pipe"))
                    .noOcclusion()));
    public static final RegistryObject<Block> DIRT_ELEMENT_STORAGE = registerBlock("dirt_element_storage",
            ()->new BlockDirtElementStorage(BlockBehaviour.Properties.of()
                    .setId(BLOCKS.key("dirt_element_storage"))));
    public static final RegistryObject<Block> DIRT_ESSENCE_EXTRACTOR = registerBlock("dirt_essence_extractor",
            ()->new BlockDirtEssenceExtractor(BlockBehaviour.Properties.of()
                    .setId(BLOCKS.key("dirt_essence_extractor"))));

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, Supplier<T> block) {
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().setId(ITEMS.key(name))));
    }

}
