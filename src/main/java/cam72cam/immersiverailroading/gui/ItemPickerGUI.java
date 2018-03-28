package cam72cam.immersiverailroading.gui;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class ItemPickerGUI extends GuiScreen {
	private List<ItemStack> items;
	public ItemStack choosenItem;
	private Consumer<ItemStack> onExit;
	
	public ItemPickerGUI(List<ItemStack> items, Consumer<ItemStack> onExit) {
		this.items = items;
		this.onExit = onExit;
	}
	
	public void setItems(List<ItemStack> items ) {
		this.items = items;
		this.initGui();
	}
	
	public boolean hasOptions() {
		return this.items.size() != 0;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);

		for (GuiButton button: this.buttonList) {
			if (((ItemButton)button).isMouseOver(mouseX, mouseY)) {
				this.renderToolTip(((ItemButton)button).stack, mouseX, mouseY);
			}
		}
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
	
	@Override
	public void initGui() {
		if (width == 0 || height == 0) {
			return;
		}
		int startX = this.width / 16;
		int startY = this.height / 8;
		
		int stacksX = this.width * 7/8 / 32;
		
		this.buttonList.clear();
		startX += Math.max(0, (stacksX - items.size())/2) * 32;
		for (int i = 0; i < items.size(); i++) {
			int col = i % stacksX;
			int row = i / stacksX;
			ItemStack item = items.get(i);
			if (item == null) {
				item = new ItemStack(Items.STRING);
			}
			this.buttonList.add(new ItemButton(i, item, startX + col * 32, startY + row * 32));
		}
	}
	
	@Override
	public void actionPerformed(GuiButton button) throws IOException {
		for (GuiButton itemButton: this.buttonList) {
			if (itemButton == button) {
				this.choosenItem = ((ItemButton)button).stack;
				if (this.choosenItem != null && this.choosenItem.getItem() == Items.STRING) {
					this.choosenItem = null;
				}
				onExit.accept(this.choosenItem);
				break;
			}
		}
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
			onExit.accept(null);
        }
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}
}
