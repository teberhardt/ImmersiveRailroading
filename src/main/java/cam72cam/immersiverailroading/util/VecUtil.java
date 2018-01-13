package cam72cam.immersiverailroading.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class VecUtil {
	private VecUtil() {
		// Disable construction since java does not have static classes
	}
	
	public static Vec3d fromYaw(double distance, float yaw)  {
		return new Vec3d(-Math.sin(Math.toRadians(yaw)) * distance, 0, Math.cos(Math.toRadians(yaw)) * distance);
	}
	
	public static float toYaw(Vec3d delta) {
		float yaw = (float) Math.toDegrees(MathHelper.atan2(-delta.xCoord, delta.zCoord));
		return (yaw + 360f) % 360f;
	}
	public static float toPitch(Vec3d delta) {
		float yaw = (float) Math.toDegrees(MathHelper.atan2(Math.sqrt(delta.zCoord * delta.zCoord + delta.xCoord * delta.xCoord), delta.yCoord));
		return (yaw + 360f) % 360f;
	}

	public static Vec3d rotateYaw(Vec3d pos, float rotationYaw) {
		return fromYaw(pos.xCoord, rotationYaw).add(fromYaw(pos.zCoord, rotationYaw + 90).addVector(0, pos.yCoord, 0));
	}

	public static BlockPos rotateYaw(BlockPos pos, float rotationYaw) {
		// Might need to do some fancier rounding here
		return new BlockPos(rotateYaw(new Vec3d(pos), rotationYaw));
	}

	public static Vec3d fromYawPitch(float distance, float rotationYaw, float rotationPitch) {
		return fromYaw(distance, rotationYaw).addVector(0, Math.tan(Math.toRadians(rotationPitch)) * distance, 0);
	}
	
	public static Vec3d between(Vec3d front, Vec3d rear) {
		return new Vec3d((front.xCoord + rear.xCoord) / 2, (front.yCoord + rear.yCoord) / 2, (front.zCoord + rear.zCoord) / 2);
	}
}
