package cam72cam.immersiverailroading.render.entity;

import java.util.List;

import cam72cam.immersiverailroading.library.ValveGearType;
import cam72cam.mod.math.Vec3d;
import cam72cam.mod.render.OpenGL;
import friedrichlp.renderlib.render.PartProperty;
import friedrichlp.renderlib.tracking.RenderObject;
import org.lwjgl.opengl.GL11;

import cam72cam.immersiverailroading.ImmersiveRailroading;
import cam72cam.immersiverailroading.entity.EntityMoveableRollingStock;
import cam72cam.immersiverailroading.library.Gauge;
import cam72cam.immersiverailroading.library.RenderComponentType;
import cam72cam.immersiverailroading.model.RenderComponent;
import cam72cam.immersiverailroading.model.MultiRenderComponent;
import cam72cam.immersiverailroading.entity.EntityRollingStock;
import cam72cam.immersiverailroading.entity.Freight;
import cam72cam.immersiverailroading.entity.LocomotiveSteam;
import cam72cam.immersiverailroading.registry.EntityRollingStockDefinition;
import cam72cam.immersiverailroading.registry.FreightDefinition;
import cam72cam.immersiverailroading.registry.LocomotiveSteamDefinition;
import cam72cam.immersiverailroading.util.VecUtil;

public class StockModel {
	private static final int MALLET_ANGLE_REAR = -45;

	private static double distanceTraveled;

	/*private void initComponents(EntityBuildableRollingStock stock) {
		this.isBuilt = stock.isBuilt();
		
		if (!isBuilt) {
			this.availComponents = new ArrayList<RenderComponentType>();
			for (ItemComponentType item : stock.getItemComponents()) {
				this.availComponents.addAll(item.render);
			}
		}
	}*/

	public static void update(EntityRollingStock stock, float partialTicks) {
		RenderObject o = stock.getRenderObject();
		Vec3d pos = stock.getPosition();
		o.setPosition((float)pos.x, (float)pos.y, (float)pos.z);
		o.setRotation(stock.getRotationPitch(), stock.getRotationYaw() - 90, 0);

		if (stock instanceof EntityMoveableRollingStock) {
			EntityMoveableRollingStock mstock = (EntityMoveableRollingStock) stock;
			distanceTraveled = mstock.distanceTraveled + mstock.getCurrentSpeed().minecraft() * mstock.getTickSkew() * partialTicks * 1.1;
		} else {
			distanceTraveled = 0;
		}

		/*if (stock instanceof LocomotiveSteam) {
			updateSteamLocomotive((LocomotiveSteam) stock);
		} else if (stock instanceof EntityMoveableRollingStock) {
			updateStandardStock((EntityMoveableRollingStock) stock);
		}*/
		//updateStandardStock((EntityMoveableRollingStock) stock);

		// ToDo: Implement
		//updateCargo(stock);
	}

	// ToDo: do update when stock cargo actually changes
	private static void updateCargo(EntityRollingStock stock) {
		if (stock instanceof Freight) {
			Freight freight = (Freight) stock;
			FreightDefinition def = freight.getDefinition();
			int fill = freight.getPercentCargoFull();
			
			List<RenderComponent> cargoLoads = def.getComponents(RenderComponentType.CARGO_FILL_X, stock.gauge);
			if (cargoLoads != null) {
				//this sorts through all the cargoLoad objects
				for (RenderComponent cargoLoad : cargoLoads) {
					if (cargoLoad.id <= fill) {
						//drawComponent(cargoLoad);
						//if the stock should only render the current cargo load it'll stop at the highest matching load
						if (def.shouldShowCurrentLoadOnly()) {
							break;
						}
					}
				}
			}
		}
	}

