package cam72cam.immersiverailroading;

import cam72cam.immersiverailroading.Config.ConfigDebug;
import cam72cam.immersiverailroading.entity.*;
import cam72cam.immersiverailroading.gui.overlay.DieselLocomotiveOverlay;
import cam72cam.immersiverailroading.gui.overlay.HandCarOverlay;
import cam72cam.immersiverailroading.gui.overlay.SteamLocomotiveOverlay;
import cam72cam.immersiverailroading.library.Gauge;
import cam72cam.immersiverailroading.library.KeyTypes;
import cam72cam.immersiverailroading.library.Particles;
import cam72cam.immersiverailroading.multiblock.*;
import cam72cam.immersiverailroading.net.*;
import cam72cam.immersiverailroading.registry.DefinitionManager;
import cam72cam.immersiverailroading.registry.EntityRollingStockDefinition;
import cam72cam.immersiverailroading.render.SmokeParticle;
import cam72cam.immersiverailroading.render.StockRenderCache;
import cam72cam.immersiverailroading.render.block.RailBaseModel;
import cam72cam.immersiverailroading.render.item.*;
import cam72cam.immersiverailroading.render.multiblock.MBBlueprintRender;
import cam72cam.immersiverailroading.render.multiblock.TileMultiblockRender;
import cam72cam.immersiverailroading.render.rail.RailPreviewRender;
import cam72cam.immersiverailroading.thirdparty.CompatLoader;
import cam72cam.immersiverailroading.tile.Rail;
import cam72cam.immersiverailroading.tile.RailGag;
import cam72cam.immersiverailroading.tile.TileMultiblock;
import cam72cam.immersiverailroading.tile.TileRailPreview;
import cam72cam.mod.render.GLBoolTracker;
import cam72cam.immersiverailroading.util.IRFuzzy;
import cam72cam.mod.ModCore;
import cam72cam.mod.config.ConfigFile;
import cam72cam.mod.entity.EntityRegistry;
import cam72cam.mod.entity.Player;
import cam72cam.mod.gui.GuiRegistry;
import cam72cam.mod.input.Keyboard;
import cam72cam.mod.math.Vec3d;
import cam72cam.mod.net.Packet;
import cam72cam.mod.net.PacketDirection;
import cam72cam.mod.render.*;
import cam72cam.mod.resource.Identifier;
import cam72cam.mod.sound.Audio;
import cam72cam.mod.sound.ISound;
import cam72cam.mod.text.Command;
import org.lwjgl.opengl.GL11;
import paulscode.sound.SoundSystemConfig;

import java.io.IOException;
import java.util.function.Consumer;

import static org.lwjgl.input.Keyboard.*;

public class ImmersiveRailroading extends ModCore.Mod {
    public static final String MODID = "immersiverailroading";
	public static final String NAME = "ImmersiveRailroading";
    public static final String VERSION = "1.6.1";

	public static final int ENTITY_SYNC_DISTANCE = 512;
	private static ImmersiveRailroading instance;

	public static GuiRegistry GUI_REGISTRY = new GuiRegistry(ImmersiveRailroading.class);

	public static void forceInit() {
		//TODO figure out auto loading
	}

