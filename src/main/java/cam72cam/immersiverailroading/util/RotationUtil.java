package cam72cam.immersiverailroading.util;

import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

public class RotationUtil {
	// Backported
	public static BlockPos rotate(BlockPos subtract, Rotation rotation) {
        switch (rotation)
        {
            case NONE:
            default:
                return subtract;
            case CLOCKWISE_90:
                return new BlockPos(-subtract.getZ(), subtract.getY(), subtract.getX());
            case CLOCKWISE_180:
                return new BlockPos(-subtract.getX(), subtract.getY(), -subtract.getZ());
            case COUNTERCLOCKWISE_90:
                return new BlockPos(subtract.getZ(), subtract.getY(), -subtract.getX());
        }
	}
}