	private static void updateStandardStock(EntityMoveableRollingStock stock) {
		EntityRollingStockDefinition def = stock.getDefinition();
		RenderObject o = stock.getRenderObject();

		updateFrameWheels(stock);

		if (def.getComponent(RenderComponentType.BOGEY_POS, "FRONT", stock.gauge) != null) {
			RenderComponent bogeyRear = def.getComponent(RenderComponentType.BOGEY_POS, "FRONT", stock.gauge);
			for (String partName : bogeyRear.modelIDs) {
				PartProperty part = o.getPart(partName);
				part.setOrigin(def.getBogeyRear(stock.gauge), 0, 0);
				part.setRotation(0, 180 - stock.getRearYaw(), 0);
				part.rotate(0, -(180 - stock.getRotationYaw()), 0);
			}

			List<RenderComponent> wheels = def.getComponents(RenderComponentType.BOGEY_POS_WHEEL_X, "FRONT", stock.gauge);
			if (wheels != null) {
				for (RenderComponent wheel : wheels) {
					double circumference = wheel.height() * (float) Math.PI;
					double relDist = distanceTraveled % circumference;
					Vec3d wheelPos = wheel.center();

					for (String partName : wheel.modelIDs) {
						PartProperty part = o.getPart(partName);
						part.setOrigin((float)wheelPos.x, (float)wheelPos.y, (float)wheelPos.z);
						part.setRotation(0, 0, (float) (360 * relDist / circumference));
					}
				}
			}
		}
		
		if (def.getComponent(RenderComponentType.BOGEY_POS, stock.gauge) != null) {
			RenderComponent bogeyRear = def.getComponent(RenderComponentType.BOGEY_POS, "REAR", stock.gauge);
			for (String partName : bogeyRear.modelIDs) {
				PartProperty part = o.getPart(partName);
				part.setOrigin(def.getBogeyRear(stock.gauge), 0, 0);
				part.setRotation(0, 180 - stock.getRearYaw(), 0);
				part.rotate(0, -(180 - stock.getRotationYaw()), 0);
			}

			List<RenderComponent> wheels = def.getComponents(RenderComponentType.BOGEY_POS_WHEEL_X, "REAR", stock.gauge);
			if (wheels != null) {
				for (RenderComponent wheel : wheels) {
					double circumference = wheel.height() * (float) Math.PI;
					double relDist = distanceTraveled % circumference;
					Vec3d wheelPos = wheel.center();

					for (String partName : wheel.modelIDs) {
						PartProperty part = o.getPart(partName);
						part.setOrigin((float)wheelPos.x, (float)wheelPos.y, (float)wheelPos.z);
						part.setRotation(0, 0, (float) (360 * relDist / circumference));
					}
				}
			}
		}
	}

	private static void updateFrameWheels(EntityMoveableRollingStock stock) {
		EntityRollingStockDefinition def = stock.getDefinition();
		RenderObject o = stock.getRenderObject();
		
		List<RenderComponent> wheels = def.getComponents(RenderComponentType.FRAME_WHEEL_X, stock.gauge);
		if (wheels != null) {
			for (RenderComponent wheel : wheels) {
				double circumference = wheel.height() * (float) Math.PI;
				double relDist = distanceTraveled % circumference;
				Vec3d wheelPos = wheel.center();

				for (String partName : wheel.modelIDs) {
					PartProperty part = o.getPart(partName);
					part.setOrigin((float)wheelPos.x, (float)wheelPos.y, (float)wheelPos.z);
					part.setRotation(0, 0, (float) (360 * relDist / circumference));
				}
			}
		}
	}


