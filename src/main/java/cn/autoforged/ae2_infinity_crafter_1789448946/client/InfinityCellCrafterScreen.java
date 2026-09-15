package cn.autoforged.ae2_infinity_crafter_1789448946.client;

import cn.autoforged.ae2_infinity_crafter_1789448946.AE2InfinityCrafter;
import cn.autoforged.ae2_infinity_crafter_1789448946.menu.InfinityCellCrafterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 无限元件制作器 GUI。
 *
 * <p>布局：左侧输入 256k 存储元件，中间进度箭头，右侧输出无限元件芯片；右边缘是能量条，
 * 左下角在检测到输入冲突（多种资源同时达标）时显示红色警告。背景使用真正的 GUI 贴图，
 * 进度 / 能量 / 警告都作为叠加层绘制在上面。
 */
public class InfinityCellCrafterScreen extends AbstractContainerScreen<InfinityCellCrafterMenu> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(
            AE2InfinityCrafter.MOD_ID, "textures/gui/infinity_cell_crafter.png");

    /** GUI 贴图的真实像素尺寸，必须与 imageWidth/imageHeight 一起传给 blit，否则贴图会被放大。 */
    private static final int TEXTURE_WIDTH = 176;
    private static final int TEXTURE_HEIGHT = 166;

    /** 进度箭头（贴图里对应位置的凹陷区域）。 */
    private static final int PROGRESS_X = 68;
    private static final int PROGRESS_Y = 38;
    private static final int PROGRESS_WIDTH = 28;
    private static final int PROGRESS_HEIGHT = 16;

    /** 能量条。 */
    private static final int ENERGY_X = 152;
    private static final int ENERGY_Y = 17;
    private static final int ENERGY_WIDTH = 14;
    private static final int ENERGY_HEIGHT = 54;

    /** 输入冲突警告文字区域。 */
    private static final int WARN_X = 8;
    private static final int WARN_Y = 57;
    private static final int WARN_WIDTH = 56;
    private static final int WARN_HEIGHT = 10;

    public InfinityCellCrafterScreen(InfinityCellCrafterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    /**
     * 1.20.1 的 AbstractContainerScreen 自身不在 render 末尾调用 renderTooltip，
     * 必须由具体 Screen 自己调用，否则物品槽的悬浮标签（tooltip）永远不会显示。
     */
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 标准容器界面：先铺一层暗化背景，再画 GUI 本体。
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
        this.renderWidgetTooltips(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        // 正式 GUI 背景（含面板边框和所有槽位框）。
        // 必须用带 textureWidth/textureHeight 的重载：6 参 blit 会把贴图当成 256x256，
        // 导致 176x166 的贴图只采样到左上角一部分，视觉上被放大。
        graphics.blit(TEXTURE, x, y, 0.0F, 0.0F, this.imageWidth, this.imageHeight,
                TEXTURE_WIDTH, TEXTURE_HEIGHT);

        // 进度箭头叠加层。
        int filled = this.menu.getScaledProgress(PROGRESS_WIDTH);
        if (filled > 0) {
            graphics.fill(x + PROGRESS_X, y + PROGRESS_Y,
                    x + PROGRESS_X + filled, y + PROGRESS_Y + PROGRESS_HEIGHT, 0xFF39C46E);
        }

        // 能量条叠加层（从下往上填充）。
        int energyFilled = this.menu.getScaledEnergy(ENERGY_HEIGHT);
        if (energyFilled > 0) {
            graphics.fill(x + ENERGY_X, y + ENERGY_Y + (ENERGY_HEIGHT - energyFilled),
                    x + ENERGY_X + ENERGY_WIDTH, y + ENERGY_Y + ENERGY_HEIGHT, 0xFF3E6EC4);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);

        // 进度百分比：注入 100 / 10000 时显示 1%。
        Component percent = Component.literal(this.menu.getProgressPercent() + "%");
        graphics.drawCenteredString(this.font, percent,
                PROGRESS_X + PROGRESS_WIDTH / 2, PROGRESS_Y - 9, 0x404040);

        // 多种资源同时达标：显示红色警告。
        if (this.menu.isConflict()) {
            graphics.drawString(this.font,
                    Component.translatable("gui.ae2_infinity_crafter_1789448946.conflict_short"),
                    WARN_X, WARN_Y, 0xFFFF5555, false);
        }
    }

    /** 自定义控件（进度条 / 能量条 / 冲突警告）的悬浮标签适配。 */
    private void renderWidgetTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
        if (isHovering(PROGRESS_X, PROGRESS_Y, PROGRESS_WIDTH, PROGRESS_HEIGHT, mouseX, mouseY)) {
            graphics.renderTooltip(this.font, Component.translatable(
                            "gui.ae2_infinity_crafter_1789448946.progress_tooltip",
                            this.menu.getProgress(), this.menu.getRequired(), this.menu.getProgressPercent()),
                    mouseX, mouseY);
        } else if (isHovering(ENERGY_X, ENERGY_Y, ENERGY_WIDTH, ENERGY_HEIGHT, mouseX, mouseY)) {
            graphics.renderTooltip(this.font, Component.translatable(
                            "gui.ae2_infinity_crafter_1789448946.energy_tooltip",
                            this.menu.getEnergy(), this.menu.getMaxEnergy(),
                            Component.translatable(this.menu.isNetworkPowered()
                                    ? "gui.ae2_infinity_crafter_1789448946.network_online"
                                    : "gui.ae2_infinity_crafter_1789448946.network_offline")),
                    mouseX, mouseY);
        } else if (this.menu.isConflict()
                && isHovering(WARN_X, WARN_Y, WARN_WIDTH, WARN_HEIGHT, mouseX, mouseY)) {
            // 具体阈值由服务端的动作栏消息给出，这里只做通用说明。
            graphics.renderTooltip(this.font, Component.translatable(
                            "gui.ae2_infinity_crafter_1789448946.conflict_tooltip"),
                    mouseX, mouseY);
        }
    }
}
