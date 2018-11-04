package cam72cam.immersiverailroading.gui.overlay;

import cam72cam.immersiverailroading.entity.CarFreight;
import cam72cam.immersiverailroading.entity.EntityCoupleableRollingStock.CouplerType;
import cam72cam.immersiverailroading.entity.Locomotive;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class FreightCarOverlay extends LocomotiveOverlay {
	public static final ResourceLocation OVERLAY_CARFREIGHT_TEXTURE = new ResourceLocation("immersiverailroading:gui/overlay_freightcar.png");
	public static final ResourceLocation OVERLAY_CARFREIGHT_COUPLER_LEFT_TEXTURE = new ResourceLocation("immersiverailroading:gui/overlay_freightcar_coupler_left.png");
	public static final ResourceLocation OVERLAY_CARFREIGHT_COUPLER_RIGHT_TEXTURE = new ResourceLocation("immersiverailroading:gui/overlay_freightcar_coupler_right.png");
	
	public void draw() {
        Entity riding = mc.player.getRidingEntity();
        if (riding == null) {
            return;
        }
        if (!(riding instanceof CarFreight)) {
            return;
        }
        
        CarFreight freight = (CarFreight) riding;
		drawSpeedDisplay(freight, 0);  
        drawTexture(OVERLAY_CARFREIGHT_TEXTURE, 256, 256);
        addSpace(10);
        
        if (freight.getCoupled(CouplerType.BACK) != null) {
            drawTexture(OVERLAY_CARFREIGHT_COUPLER_LEFT_TEXTURE, 256, 256);
            drawCenteredString(mc.fontRenderer, "Connected", 0, 0, 0xFFFFFF);
        } else {
        	drawCenteredString(mc.fontRenderer, "Disconnected", 0, 0, 0xFFFFFF);
        }
        
        if (freight.getCoupled(CouplerType.FRONT) != null) {
            drawTexture(OVERLAY_CARFREIGHT_COUPLER_RIGHT_TEXTURE, 256, 256);
            drawCenteredString(mc.fontRenderer, "Connected", 0, 0, 0xFFFFFF);
        } else {
        	drawCenteredString(mc.fontRenderer, "Disconnected", 0, 0, 0xFFFFFF);
        }
	
    }
    
    public void drawTexture (ResourceLocation texture, int textureHeight, int textureWidth) {
        mc.renderEngine.bindTexture(texture);
        drawTexturedModalRect(bgPosX, bgPosY, 0, 0, textureHeight, textureWidth);
    }
}