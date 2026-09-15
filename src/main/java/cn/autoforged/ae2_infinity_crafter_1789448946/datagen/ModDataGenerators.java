package cn.autoforged.ae2_infinity_crafter_1789448946.datagen;

import cn.autoforged.ae2_infinity_crafter_1789448946.AE2InfinityCrafter;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * DataGen 入口。生成方块状态 / 模型 / 物品模型 / 战利品表 / 标签 / 配方。
 * 语言文件与纹理是手写资源，不由 DataGen 生成。
 */
@Mod.EventBusSubscriber(modid = AE2InfinityCrafter.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModDataGenerators {

    private ModDataGenerators() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        generator.addProvider(event.includeClient(), new ModBlockStateProvider(output, existingFileHelper));
        generator.addProvider(event.includeClient(), new ModItemModelProvider(output, existingFileHelper));
        generator.addProvider(event.includeServer(), ModLootTableProvider.create(output));
        generator.addProvider(event.includeServer(),
                new ModBlockTagsProvider(output, event.getLookupProvider(), existingFileHelper));
        generator.addProvider(event.includeServer(), new ModRecipeProvider(output));
    }
}
