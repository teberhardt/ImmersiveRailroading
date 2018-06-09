package cam72cam.immersiverailroading.items;

import java.util.List;

import cam72cam.immersiverailroading.ImmersiveRailroading;
import cam72cam.immersiverailroading.items.nbt.ItemDefinition;
import cam72cam.immersiverailroading.items.nbt.ItemGauge;
import cam72cam.immersiverailroading.items.nbt.ItemPlateType;
import cam72cam.immersiverailroading.library.Gauge;
import cam72cam.immersiverailroading.library.GuiText;
import cam72cam.immersiverailroading.library.ItemComponentType;
import cam72cam.immersiverailroading.library.PlateType;
import cam72cam.immersiverailroading.registry.DefinitionManager;
import cam72cam.immersiverailroading.registry.EntityRollingStockDefinition;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemPlate extends Item {
	public static final String NAME = "item_plate";
	
	public ItemPlate() {
		super();
		setUnlocalizedName(ImmersiveRailroading.MODID + ":" + NAME);
		setRegistryName(new ResourceLocation(ImmersiveRailroading.MODID, NAME));
        this.setCreativeTab(ItemTabs.MAIN_TAB);
	}
	
	@Override
	public void getSubItems(Item itemIn, CreativeTabs tab, NonNullList<ItemStack> items)
    {
        if (this.getCreativeTab() == tab)
        {
        	for (PlateType plate : PlateType.values()) {
        		ItemStack stack = new ItemStack(this);
        		ItemPlateType.set(stack, plate);
        		ItemGauge.set(stack, Gauge.from(Gauge.STANDARD));
        		
        		if (plate != PlateType.BOILER) {
					stack.getUnlocalizedName();
	                items.add(stack);
        		} else {
		        	for (String defID : DefinitionManager.getDefinitionNames()) {
		        		EntityRollingStockDefinition def = DefinitionManager.getDefinition(defID);
		        		if (def.getItemComponents().contains(ItemComponentType.BOILER_SEGMENT) ) {
			        		stack = stack.copy();
			        		ItemDefinition.setID(stack, defID);
							stack.getUnlocalizedName();
			                items.add(stack);
		        		}
		        	}
        		}
        	}
        }
    }
	
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		PlateType plate = ItemPlateType.get(stack);
		if (plate == PlateType.BOILER) {
			EntityRollingStockDefinition def = ItemDefinition.get(stack);
			if (def != null) {
				stack.setStackDisplayName(TextFormatting.RESET + plate.toString() + " " + def.name());
			}
		} else {
			stack.setStackDisplayName(TextFormatting.RESET + plate.toString());
		}
		return super.getUnlocalizedName(stack) + "." + plate.toString();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced)
    {
		super.addInformation(stack, playerIn, tooltip, advanced);
        tooltip.add(GuiText.GAUGE_TOOLTIP.toString(ItemGauge.get(stack)));
    }
}

