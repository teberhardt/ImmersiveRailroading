package cam72cam.immersiverailroading.model;

import cam72cam.immersiverailroading.entity.Locomotive;
import cam72cam.immersiverailroading.library.LightFlare;
import cam72cam.immersiverailroading.library.ModelComponentType;
import cam72cam.immersiverailroading.model.components.ComponentProvider;
import cam72cam.immersiverailroading.model.components.ModelComponent;
import cam72cam.immersiverailroading.model.part.Bell;
import cam72cam.immersiverailroading.model.part.Control;
import cam72cam.immersiverailroading.registry.EntityRollingStockDefinition;
import cam72cam.immersiverailroading.registry.LocomotiveDefinition;

import java.util.ArrayList;
import java.util.List;
import cam72cam.immersiverailroading.util.VecUtil;
import cam72cam.mod.math.Vec3d;
import cam72cam.mod.render.Light;

import java.util.*;

public class LocomotiveModel<T extends Locomotive> extends FreightModel<T> {
    private List<ModelComponent> components;
    private Bell bell;
    private Control throttle;
    private Control reverser;
    private Control train_brake;
    private Map<UUID, List<Light>> lights = new HashMap<>();
    // TODO front/rear locomotives!
    private List<LightFlare> headlights;

    public LocomotiveModel(LocomotiveDefinition def) throws Exception {
        super(def);
    }

    @Override
    protected void parseComponents(ComponentProvider provider, EntityRollingStockDefinition def) {
        super.parseComponents(provider, def);

        components = provider.parse(
                new ModelComponentType[]{ModelComponentType.CAB}
        );
        bell = Bell.get(
                provider,
                ((LocomotiveDefinition)def).bell
        );
        throttle = Control.get(this, provider, ModelComponentType.THROTTLE);
        reverser = Control.get(this, provider, ModelComponentType.REVERSER);
        train_brake = Control.get(this, provider, ModelComponentType.TRAIN_BRAKE);
        headlights = LightFlare.get(provider, ModelComponentType.HEADLIGHT_X);
    }

    @Override
    public List<ModelComponent> getDraggableComponents() {
        List<ModelComponent> draggable = new ArrayList<>();
        if (throttle != null) {
            draggable.add(throttle.part);
        }
        if (reverser != null) {
            draggable.add(reverser.part);
        }
        if (train_brake != null) {
            draggable.add(train_brake.part);
        }
        return draggable;
    }

    @Override
    protected void effects(T stock) {
        super.effects(stock);
        bell.effects(stock, stock.getBell() > 0 ? 0.8f : 0);

        Vec3d lightPos = stock.getPosition().add(VecUtil.rotateWrongYaw(new Vec3d(stock.getDefinition().getLength(stock.gauge), 0, 0), stock.getRotationYaw()));
        Vec3d lightOff = VecUtil.rotateWrongYaw(new Vec3d(1, 0, 0), stock.getRotationYaw());
        if (!lights.containsKey(stock.getUUID())) {
            lights.put(stock.getUUID(), new ArrayList<>());
            for (int i = 0; i < 15; i++) {
                lights.get(stock.getUUID()).add(new Light(stock.getWorld(), lightPos.add(lightOff.scale(i*2)), 1 - i/15f));
            }
        }
        for (int i = 0; i < lights.get(stock.getUUID()).size(); i++) {
            lights.get(stock.getUUID()).get(i).setPosition(lightPos.add(lightOff.scale(i*2)));
        }
    }

    @Override
    protected void removed(T stock) {
        super.removed(stock);
        bell.removed(stock);
        lights.get(stock.getUUID()).forEach(Light::remove);
        lights.remove(stock.getUUID());
    }

    @Override
    protected void render(T stock, ComponentRenderer draw, double distanceTraveled) {
        super.render(stock, draw, distanceTraveled);
        try (ComponentRenderer light = draw.withBrightGroups(true)) {
            light.render(components);
            headlights.forEach(x -> x.render(light));
        }
        bell.render(draw);
        if (throttle != null) {
            throttle.render(Math.abs(stock.getThrottle()), draw);
        }
        if (reverser != null) {
            reverser.render(-stock.getReverser() / 2, draw);
        }
        if (train_brake != null) {
            train_brake.render(stock.getAirBrake(), draw);
        }
    }

    @Override
    void postRender(T stock, ComponentRenderer draw, double distanceTraveled) {
        headlights.forEach(x -> x.postRender(stock));
    }
}
