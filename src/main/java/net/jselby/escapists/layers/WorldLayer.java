package net.jselby.escapists.layers;

import net.jselby.escapists.Map;
import net.jselby.escapists.MapRenderer;
import net.jselby.escapists.WorldObject;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Random;

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

        genericRender(map, renderer, image, g, tileCache, tiles, ground, 1);
    }

    public String getLayerName() {
        return "World";
    }
}
