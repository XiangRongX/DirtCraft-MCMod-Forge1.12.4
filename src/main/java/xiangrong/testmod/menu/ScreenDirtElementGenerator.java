package xiangrong.testmod.menu;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.FurnaceScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import xiangrong.testmod.TestMod;

public class ScreenDirtElementGenerator extends AbstractContainerScreen<MenuDirtElementGenerator> {
    private static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(TestMod.MODID, "textures/gui/container/dirt_element_generator.png");
    private static final ResourceLocation COOK_TIME_TEXTURE = ResourceLocation.fromNamespaceAndPath(TestMod.MODID, "container/dirt_element_generator/cook_time_progress");
    private static final ResourceLocation BURN_TIME_TEXTURE = ResourceLocation.fromNamespaceAndPath(TestMod.MODID, "container/dirt_element_generator/burn_time_progress");
    private static final ResourceLocation DIRT_ELEMENT_LOGO_TEXTURE = ResourceLocation.fromNamespaceAndPath(TestMod.MODID, "container/dirt_element_generator/dirt_element_logo");
    private static final ResourceLocation DIRT_ELEMENT_VALUE_TEXTURE = ResourceLocation.fromNamespaceAndPath(TestMod.MODID, "container/dirt_element_generator/dirt_element_value");

    public ScreenDirtElementGenerator(MenuDirtElementGenerator pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        pGuiGraphics.blit(RenderType::guiTextured, GUI_TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);

        // 1. cookTime 进度条（水平条，从左到右）
        int cookWidth = menu.getCookProgressScaled(16);
        if (cookWidth > 0) {
            pGuiGraphics.blitSprite(RenderType::guiTextured, COOK_TIME_TEXTURE, 16, 4, 0, 0, x + 80, y + 15, cookWidth, 4);
        }

        // 2. burnTime 火焰条（垂直条，从下往上）
        int totalHeight = 14; // 火焰条总高度
        float litProgress = menu.getBurnProgress(); // 返回 0~1 的比例
        int burnHeight = Mth.ceil(litProgress * (totalHeight - 1)) + 1; // 最少显示1px
        if (burnHeight > 0) {
            // blitSprite 参数顺序：
            // RenderType, 贴图资源, 贴图原始宽,贴图原始高, UV起点x, UV起点y, 屏幕x,屏幕y, 显示宽, 显示高
            pGuiGraphics.blitSprite(RenderType::guiTextured, BURN_TIME_TEXTURE,
                    14, 14,        // 原贴图宽高
                    0, totalHeight - burnHeight, // 从贴图底部向上裁剪
                    x + 80, y + 42 + (totalHeight - burnHeight), // 屏幕位置
                    14, burnHeight);        // 显示尺寸
        }

        // 3. 泥素槽（水平条）
        int stored = menu.getDirtStored();
        int capacity = menu.getDirtCapacity();
        if (stored > 0 && capacity > 0) {
            int filledWidth = (int)((stored / (float)capacity) * 54);
            pGuiGraphics.blitSprite(RenderType::guiTextured, DIRT_ELEMENT_VALUE_TEXTURE, 54, 8,
                    0, 0, x + 113, y + 68, filledWidth, 8);
        }

        // 4. 泥素 logo（固定大小）
        if (stored > 0) {
            pGuiGraphics.blitSprite(RenderType::guiTextured, DIRT_ELEMENT_LOGO_TEXTURE, 6, 10,
                    0, 0, x + 103, y + 66, 6, 10);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        super.renderLabels(pGuiGraphics, pMouseX, pMouseY);

        float cookTime = menu.getCookTime();
        if(cookTime == 0) {
            pGuiGraphics.drawString(this.font, Component.literal("功率：0"), 107, 32, 0x000000, false);
        }else{
            pGuiGraphics.drawString(this.font, Component.literal("功率：100DE/个"), 107, 32, 0x000000, false);
        }

        int output = menu.getOutput();
        if(output > 0){
            pGuiGraphics.drawString(this.font, Component.literal("输出：5DE/tick"), 107, 42, 0x000000, false);
        }else{
            pGuiGraphics.drawString(this.font, Component.literal("输出：0"), 107, 42, 0x000000, false);

        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        this.renderTooltip(guiGraphics, mouseX, mouseY);

        int barX = this.leftPos + 113;
        int barY = this.topPos + 68;
        int barWidth = 54;
        int barHeight = 8;

        if (mouseX >= barX && mouseX < barX + barWidth &&
                mouseY >= barY && mouseY < barY + barHeight) {
            int stored = menu.getDirtStored();
            int capacity = menu.getDirtCapacity();

            Component tooltip = Component.literal("容量：" + stored + "DE/" + capacity + "DE");
            guiGraphics.renderTooltip(this.font, tooltip, mouseX, mouseY);
        }
    }
}
