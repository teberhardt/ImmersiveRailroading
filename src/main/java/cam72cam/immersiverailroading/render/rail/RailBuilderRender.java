package cam72cam.immersiverailroading.render.rail;

import cam72cam.immersiverailroading.model.TrackModel;
import cam72cam.immersiverailroading.registry.DefinitionManager;
import cam72cam.immersiverailroading.tile.TileRail;
import cam72cam.mod.math.Vec3d;
import cam72cam.mod.math.Vec3i;
import cam72cam.mod.render.Layers;
import cam72cam.immersiverailroading.track.BuilderBase.VecYawPitch;
import cam72cam.immersiverailroading.util.RailInfo;
import cam72cam.mod.render.OpenGL;
import cam72cam.mod.world.World;
import friedrichlp.renderlib.library.RenderMode;
import friedrichlp.renderlib.math.TVector3;
import friedrichlp.renderlib.math.Vector3;
import friedrichlp.renderlib.render.MultiRender;
import friedrichlp.renderlib.render.ViewBoxes;
import friedrichlp.renderlib.tracking.RenderLayer;
import friedrichlp.renderlib.tracking.RenderObject;
import friedrichlp.renderlib.tracking.RenderManager;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RailBuilderRender {
    private static HashMap<Vec3i, List<RenderObject>> objectCache = new HashMap<>();

    /**
     * Renders a single RailInfo immediately to the screen.
     */
    public static void renderSingle(RailInfo info, World world) {
        TrackModel mdl = DefinitionManager.getTrack(info.settings.track, info.settings.gauge.value());
        if (mdl == null) {
            return;
        }

        ObjectSet<String> parts = mdl.model.getParts().get().data.keySet();

        try (MultiRender r = MultiRender.get(mdl.model, RenderMode.USE_FFP_MATS)) {
            for (VecYawPitch piece : info.getBuilder(world).getRenderData()) {
                try (OpenGL.With matrix = OpenGL.matrix()) {
                    RenderObject obj = RenderObject.single(mdl.model);
                    if (piece.getGroups().size() != 0) {
                        obj.setAllPartsHidden(true);
                        for (String group : piece.getGroups()) {
                            for (String part : parts) {
                                if (part.contains(group)) {
                                    obj.setPartHidden(part, false);
                                }
                            }
                        }
                    }

                    GL11.glTranslated(piece.x, piece.y, piece.z);
                    GL11.glRotated(piece.getYaw(), 0, 1, 0);
                    GL11.glRotated(piece.getPitch(), 1, 0, 0);
                    GL11.glRotated(-90, 0, 1, 0);
                    if (piece.getLength() != -1) {
                        GL11.glScaled((float) (piece.getLength() / info.settings.gauge.scale()), 1, 1);
                    }
                    double scale = info.settings.gauge.scale();
                    GL11.glScaled(scale, scale, scale);

                    r.updateMatrices();
                    obj.renderSingle(r);
                }
            }
        }
    }

    public static void tryAddRail(RailInfo info, TileRail rail) {
        if (rail.renderState == TileRail.RenderState.NONE) {
            rail.renderState = TileRail.RenderState.ACTIVE;
            Vec3i pos = rail.getPos();

            TrackModel mdl = DefinitionManager.getTrack(info.settings.track, info.settings.gauge.value());
            if (mdl == null) {
                return;
            }

            List<RenderObject> objects = new ArrayList<>();
            ObjectSet<String> parts = mdl.model.getParts().get().data.keySet();

            for (VecYawPitch piece : info.getBuilder(rail.getWorld()).getRenderData()) {
                RenderObject obj = Layers.TILES.addRenderObject(mdl.model);
                if (piece.getGroups().size() != 0) {
                    obj.setAllPartsHidden(true);
                    for (String group : piece.getGroups()) {
                        for (String part : parts) {
                            if (part.contains(group)) {
                                obj.setPartHidden(part, false);
                            }
                        }
                    }
                }

                Vec3d pp = info.placementInfo.placementPosition;
                TVector3 position = TVector3.create(pos.x, pos.y, pos.z)
                        .add((float)piece.x, (float)piece.y, (float)piece.z)
                        .add((float)pp.x, (float)pp.y, (float)pp.z);
                obj.setPosition(position);

                obj.rotate(piece.getPitch(), 90 - piece.getYaw(), 0);
                if (piece.getLength() != -1) {
                    obj.scale((float) (piece.getLength() / info.settings.gauge.scale()), 1, 1);
                }
                float scale = (float) info.settings.gauge.scale();
                obj.scale(scale, scale, scale);

                objects.add(obj);
            }

            objectCache.put(pos, objects);
        }
    }

    public static void breakRail(TileRail rail) {
        List<RenderObject> objects = objectCache.remove(rail.getPos());

        if (objects != null) {
            objects.forEach(Layers.TILES::removeRenderObject);
        }

        rail.renderState = TileRail.RenderState.DISABLED;
    }
}
