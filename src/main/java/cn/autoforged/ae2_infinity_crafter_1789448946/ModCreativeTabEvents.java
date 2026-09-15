package cn.autoforged.ae2_infinity_crafter_1789448946;

import appeng.api.ids.AECreativeTabIds;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 把本 mod 的两个物品放进 AE2 的主创造模式选项卡，符合“附属 mod”的定位。
 */
@Mod.EventBusSubscriber(modid = AE2InfinityCrafter.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModCreativeTabEvents {

    private ModCreativeTabEvents() {
    }

    @SubscribeEvent
    public static void onBuildContents(BuildCreativeModeTabContentsEvent event) {
        if (AECreativeTabIds.MAIN.equals(event.getTabKey())) {
            event.accept(ModItems.INFINITY_CELL_CRAFTER.get());
            event.accept(ModItems.INFINITY_CELL.get());
            event.accept(ModItems.INFINITY_CELL_CHIP.get());
            event.accept(ModItems.INFINITY_CELL_HOUSING.get());
            event.accept(ModItems.PURIFIED_SINGULARITY.get());
        }
    }
}
