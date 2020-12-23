package cam72cam.immersiverailroading.registry;

import cam72cam.immersiverailroading.library.RenderComponentType;
import cam72cam.immersiverailroading.model.RenderComponent;
import cam72cam.mod.math.Vec3d;
import friedrichlp.renderlib.caching.ICacheData;
import friedrichlp.renderlib.caching.serialization.Serializer;
import friedrichlp.renderlib.math.TVector3;
import friedrichlp.renderlib.model.MeshBuf;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.*;

public class HeightmapCache implements ICacheData {
    protected Object2ObjectArrayMap<RenderComponent, float[][]> partMapCache = new Object2ObjectArrayMap<>();
    protected int xRes;
    protected int zRes;

    public void recalculate(EntityRollingStockDefinition def, MeshBuf mesh) {
        partMapCache.clear();

        double ratio = 8;
        xRes = (int) Math.ceil((def.frontBounds + def.rearBounds) * ratio);
        zRes = (int) Math.ceil(def.widthBounds * ratio);

        int precision = (int) Math.ceil(def.heightBounds * 4);

        def.model.getParts().onInit(groups -> {
            for (List<RenderComponent> rcl : def.renderComponents.values()) {
                for (RenderComponent rc : rcl) {
                    if (!rc.type.collisionsEnabled) {
                        continue;
                    }
                    float[][] heightMap = new float[xRes][zRes];
                    for (String group : rc.modelIDs) {
                        int[] faces = groups.data.get(group).faces;
                        for (int face : faces) {
                            Path2D path = new Path2D.Double();
                            float fheight = 0;
                            boolean first = true;
                            for (MeshBuf.Vertex vert : mesh.faceVertices(face)) {
                                TVector3 v = vert.position();
                                v.x += def.frontBounds;
                                v.z += def.widthBounds / 2;
                                if (first) {
                                    path.moveTo(v.x * ratio, v.z * ratio);
                                    first = false;
                                } else {
                                    path.lineTo(v.x * ratio, v.z * ratio);
                                }
                                fheight += v.y / 3; // We know we are using tris
                            }
                            Rectangle2D bounds = path.getBounds2D();
                            if (bounds.getWidth() * bounds.getHeight() < 1) {
                                continue;
                            }
                            for (int x = 0; x < xRes; x++) {
                                for (int z = 0; z < zRes; z++) {
                                    float relX = ((xRes - 1) - x);
                                    float relZ = z;
                                    if (bounds.contains(relX, relZ) && path.contains(relX, relZ)) {
                                        float relHeight = fheight / (float)def.heightBounds;
                                        relHeight = ((int) Math.ceil(relHeight * precision)) / (float) precision;
                                        heightMap[x][z] = Math.max(heightMap[x][z], relHeight);
                                    }
                                }
                            }
                        }
                    }

                    partMapCache.put(rc, heightMap);
                }
            }

            def.model.onCacheChange();
        });
    }

    @Override
    public void save(Serializer.Out out) throws IOException {
        out.writeI(partMapCache.size());
        for (Map.Entry<RenderComponent, float[][]> e : partMapCache.entrySet()) {
            // the RenderComponent is only partially saved to disk since we can reconstruct
            // the entire component later when the rolling stock is loaded.
            RenderComponent rc = e.getKey();
            out.writeList(rc.modelIDs, String.class);
            out.writeI(rc.type.ordinal());
            out.writeI(rc.id);
            out.writeStr(rc.side);
            out.writeStr(rc.pos);
            out.writeD(rc.scale);
            out.writeBool(rc.isWooden());
            out.writeD(rc.min.x);
            out.writeD(rc.min.y);
            out.writeD(rc.min.z);
            out.writeD(rc.max.x);
            out.writeD(rc.max.y);
            out.writeD(rc.max.z);

            float[][] arr = e.getValue();
            out.writeI(arr.length);
            for (float[] a : arr) {
                out.writeI(a.length);
                for (float f : a) {
                    out.writeF(f);
                }
            }
        }

        out.writeI(xRes);
        out.writeI(zRes);
    }

    @Override
    public void load(Serializer.In in) throws IOException {
        partMapCache.clear();

        int size = in.readI();
        for (int i = 0; i < size; i++) {
            Set<String> modelIDs = in.readList(HashSet::new, String.class);

            RenderComponentType type = RenderComponentType.values()[in.readI()];
            int id = in.readI();
            String side = in.readStr();
            String pos = in.readStr();
            double scale = in.readD();
            boolean wooden = in.readBool();
            Vec3d min = new Vec3d(in.readD(), in.readD(), in.readD());
            Vec3d max = new Vec3d(in.readD(), in.readD(), in.readD());
            RenderComponent rc = new RenderComponent(modelIDs, type, id, side, pos, scale, wooden, min, max);

            int width = in.readI();
            float[][] arr = new float[width][];
            for (int j = 0; j < width; j++) {
                int height = in.readI();
                float[] a = new float[height];
                for (int k = 0; k < height; k++) {
                    a[k] = in.readF();
                }
                arr[j] = a;
            }

            partMapCache.put(rc, arr);
        }

        xRes = in.readI();
        zRes = in.readI();
    }

    @Override
    public void reload() {
        // we don't do anything here since we manually hook into the model rebuilding process
    }
}
