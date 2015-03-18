package net.jselby.escapists.layers;

import net.jselby.escapists.mapping.Map;
import net.jselby.escapists.mapping.MapRenderer;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Renders the main world.
 */
public class WorldLayer extends Layer {
    @Override
    public void render(Map map, MapRenderer renderer, BufferedImage image, Graphics2D g,
                       java.util.Map<Integer, BufferedImage> tileCache,
                       BufferedImage tiles, BufferedImage ground) {

        // Draw the ground texture
        g.drawImage(ground, null, 0, 0);

        genericRender(map, image, g, tileCache, tiles, 1);
    }

    public String getLayerName() {
        return "World";
    }

    @Override
    public boolean isTransparent() {
        return false;
    }
}
