package cam72cam.immersiverailroading.model;

import java.util.List;

import cam72cam.immersiverailroading.library.Gauge;
import net.minecraft.util.math.Vec3d;

public class MultiRenderComponent {
	private final Vec3d min;
	private final Vec3d max;
	public final double scale;

	public MultiRenderComponent(List<RenderComponent> subComponents) {
		double minX = subComponents.get(0).min.xCoord;
		double minY = subComponents.get(0).min.yCoord;
		double minZ = subComponents.get(0).min.zCoord;
		double maxX = subComponents.get(0).max.xCoord;
		double maxY = subComponents.get(0).max.yCoord;
		double maxZ = subComponents.get(0).max.zCoord;
		
		for (RenderComponent rc : subComponents) {
			minX = Math.min(minX, rc.min.xCoord);
			minY = Math.min(minY, rc.min.yCoord);
			minZ = Math.min(minZ, rc.min.zCoord);
			maxX = Math.max(maxX, rc.max.xCoord);
			maxY = Math.max(maxY, rc.max.yCoord);
			maxZ = Math.max(maxZ, rc.max.zCoord);
		}
		min = new Vec3d(minX, minY, minZ);
		max = new Vec3d(maxX, maxY, maxZ);
		scale = subComponents.get(0).scale;
	}

	private MultiRenderComponent(Vec3d min, Vec3d max, double scale) {
		this.min = min;
		this.max = max;
		this.scale = scale;
	}

	public MultiRenderComponent scale(Gauge gauge) {
		return new MultiRenderComponent(min, max, gauge.scale());
	}

	public Vec3d center() {
		Vec3d min = this.min.scale(scale);
		Vec3d max = this.max.scale(scale);
		return new Vec3d((min.xCoord + max.xCoord)/2, (min.yCoord + max.yCoord)/2, (min.zCoord + max.zCoord)/2);
	}

	public double height() {
		Vec3d min = this.min.scale(scale);
		Vec3d max = this.max.scale(scale);
		return max.yCoord - min.yCoord;
	}
}
