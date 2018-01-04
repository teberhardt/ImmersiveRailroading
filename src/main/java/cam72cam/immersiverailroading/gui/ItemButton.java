package cam72cam.immersiverailroading.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

public class ItemButton extends GuiButton {

	public ItemStack stack;

	public ItemButton(int buttonId, ItemStack stack, int x, int y) {
		super(buttonId, x, y, 16, 16, "");
		this.stack = stack;
	}
	
	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY) {
		super.drawButton(mc, mouseX, mouseY);
		Gui.drawRect(xPosition, yPosition, xPosition+16, yPosition+16, 0xFFFFFFFF);
		RenderHelper.enableStandardItemLighting();

        FontRenderer font = stack.getItem().getFontRenderer(stack);
        if (font == null) {
        	font = mc.fontRendererObj;
        }
		//mc.getRenderItem().renderItemIntoGUI(stack, x, y);
        mc.getRenderItem().renderItemAndEffectIntoGUI(stack, xPosition, yPosition);
		mc.getRenderItem().renderItemOverlays(font, stack, xPosition, yPosition);
	}

	public boolean isMouseOver(int mouseX, int mouseY) {
		return mouseX >= this.xPosition && mouseX < this.xPosition + 16 && mouseY >= this.yPosition && mouseY < this.yPosition + 16;
	}
}