    static {
    	//ModCore.register(ImmersiveRailroading::new);

		EntityRegistry.register(ImmersiveRailroading.class, CarFreight::new, EntityRollingStock.settings, ImmersiveRailroading.ENTITY_SYNC_DISTANCE);
		EntityRegistry.register(ImmersiveRailroading.class, CarPassenger::new, EntityRollingStock.settings, ImmersiveRailroading.ENTITY_SYNC_DISTANCE);
		EntityRegistry.register(ImmersiveRailroading.class, CarTank::new, EntityRollingStock.settings, ImmersiveRailroading.ENTITY_SYNC_DISTANCE);
		EntityRegistry.register(ImmersiveRailroading.class, HandCar::new, EntityRollingStock.settings, ImmersiveRailroading.ENTITY_SYNC_DISTANCE);
		EntityRegistry.register(ImmersiveRailroading.class, LocomotiveDiesel::new, EntityRollingStock.settings, ImmersiveRailroading.ENTITY_SYNC_DISTANCE);
		EntityRegistry.register(ImmersiveRailroading.class, LocomotiveSteam::new, EntityRollingStock.settings, ImmersiveRailroading.ENTITY_SYNC_DISTANCE);
		EntityRegistry.register(ImmersiveRailroading.class, Tender::new, EntityRollingStock.settings, ImmersiveRailroading.ENTITY_SYNC_DISTANCE);

		MultiblockRegistry.register(SteamHammerMultiblock.NAME, new SteamHammerMultiblock());
		MultiblockRegistry.register(PlateRollerMultiblock.NAME, new PlateRollerMultiblock());
		MultiblockRegistry.register(RailRollerMultiblock.NAME, new RailRollerMultiblock());
		MultiblockRegistry.register(BoilerRollerMultiblock.NAME, new BoilerRollerMultiblock());
		MultiblockRegistry.register(CastingMultiblock.NAME, new CastingMultiblock());

		Packet.register(BuildableStockSyncPacket::new, PacketDirection.ServerToClient);
		Packet.register(ItemRailUpdatePacket::new, PacketDirection.ClientToServer);
		Packet.register(MRSSyncPacket::new, PacketDirection.ServerToClient);
		Packet.register(MultiblockSelectCraftPacket::new, PacketDirection.ClientToServer);
		Packet.register(PaintSyncPacket::new, PacketDirection.ServerToClient);
		Packet.register(PreviewRenderPacket::new, PacketDirection.ServerToClient);
		Packet.register(SoundPacket::new, PacketDirection.ServerToClient);

		Command.register(new IRCommand());
	}

	public ImmersiveRailroading() {
    	instance = this;

		Config.init();
		ConfigFile.sync(Config.class, getConfig("immersiverailroading.cfg"));
		ConfigFile.sync(ConfigGraphics.class, getConfig("immersiverailroading_graphics.cfg"));
		ConfigFile.sync(ConfigSound.class, getConfig("immersiverailroading_sound.cfg"));

		try {
			DefinitionManager.initDefinitions();
		} catch (IOException e) {
			throw new RuntimeException("Unable to load IR definitions", e);
		}

		IRBlocks.register();
		IRItems.register();

		CompatLoader.init();
	}

	@Override
	public String modID() {
		return MODID;
	}

	public static Consumer<Player> onKeyPress(KeyTypes type) {
		return player -> {
			if (player.getWorld().isServer && player.getRiding() instanceof EntityRollingStock) {
				player.getRiding().as(EntityRollingStock.class).handleKeyPress(player, type);
			}
		};
	}


	@Override
	public void setup()
	{
		Keyboard.registerKey("ir_keys.increase_throttle", KEY_NUMPAD8, "key.categories." + ImmersiveRailroading.MODID, onKeyPress(KeyTypes.THROTTLE_UP));
		Keyboard.registerKey("ir_keys.zero_throttle", KEY_NUMPAD5, "key.categories." + ImmersiveRailroading.MODID, onKeyPress(KeyTypes.THROTTLE_ZERO));
		Keyboard.registerKey("ir_keys.decrease_throttle", KEY_NUMPAD2, "key.categories." + ImmersiveRailroading.MODID, onKeyPress(KeyTypes.THROTTLE_DOWN));
		Keyboard.registerKey("ir_keys.increase_brake", KEY_NUMPAD7, "key.categories." + ImmersiveRailroading.MODID, onKeyPress(KeyTypes.AIR_BRAKE_UP));
		Keyboard.registerKey("ir_keys.zero_brake", KEY_NUMPAD4, "key.categories." + ImmersiveRailroading.MODID, onKeyPress(KeyTypes.AIR_BRAKE_ZERO));
		Keyboard.registerKey("ir_keys.decrease_brake", KEY_NUMPAD1, "key.categories." + ImmersiveRailroading.MODID, onKeyPress(KeyTypes.AIR_BRAKE_DOWN));
		Keyboard.registerKey("ir_keys.horn", KEY_NUMPADENTER, "key.categories." + ImmersiveRailroading.MODID, onKeyPress(KeyTypes.HORN));
		Keyboard.registerKey("ir_keys.dead_mans_switch", KEY_NUMPADEQUALS, "key.categories." + ImmersiveRailroading.MODID, onKeyPress(KeyTypes.DEAD_MANS_SWITCH));
		Keyboard.registerKey("ir_keys.start_stop_engine", KEY_ADD, "key.categories." + ImmersiveRailroading.MODID, onKeyPress(KeyTypes.START_STOP_ENGINE));
		Keyboard.registerKey("ir_keys.bell", KEY_SUBTRACT, "key.categories." + ImmersiveRailroading.MODID, onKeyPress(KeyTypes.BELL));
	}

