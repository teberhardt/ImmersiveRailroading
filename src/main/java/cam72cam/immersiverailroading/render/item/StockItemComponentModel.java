package cam72cam.immersiverailroading.render.item;

import cam72cam.immersiverailroading.ImmersiveRailroading;
import cam72cam.immersiverailroading.items.ItemRollingStockComponent;
import cam72cam.immersiverailroading.library.Gauge;
import cam72cam.immersiverailroading.library.RenderComponentType;
import cam72cam.immersiverailroading.model.RenderComponent;
import cam72cam.immersiverailroading.render.entity.StockModel;
import cam72cam.mod.item.ItemStack;
import cam72cam.mod.math.Vec3d;
import cam72cam.mod.render.ItemRender;
import cam72cam.mod.render.OpenGL;
import cam72cam.mod.render.StandardModel;
import cam72cam.mod.world.World;
import friedrichlp.renderlib.library.RenderMode;
import friedrichlp.renderlib.math.HitBox3;
import friedrichlp.renderlib.math.TVector3;
import friedrichlp.renderlib.math.Vector3;
import friedrichlp.renderlib.render.ViewBoxes;
import friedrichlp.renderlib.tracking.ModelInfo;
import friedrichlp.renderlib.tracking.RenderLayer;
import friedrichlp.renderlib.tracking.RenderObject;
import friedrichlp.renderlib.tracking.RenderManager;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

public class StockItemComponentModel implements ItemRender.IItemModel {
    private static RenderLayer renderLayer = RenderManager.addRenderLayer(ViewBoxes.ALWAYS);

    @Override
    public StandardModel getModel(World world, ItemStack stack) {
        return new StandardModel().addCustom(() -> StockItemComponentModel.render(stack));
    }
    public static void render(ItemStack stack) {
        ItemRollingStockComponent.Data data = new ItemRollingStockComponent.Data(stack);
        double itemScale = data.gauge.scale();

        if (data.def == null) {
            ImmersiveRailroading.error("Item %s missing definition!", stack);
            stack.setCount(0);
            return;
        }

        ModelInfo model = data.def.getModel();

        ArrayList<String> groups = new ArrayList<>();
        for (RenderComponentType r : data.componentType.render) {
            RenderComponent comp = data.def.getComponent(r, Gauge.from(Gauge.STANDARD));
            if (comp == null || r == RenderComponentType.CARGO_FILL_X) {
                continue;
            }
            groups.addAll(comp.modelIDs);
        }

        HitBox3 box = model.getHitboxNow(groups);
        TVector3 center = box.center();
        float width = box.yDiff();
        float length = box.xDiff();
        float scale = 1;
        if (width != 0 || length != 0) {
            scale = (float)(0.95 / Math.max(width, length));
        }
        scale *= Math.sqrt(itemScale);

        RenderObject obj = renderLayer.addRenderObject(model);

        obj.setAllPartsHidden(true);
        for (String part : groups) {
            obj.setPartHidden(part, false);
        }

        obj.setPosition(0.5f - center.x, 0.5f - center.y, 0.5f - center.z);
        obj.setScale(scale, scale, scale);
        obj.forceTransformUpdate();
        RenderManager.render(renderLayer, RenderMode.USE_CUSTOM_MATS);

        renderLayer.clear();
    }
}