	private static void updateSteamLocomotive(LocomotiveSteam stock) {
		LocomotiveSteamDefinition def = stock.getDefinition();

		updateBogies(stock);
		updateFrameWheels(stock);

		switch (def.getValveGear()) {
			case WALSCHAERTS: {
				List<RenderComponent> wheels = def.getComponents(RenderComponentType.WHEEL_DRIVER_X, stock.gauge);
				updateDrivingWheels(stock, wheels);
				MultiRenderComponent center = new MultiRenderComponent(wheels).scale(stock.gauge);
				RenderComponent wheel = wheels.get(wheels.size() / 2);
				updateWalschaerts(stock, "LEFT", 0, wheel.height(), center.center(), wheel.center(), false);
				updateWalschaerts(stock, "RIGHT", -90, wheel.height(), center.center(), wheel.center(), false);
			}
			break;
			case TRI_WALSCHAERTS: {
				List<RenderComponent> wheels = def.getComponents(RenderComponentType.WHEEL_DRIVER_X, stock.gauge);
				updateDrivingWheels(stock, wheels);
				MultiRenderComponent center = new MultiRenderComponent(wheels).scale(stock.gauge);
				RenderComponent wheel = wheels.get(wheels.size() / 2);
				updateWalschaerts(stock, "LEFT", 0, wheel.height(), center.center(), wheel.center(), false);
				updateWalschaerts(stock, "RIGHT", -240, wheel.height(), center.center(), wheel.center(), false);
				updateWalschaerts(stock, "CENTER", -120, wheel.height(), wheels.get(0).center(), wheels.get(0).center(), false);
				break;
			}
			case MALLET_WALSCHAERTS:
			case GARRAT:
				try (OpenGL.With matrix = OpenGL.matrix()) {
					RenderComponent frontLocomotive = def.getComponent(RenderComponentType.FRONT_LOCOMOTIVE, stock.gauge);

					Vec3d frontVec = frontLocomotive.center();
					Vec3d frontPos = stock.predictFrontBogeyPosition((float) (-frontVec.x - def.getBogeyFront(stock.gauge)));
					Vec3d frontNext = stock.predictFrontBogeyPosition((float) (-frontVec.x - def.getBogeyFront(stock.gauge) - 0.5));
					float frontWheelYaw = VecUtil.toYaw(frontPos.subtract(frontNext));
					float frontYaw = VecUtil.toYaw(frontPos) + stock.getRotationYaw() + 180;
					float frontPitch = -VecUtil.toPitch(VecUtil.rotateYaw(frontPos, stock.getRotationYaw() + 180)) + 90 + stock.getRotationPitch();

					GL11.glRotated(frontYaw, 0, 1, 0);
					GL11.glRotated(frontPitch, 0, 0, 1);

					if (frontPos.distanceTo(frontNext) > 0.1) {
						GL11.glTranslated(frontVec.x, frontVec.y, frontVec.z);
						GL11.glRotated(frontWheelYaw + stock.getRotationYaw() - frontYaw + 180, 0, 1, 0);
						GL11.glTranslated(-frontVec.x, -frontVec.y, -frontVec.z);
					}

					List<RenderComponent> wheels = def.getComponents(RenderComponentType.WHEEL_DRIVER_FRONT_X, stock.gauge);
					MultiRenderComponent center = new MultiRenderComponent(wheels).scale(stock.gauge);
					//drawComponent(def.getComponent(RenderComponentType.STEAM_CHEST_FRONT, stock.gauge));
					//drawComponent(frontLocomotive);
					updateDrivingWheels(stock, wheels);
					RenderComponent wheel = wheels.get(wheels.size() / 2);
					boolean reverse = def.getValveGear() == ValveGearType.GARRAT;
					updateWalschaerts(stock, "LEFT_FRONT", 0, wheel.height(), center.center(), wheel.center(), reverse);
					updateWalschaerts(stock, "RIGHT_FRONT", -90, wheel.height(), center.center(), wheel.center(), reverse);
				}
			{
				List<RenderComponent> wheels = def.getComponents(RenderComponentType.WHEEL_DRIVER_REAR_X, stock.gauge);
				MultiRenderComponent center = new MultiRenderComponent(wheels).scale(stock.gauge);
				updateDrivingWheels(stock, wheels);
				RenderComponent wheel = wheels.get(wheels.size() / 2);
				updateWalschaerts(stock, "LEFT_REAR", 0 + MALLET_ANGLE_REAR, center.height(), center.center(), wheel.center(), false);
				updateWalschaerts(stock, "RIGHT_REAR", -90 + MALLET_ANGLE_REAR, center.height(), center.center(), wheel.center(), false);
			}
			break;
			case CLIMAX:
				break;
			case SHAY:
				break;
			case HIDDEN: {
				List<RenderComponent> wheels = def.getComponents(RenderComponentType.WHEEL_DRIVER_X, stock.gauge);
				updateDrivingWheels(stock, wheels);
			}
			break;
			case STEPHENSON: {
				List<RenderComponent> wheels = def.getComponents(RenderComponentType.WHEEL_DRIVER_X, stock.gauge);
				RenderComponent wheel = wheels.get(wheels.size() / 2);
				updateDrivingWheels(stock, wheels);
				MultiRenderComponent center = new MultiRenderComponent(wheels).scale(stock.gauge);
				updateStephenson(stock, "LEFT", 0, wheel.height(), center.center(), wheel.center());
				updateStephenson(stock, "RIGHT", -90, wheel.height(), center.center(), wheel.center());
			}
			break;
			case T1:
				//drawComponent(def.getComponent(RenderComponentType.STEAM_CHEST_FRONT, stock.gauge));
			{
				List<RenderComponent> wheels = def.getComponents(RenderComponentType.WHEEL_DRIVER_FRONT_X, stock.gauge);
				updateDrivingWheels(stock, wheels);
				MultiRenderComponent center = new MultiRenderComponent(wheels).scale(stock.gauge);
				RenderComponent wheel = wheels.get(wheels.size() / 2);
				updateT1(stock, "LEFT_FRONT", 0, wheel.height(), center.center(), wheel.center());
				updateT1(stock, "RIGHT_FRONT", -90, wheel.height(), center.center(), wheel.center());
			}
			{
				List<RenderComponent> wheels = def.getComponents(RenderComponentType.WHEEL_DRIVER_REAR_X, stock.gauge);
				updateDrivingWheels(stock, wheels);
				MultiRenderComponent center = new MultiRenderComponent(wheels).scale(stock.gauge);
				RenderComponent wheel = wheels.get(wheels.size() / 2);

				updateT1(stock, "LEFT_REAR", 0 + MALLET_ANGLE_REAR, wheel.height(), center.center(), wheel.center());
				updateT1(stock, "RIGHT_REAR", -90 + MALLET_ANGLE_REAR, wheel.height(), center.center(), wheel.center());
			}
			break;
		}
	}


	private static void updateDrivingWheels(LocomotiveSteam stock, List<RenderComponent> wheels) {
		RenderComponent center = wheels.get(wheels.size() / 2);
		double circumference = center.height() * (float) Math.PI;
		double relDist = distanceTraveled % circumference;
		double wheelAngle = 360 * relDist / circumference;
		if (center.type == RenderComponentType.WHEEL_DRIVER_REAR_X) {
			//MALLET HACK
			wheelAngle += MALLET_ANGLE_REAR;
		}
		for (RenderComponent wheel : wheels) {
			Vec3d wheelPos = wheel.center();
			try (OpenGL.With matrix = OpenGL.matrix()) {
			GL11.glTranslated(wheelPos.x, wheelPos.y, wheelPos.z);
			GL11.glRotated((float) wheelAngle, 0, 0, 1);
			GL11.glTranslated(-wheelPos.x, -wheelPos.y, -wheelPos.z);
			//drawComponent(wheel);
			}
		}
	}


