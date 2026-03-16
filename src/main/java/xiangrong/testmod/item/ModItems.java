package xiangrong.testmod.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.DamageResistant;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import xiangrong.testmod.TestMod;
import xiangrong.testmod.entity.ModEntities;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TestMod.MODID);

    public static final RegistryObject<Item> DIRT_BALL = ITEMS.register("dirt_ball",
            () -> new ItemDirtBall(new Item.Properties().setId(ITEMS.key("dirt_ball"))
                    .stacksTo(16)));

    public static final RegistryObject<Item> DIRT_PICKAXE = ITEMS.register("dirt_pickaxe",
            () -> new ItemDirtPickaxe(ModToolMaterials.DIRT, 2F, -2.8F, new Item.Properties().setId(ITEMS.key("dirt_pickaxe"))
                    .enchantable(15)));

    public static final RegistryObject<Item> DIRT_HELMET = ITEMS.register("dirt_helmet",
            () -> new ArmorItem(ModArmorMaterials.DIRT, ArmorType.HELMET, new Item.Properties().setId(ITEMS.key("dirt_helmet"))));
    public static final RegistryObject<Item> DIRT_CHESTPLATE = ITEMS.register("dirt_chestplate",
            () -> new ArmorItem(ModArmorMaterials.DIRT, ArmorType.CHESTPLATE, new Item.Properties().setId(ITEMS.key("dirt_chestplate"))));
    public static final RegistryObject<Item> DIRT_LEGGINGS = ITEMS.register("dirt_leggings",
            () -> new ArmorItem(ModArmorMaterials.DIRT, ArmorType.LEGGINGS, new Item.Properties().setId(ITEMS.key("dirt_leggings"))));
    public static final RegistryObject<Item> DIRT_BOOTS = ITEMS.register("dirt_boots",
            () -> new ArmorItem(ModArmorMaterials.DIRT, ArmorType.BOOTS, new Item.Properties().setId(ITEMS.key("dirt_boots"))));

    public static final RegistryObject<SpawnEggItem> DIRT_GOLEM_SPAWN_EGG = ITEMS.register("dirt_golem_spawn_egg",
            () -> new SpawnEggItem(ModEntities.DIRT_GOLEM.get(), new Item.Properties().setId(ITEMS.key("dirt_golem_spawn_egg"))));

    public static final RegistryObject<Item> DIRT_ESSENCE = ITEMS.register("dirt_essence",
            ()->new Item(new Item.Properties().setId(ITEMS.key("dirt_essence"))));

    public static final RegistryObject<Item> DIRT_STAR = ITEMS.register("dirt_star",
            () -> new ItemDirtStar(new Item.Properties().setId(ITEMS.key("dirt_star"))
                    .stacksTo(64)
                    .rarity(Rarity.RARE)
                    .component(DataComponents.DAMAGE_RESISTANT, new DamageResistant(DamageTypeTags.IS_EXPLOSION))));
}
