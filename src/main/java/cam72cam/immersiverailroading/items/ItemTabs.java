package cam72cam.immersiverailroading.items;

import java.util.ArrayList;
import java.util.List;

import cam72cam.immersiverailroading.ImmersiveRailroading;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemTabs {

	public static CreativeTabs MAIN_TAB = new CreativeTabs(ImmersiveRailroading.MODID) {
		@Override
		public Item getTabIconItem() {
			return ImmersiveRailroading.ITEM_LARGE_WRENCH;
		}
	};
	
	public static CreativeTabs STOCK_TAB = new CreativeTabs(ImmersiveRailroading.MODID + ".stock") {
		@Override
		public Item getTabIconItem() {
			List<ItemStack> items = new ArrayList<ItemStack>();
			ImmersiveRailroading.ITEM_ROLLING_STOCK.getSubItems(null, this, items);
			//return items.get(0).getItem();

			return ImmersiveRailroading.ITEM_LARGE_WRENCH;
		}
	};
	
	public static CreativeTabs COMPONENT_TAB = new CreativeTabs(ImmersiveRailroading.MODID + ".components") {
		@Override
		public Item getTabIconItem() {
			List<ItemStack> items = new ArrayList<ItemStack>();
			ImmersiveRailroading.ITEM_ROLLING_STOCK_COMPONENT.getSubItems(null, this, items);
			//return items.get(0).getItem();

			return ImmersiveRailroading.ITEM_LARGE_WRENCH;
		}
	};

}
