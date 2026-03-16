package xiangrong.testmod.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;

import java.util.Map;

public class ModArmorMaterials {
    public static final ArmorMaterial DIRT = new ArmorMaterial(
            3,
            Map.of(
                    ArmorType.HELMET, 1,
                    ArmorType.CHESTPLATE, 3,
                    ArmorType.LEGGINGS, 2,
                    ArmorType.BOOTS, 1
            ),
            9,
            SoundEvents.ARMOR_EQUIP_LEATHER,
            0.0F,
            0.0F,
            ItemTags.DIRT,
            ModEquipmentAssets.DIRT
    );


}