	private static void updateBogies(EntityMoveableRollingStock stock) {
		EntityRollingStockDefinition def = stock.getDefinition();
		
		RenderComponent frontBogey = def.getComponent(RenderComponentType.BOGEY_FRONT, stock.gauge);
		List<RenderComponent> frontBogeyWheels = def.getComponents(RenderComponentType.BOGEY_FRONT_WHEEL_X, stock.gauge);
		RenderComponent rearBogey = def.getComponent(RenderComponentType.BOGEY_REAR, stock.gauge);
		List<RenderComponent> rearBogeyWheels = def.getComponents(RenderComponentType.BOGEY_REAR_WHEEL_X, stock.gauge);

		if (frontBogey != null) {
			Vec3d frontVec = frontBogey.center();
			Vec3d frontPos = stock.predictFrontBogeyPosition((float) (-frontVec.x - def.getBogeyFront(stock.gauge)));
			Vec3d frontNext = stock.predictFrontBogeyPosition((float) (-frontVec.x - def.getBogeyFront(stock.gauge) - 0.5));
			float frontWheelYaw = VecUtil.toYaw(frontPos.subtract(frontNext));
			float frontYaw = VecUtil.toYaw(frontPos)+stock.getRotationYaw()+180;
			float frontPitch = -VecUtil.toPitch(VecUtil.rotateYaw(frontPos, stock.getRotationYaw()+180))+90 +stock.getRotationPitch();

			try (OpenGL.With matrix = OpenGL.matrix()) {
				GL11.glRotated(frontYaw, 0, 1, 0);
				GL11.glRotated(frontPitch, 0, 0, 1);

				if (frontPos.distanceTo(frontNext) > 0.1) {
					GL11.glTranslated(frontVec.x, frontVec.y, frontVec.z);
					GL11.glRotated(frontWheelYaw + stock.getRotationYaw() - frontYaw + 180, 0, 1, 0);
					GL11.glTranslated(-frontVec.x, -frontVec.y, -frontVec.z);
				}

				//drawComponent(frontBogey);
				if (frontBogeyWheels != null) {
					for (RenderComponent wheel : frontBogeyWheels) {
						double circumference = wheel.height() * (float) Math.PI;
						double relDist = distanceTraveled % circumference;
						Vec3d wheelPos = wheel.center();
						try (OpenGL.With wheelMatrix = OpenGL.matrix()) {
							GL11.glTranslated(wheelPos.x, wheelPos.y, wheelPos.z);
							GL11.glRotatef((float) (360 * relDist / circumference), 0, 0, 1);
							GL11.glTranslated(-wheelPos.x, -wheelPos.y, -wheelPos.z);
							//drawComponent(wheel);
						}
					}
				}
			}
		}
		if (rearBogey != null)
		{
			Vec3d rearVec = rearBogey.center();
			Vec3d rearPos = stock.predictRearBogeyPosition((float) (-rearVec.x - def.getBogeyRear(stock.gauge)));
			Vec3d rearNext = stock.predictRearBogeyPosition((float) (-rearVec.x - def.getBogeyRear(stock.gauge) - 0.5));
			float rearWheelYaw = VecUtil.toYaw(rearPos.subtract(rearNext));
			float rearYaw = VecUtil.toYaw(rearPos)+stock.getRotationYaw();
			float rearPitch = VecUtil.toPitch(VecUtil.rotateYaw(rearPos, stock.getRotationYaw()+180))-90 +stock.getRotationPitch();

			try (OpenGL.With matrix = OpenGL.matrix()) {
				GL11.glRotated(rearYaw, 0, 1, 0);
				GL11.glRotated(rearPitch, 0, 0, 1);

				if (rearPos.distanceTo(rearNext) > 0.1) {
					GL11.glTranslated(rearVec.x, rearVec.y, rearVec.z);
					GL11.glRotated(rearWheelYaw + stock.getRotationYaw() - rearYaw + 180, 0, 1, 0);

					GL11.glTranslated(-rearVec.x, -rearVec.y, -rearVec.z);
				}

				//drawComponent(rearBogey);
				if (rearBogeyWheels != null) {
					for (RenderComponent wheel : rearBogeyWheels) {
						double circumference = wheel.height() * (float) Math.PI;
						double relDist = distanceTraveled % circumference;
						Vec3d wheelPos = wheel.center();
						try (OpenGL.With wheelMatrix = OpenGL.matrix()) {
							GL11.glTranslated(wheelPos.x, wheelPos.y, wheelPos.z);
							GL11.glRotatef((float) (360 * relDist / circumference), 0, 0, 1);
							GL11.glTranslated(-wheelPos.x, -wheelPos.y, -wheelPos.z);
							//drawComponent(wheel);
						}
					}
				}
			}
		}
	}
	
