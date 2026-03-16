package xiangrong.testmod.recipe;

import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import xiangrong.testmod.TestMod;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, TestMod.MODID);

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, TestMod.MODID);

    public static final RegistryObject<RecipeSerializer<DirtArmorSmeltingRecipe>> DIRT_ARMOR_SMELTING_SERIALIZER =
            RECIPE_SERIALIZERS.register("dirt_armor_smelting", () -> new AbstractCookingRecipe.Serializer<>(DirtArmorSmeltingRecipe::new, 200));


    public static final RegistryObject<RecipeType<DirtArmorSmeltingRecipe>> DIRT_ARMOR_SMELTING_TYPE =
            RECIPE_TYPES.register("dirt_armor_smelting", () -> new RecipeType<DirtArmorSmeltingRecipe>() {
            });

}
