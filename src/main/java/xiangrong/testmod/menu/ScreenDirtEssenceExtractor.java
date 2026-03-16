package xiangrong.testmod.menu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import xiangrong.testmod.TestMod;

public class ScreenDirtEssenceExtractor extends AbstractContainerScreen<MenuDirtEssenceExtractor> {
    private static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(TestMod.MODID, "textures/gui/container/dirt_essence_extractor.png");
    private static final ResourceLocation DIRT_ELEMENT_LOGO_TEXTURE = ResourceLocation.fromNamespaceAndPath(TestMod.MODID, "container/dirt_essence_extractor/dirt_element_logo");
    private static final ResourceLocation DIRT_ELEMENT_VALUE_TEXTURE = ResourceLocation.fromNamespaceAndPath(TestMod.MODID, "container/dirt_essence_extractor/dirt_element_value");
    private static final ResourceLocation BURN_PROGRESS = ResourceLocation.fromNamespaceAndPath(TestMod.MODID, "container/dirt_essence_extractor/burn_progress");

    public ScreenDirtEssenceExtractor(MenuDirtEssenceExtractor pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        pGuiGraphics.blit(RenderType::guiTextured, GUI_TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);

        int stored = menu.getDirtStored();
        int capacity = menu.getDirtCapacity();
        if (stored > 0 && capacity > 0) {
            int filledWidth = (int)((stored / (float)capacity) * 54);
            pGuiGraphics.blitSprite(RenderType::guiTextured, DIRT_ELEMENT_VALUE_TEXTURE, 54, 8,
                    0, 0, x + 113, y + 68, filledWidth, 8);
        }

        if (stored > 0) {
            pGuiGraphics.blitSprite(RenderType::guiTextured, DIRT_ELEMENT_LOGO_TEXTURE, 6, 10,
                    0, 0, x + 103, y + 66, 6, 10);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        super.renderLabels(pGuiGraphics, pMouseX, pMouseY);
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
