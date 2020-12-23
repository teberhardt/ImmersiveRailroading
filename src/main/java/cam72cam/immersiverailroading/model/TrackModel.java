package cam72cam.immersiverailroading.model;

import cam72cam.mod.resource.IdentifierFileContainer;
import friedrichlp.renderlib.model.ModelLoaderProperty;
import friedrichlp.renderlib.tracking.ModelInfo;
import friedrichlp.renderlib.tracking.ModelManager;

import java.util.ArrayList;

public class TrackModel {
    private final String compare;
    private final double size;
    private double height;

    public final ModelInfo model;

    public TrackModel(String condition, String resource) {
        this.compare = condition.substring(0, 1);
        this.size = Double.parseDouble(condition.substring(1));

        model = ModelManager.registerModel(new IdentifierFileContainer(resource), new ModelLoaderProperty(0));
        model.forceLoad();

        model.getParts().onInit(parts -> {
            ArrayList<String> groups = new ArrayList<>();
            for (String group : parts.data.keySet()) {
                if (group.contains("RAIL_LEFT") || group.contains("RAIL_RIGHT")) {
                    groups.add(group);
                }
            }

            model.getHitbox(groups).onSet(box -> {
                height = box.maxY;
            });
        });
    }

    public boolean canRender(double gauge) {
        switch (compare) {
            case ">":
                return gauge > size;
            case "<":
                return gauge < size;
            case "=":
                return gauge == size;
            default:
                return true;
        }
    }

    public double getHeight() {
        return height;
    }
}
