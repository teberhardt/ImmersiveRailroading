package cam72cam.immersiverailroading.items;

import cam72cam.immersiverailroading.IRItems;
import cam72cam.immersiverailroading.ImmersiveRailroading;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ItemTabs {

	public static CreativeTabs MAIN_TAB = new CreativeTabs(ImmersiveRailroading.MODID) {
		@Override
		public Item getTabIconItem() {
			return IRItems.ITEM_LARGE_WRENCH;
		}
	};
	
	public static CreativeTabs LOCOMOTIVE_TAB = new CreativeTabs(ImmersiveRailroading.MODID + ".locomotive") {
		@Override
		public Item getTabIconItem() {
			return IRItems.ITEM_LARGE_WRENCH;
		}
	};
	
	public static CreativeTabs STOCK_TAB = new CreativeTabs(ImmersiveRailroading.MODID + ".stock") {
		@Override
		public Item getTabIconItem() {
			return IRItems.ITEM_LARGE_WRENCH;
		}
	};
	
	public static CreativeTabs PASSENGER_TAB = new CreativeTabs(ImmersiveRailroading.MODID + ".passenger") {
		@Override
		public Item getTabIconItem() {
			return IRItems.ITEM_LARGE_WRENCH;
		}
	};
	
	public static CreativeTabs COMPONENT_TAB = new CreativeTabs(ImmersiveRailroading.MODID + ".components") {
		@Override
		public Item getTabIconItem() {
			return IRItems.ITEM_LARGE_WRENCH;
		}
	};

}
