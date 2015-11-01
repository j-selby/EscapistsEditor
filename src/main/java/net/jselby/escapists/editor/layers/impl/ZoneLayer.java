package net.jselby.escapists.editor.layers.impl;

import net.jselby.escapists.editor.layers.Layer;
import net.jselby.escapists.editor.mapping.Map;
import net.jselby.escapists.editor.mapping.MapRenderer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Random;

/**
 * Renders zones ontop of the world.
 */
public class ZoneLayer extends Layer {

    private static final int margin = 10;

    private java.util.Map<String, Color> zoneColorMappings = new HashMap<>();
    private java.util.Map.Entry<String, Object> zoneClicked;

    private int origX;
    private int origY;

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
            if (!zoneColorMappings.containsKey(values.getKey())) {
                zoneColorMappings.put(values.getKey(),
                        new Color(r.nextFloat(), r.nextFloat(), r.nextFloat(), 0.3f));
            }
            Color color = zoneColorMappings.get(values.getKey());
            zoneColorMappings.put(values.getKey(), color);
            g.setColor(color);
            g.fillRect(x1, y1, x2 - x1, y2 - y1);

            g.setColor(Color.RED);
            g.drawRect(x1, y1, x2 - x1, y2 - y1);

            // Draw little dragging spots
            if (renderer.zoneEditing) {
                g.setColor(Color.WHITE);
                g.fillRect(x1, y1, margin, margin); // Top left
                g.fillRect(x2 - margin, y1, margin, margin); // Top right
                g.fillRect(x2 - margin, y2 - margin, margin, margin); // Bottom right
                g.fillRect(x1, y2 - margin, margin, margin); // Bottom left
                g.setColor(Color.BLACK);
                g.drawRect(x1, y1, margin, margin); // Top left
                g.drawRect(x2 - margin, y1, margin, margin); // Top right
                g.drawRect(x2 - margin, y2 - margin, margin, margin); // Bottom right
                g.drawRect(x1, y2 - margin, margin, margin); // Bottom left
            }

            g.setColor(Color.WHITE);
            g.drawString(values.getKey(), x1 + 12, y1 + 12);
        }
    }

    @Override
    public String getLayerName() {
        return "Zones";
    }

    @Override
    public boolean isTransparent() {
        return true;
    }

    public void mouseDragged(Map map, int x, int y) {
        // Update the zone accordingly
        if (zoneClicked != null) {
            // Move the point around

            // Build arguments
            String[] args = zoneClicked.getValue().toString().trim().split("_");
            int zoneX1 = Integer.parseInt(args[0]);
            int zoneY1 = Integer.parseInt(args[1]);
            int zoneX2 = Integer.parseInt(args[2]);
            int zoneY2 = Integer.parseInt(args[3]);

            // Update the actual values
            int diffX = x - origX;
            int diffY = y - origY;

            int margin = 12;
            // Check if it is in a corner
            if (origX < zoneX1 + margin
                    && origY < zoneY1 + margin) { // Top left corner
                zoneX1 += diffX;
                zoneY1 += diffY;
            } else if (origX > zoneX2 - margin
                    && origY < zoneY1 + margin) { // Top right corner
                zoneX2 += diffX;
                zoneY1 += diffY;
            } else if (origX > zoneX2 - margin
                    && origY > zoneY2 - margin) { // Bottom right corner
                zoneX2 += diffX;
                zoneY2 += diffY;
            } else if (origX < zoneX1 + margin
                    && origY > zoneY2 - margin) { // Bottom left corner
                zoneX1 += diffX;
                zoneY2 += diffY;
            } else {
                zoneX1 += diffX;
                zoneX2 += diffX;
                zoneY1 += diffY;
                zoneY2 += diffY;
            }

            origX = x;
            origY = y;

            // Update it
            String builtArg = zoneX1 + "_" + zoneY1 + "_" + zoneX2 + "_" + zoneY2;
            zoneClicked.setValue(builtArg);
        }

    }

    public void mouseDown(Map map, int x, int y) {

        origX = x;
        origY = y;

        int minZoneSize = Integer.MAX_VALUE;

        // Get the zone this refers to
        for (java.util.Map.Entry<String, Object> values : map.getMap("Zones").entrySet()) {
            String[] args = values.getValue().toString().trim().split("_");
            if (args.length != 4) {
                continue;
            }

            int zoneX1 = Integer.parseInt(args[0]);
            int zoneY1 = Integer.parseInt(args[1]);
            int zoneX2 = Integer.parseInt(args[2]);
            int zoneY2 = Integer.parseInt(args[3]);

            int width = zoneX2 - zoneX1;
            int height = zoneY2 - zoneY1;
            int size = width * height;

            if (minZoneSize > size && x > zoneX1 && x < zoneX2
                    && y > zoneY1 && y < zoneY2) {
                minZoneSize = size;
                zoneClicked = values;
            }
        }
    }
}
