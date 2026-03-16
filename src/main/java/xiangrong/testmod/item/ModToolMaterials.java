package xiangrong.testmod.item;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ToolMaterial;

public class ModToolMaterials {
    public static final ToolMaterial DIRT = new ToolMaterial(
            BlockTags.INCORRECT_FOR_WOODEN_TOOL,
            32,
            2.0F,
            0.0F,
            15,
            ItemTags.DIRT
    );
}