	// PISTON/MAIN/SIDE
	private static void updateStephenson(LocomotiveSteam stock, String side, int wheelAngleOffset, double diameter,
										 Vec3d wheelCenter, Vec3d wheelPos) {
		LocomotiveSteamDefinition def = stock.getDefinition();
		
		double circumference = diameter * (float) Math.PI;
		double relDist = distanceTraveled % circumference;
		double wheelAngle = 360 * relDist / circumference + wheelAngleOffset;
		
		RenderComponent connectingRod = requireComponent(def, RenderComponentType.SIDE_ROD_SIDE, side, stock.gauge);
		RenderComponent drivingRod = requireComponent(def, RenderComponentType.MAIN_ROD_SIDE, side, stock.gauge);
		RenderComponent pistonRod = requireComponent(def, RenderComponentType.PISTON_ROD_SIDE, side, stock.gauge);


		// Center of the connecting rod, may not line up with a wheel directly
		Vec3d connRodPos = connectingRod.center();
		// Wheel Center is the center of all wheels, may not line up with a wheel directly
		// The difference between these centers is the radius of the connecting rod movement
		double connRodRadius = connRodPos.x - wheelCenter.x;
		// Find new connecting rod pos based on the connecting rod rod radius 
		Vec3d connRodMovment = VecUtil.fromWrongYaw(connRodRadius, (float) wheelAngle);
		
		// Draw Connecting Rod
		try (OpenGL.With matrix = OpenGL.matrix()) {
			// Move to origin
			GL11.glTranslated(-connRodRadius, 0, 0);
			// Apply connection rod movement
			GL11.glTranslated(connRodMovment.x, connRodMovment.z, 0);
			
			//drawComponent(connectingRod);
		}
		
		// X: rear driving rod X - driving rod height/2 (hack assuming diameter == height)
		// Y: Center of the rod
		// Z: does not really matter due to rotation axis
		Vec3d drivingRodRotPoint = new Vec3d(drivingRod.max().x - drivingRod.height()/2, drivingRod.center().y, drivingRod.max().z);
		// Angle for movement height vs driving rod length (adjusted for assumed diameter == height, both sides == 2r)
		float drivingRodAngle = (float) Math.toDegrees(Math.atan2(connRodMovment.z, drivingRod.length() - drivingRod.height()));
		
		// Draw driving rod
		try (OpenGL.With matrix = OpenGL.matrix()) {
			// Move to conn rod center
			GL11.glTranslated(-connRodRadius, 0, 0);
			// Apply conn rod movement
			GL11.glTranslated(connRodMovment.x, connRodMovment.z, 0);
			
			// Move to rot point center
			GL11.glTranslated(drivingRodRotPoint.x, drivingRodRotPoint.y, drivingRodRotPoint.z);
			// Rotate rod angle
			GL11.glRotated(drivingRodAngle, 0, 0, 1);
			// Move back from rot point center
			GL11.glTranslated(-drivingRodRotPoint.x, -drivingRodRotPoint.y, -drivingRodRotPoint.z);
			
			//drawComponent(drivingRod);
		}
		
		// Piston movement is rod movement offset by the rotation radius
		// Not 100% accurate, missing the offset due to angled driving rod
		double pistonDelta = connRodMovment.x - connRodRadius;
		
		// Draw piston rod and cross head
		try (OpenGL.With matrix = OpenGL.matrix()) {
			GL11.glTranslated(pistonDelta, 0, 0);
			//drawComponent(pistonRod);
		}
	}
	
