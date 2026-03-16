package xiangrong.testmod.item;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import xiangrong.testmod.block.ModBlocks;

public class ItemDirtPickaxe extends PickaxeItem {
    public ItemDirtPickaxe(ToolMaterial pMaterial, float pAttackDamage, float pAttackSpeed, Properties pProperties) {
        super(pMaterial, pAttackDamage, pAttackSpeed, pProperties);
    }

    @Override
    public float getDestroySpeed(ItemStack pStack, BlockState pState) {
        Block block = pState.getBlock();
        float speed = super.getDestroySpeed(pStack, pState);
        return (block == Blocks.DIRT || block == ModBlocks.COMPRESSED_DIRT_BLOCK.get()) ? speed * 5 : speed;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Holder<Enchantment> enchantment) {
        return true;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return true;
    }
}