	@Override
	protected void finalize() {
		CompatLoader.setup();
		IRFuzzy.applyFallbacks();
	}

	@Override
	protected void initClient() {
		if (ConfigSound.overrideSoundChannels) {
			SoundSystemConfig.setNumberNormalChannels(Math.max(SoundSystemConfig.getNumberNormalChannels(), 300));
		}
		BlockRender.register(IRBlocks.BLOCK_RAIL, RailBaseModel::getModel, Rail.class);
		BlockRender.register(IRBlocks.BLOCK_RAIL_GAG, RailBaseModel::getModel, RailGag.class);
		BlockRender.register(IRBlocks.BLOCK_RAIL_PREVIEW, RailPreviewRender::render, TileRailPreview.class);
		BlockRender.register(IRBlocks.BLOCK_MULTIBLOCK, TileMultiblockRender::render, TileMultiblock.class);

		ItemRender.register(IRItems.ITEM_PLATE, PlateItemModel::getModel);
		ItemRender.register(IRItems.ITEM_AUGMENT, RailAugmentItemModel::getModel);
		ItemRender.register(IRItems.ITEM_RAIL, RailItemRender::getModel);
		ItemRender.register(IRItems.ITEM_CAST_RAIL, RailCastItemRender::getModel);
		ItemRender.register(IRItems.ITEM_TRACK_BLUEPRINT, TrackBlueprintItemModel::getModel);
		ItemRender.register(IRItems.ITEM_ROLLING_STOCK_COMPONENT, StockItemComponentModel::getModel);
		ItemRender.register(IRItems.ITEM_ROLLING_STOCK, StockItemModel::getModel, StockItemModel::getIcon);
		ItemRender.register(IRItems.ITEM_LARGE_WRENCH, ObjItemRender.getModelFor(new Identifier(MODID, "models/item/large_wrench.obj"), new Vec3d(0.5, 0, 0.5), 2));
		ItemRender.register(IRItems.ITEM_CONDUCTOR_WHISTLE, ObjItemRender.getModelFor(new Identifier(MODID, "models/item/whistle.obj"), new Vec3d(0.5, 0.75, 0.5), 0.1f));
		ItemRender.register(IRItems.ITEM_GOLDEN_SPIKE, ObjItemRender.getModelFor(new Identifier(MODID, "models/item/goldenspike/goldenspike.obj"), new Vec3d(0.5, 0.5, 0.5), 0.1f));
		ItemRender.register(IRItems.ITEM_HOOK, ObjItemRender.getModelFor(new Identifier(MODID, "models/item/brake_stick.obj"), new Vec3d(0.5, 0, 0.5), 2));
		ItemRender.register(IRItems.ITEM_SWITCH_KEY, ObjItemRender.getModelFor(new Identifier(MODID, "models/item/switch_key/switch_key.obj"), new Vec3d(0.5, 0, 0.5), 1));
		ItemRender.register(IRItems.ITEM_PAINT_BRUSH, ObjItemRender.getModelFor(new Identifier(MODID, "models/item/paint_brush.obj"), new Vec3d(0.5, 0.25, 0.5), 3));
		ItemRender.register(IRItems.ITEM_RADIO_CONTROL_CARD, new Identifier(MODID, "items/radio_card"));
		ItemRender.register(IRItems.ITEM_MANUAL, new Identifier(MODID, "items/engineerslexicon"));


		IEntityRender<EntityRollingStock> stockRender = (entity, partialTicks) -> {
			GLBoolTracker light = new GLBoolTracker(GL11.GL_LIGHTING, true);
			GLBoolTracker cull = new GLBoolTracker(GL11.GL_CULL_FACE, false);

			String def = entity.getDefinitionID();

			StockRenderCache.getRender(def).draw(entity, partialTicks);

			cull.restore();
			light.restore();
		};
		EntityRenderer.register(LocomotiveSteam.class, stockRender);
		EntityRenderer.register(LocomotiveDiesel.class, stockRender);
		EntityRenderer.register(CarPassenger.class, stockRender);
		EntityRenderer.register(CarFreight.class, stockRender);
		EntityRenderer.register(CarTank.class, stockRender);
		EntityRenderer.register(Tender.class, stockRender);
		EntityRenderer.register(HandCar.class, stockRender);

		GlobalRender.registerItemMouseover(IRItems.ITEM_TRACK_BLUEPRINT, TrackBlueprintItemModel::renderMouseover);
		GlobalRender.registerItemMouseover(IRItems.ITEM_MANUAL, MBBlueprintRender::renderMouseover);

		GlobalRender.registerOverlay(pt -> {
			new SteamLocomotiveOverlay().draw();
			new DieselLocomotiveOverlay().draw();
			new HandCarOverlay().draw();
		});

		ModCore.onReload(() -> {
			try {
				DefinitionManager.initDefinitions();
			} catch (IOException e) {
				throw new RuntimeException("Unable to load IR definitions", e);
			}

			StockRenderCache.clearRenderCache();
		});

		Particles.SMOKE = IParticle.register(SmokeParticle::new, SmokeParticle::renderAll);
	}

