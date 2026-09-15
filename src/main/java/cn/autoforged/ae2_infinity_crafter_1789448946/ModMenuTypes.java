package cn.autoforged.ae2_infinity_crafter_1789448946;

import cn.autoforged.ae2_infinity_crafter_1789448946.menu.InfinityCellCrafterMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, AE2InfinityCrafter.MOD_ID);

    public static final RegistryObject<MenuType<InfinityCellCrafterMenu>> INFINITY_CELL_CRAFTER =
            MENU_TYPES.register("infinity_cell_crafter",
                    () -> IForgeMenuType.create(InfinityCellCrafterMenu::new));

    private ModMenuTypes() {
    }

    public static void register(IEventBus modBus) {
        MENU_TYPES.register(modBus);
    }
}
