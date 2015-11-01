package net.jselby.escapists.editor.mapping;

import net.jselby.escapists.editor.layers.Layer;
import net.jselby.escapists.editor.layers.impl.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Renders maps into a BufferedImage.
 *
 * @author j_selby
 */
public class MapRenderer {

    // These are public for ease-of-use
    public boolean showZones = false;
    public boolean zoneEditing = false;

    public String view = "World";

    // Caches individual assets
    private java.util.Map<Integer, BufferedImage> tileCache = new HashMap<>();

    // Caches entire layers
    public java.util.Map<String, BufferedImage> layerCache = new HashMap<>();

    private java.util.List<Layer> layers = new ArrayList<>();

    public MapRenderer() {
        // Build layers
        layers.add(new UndergroundLayer());
        layers.add(new WorldLayer());
        layers.add(new VentsLayer());
        layers.add(new RoofLayer());
        layers.add(new ZoneLayer());
    }

    public BufferedImage render(Map map, String view) {
        int renderView = 1;
        if (view.equalsIgnoreCase("Underground")) {
            renderView = 0;
        } else if (view.equalsIgnoreCase("Vents")) {
            renderView = 2;
        } else if (view.equalsIgnoreCase("Roof")) {
            renderView = 3;
        }

        BufferedImage renderPlatform =
                new BufferedImage((map.getHeight() - 1) * 16, (map.getWidth() - 3) * 16,
                        BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = renderPlatform.createGraphics();

        // Render this layer
        g.drawImage(renderLayer(map, renderView), null, 0, 0);

        // Render zones, if required
        if (showZones) {
            g.drawImage(renderLayer(map, 4), null, 0, 0);
        }

        return renderPlatform;
    }

    private BufferedImage renderLayer(Map map, int layer) {
        // Compute view

        // Get that index
        Layer mainLayer = layers.get(layer);

        // Get tiles, and render it
        BufferedImage renderPlatform =
                new BufferedImage((map.getHeight() - 1) * 16, (map.getWidth() - 3) * 16,
                        mainLayer.isTransparent() ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);

        Graphics2D g = renderPlatform.createGraphics();

        // Get tiles
        BufferedImage tiles = map.getTilesImage();
        BufferedImage ground = map.getBackgroundImage();

        // Make sure dependencies are sorted
        if (mainLayer.getLayerName().equalsIgnoreCase("Roof")
                && !layerCache.containsKey("World")) {
            // Render world first
            renderLayer(map, 1);
        }

        // Render that layer
        mainLayer.render(map, this, renderPlatform, g, tileCache, tiles, ground);

        // Add it to the main index
        layerCache.put(mainLayer.getLayerName(), renderPlatform);

        return renderPlatform;
    }

    public ZoneLayer getZoning() {
        return (ZoneLayer) layers.get(4);
    }
}
