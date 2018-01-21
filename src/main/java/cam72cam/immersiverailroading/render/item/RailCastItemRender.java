package cam72cam.immersiverailroading.render.item;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import cam72cam.immersiverailroading.ImmersiveRailroading;
import cam72cam.immersiverailroading.model.obj.OBJModel;
import cam72cam.immersiverailroading.render.OBJRender;
import cam72cam.immersiverailroading.util.GLBoolTracker;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ItemTransformVec3f;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

@SuppressWarnings("deprecation")
public class RailCastItemRender implements IBakedModel {
	private static OBJRender model;
	private static List<String> groups;

	static {
		try {
			model = new OBJRender(new OBJModel(new ResourceLocation(ImmersiveRailroading.MODID, "models/multiblocks/rail_machine.obj"), 0.05f));
			groups = new ArrayList<String>();
			
			for (String groupName : model.model.groups())  {
				if (groupName.contains("INPUT_CAST")) {
					groups.add(groupName);
				}
			}
		} catch (Exception e) {
			ImmersiveRailroading.catching(e);
		}
	}


	@Override
	public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
		GL11.glPushMatrix();
		{
			GLBoolTracker tex = new GLBoolTracker(GL11.GL_TEXTURE_2D, true);
			model.bindTexture();
			GL11.glRotated(90, 1, 0, 0);
			GL11.glTranslated(0, -1, 1);
			GL11.glTranslated(-0.5, 0.6, 0.6);
			model.drawGroups(groups);
			model.restoreTexture();
			tex.restore();
		}
		GL11.glPopMatrix();
		return new ArrayList<BakedQuad>();
	}

	@Override
	public boolean isAmbientOcclusion() {
		return true;
	}

	@Override
	public boolean isGui3d() {
		return true;
	}

	@Override
	public boolean isBuiltInRenderer() {
		return false;
	}

	@Override
	public TextureAtlasSprite getParticleTexture() {
		return Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(Blocks.IRON_BLOCK.getDefaultState()).getParticleTexture();
	}

	@Override
	public ItemOverrideList getOverrides() {
		return ItemOverrideList.NONE;
	}
	
	@Override
	public ItemCameraTransforms getItemCameraTransforms() {
		return new ItemCameraTransforms(ItemCameraTransforms.DEFAULT) {
			public ItemTransformVec3f getTransform(ItemCameraTransforms.TransformType type) {
				switch (type) {
				case GUI:
					return new ItemTransformVec3f(new Vector3f(0, 0, 0), new Vector3f(0, -0.5f, 0), new Vector3f(1, 0.1f, 1));
				default:
					break;
				}
				
				return super.getTransform(type);
			}
		};
	}
}
