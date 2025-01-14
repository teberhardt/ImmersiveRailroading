package cam72cam.immersiverailroading.gui.overlay;

import cam72cam.immersiverailroading.entity.*;
import cam72cam.immersiverailroading.library.GuiText;
import cam72cam.mod.gui.helpers.GUIHelpers;
import cam72cam.mod.math.Vec3d;
import cam72cam.mod.render.OpenGL;
import cam72cam.mod.resource.Identifier;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.lwjgl.opengl.GL11;
import util.Matrix4;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class GuiBuilder {
    private final float x;
    private final float y;
    private final boolean centerx;
    private final boolean centery;

    private final Identifier image;
    private final int imageWidth;
    private final int imageHeight;

    private final String text;
    private final float textHeight;

    private final Readouts readout;
    private final String control;
    private final boolean invert;
    private final boolean hide;
    private final float tlx;
    private final float tly;
    private final float rotx;
    private final float roty;
    private final float rotdeg;
    private final float rotoff;
    private final Float scalex;
    private final Float scaley;

    private final Map<Float, Integer> colors = new HashMap<>();

    private final List<GuiBuilder> elements;

    protected GuiBuilder(JsonObject data) throws IOException {
        // common stuff
        this.x = data.has("x") ? data.get("x").getAsFloat() : 0;
        this.y = data.has("y") ? data.get("y").getAsFloat() : 0;
        if (data.has("centered")) {
            JsonObject centered = data.get("centered").getAsJsonObject();
            this.centerx = centered.has("x") && centered.get("x").getAsBoolean();
            this.centery = centered.has("y") && centered.get("y").getAsBoolean();
        } else {
            this.centerx = this.centery = false;
        }

        // Image stuff
        this.image = data.has("image") ? new Identifier(data.get("image").getAsString()) : null;
        if (image != null) {
            BufferedImage tmp = ImageIO.read(this.image.getResourceStream());
            imageWidth = tmp.getWidth();
            imageHeight = tmp.getHeight();
        } else {
            imageWidth = 0;
            imageHeight = 0;
        }

        // Text stuff
        if (data.has("text")) {
            JsonObject txt = data.get("text").getAsJsonObject();
            text = txt.get("value").getAsString();
            textHeight = txt.has("height") ? txt.get("height").getAsFloat() : 8;
        } else {
            text = null;
            textHeight = 0;
        }

        // Controls
        this.readout = data.has("readout") ? Readouts.valueOf(data.get("readout").getAsString().toUpperCase(Locale.ROOT)) : null;
        this.control = data.has("control") ? data.get("control").getAsString() : null;
        this.invert = data.has("invert") && data.get("invert").getAsBoolean();
        this.hide = data.has("hide") && data.get("hide").getAsBoolean();

        if (data.has("translate")) {
            JsonObject tl = data.get("translate").getAsJsonObject();
            this.tlx = tl.has("x") ? tl.get("x").getAsFloat() : 0;
            this.tly = tl.has("y") ? tl.get("y").getAsFloat() : 0;
        } else {
            tlx = tly = 0;
        }

        if (data.has("rotate")) {
            JsonObject rot = data.get("rotate").getAsJsonObject();
            this.rotx = rot.has("x") ? rot.get("x").getAsFloat() : 0;
            this.roty = rot.has("y") ? rot.get("y").getAsFloat() : 0;
            this.rotdeg = rot.has("degrees") ? rot.get("degrees").getAsFloat() : 360;
            this.rotoff = rot.has("offset") ? rot.get("offset").getAsFloat() : 0;
        } else {
            this.rotx = 0;
            this.roty = 0;
            this.rotdeg = 0;
            this.rotoff = 0;
        }

        if (data.has("scale")) {
            JsonObject scale = data.get("scale").getAsJsonObject();
            this.scalex = scale.has("x") ? scale.get("x").getAsFloat() : null;
            this.scaley = scale.has("y") ? scale.get("y").getAsFloat() : null;
        } else {
            this.scalex = null;
            this.scaley = null;
        }

        if (data.has("color")) {
            for (Map.Entry<String, JsonElement> entry : data.get("color").getAsJsonObject().entrySet()) {
                String hex = entry.getValue().getAsString();
                if (hex.length() == 8) {
                    hex = hex.replace("0x", "0xFF");
                    hex = hex.replace("0X", "0XFF");
                }
                colors.put(Float.parseFloat(entry.getKey()), (int)(long)Long.decode(hex));
            }
        }

        // Children
        if (data.has("elements")) {
            elements = new ArrayList<>();
            for (JsonElement element : data.get("elements").getAsJsonArray()) {
                elements.add(new GuiBuilder(element.getAsJsonObject()));
            }
        } else {
            elements = Collections.emptyList();
        }
    }

    public static GuiBuilder parse(Identifier overlay) throws IOException {
        return new GuiBuilder(new JsonParser().parse(new InputStreamReader(overlay.getResourceStream())).getAsJsonObject());
    }

    public void render(EntityRollingStock stock) {
        GL11.glColor4f(1, 1, 1, 1);
        render(stock, new Matrix4(), GUIHelpers.getScreenWidth(), GUIHelpers.getScreenHeight());
    }
    private void render(EntityRollingStock stock, Matrix4 m, int maxx, int maxy) {
        m = m.copy(); // TODO mem opt?
        m.translate(x, y, 0);
        if (centerx) {
            m.translate(maxx/2f, 0, 0);
        }
        if (centery) {
            m.translate(0, maxy/2f, 0);
        }

        float value = 0;
        if (readout != null) {
            value = readout.getValue(stock);
        } else if (control != null) {
            value = stock.getControlPosition(control);
        }

        if (invert) {
            value = 1 - value;
        }
        if (hide && value != 1) {
            return;
        }

        if (tlx != 0 || tly != 0) {
            m.translate(tlx * value, tly * value, 0);
        }
        if (rotdeg != 0) {
            m.translate(rotx, roty, 0);
            m.rotate(Math.toRadians(rotdeg * value + rotoff), 0, 0, 1);
            m.translate(-rotx, -roty, 0);
        }
        if (scalex != null || scaley != null) {
            m.scale(scalex != null ? scalex * value : 1, scaley != null ? scaley * value : 1, 1);
        }

        Vec3d offset = m.apply(Vec3d.ZERO);
        if (offset.x < 0) {
            m.translate(maxx, 0, 0);
        }
        if (offset.y < 0) {
            m.translate(0, maxy, 0);
        }

        Float colorKey = null;
        for (float key : colors.keySet()) {
            if (key <= value && (colorKey == null || key > colorKey)) {
                colorKey = key;
            }
        }

        int col = colors.getOrDefault(colorKey, 0xFFFFFFFF);
        try (OpenGL.With c = colorKey == null ?
                () -> {} :
                OpenGL.color((col >> 16 & 255) / 255.0f, (col >> 8 & 255) / 255.0f, (col & 255) / 255.0f, (col >> 24 & 255) / 255.0f)
        ) {
            try (OpenGL.With matrix = OpenGL.matrix()) {
                m.transpose();
                OpenGL.multMatrix(m);
                m.transpose();

                if (image != null) {
                    try (
                            OpenGL.With tex = OpenGL.texture(image);
                            OpenGL.With alpha = OpenGL.bool(GL11.GL_ALPHA_TEST, false);
                            OpenGL.With blend = OpenGL.blend(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
                    ) {
                        GL11.glBegin(GL11.GL_QUADS);
                        GL11.glTexCoord2d(0, 0);
                        GL11.glVertex3d(0, 0, 0);
                        GL11.glTexCoord2d(0, 1);
                        GL11.glVertex3d(0, imageHeight, 0);
                        GL11.glTexCoord2d(1, 1);
                        GL11.glVertex3d(imageWidth, imageHeight, 0);
                        GL11.glTexCoord2d(1, 0);
                        GL11.glVertex3d(imageWidth, 0, 0);
                        GL11.glEnd();
                    }
                }
                if (text != null) {
                    String out = text;
                    for (Stat stat : Stat.values()) {
                        if (out.contains(stat.toString())) {
                            out = out.replace(stat.toString(), stat.getValue(stock));
                        }
                    }
                    for (GuiText label : new GuiText[]{GuiText.LABEL_THROTTLE, GuiText.LABEL_REVERSER, GuiText.LABEL_BRAKE}) {
                        out = out.replace(label.getValue(), label.toString());
                    }
                    // Text is 8px tall
                    try (OpenGL.With textmatrix = OpenGL.matrix()) {
                        float scale = textHeight / 8f;
                        GL11.glScalef(scale, scale, scale);
                        GUIHelpers.drawCenteredString(out, 0, 0, col);
                    }
                }
            }
            for (GuiBuilder element : elements) {
                element.render(stock, m, maxx, maxy);
            }
        }
    }
}
