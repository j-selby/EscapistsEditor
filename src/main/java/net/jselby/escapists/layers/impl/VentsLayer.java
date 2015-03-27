package net.jselby.escapists.layers.impl;

import net.jselby.escapists.layers.Layer;
import net.jselby.escapists.mapping.Map;
import net.jselby.escapists.mapping.MapRenderer;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Renders vents.
 */
public class VentsLayer extends Layer {
    @Override
    public void render(Map map, MapRenderer renderer, BufferedImage image, Graphics2D g,
                       java.util.Map<Integer, BufferedImage> tileCache,
                       BufferedImage tiles, BufferedImage ground) {
        // Black out everything
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());

        g.drawImage(renderer.layerCache.get("World"), null, 0, 0);

        g.setColor(new Color(1f, 1f, 1f, 0.8f));
        g.fillRect(0, 0, image.getWidth(), image.getHeight());

        genericRender(map, image, g, tileCache, tiles, 2);
    }

    public String getLayerName() {
        return "Vents";
    }

    @Override
    public boolean isTransparent() {
        return false;
    }
}