	private static void updateT1(LocomotiveSteam stock, String side, int wheelAngleOffset, double diameter,
								 Vec3d wheelCenter, Vec3d wheelPos) {
		LocomotiveSteamDefinition def = stock.getDefinition();
		
		double circumference = diameter * (float) Math.PI;
		double relDist = distanceTraveled % circumference;
		double wheelAngle = 360 * relDist / circumference + wheelAngleOffset;
		
		RenderComponent connectingRod = requireComponent(def, RenderComponentType.SIDE_ROD_SIDE, side, stock.gauge);
		RenderComponent drivingRod = requireComponent(def, RenderComponentType.MAIN_ROD_SIDE, side, stock.gauge);
		RenderComponent pistonRod = requireComponent(def, RenderComponentType.PISTON_ROD_SIDE, side, stock.gauge);
		
		// Center of the connecting rod, may not line up with a wheel directly
		Vec3d connRodPos = connectingRod.center();
		// Wheel Center is the center of all wheels, may not line up with a wheel directly
		// The difference between these centers is the radius of the connecting rod movement
		double connRodRadius = connRodPos.x - wheelCenter.x;
		// Find new connecting rod pos based on the connecting rod rod radius 
		Vec3d connRodMovment = VecUtil.fromWrongYaw(connRodRadius, (float) wheelAngle);
		
		// Draw Connecting Rod
		try (OpenGL.With matrix = OpenGL.matrix()) {
			// Move to origin
			GL11.glTranslated(-connRodRadius, 0, 0);
			// Apply connection rod movement
			GL11.glTranslated(connRodMovment.x, connRodMovment.z, 0);
			
			//drawComponent(connectingRod);
		}
		
		// X: rear driving rod X - driving rod height/2 (hack assuming diameter == height)
		// Y: Center of the rod
		// Z: does not really matter due to rotation axis
		Vec3d drivingRodRotPoint = new Vec3d(drivingRod.max().x - drivingRod.height()/2, drivingRod.center().y, drivingRod.max().z);
		// Angle for movement height vs driving rod length (adjusted for assumed diameter == height, both sides == 2r)
		float drivingRodAngle = (float) Math.toDegrees(Math.atan2(connRodMovment.z, drivingRod.length() - drivingRod.height()));
		
		// Draw driving rod
		try (OpenGL.With matrix = OpenGL.matrix()) {
			// Move to conn rod center
			GL11.glTranslated(-connRodRadius, 0, 0);
			// Apply conn rod movement
			GL11.glTranslated(connRodMovment.x, connRodMovment.z, 0);
			
			// Move to rot point center
			GL11.glTranslated(drivingRodRotPoint.x, drivingRodRotPoint.y, drivingRodRotPoint.z);
			// Rotate rod angle
			GL11.glRotated(drivingRodAngle, 0, 0, 1);
			// Move back from rot point center
			GL11.glTranslated(-drivingRodRotPoint.x, -drivingRodRotPoint.y, -drivingRodRotPoint.z);
			
			//drawComponent(drivingRod);
		}
		
		// Piston movement is rod movement offset by the rotation radius
		// Not 100% accurate, missing the offset due to angled driving rod
		double pistonDelta = connRodMovment.x - connRodRadius;
		
		// Draw piston rod and cross head
		try (OpenGL.With matrix = OpenGL.matrix()) {
			GL11.glTranslated(pistonDelta, 0, 0);
			//drawComponent(pistonRod);
		}
	}
	
	private static RenderComponent requireComponent(LocomotiveSteamDefinition def, RenderComponentType rct, String side,
											  		Gauge gauge) {
		RenderComponent comp = def.getComponent(rct, side, gauge);
		if (comp == null) {
			ImmersiveRailroading.error("Missing component for %s: %s %s", def.name(), rct, side);
		}
		
		return comp;
	}
	
