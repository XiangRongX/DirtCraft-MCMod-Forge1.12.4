package xiangrong.testmod.item;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import xiangrong.testmod.TestMod;

public class ModEquipmentAssets implements EquipmentAssets {
    static ResourceKey<? extends Registry<EquipmentAsset>> ROOT_ID = ResourceKey.createRegistryKey(ResourceLocation.withDefaultNamespace("equipment_asset"));
    static ResourceKey<EquipmentAsset> DIRT = createId("dirt");

    static ResourceKey<EquipmentAsset> createId(String pName) {
        return ResourceKey.create(ROOT_ID, ResourceLocation.fromNamespaceAndPath(TestMod.MODID, pName));
    }
}
