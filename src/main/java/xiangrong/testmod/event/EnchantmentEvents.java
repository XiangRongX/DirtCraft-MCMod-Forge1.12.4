package xiangrong.testmod.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xiangrong.testmod.TestMod;
import xiangrong.testmod.block.ModBlocks;
import xiangrong.testmod.enchantment.ModEnchantments;

@Mod.EventBusSubscriber(modid = TestMod.MODID)
public class EnchantmentEvents {
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player == null || player.level().isClientSide) return;

        ItemStack tool = player.getMainHandItem();
        ItemEnchantments enchantments = tool.getEnchantments();

        Holder<Enchantment> dirtDestroyer = player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(ModEnchantments.DIRT_DESTROYER);
        int level = enchantments.getLevel(dirtDestroyer);
        if (level <= 0) return;

        ServerLevel levelWorld = (ServerLevel) event.getLevel();
        BlockPos pos = event.getPos();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos target = pos.offset(dx, dy, dz);
                    if (target.equals(pos)) continue;

                    BlockState blockState = levelWorld.getBlockState(target);
                    if (blockState.is(Blocks.DIRT) ||
                            blockState.is(Blocks.GRASS_BLOCK) ||
                            blockState.is(ModBlocks.COMPRESSED_DIRT_BLOCK.get())) {

                        if (!levelWorld.isClientSide) {
                            levelWorld.destroyBlock(target, true);
                            EquipmentSlot usedHand = (player.getMainHandItem() == tool) ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                            tool.hurtAndBreak(1, player, usedHand);
                        }

                    }
                }
            }
        }
    }
}
