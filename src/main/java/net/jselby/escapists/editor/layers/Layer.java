package net.jselby.escapists.editor.layers;

import net.jselby.escapists.editor.mapping.Map;
import net.jselby.escapists.editor.mapping.MapRenderer;
import net.jselby.escapists.editor.objects.WorldObject;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * A layer is a renderable layer for the MapRenderer which lays out everything in a proper fashion.
 */
public abstract class Layer {
    public abstract void render(Map map, MapRenderer renderer,

                       BufferedImage image, Graphics2D g,

                       java.util.Map<Integer, BufferedImage> tileCache,
                       BufferedImage tiles,
                       BufferedImage ground);

    protected void genericRender(Map map,

                                 BufferedImage image, Graphics2D g,

                                 java.util.Map<Integer, BufferedImage> tileCache,
                                 BufferedImage tiles,

                                 int layer) {
        BufferedImage lighting =
                new BufferedImage(image.getWidth(), image.getHeight(),
                        BufferedImage.TYPE_INT_ARGB);
        Graphics2D gLighting = lighting.createGraphics();

        // Add some minimal darkness to the world
        gLighting.setColor(new Color(0f, 0f, 0f, 0.1f));
        gLighting.fillRect(0, 0, lighting.getWidth(), lighting.getHeight());

        drawTiles(map, g, tileCache, tiles, getLayerName());

        // Draw objects
        for (WorldObject object : map.getObjects()) {
            if (object.getIDArgument() == layer) {
                object.draw(g, gLighting);
            }
        }

        // Merge lighting
        g.drawImage(lighting, null, 0, 0);

        // Done!
        // No need to return, our parent already has everything.
    }

    protected void drawTiles(Map map, Graphics2D g,
                           java.util.Map<Integer, BufferedImage> tileCache, BufferedImage tiles,
                           String layer) {
        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                int tile = map.getTile(x, y, layer);
                if (tile == 0) {
                    continue;
                }
                tile--;

                // Get tile from main tiles
                int tileX = (int) Math.floor(((double) tile) / (tiles.getHeight() / 16));
                int tileY = tile - (tileX * (tiles.getHeight() / 16));
                int absTileX = tileX * 16;
                int absTileY = tileY * 16;

                if (!tileCache.containsKey(tile)) {
                    tileCache.put(tile, tiles.getSubimage(absTileX, absTileY, 16, 16));
                }
                BufferedImage tileRender = tileCache.get(tile);

                g.drawImage(tileRender, null, x * 16, y * 16);
            }
        }
    }

    public abstract String getLayerName();

    public abstract boolean isTransparent();
}
