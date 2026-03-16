package xiangrong.testmod.menu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import xiangrong.testmod.TestMod;

public class ScreenDirtElementStorage extends AbstractContainerScreen<MenuDirtElementStorage> {
    private static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(TestMod.MODID, "textures/gui/container/dirt_element_storage.png");
    private static final ResourceLocation DIRT_ELEMENT_LOGO_TEXTURE = ResourceLocation.fromNamespaceAndPath(TestMod.MODID, "container/dirt_element_storage/dirt_element_logo");
    private static final ResourceLocation DIRT_ELEMENT_VALUE_TEXTURE = ResourceLocation.fromNamespaceAndPath(TestMod.MODID, "container/dirt_element_storage/dirt_element_value");


    public ScreenDirtElementStorage(MenuDirtElementStorage pMenu, Inventory pPlayerInventory, Component pTitle) {
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
            pGuiGraphics.blitSprite(RenderType::guiTextured, DIRT_ELEMENT_VALUE_TEXTURE, 92, 8,
                    0, 0, x + 45, y + 36, filledWidth, 8);
        }

        if (stored > 0) {
            pGuiGraphics.blitSprite(RenderType::guiTextured, DIRT_ELEMENT_LOGO_TEXTURE, 6, 10,
                    0, 0, x + 35, y + 34, 6, 10);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        super.renderLabels(pGuiGraphics, pMouseX, pMouseY);

        int input = menu.getInput();
        int output = menu.getOutput();
        if(input > 0 ){
            pGuiGraphics.drawString(this.font, Component.literal("输入：5DE/tick"), 60, 48, 0x000000, false);
        }else{
            pGuiGraphics.drawString(this.font, Component.literal("输入：0"), 60, 48, 0x000000, false);

        }
        if(output > 0){
            pGuiGraphics.drawString(this.font, Component.literal("输出：5DE/tick"), 60, 58, 0x000000, false);
        }else{
            pGuiGraphics.drawString(this.font, Component.literal("输出：0"), 60, 58, 0x000000, false);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        this.renderTooltip(guiGraphics, mouseX, mouseY);

        int barX = this.leftPos + 45;
        int barY = this.topPos + 36;
        int barWidth = 92;
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
