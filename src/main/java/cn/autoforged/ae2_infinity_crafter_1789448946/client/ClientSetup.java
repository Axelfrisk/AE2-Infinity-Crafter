package cn.autoforged.ae2_infinity_crafter_1789448946.client;

import cn.autoforged.ae2_infinity_crafter_1789448946.AE2InfinityCrafter;
import cn.autoforged.ae2_infinity_crafter_1789448946.ModItems;
import cn.autoforged.ae2_infinity_crafter_1789448946.ModMenuTypes;
import appeng.api.client.StorageCellModels;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * 客户端专用设置：注册 GUI、以及让无限元件在 AE2 的 ME 驱动器里有正确的渲染模型。
 *
 * <p>本类通过 {@code value = Dist.CLIENT} + {@code bus = Bus.MOD} 双重隔离，
 * 保证物理服务端不会加载任何客户端类。
 */
@Mod.EventBusSubscriber(modid = AE2InfinityCrafter.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ClientSetup {

    private ClientSetup() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            net.minecraft.client.gui.screens.MenuScreens.register(
                    ModMenuTypes.INFINITY_CELL_CRAFTER.get(), InfinityCellCrafterScreen::new);
            // 复用 AE2 创造元件的驱动器渲染模型。
            StorageCellModels.registerModel(ModItems.INFINITY_CELL.get(),
                    new ResourceLocation("ae2", "block/drive/cells/creative_cell"));
        });
    }

}
