package cn.autoforged.ae2_infinity_crafter_1789448946;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * AE2 无限元件制作器 (AE2 Infinity Cell Crafter)
 *
 * <p>与 AE2 联动的方块：输入一个 256k 存储元件（物品 / 流体 / 化学品），
 * 消耗 10000 份对应资源后，产出配置了该资源的“无限元件”。
 *
 * <p>本类只负责把各 DeferredRegister 挂到 mod 事件总线上，具体逻辑见各子包。
 */
@Mod(AE2InfinityCrafter.MOD_ID)
public class AE2InfinityCrafter {

    /** 必须与 gradle.properties 的 mod_id / mods.toml 一致。 */
    public static final String MOD_ID = "ae2_infinity_crafter_1789448946";

    public AE2InfinityCrafter() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.register(modBus);
        ModBlocks.register(modBus);
        ModBlockEntities.register(modBus);
        ModMenuTypes.register(modBus);
        ModRecipes.register(modBus);
    }
}
