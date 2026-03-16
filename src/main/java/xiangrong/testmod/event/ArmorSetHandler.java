package xiangrong.testmod.event;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import xiangrong.testmod.effect.ModEffects;
import xiangrong.testmod.item.ModItems;

public class ArmorSetHandler {
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if(event.phase != TickEvent.Phase.END) return;

        Player player = event.player;
        if(player.level().isClientSide) return;

        if(hasFullSet(player)){
            player.addEffect(new MobEffectInstance(
                    ModEffects.DIRT_PROTECTION_EFFECT.getHolder().get(),
                    200, 0 ,false, false, true
            ));
        }
    }

    private boolean hasFullSet(Player player) {
        ItemStack head = player.getInventory().getArmor(3);
        ItemStack chest = player.getInventory().getArmor(2);
        ItemStack legs = player.getInventory().getArmor(1);
        ItemStack feet = player.getInventory().getArmor(0);

        return isMyArmor(head, ModItems.DIRT_HELMET.get())&&
                isMyArmor(chest, ModItems.DIRT_CHESTPLATE.get())&&
                isMyArmor(legs, ModItems.DIRT_LEGGINGS.get())&&
                isMyArmor(feet, ModItems.DIRT_BOOTS.get());
    }

    private boolean isMyArmor(ItemStack stack, Item item) {
        return !stack.isEmpty() && stack.getItem() == item;
    }
}
