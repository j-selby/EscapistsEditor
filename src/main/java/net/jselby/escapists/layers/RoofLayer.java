package net.jselby.escapists.layers;

import net.jselby.escapists.Map;
import net.jselby.escapists.MapRenderer;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Renders the main world.
 */
public class RoofLayer extends Layer {
    @Override
    public void render(Map map, MapRenderer renderer, BufferedImage image, Graphics2D g,
                       java.util.Map<Integer, BufferedImage> tileCache,
                       BufferedImage tiles, BufferedImage ground) {

        // Draw the background floor
        g.drawImage(renderer.layerCache.get("World"), null, 0, 0);

        g.setColor(new Color(1f, 1f, 1f, 0.8f));
        g.fillRect(0, 0, image.getWidth(), image.getHeight());

        genericRender(map, image, g, tileCache, tiles, 3);
    }

    public String getLayerName() {
        return "Roof";
    }

    @Override
    public boolean isTransparent() {
        return false;
    }
}
