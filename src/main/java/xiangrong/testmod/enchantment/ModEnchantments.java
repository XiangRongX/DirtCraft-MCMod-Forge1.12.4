package xiangrong.testmod.enchantment;


import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import xiangrong.testmod.TestMod;
import xiangrong.testmod.item.ModItems;

public class ModEnchantments {
    public static final ResourceKey<Enchantment> DIRT_DESTROYER = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(TestMod.MODID, "dirt_destroyer"));

    public static void dirt_destroyer_bootstrap(BootstrapContext<Enchantment> context) {
        Holder<Item> itemHolder = ModItems.DIRT_PICKAXE.getHolder().get();
        HolderSet<Item> supported = HolderSet.direct(itemHolder);

        register(context, DIRT_DESTROYER, Enchantment.enchantment(Enchantment.definition(
                supported,
                supported,
                5,
                1,
                Enchantment.constantCost(10),
                Enchantment.constantCost(30),
                1,
                EquipmentSlotGroup.HAND))
        );
    }

    private static void register(BootstrapContext<Enchantment> registry, ResourceKey<Enchantment> key, Enchantment.Builder builder) {
        registry.register(key, builder.build(key.location()));
    }


}
