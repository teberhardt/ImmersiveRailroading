package cam72cam.immersiverailroading.render.item;

import cam72cam.immersiverailroading.items.ItemRollingStock;
import cam72cam.immersiverailroading.library.Gauge;
import cam72cam.immersiverailroading.registry.EntityRollingStockDefinition;
import cam72cam.immersiverailroading.render.entity.StockModel;
import cam72cam.mod.item.ItemStack;
import cam72cam.mod.render.ItemRender;
import cam72cam.mod.render.OpenGL;
import cam72cam.mod.render.StandardModel;
import cam72cam.mod.world.World;
import friedrichlp.renderlib.library.RenderMode;
import friedrichlp.renderlib.render.MultiRender;
import friedrichlp.renderlib.render.ViewBoxes;
import friedrichlp.renderlib.tracking.ModelInfo;
import friedrichlp.renderlib.tracking.RenderLayer;
import friedrichlp.renderlib.tracking.RenderManager;
import friedrichlp.renderlib.tracking.RenderObject;
import org.lwjgl.opengl.GL11;

public class StockItemModel implements ItemRender.ISpriteItemModel {
	@Override
	public StandardModel getModel(World world, ItemStack stack) {
		return new StandardModel().addCustom(() -> render(stack));
	}

	private void render(ItemStack stack) {
		ItemRollingStock.Data data = new ItemRollingStock.Data(stack);

		double scale = data.gauge.scale();
		ModelInfo model = data.def.getModel();
		/*if (model == null) {
			stack.setCount(0);
			return;
		}*/

		RenderObject obj = RenderObject.single(model);

		try (OpenGL.With matrix = OpenGL.matrix()) {
			GL11.glTranslated(0.5, 0, 0);
			GL11.glRotated(-90, 0, 1, 0);
			scale = 0.2 * Math.sqrt(scale);
			GL11.glScaled(scale, scale, scale);

			try (MultiRender r = MultiRender.get(model, RenderMode.USE_FFP_MATS)) {
				obj.renderSingle(r);
			}
		}
	}

	@Override
	public String getSpriteKey(ItemStack stack) {
		ItemRollingStock.Data data = new ItemRollingStock.Data(stack);
		if (data.def == null) {
			// Stock pack removed
			System.out.println(stack.getTagCompound());
			return null;
		}
		return data.def.defID + data.def.getModel().getNameHash();
	}

	@Override
	public StandardModel getSpriteModel(ItemStack stack) {
		ItemRollingStock.Data data = new ItemRollingStock.Data(stack);
		EntityRollingStockDefinition def = data.def;
		ModelInfo model = data.def.getModel();

		return new StandardModel().addCustom(() -> {
			RenderObject obj = RenderObject.single(model);

			try (OpenGL.With matrix = OpenGL.matrix()) {
				Gauge std = Gauge.from(Gauge.STANDARD);
				double modelLength = def.getLength(std);
				double size = Math.max(def.getHeight(std), def.getWidth(std));
				double scale = -1.6 / size;
				GL11.glTranslated(0, 0.85, -0.5);
				GL11.glScaled(scale, scale, scale / (modelLength / 2));
				GL11.glRotated(85, 0, 1, 0);

				try (MultiRender r = MultiRender.get(model, RenderMode.USE_FFP_MATS)) {
					obj.renderSingle(r);
				}
			}
		});
	}
}
