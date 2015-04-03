package net.jselby.escapists.layers.impl;

import net.jselby.escapists.layers.Layer;
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
        for (int x = 0; x < image.getWidth(); x += ground.getWidth()) {
            for (int y = 0; y < image.getHeight(); y+= ground.getHeight()) {
                g.drawImage(ground, null, x, y);
            }
        }

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
