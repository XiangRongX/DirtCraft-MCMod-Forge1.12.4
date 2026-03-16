package xiangrong.testmod.menu;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import xiangrong.testmod.TestMod;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, TestMod.MODID);

    public static final RegistryObject<MenuType<MenuDirtElementGenerator>> DIRT_ELEMENT_GENERATOR_MENU = MENUS.register("dirt_element_generator_menu",
            ()-> IForgeMenuType.create(MenuDirtElementGenerator::new));
    public static final RegistryObject<MenuType<MenuDirtElementStorage>> DIRT_ELEMENT_STORAGE_MENU = MENUS.register("dirt_element_storage_menu",
            ()-> IForgeMenuType.create(MenuDirtElementStorage::new));
    public static final RegistryObject<MenuType<MenuDirtEssenceExtractor>> DIRT_ESSENCE_EXTRACTOR_MENU = MENUS.register("dirt_essence_extractor_menu",
            ()->IForgeMenuType.create(MenuDirtEssenceExtractor::new));
}
