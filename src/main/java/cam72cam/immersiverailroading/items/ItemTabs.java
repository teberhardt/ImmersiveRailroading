package cam72cam.immersiverailroading.items;

import cam72cam.immersiverailroading.ImmersiveRailroading;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class ItemTabs {

	public static CreativeTabs MAIN_TAB = new CreativeTabs(ImmersiveRailroading.MODID) {
		@Override
		public ItemStack getTabIconItem() {
			return new ItemStack(ImmersiveRailroading.ITEM_LARGE_WRENCH, 1);
		}
	};
	
	public static CreativeTabs STOCK_TAB = new CreativeTabs(ImmersiveRailroading.MODID + ".stock") {
		@Override
		public ItemStack getTabIconItem() {
			NonNullList<ItemStack> items = NonNullList.create();
			ImmersiveRailroading.ITEM_ROLLING_STOCK.getSubItems(null, this, items);
			return items.get(0);
		}
	};
	
	public static CreativeTabs COMPONENT_TAB = new CreativeTabs(ImmersiveRailroading.MODID + ".components") {
		@Override
		public ItemStack getTabIconItem() {
			NonNullList<ItemStack> items = NonNullList.create();
			ImmersiveRailroading.ITEM_ROLLING_STOCK_COMPONENT.getSubItems(null, this, items);
			return items.get(0);
		}
	};

}
