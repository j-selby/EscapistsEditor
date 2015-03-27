package net.jselby.escapists.layers.impl;

import net.jselby.escapists.layers.Layer;
import net.jselby.escapists.mapping.Map;
import net.jselby.escapists.mapping.MapRenderer;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Renders under the world.
 */
public class UndergroundLayer extends Layer {
    @Override
    public void render(Map map, MapRenderer renderer, BufferedImage image, Graphics2D g,
                       java.util.Map<Integer, BufferedImage> tileCache,
                       BufferedImage tiles, BufferedImage ground) {

        g.drawImage(renderer.layerCache.get("World"), null, 0, 0);

        g.setColor(new Color(0f, 0f, 0f, 0.8f));
        g.fillRect(0, 0, image.getWidth(), image.getHeight());

        genericRender(map, image, g, tileCache, tiles, 0);
    }

    public String getLayerName() {
        return "Underground";
    }

    @Override
    public boolean isTransparent() {
        return false;
    }
}
