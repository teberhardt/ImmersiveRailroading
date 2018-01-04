package cam72cam.immersiverailroading.tile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

public class SyncdTileEntity extends TileEntity {
	public boolean hasTileData;
	
	public boolean isLoaded() {
		return this.hasWorldObj() && (!worldObj.isRemote || hasTileData);
	}

	@Override
	public void markDirty() {
		super.markDirty();
		worldObj.notifyBlockUpdate(getPos(), worldObj.getBlockState(getPos()), worldObj.getBlockState(getPos()), 1 + 2 + 8);
		worldObj.notifyNeighborsOfStateChange(pos, this.getBlockType());
	}
	
	public void writeUpdateNBT(NBTTagCompound nbt) {
	}
	public void readUpdateNBT(NBTTagCompound nbt) {
	}
	
	
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound nbt = new NBTTagCompound();
		this.writeToNBT(nbt);
		this.writeUpdateNBT(nbt);
		
		return new SPacketUpdateTileEntity(this.getPos(), 1, nbt);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		this.readFromNBT(pkt.getNbtCompound());
		this.readUpdateNBT(pkt.getNbtCompound());
		super.onDataPacket(net, pkt);
		worldObj.markBlockRangeForRenderUpdate(getPos(), getPos());
		hasTileData = true;
	}
	
	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound tag = super.getUpdateTag();
		this.writeToNBT(tag);
		this.writeUpdateNBT(tag);
		return tag;
	}
	
	@Override 
	public void handleUpdateTag(NBTTagCompound tag) {
		this.readFromNBT(tag);
		this.readUpdateNBT(tag);
		super.handleUpdateTag(tag);
		worldObj.markBlockRangeForRenderUpdate(getPos(), getPos());
		hasTileData = true;
	}
}
