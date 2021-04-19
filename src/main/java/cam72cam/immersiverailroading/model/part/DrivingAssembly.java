package cam72cam.immersiverailroading.model.part;

import cam72cam.immersiverailroading.library.ModelComponentType;
import cam72cam.immersiverailroading.library.ValveGearType;
import cam72cam.immersiverailroading.model.ComponentRenderer;
import cam72cam.immersiverailroading.model.components.ComponentProvider;
import cam72cam.immersiverailroading.model.components.ModelComponent;

public class DrivingAssembly {
    public final DrivingWheels wheels;
    private final ValveGear right;
    private final ValveGear center;
    private final ValveGear left;
    private final ModelComponent steamChest;

    public static DrivingAssembly get(ValveGearType type, ComponentProvider provider, String pos, float angleOffset) {
        DrivingWheels wheels = DrivingWheels.get(provider, pos, angleOffset);
        if (wheels == null) {
            return null;
        }

        ValveGear left = ValveGear.get(wheels, type, provider, "LEFT" + (pos == null ? "" : ("_" + pos)), 0);
        ValveGear center = ValveGear.get(wheels, type, provider, "CENTER" + (pos == null ? "" : ("_" + pos)), -120);
        ValveGear right = ValveGear.get(wheels, type, provider, "RIGHT" + (pos == null ? "" : ("_" + pos)), center == null ? -90 : -240);

        ModelComponent steamChest = pos == null ?
                provider.parse(ModelComponentType.STEAM_CHEST) :
                provider.parse(ModelComponentType.STEAM_CHEST_POS, pos);

        return new DrivingAssembly(wheels, right, center, left, steamChest);
    }
    public DrivingAssembly(DrivingWheels wheels, ValveGear right, ValveGear center, ValveGear left, ModelComponent steamChest) {
        this.wheels = wheels;
        this.right = right;
        this.center = center;
        this.left = left;
        this.steamChest = steamChest;
    }

    public void render(double distance, float throttle, ComponentRenderer draw) {
        wheels.render(distance, draw);
        if (right != null) {
            right.render(distance, throttle, draw);
        }
        if (center != null) {
            center.render(distance, throttle, draw);
        }
        if (left != null) {
            left.render(distance, throttle, draw);
        }
        draw.render(steamChest);
    }

}