package net.jselby.escapists.layers;

import net.jselby.escapists.Map;
import net.jselby.escapists.MapRenderer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Random;

/**
 * Renders zones ontop of the world.
 */
public class ZoneLayer extends Layer {
    private java.util.Map<String, Color> zoneColorMappings = new HashMap<>();

    @Override
    public void render(Map map, MapRenderer renderer, BufferedImage image, Graphics2D g,
                       java.util.Map<Integer, BufferedImage> tileCache,
                       BufferedImage tiles, BufferedImage ground) {
        for (java.util.Map.Entry<String, Object> values : map.getMap("Zones").entrySet()) {
            String value = ((String) values.getValue()).trim();
            if (!value.contains("_")) {
                continue;
            }
            String[] args = value.split("_");
            int x1 = Integer.parseInt(args[0]);
            int y1 = Integer.parseInt(args[1]);
            int x2 = Integer.parseInt(args[2]);
            int y2 = Integer.parseInt(args[3]);

            Random r = new Random();
            Color color = zoneColorMappings.getOrDefault(values.getKey(),
                    new Color(r.nextFloat(), r.nextFloat(), r.nextFloat(), 0.3f));
            zoneColorMappings.put(values.getKey(), color);
            g.setColor(color);
            g.fillRect(x1, y1, x2 - x1, y2 - y1);
            g.setColor(Color.BLACK);
            g.drawString(values.getKey(), x1, y1 + 12);

            if (renderer.selectedZone != null
                    && values.getKey().trim().equalsIgnoreCase(renderer.selectedZone.trim())) {
                g.setColor(Color.RED);
                g.drawRect(x1, y1, x2 - x1, y2 - y1);
            }
        }
    }

    @Override
    public String getLayerName() {
        return "Zones";
    }
}