	@Override
	protected void initServer() {
		for (EntityRollingStockDefinition def : DefinitionManager.getDefinitions()) {
			def.clearModel();
		}
	}

	public static ISound newSound(Identifier oggLocation, boolean repeats, float attenuationDistance, Gauge gauge) {
		return Audio.newSound(oggLocation, repeats, (float) (attenuationDistance * gauge.scale() * ConfigSound.soundDistanceScale), (float)Math.sqrt(Math.sqrt(gauge.scale())));
	}

    public static void debug(String msg, Object...params) {
    	if (instance == null || instance.logger == null) {
    		System.out.println("DEBUG: " + String.format(msg, params));
    		return;
    	}
    	
    	if (ConfigDebug.debugLog) {
    		instance.logger.info(String.format(msg, params));
    	}
    }
    public static void info(String msg, Object...params) {
    	if (instance == null || instance.logger == null) {
    		System.out.println("INFO: " + String.format(msg, params));
    		return;
    	}
    	
    	instance.logger.info(String.format(msg, params));
    }
    public static void warn(String msg, Object...params) {
    	if (instance == null || instance.logger == null) {
    		System.out.println("WARN: " + String.format(msg, params));
    		return;
    	}
    	
    	instance.logger.warn(String.format(msg, params));
    }
    public static void error(String msg, Object...params) {
    	if (instance == null || instance.logger == null) {
    		System.out.println("ERROR: " + String.format(msg, params));
    		return;
    	}
    	
    	instance.logger.error(String.format(msg, params));
    }
	public static void catching(Throwable ex) {
    	if (instance == null || instance.logger == null) {
    		ex.printStackTrace();
    		return;
    	}
    	
		instance.logger.catching(ex);
	}
}
