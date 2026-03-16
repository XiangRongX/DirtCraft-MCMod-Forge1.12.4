package xiangrong.testmod.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import xiangrong.testmod.item.ModItems;

public class DirtArmorSmeltingRecipe extends SmeltingRecipe {
    public DirtArmorSmeltingRecipe(String pGroup, CookingBookCategory pCategory, Ingredient pIngredient, ItemStack pResult, float pExperience, int pCookingTime) {
        super(pGroup, pCategory, pIngredient, pResult, pExperience, pCookingTime);
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        if (level.isClientSide) return false;
        ItemStack itemStack = input.getItem(0);
        if (itemStack.is(ModItems.DIRT_BOOTS.get()) ||
                itemStack.is(ModItems.DIRT_LEGGINGS.get()) ||
                itemStack.is(ModItems.DIRT_CHESTPLATE.get()) ||
                itemStack.is(ModItems.DIRT_HELMET.get()) ||
                itemStack.is(ModItems.DIRT_PICKAXE.get())) {
            return itemStack.getDamageValue() == 0;
        }
        return false;
    }


}
