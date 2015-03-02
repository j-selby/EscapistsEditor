package net.jselby.escapists.layers;

import net.jselby.escapists.Map;
import net.jselby.escapists.MapRenderer;

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

        genericRender(map, image, g, tileCache, tiles, 2);
    }

    public String getLayerName() {
        return "Vents";
    }
}
