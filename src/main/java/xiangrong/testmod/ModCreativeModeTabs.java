package xiangrong.testmod;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import xiangrong.testmod.block.ModBlocks;
import xiangrong.testmod.item.ModItems;
import xiangrong.testmod.potion.ModPotions;

import static xiangrong.testmod.enchantment.ModEnchantments.DIRT_DESTROYER;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TestMod.MODID);

    // Creates a creative tab with the id "examplemod:example_tab" for the example item, that is placed after the combat tab
    public static final RegistryObject<CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("testmod_tab",
            () -> net.minecraft.world.item.CreativeModeTab.builder()
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .title(Component.translatable("creativetab.testmod.testmod_tab"))
                    .icon(() -> new ItemStack(ModBlocks.COMPRESSED_DIRT_BLOCK.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModBlocks.COMPRESSED_DIRT_BLOCK.get());
                        output.accept(ModItems.DIRT_BALL.get());
                        output.accept(ModItems.DIRT_PICKAXE.get());
                        output.accept(ModItems.DIRT_HELMET.get());
                        output.accept(ModItems.DIRT_CHESTPLATE.get());
                        output.accept(ModItems.DIRT_LEGGINGS.get());
                        output.accept(ModItems.DIRT_BOOTS.get());
                        output.accept(getDirtDestroyerBook(parameters.holders()));
                        output.accept(PotionContents.createItemStack(Items.POTION,ModPotions.DIRT_PROTECTION_POTION.getHolder().get()));
                        output.accept(PotionContents.createItemStack(Items.SPLASH_POTION,ModPotions.DIRT_PROTECTION_POTION.getHolder().get()));
                        output.accept(PotionContents.createItemStack(Items.LINGERING_POTION,ModPotions.DIRT_PROTECTION_POTION.getHolder().get()));
                        output.accept(PotionContents.createItemStack(Items.TIPPED_ARROW,ModPotions.DIRT_PROTECTION_POTION.getHolder().get()));
                        output.accept(ModItems.DIRT_GOLEM_SPAWN_EGG.get());
                        output.accept(ModBlocks.DIRT_GOLEM_HEAD_ITEM.get());
                        output.accept(ModItems.DIRT_STAR.get());
                        output.accept(ModItems.DIRT_ESSENCE.get());
                        output.accept(ModBlocks.DIRT_ESSENCE_BLOCK.get());
                        output.accept(ModBlocks.DIRT_ESSENCE_ORE.get());
                    }).build());


    private static ItemStack getDirtDestroyerBook(HolderLookup.Provider holders) {
        Holder<Enchantment> enchantmentHolder = holders.lookupOrThrow(Registries.ENCHANTMENT)
                .get(DIRT_DESTROYER).orElseThrow();
        return EnchantmentHelper.createBook(new EnchantmentInstance(enchantmentHolder, 1));
    }
}
