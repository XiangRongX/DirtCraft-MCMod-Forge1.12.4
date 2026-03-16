package xiangrong.testmod.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import xiangrong.testmod.entity.EntityDirtBall;
import xiangrong.testmod.entity.ModEntities;

public class ItemDirtBall extends Item {
    public ItemDirtBall(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
        if (!level.isClientSide) {
            ItemStack shown = itemstack.copy();
            shown.setCount(1);
            EntityDirtBall dirtBallEntity = new EntityDirtBall(ModEntities.DIRT_BALL.get(), player, level, shown);
            dirtBallEntity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            level.addFreshEntity(dirtBallEntity);
            if (!player.getAbilities().instabuild) {
                itemstack.shrink(1);
            }
        }
        player.getCooldowns().addCooldown(itemstack, 10);
        return InteractionResult.SUCCESS;
    }

}
