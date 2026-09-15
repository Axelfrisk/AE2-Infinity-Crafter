package cn.autoforged.ae2_infinity_crafter_1789448946;

import cn.autoforged.ae2_infinity_crafter_1789448946.recipe.InfinityCellAssemblyRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 配方序列化器注册。
 *
 * <p>“无限元件芯片 + 无限元件外壳 → 无限元件”属于特殊配方（{@code CustomRecipe}），
 * 需要在这里注册自己的 {@link RecipeSerializer}；配方本身由 DataGen 用
 * {@code SpecialRecipeBuilder} 生成一个只有 type/category 的 JSON 引用它。
 */
public final class ModRecipes {

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, AE2InfinityCrafter.MOD_ID);

    public static final RegistryObject<RecipeSerializer<InfinityCellAssemblyRecipe>> INFINITY_CELL_ASSEMBLY =
            RECIPE_SERIALIZERS.register("infinity_cell_assembly",
                    () -> new SimpleCraftingRecipeSerializer<InfinityCellAssemblyRecipe>(
                            InfinityCellAssemblyRecipe::new));

    private ModRecipes() {
    }

    public static void register(IEventBus modBus) {
        RECIPE_SERIALIZERS.register(modBus);
    }
}