	private static void updateWalschaerts(LocomotiveSteam stock, String side, int wheelAngleOffset, double diameter,
										  Vec3d wheelCenter, Vec3d wheelPos, boolean reverse) {
		LocomotiveSteamDefinition def = stock.getDefinition();
		
		double circumference = diameter * (float) Math.PI;
		double relDist = distanceTraveled % circumference;
		double wheelAngle = 360 * relDist / circumference + wheelAngleOffset;
		if (reverse) {
			wheelAngle -= 90;
		}
		
		RenderComponent connectingRod = requireComponent(def, RenderComponentType.SIDE_ROD_SIDE, side, stock.gauge);
		RenderComponent drivingRod = requireComponent(def, RenderComponentType.MAIN_ROD_SIDE, side, stock.gauge);
		RenderComponent pistonRod = requireComponent(def, RenderComponentType.PISTON_ROD_SIDE, side, stock.gauge);
		RenderComponent crossHead = requireComponent(def, RenderComponentType.UNION_LINK_SIDE, side, stock.gauge);
		RenderComponent combinationLever = requireComponent(def, RenderComponentType.COMBINATION_LEVER_SIDE, side, stock.gauge);
		RenderComponent returnCrank = requireComponent(def, RenderComponentType.ECCENTRIC_CRANK_SIDE, side, stock.gauge);
		RenderComponent returnCrankRod = requireComponent(def, RenderComponentType.ECCENTRIC_ROD_SIDE, side, stock.gauge);
		RenderComponent slottedLink = requireComponent(def, RenderComponentType.EXPANSION_LINK_SIDE, side, stock.gauge);
		RenderComponent radiusBar = requireComponent(def, RenderComponentType.RADIUS_BAR_SIDE, side, stock.gauge);
		
		// Center of the connecting rod, may not line up with a wheel directly
		Vec3d connRodPos = connectingRod.center();
		// Wheel Center is the center of all wheels, may not line up with a wheel directly
		// The difference between these centers is the radius of the connecting rod movement
		double connRodRadius = connRodPos.x - wheelCenter.x;
		// Find new connecting rod pos based on the connecting rod rod radius 
		Vec3d connRodMovment = VecUtil.fromWrongYaw(connRodRadius, (float) wheelAngle);
		
		// Draw Connecting Rod
		try (OpenGL.With matrix = OpenGL.matrix()) {
			// Move to origin
			GL11.glTranslated(-connRodRadius, 0, 0);
			// Apply connection rod movement
			GL11.glTranslated(connRodMovment.x, connRodMovment.z, 0);
			
			//drawComponent(connectingRod);
		}
		
		// X: rear driving rod X - driving rod height/2 (hack assuming diameter == height)
		// Y: Center of the rod
		// Z: does not really matter due to rotation axis
		Vec3d drivingRodRotPoint = new Vec3d((reverse ? drivingRod.min() : drivingRod.max()).x - drivingRod.height()/2, drivingRod.center().y, (reverse ? drivingRod.min() : drivingRod.max()).z);
		// Angle for movement height vs driving rod length (adjusted for assumed diameter == height, both sides == 2r)
		float drivingRodAngle = (float) Math.toDegrees(Math.atan2((reverse ? -connRodMovment.z : connRodMovment.z), drivingRod.length() - drivingRod.height()));
		
		// Draw driving rod
		try (OpenGL.With matrix = OpenGL.matrix()) {
			// Move to conn rod center
			GL11.glTranslated(-connRodRadius, 0, 0);
			// Apply conn rod movement
			GL11.glTranslated(connRodMovment.x, connRodMovment.z, 0);
			
			// Move to rot point center
			GL11.glTranslated(drivingRodRotPoint.x, drivingRodRotPoint.y, drivingRodRotPoint.z);
			// Rotate rod angle
			GL11.glRotated(drivingRodAngle, 0, 0, 1);
			// Move back from rot point center
			GL11.glTranslated(-drivingRodRotPoint.x, -drivingRodRotPoint.y, -drivingRodRotPoint.z);
			
			//drawComponent(drivingRod);
		}
		
		// Piston movement is rod movement offset by the rotation radius
		// Not 100% accurate, missing the offset due to angled driving rod
		double pistonDelta = connRodMovment.x - connRodRadius;
		
		// Draw piston rod and cross head
		try (OpenGL.With matrix = OpenGL.matrix()) {
			GL11.glTranslated(pistonDelta, 0, 0);
			//drawComponent(pistonRod);
			//drawComponent(crossHead);
		}

		Vec3d returnCrankRotPoint = reverse ?
				returnCrank.min().add(returnCrank.height()/2, returnCrank.height()/2, 0) :
				returnCrank.max().add(-returnCrank.height()/2, -returnCrank.height()/2, 0);
		Vec3d wheelRotationOffset = reverse ?
				VecUtil.fromWrongYaw(returnCrankRotPoint.x - wheelPos.x, (float) wheelAngle) :
				VecUtil.fromWrongYaw(returnCrankRotPoint.x - wheelPos.x, (float) wheelAngle);
		Vec3d returnCrankOriginOffset = wheelPos.add(wheelRotationOffset.x, wheelRotationOffset.z, 0);
		double returnCrankAngle = wheelAngle + 90 + 30;
		try (OpenGL.With matrix = OpenGL.matrix()) {
			// Move to crank offset from origin
			GL11.glTranslated(returnCrankOriginOffset.x, returnCrankOriginOffset.y, 0);
			// Rotate crank
			GL11.glRotated(returnCrankAngle, 0, 0, 1);
			// Draw return crank at current position
			GL11.glTranslated(-returnCrankRotPoint.x, -returnCrankRotPoint.y, 0);
			//drawComponent(returnCrank);
		}

		// We take the length of the crank and subtract the radius on either side.
		// We use rod radius and crank radius since it can be a funny shape 
		double returnCrankLength = -(returnCrank.length() - returnCrank.height()/2 - returnCrankRod.height()/2);
		// Rotation offset around the return crank point
		Vec3d returnCrankRotationOffset = VecUtil.fromWrongYaw(returnCrankLength, (float) returnCrankAngle + (reverse ? 90 : -90));
		// Combine wheel->crankpoint offset and the crankpoint->crankrod offset 
		Vec3d returnCrankRodOriginOffset = returnCrankOriginOffset.add(returnCrankRotationOffset.x, returnCrankRotationOffset.z, 0);
		// Point about which the return crank rotates
		Vec3d returnCrankRodRotPoint = reverse ?
				returnCrankRod.min().add(returnCrankRod.height()/2, returnCrankRod.height()/2, 0) :
				returnCrankRod.max().add(-returnCrankRod.height()/2, -returnCrankRod.height()/2, 0);
		// Length between return crank rod centers
		double returnCrankRodLength = returnCrankRod.length() - returnCrankRod.height()/2; 
		// Height that the return crank rod should shoot for
		double slottedLinkLowest = slottedLink.min().y + slottedLink.width()/2;
		// Fudge
		double returnCrankRodFudge = reverse ?
				Math.abs(slottedLink.center().x - (returnCrankRodOriginOffset.x + returnCrankRodLength))/3 :
                Math.abs(slottedLink.center().x - (returnCrankRodOriginOffset.x - returnCrankRodLength))/3;
		float returnCrankRodRot = reverse ?
				-VecUtil.toWrongYaw(new Vec3d(slottedLinkLowest - returnCrankRodOriginOffset.y + returnCrankRodFudge, 0, returnCrankRodLength)) :
				VecUtil.toWrongYaw(new Vec3d(slottedLinkLowest - returnCrankRodOriginOffset.y + returnCrankRodFudge, 0, returnCrankRodLength));
		// Angle the return crank rod should be at to hit the slotted link
		try (OpenGL.With matrix = OpenGL.matrix()) {
			// Move to crank rod offset from origin
			GL11.glTranslated(returnCrankRodOriginOffset.x, returnCrankRodOriginOffset.y, 0);
			
			GL11.glRotated(returnCrankRodRot, 0, 0, 1);
			
			// Draw return crank rod at current position
			GL11.glTranslated(-returnCrankRodRotPoint.x, -returnCrankRodRotPoint.y, 0);
			//drawComponent(returnCrankRod);
		}
		
		Vec3d returnCrankRodRotationOffset = VecUtil.fromWrongYaw(returnCrankRodLength, returnCrankRodRot+(reverse ? -90 : 90));
		Vec3d returnCrankRodFarPoint = returnCrankRodOriginOffset.add(returnCrankRodRotationOffset.x, returnCrankRodRotationOffset.z, 0);
		// Slotted link rotation point
		Vec3d slottedLinkRotPoint = slottedLink.center();
		double slottedLinkRot = Math.toDegrees(Math.atan2(-slottedLinkRotPoint.x + returnCrankRodFarPoint.x, slottedLinkRotPoint.y - returnCrankRodFarPoint.y));
		try (OpenGL.With matrix = OpenGL.matrix()) {
			// Move to origin
			GL11.glTranslated(slottedLinkRotPoint.x, slottedLinkRotPoint.y, 0);
			
			// Rotate around center point
			GL11.glRotated(slottedLinkRot, 0, 0, 1);
			
			// Draw slotted link at current position
			GL11.glTranslated(-slottedLinkRotPoint.x, -slottedLinkRotPoint.y, 0);
			//drawComponent(slottedLink);
		}
		
		float throttle = stock.getThrottle();
		double forwardMax = (slottedLink.min().y - slottedLinkRotPoint.y) * 0.4;
		double forwardMin = (slottedLink.max().y - slottedLinkRotPoint.y) * 0.65;
		double throttleSlotPos = 0;
		if (throttle > 0) {
			throttleSlotPos = forwardMax * throttle;
		} else {
			throttleSlotPos = forwardMin * -throttle;
		}
		
		double radiusBarSliding = Math.sin(Math.toRadians(-slottedLinkRot)) * (throttleSlotPos);
		
		Vec3d radiusBarClose = reverse ? radiusBar.min() : radiusBar.max();
		throttleSlotPos += slottedLinkRotPoint.y - radiusBar.max().y;
		
		float raidiusBarAngle = reverse ?
				-(VecUtil.toWrongYaw(new Vec3d(radiusBar.length(), 0, throttleSlotPos))+90) :
                VecUtil.toWrongYaw(new Vec3d(radiusBar.length(), 0, throttleSlotPos))+90;

		try (OpenGL.With matrix = OpenGL.matrix()) {
			GL11.glTranslated(0, throttleSlotPos, 0);
			
			GL11.glTranslated(radiusBarSliding, 0, 0);
			
			GL11.glTranslated(radiusBarClose.x, radiusBarClose.y, 0);
			GL11.glRotated(raidiusBarAngle, 0, 0, 1);
			GL11.glTranslated(-radiusBarClose.x, -radiusBarClose.y, 0);
			//drawComponent(radiusBar);
		}
		
		Vec3d radiusBarFar = reverse ? radiusBar.max() : radiusBar.min();
		//radiusBarSliding != correct TODO angle offset
		Vec3d radiusBarFarPoint = radiusBarFar.add(radiusBarSliding + combinationLever.width()/2, 0, 0);
		
		Vec3d combinationLeverRotPos = combinationLever.min().add(combinationLever.width()/2, combinationLever.width()/2, 0);
		
		Vec3d delta = radiusBarFarPoint.subtract(combinationLeverRotPos.add(pistonDelta, 0, 0));
		
		float combinationLeverAngle = VecUtil.toWrongYaw(new Vec3d(delta.x, 0, delta.y));

		try (OpenGL.With matrix = OpenGL.matrix()) {
			GL11.glTranslated(pistonDelta, 0, 0);
			GL11.glTranslated(combinationLeverRotPos.x, combinationLeverRotPos.y, 0);
			GL11.glRotated(combinationLeverAngle, 0, 0, 1);
			GL11.glTranslated(-combinationLeverRotPos.x, -combinationLeverRotPos.y, 0);
			//drawComponent(combinationLever);
		}
	}
}
