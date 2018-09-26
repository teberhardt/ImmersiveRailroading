package cam72cam.immersiverailroading.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;

import cam72cam.immersiverailroading.model.obj.OBJModel;
import net.minecraft.util.math.Vec3d;

public class BBModel {
	public Map<String, Pair<Vec3d, Vec3d>> boundingBoxes = new LinkedHashMap<String, Pair<Vec3d, Vec3d>>();
	public Map<String, Vec3d> boundingBoxCenters = new LinkedHashMap<String, Vec3d>();
	
	public BBModel (OBJModel model) {
		for (String comp : model.groups()) {
			List<String> cmp = Lists.newArrayList(comp);
			boundingBoxes.put(comp, Pair.of(model.minOfGroup(cmp), model.maxOfGroup(cmp)));
			boundingBoxCenters.put(comp, model.centerOfGroups(cmp));
		}
	}
}
