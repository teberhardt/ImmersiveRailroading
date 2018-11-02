package cam72cam.immersiverailroading.interaction;

import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import cam72cam.immersiverailroading.entity.EntityRollingStock;
import cam72cam.immersiverailroading.model.BBModel;
import net.minecraft.util.math.Vec3d;

public class RayCaster {
	private BBModel model;
	
	public RayCaster (BBModel model) {
		this.model = model;
	}
	
	/**
	 * 
	 * @param rayStart - the position where the ray should begin (relative to the rolling stock)
	 * @param rayDirection - the direction the ray should shoot in (must be a normalized vector)
	 * @param maxDistance - the maximum distance the ray should shoot (set to = for infinite distance)
	 * @return the name of the OBJ part that got hit (null if nothing got hit)
	 */
	public String getHit (Vec3d rayStart, Vec3d rayDirection, double maxDistance) {
		Map<String, Pair<Vec3d, Vec3d>> bb = model.boundingBoxes;
		Map<String, Vec3d> bbc = model.boundingBoxCenters;
		
		for (String comp : bb.keySet()) {
			Vec3d c = bbc.get(comp);
			double distance = c.distanceTo(rayStart);
			System.out.println(rayDirection);
			System.out.println(distance);
			System.out.println(rayDirection.scale(distance));
			Vec3d p = rayStart.add(rayDirection.scale(distance));
			Vec3d min = bb.get(comp).getLeft();
			Vec3d max = bb.get(comp).getRight();
			System.out.println(String.format("%s %s %s", p, min, max));
			if (distance <= maxDistance || maxDistance == 0) {
				if (p.x > min.x && p.y > min.y && p.z > min.z && p.x < max.x && p.y < max.y && p.z < max.z) { //<- ray hit object
					return comp;
				}
			}
		}
		
		return null;
	}
}
