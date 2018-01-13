package cam72cam.immersiverailroading.util;

import java.io.IOException;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;
import net.minecraft.network.datasync.DataSerializers;

public class DataSerializerUtil {
	// From 1.12.2
	public static final DataSerializer<NBTTagCompound> COMPOUND_TAG = new DataSerializer<NBTTagCompound>()
    {
        public void write(PacketBuffer buf, NBTTagCompound value)
        {
            buf.writeCompoundTag(value);
        }
        public NBTTagCompound read(PacketBuffer buf) throws IOException
        {
            return buf.readCompoundTag();
        }
        public DataParameter<NBTTagCompound> createKey(int id)
        {
            return new DataParameter<NBTTagCompound>(id, this);
        }
    };
    
    static {
    	DataSerializers.registerSerializer(COMPOUND_TAG);
    }
}
