package cn.autoforged.ae2_infinity_crafter_1789448946;

import cn.autoforged.ae2_infinity_crafter_1789448946.block.entity.InfinityCellCrafterBlockEntity;
import com.mojang.brigadier.arguments.LongArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 调试命令 {@code /aeinfinite <数量>}：修改无限元件制作器制作一个无限元件所需的
 * 物品数量 / 流体量（默认 10000）。不带参数时查询当前值。
 */
@Mod.EventBusSubscriber(modid = AE2InfinityCrafter.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ModCommands {

    private ModCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("aeinfinite")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("amount", LongArgumentType.longArg(1L))
                        .executes(ctx -> {
                            long amount = LongArgumentType.getLong(ctx, "amount");
                            InfinityCellCrafterBlockEntity.setRequiredBase(amount);
                            ctx.getSource().sendSuccess(() -> Component.translatable(
                                    "commands.ae2_infinity_crafter_1789448946.set_requirement", amount), true);
                            return 1;
                        }))
                .executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> Component.translatable(
                            "commands.ae2_infinity_crafter_1789448946.query_requirement",
                            InfinityCellCrafterBlockEntity.getRequiredBase()), false);
                    return 1;
                }));
    }
}
