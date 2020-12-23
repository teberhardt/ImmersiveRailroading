package cam72cam.immersiverailroading.render.item;

import cam72cam.immersiverailroading.registry.DefinitionManager;
import cam72cam.mod.render.ItemRender;
import cam72cam.mod.render.OpenGL;
import cam72cam.mod.render.obj.OBJRender;
import cam72cam.mod.item.ItemStack;
import cam72cam.mod.render.StandardModel;
import cam72cam.mod.world.World;
import friedrichlp.renderlib.library.RenderMode;
import friedrichlp.renderlib.render.MultiRender;
import friedrichlp.renderlib.tracking.ModelInfo;
import friedrichlp.renderlib.tracking.RenderObject;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class RailItemRender implements ItemRender.IItemModel {
	private static ModelInfo baseRailModel;
	private static List<String> left;

	@Override
	public StandardModel getModel(World world, ItemStack stack) {
		if (baseRailModel == null) {
			baseRailModel = DefinitionManager.getTracks().stream().findFirst().get().getTrackForGauge(0).model;

			baseRailModel.getParts().onInit(parts -> {
				List<String> groups = new ArrayList<>();

				for (String groupName : parts.data.keySet())  {
					if (groupName.contains("RAIL_LEFT")) {
						groups.add(groupName);
					}
				}
				left = groups;
			});
		}


		return new StandardModel().addCustom(() -> {
			try (OpenGL.With matrix = OpenGL.matrix()) {
                GL11.glTranslated(0.5, 0.2, -0.3);

                try (MultiRender r = MultiRender.get(baseRailModel, RenderMode.USE_FFP_MATS)) {
					RenderObject obj = RenderObject.single(baseRailModel);
					obj.setAllPartsHidden(true);
					for (String part : left) {
						obj.setPartHidden(part, false);
					}
					obj.renderSingle(r);
				}
            }
		});
	}
}
