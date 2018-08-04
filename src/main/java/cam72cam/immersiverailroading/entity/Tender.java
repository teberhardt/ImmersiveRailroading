package cam72cam.immersiverailroading.entity;

import java.util.List;

import cam72cam.immersiverailroading.Config.ConfigDebug;
import cam72cam.immersiverailroading.inventory.SlotFilter;
import cam72cam.immersiverailroading.library.GuiTypes;
import cam72cam.immersiverailroading.registry.TenderDefinition;
import cam72cam.immersiverailroading.util.BurnUtil;
import cam72cam.immersiverailroading.util.FluidQuantity;
import cam72cam.immersiverailroading.util.LiquidUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.ItemHandlerHelper;

public class Tender extends CarTank {
	
	private static final DataParameter<Integer> OIL_AMOUNT = EntityDataManager.createKey(FreightTank.class, DataSerializers.VARINT);
	private static final DataParameter<String> OIL_TYPE = EntityDataManager.createKey(FreightTank.class, DataSerializers.STRING);
	
	protected final FluidTank oilTank = new FluidTank(null, 0) {
		@Override
		public boolean canFillFluidType(FluidStack fluid) {
			return canFill() && BurnUtil.burnableFluids().contains(fluid.getFluid());
		}
		
		@Override
		public void onContentsChanged() {
			if (!world.isRemote) {
				Tender.this.onTankContentsChanged();
			}
		}
	};;
	
	public Tender(World world) {
		this(world, null);
	}

	public Tender(World world, String defID) {
		super(world, defID);
		
		dataManager.register(OIL_AMOUNT, 0);
		dataManager.register(OIL_TYPE, "EMPTY");
	}
	
	
	@Override
	public TenderDefinition getDefinition() {
		return super.getDefinition(TenderDefinition.class);
	}
	
	@Override
	public GuiTypes guiType() {
		return GuiTypes.TENDER;
	}

	@Override
	public List<Fluid> getFluidFilter() {
		return LiquidUtil.getWater();
	}

	@Override
	public int getInventorySize() {
		return this.getDefinition().getInventorySize(gauge) + 4;
	}
	
	public int getInventoryWidth() {
		return this.getDefinition().getInventoryWidth(gauge);
	}
	
	public FluidQuantity getOilTankCapacity () {
		return this.getDefinition().getOilTankCapacity(gauge);
	}
	
	public int getOilAmount() {
		return this.dataManager.get(OIL_AMOUNT);
	}
	
	public Fluid getOilType() {
		String type = this.dataManager.get(OIL_TYPE);
		if (type.equals("EMPTY")) {
			return null;
		}
		return FluidRegistry.getFluid(type);
	}
	
	public boolean canHoldOil() {
		return this.getDefinition().getOilTankCapacity(gauge) != FluidQuantity.ZERO;
	}
 	
	@Override
	protected void initContainerFilter() {
		cargoItems.filter.clear();
		cargoItems.filter.put(getInventorySize()-4, SlotFilter.FLUID_CONTAINER);
		cargoItems.filter.put(getInventorySize()-3, SlotFilter.FLUID_CONTAINER);
		cargoItems.filter.put(getInventorySize()-2, SlotFilter.FLUID_CONTAINER);
		cargoItems.filter.put(getInventorySize()-1, SlotFilter.FLUID_CONTAINER);
		cargoItems.defaultFilter = SlotFilter.BURNABLE;
	}
	
	protected int[] getOilContainerInputSlots() {
		return new int[] { getInventorySize() - 4 };
	}
	
	protected int[] getOilContainertOutputSlots() {
		return new int[] { getInventorySize() - 3 };
	}
	
	protected int[] getContainerInputSlots() {
		return new int[] { getInventorySize() - 2 };
	}
	
	protected int[] getContainertOutputSlots() {
		return new int[] { getInventorySize() - 1 };
	}
	
	@Override
	protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
		super.writeEntityToNBT(nbttagcompound);
		nbttagcompound.setTag("oil_tank", this.oilTank.writeToNBT(new NBTTagCompound()));
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
		super.readEntityFromNBT(nbttagcompound);
		this.oilTank.readFromNBT(nbttagcompound.getCompoundTag("oil_tank"));
		onTankContentsChanged();
	}
	
	protected void onTankContentsChanged() {
		super.onTankContentsChanged();
		if (world.isRemote) {
			return;
		}
		
		this.dataManager.set(OIL_AMOUNT, oilTank.getFluidAmount());
		if (oilTank.getFluid() == null) {
			this.dataManager.set(OIL_TYPE, "EMPTY");
		} else {
			this.dataManager.set(OIL_TYPE, FluidRegistry.getFluidName(oilTank.getFluid()));
		}
	}

	protected void checkInvent() {
		super.checkInvent();

		if (world.isRemote) {
			return;
		}
		
		if (!this.isBuilt()) {
			return;
		}
		
		if (cargoItems.getSlots() == 0) {
			return;
		}

		for (int inputSlot : getOilContainerInputSlots()) {
			ItemStack input = cargoItems.getStackInSlot(inputSlot);

			if (input == null) {
				continue;
			}
			
			System.out.println(input);

			ItemStack inputCopy = ItemHandlerHelper.copyStackWithSize(input, 1);
			IFluidHandlerItem containerFluidHandler = FluidUtil.getFluidHandler(inputCopy);

			if (containerFluidHandler == null) {
				continue;
			}

			// This is kind of funky but it works
			// WILL BE CALLED RECUSIVELY from onInventoryChanged
			if (input.getCount() > 0) {
				// First try to drain the container, if we can't do that we try
				// to fill it

				for (Boolean doFill : new Boolean[] { false, true }) {

					FluidActionResult inputAttempt;

					if (doFill) {
						inputAttempt = FluidUtil.tryFillContainer(inputCopy, oilTank, Integer.MAX_VALUE, null, false);
					} else {
						inputAttempt = FluidUtil.tryEmptyContainer(inputCopy, oilTank, Integer.MAX_VALUE, null, false);
					}

					if (inputAttempt.isSuccess()) {
						// We were able to drain into the container

						// Can we move it to an output slot?
						ItemStack out = inputAttempt.getResult();
						for (Integer slot : this.getOilContainertOutputSlots()) {
							if (this.cargoItems.insertItem(slot, out, true).getCount() == 0) {
								// Move Liquid
								if (doFill) {
									FluidUtil.tryFillContainer(inputCopy, oilTank, Integer.MAX_VALUE, null, true);
								} else {
									FluidUtil.tryEmptyContainer(inputCopy, oilTank, Integer.MAX_VALUE, null, true);
								}
								if (!ConfigDebug.debugInfiniteLiquids) {
									// Decrease input
									cargoItems.extractItem(inputSlot, 1, false);
									
									// Increase output
									this.cargoItems.insertItem(slot, out, false);
									break;
								}
							}
						}
					}
				}
			}
		}
	}
	
	@Override
	public void onAssemble() {
		super.onAssemble();
		this.oilTank.setCapacity(this.getOilTankCapacity().MilliBuckets());
		onTankContentsChanged();
	}
	
	@Override
	public void onDissassemble() {
		super.onDissassemble();
		this.oilTank.drain(this.oilTank.getFluidAmount(), true);
		this.oilTank.setCapacity(0);
		onTankContentsChanged();
	}
}