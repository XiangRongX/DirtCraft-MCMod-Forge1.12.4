package xiangrong.testmod;

import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import xiangrong.testmod.block.ModBlocks;
import xiangrong.testmod.energy.NetworkManager;
import xiangrong.testmod.event.ArmorSetHandler;
import xiangrong.testmod.event.ModEventBusEvents;
import xiangrong.testmod.item.ModItems;

import static xiangrong.testmod.ModCreativeModeTabs.CREATIVE_MODE_TABS;
import static xiangrong.testmod.block.ModBlocks.BLOCKS;
import static xiangrong.testmod.blockentity.ModBlockEntities.BLOCK_ENTITIES;
import static xiangrong.testmod.effect.ModEffects.MOB_EFFECTS;
import static xiangrong.testmod.enchantment.ModEnchantmentEffects.LOCATION_BASED_ENCHANTMENT_EFFECTS;
import static xiangrong.testmod.entity.ModEntities.ENTITIES;
import static xiangrong.testmod.item.ModItems.ITEMS;
import static xiangrong.testmod.menu.ModMenuTypes.MENUS;
import static xiangrong.testmod.particle.ModParticles.PARTICLES;
import static xiangrong.testmod.potion.ModPotions.POTIONS;
import static xiangrong.testmod.recipe.ModRecipes.RECIPE_SERIALIZERS;
import static xiangrong.testmod.recipe.ModRecipes.RECIPE_TYPES;
import static xiangrong.testmod.villager.ModVillagers.POI_TYPES;
import static xiangrong.testmod.villager.ModVillagers.VILLAGER_PROFESSIONS;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(TestMod.MODID)
public class TestMod {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "testmod";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public TestMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::setup);
        //modEventBus.addListener(this::onDataPackRegistry);

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        ENTITIES.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
        RECIPE_TYPES.register(modEventBus);
        LOCATION_BASED_ENCHANTMENT_EFFECTS.register(modEventBus);
        POI_TYPES.register(modEventBus);
        VILLAGER_PROFESSIONS.register(modEventBus);
        MOB_EFFECTS.register(modEventBus);
        POTIONS.register(modEventBus);
        PARTICLES.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        MENUS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new ArmorSetHandler());

        MinecraftForge.EVENT_BUS.addListener((TickEvent.LevelTickEvent event) -> {
            if(event.phase == TickEvent.Phase.END && !event.level.isClientSide) {
                if(event.level instanceof ServerLevel serverLevel) {
                    NetworkManager.get(serverLevel).tick(serverLevel);
                }
            }
        });

        modEventBus.addListener(this::addCreative);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        //context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }


    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(ModBlocks.COMPRESSED_DIRT_BLOCK);
        }

        if(event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ModBlocks.DIRT_GOLEM_HEAD_ITEM);
            event.accept(ModBlocks.DIRT_ESSENCE_BLOCK);
        }

        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.DIRT_BALL);
            event.accept(ModItems.DIRT_STAR);
            event.accept(ModItems.DIRT_ESSENCE);
        }

        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(ModItems.DIRT_BOOTS);
            event.accept(ModItems.DIRT_CHESTPLATE);
            event.accept(ModItems.DIRT_HELMET);
            event.accept(ModItems.DIRT_LEGGINGS);
        }

        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.DIRT_PICKAXE);
        }

        if(event.getTabKey() == CreativeModeTabs.SPAWN_EGGS){
            event.accept(ModItems.DIRT_GOLEM_SPAWN_EGG);
        }

        if(event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS){
            event.accept(ModBlocks.DIRT_ESSENCE_ORE);
        }

    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(ModEventBusEvents::init);
    }

}
