package net.jselby.escapists;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Random;

/**
 * Renders maps into a BufferedImage.
 *
 * @author j_selby
 */
public class MapRenderer {
    private static java.util.Map<String, Color> zoneColorMappings = new HashMap<>();

    public static BufferedImage render(Map map) {
        return render(map, false, null);
    }

    public static BufferedImage render(Map map, boolean showZones, String selectedZone) {
        // Get tiles, and render it
        BufferedImage off_Image =
                new BufferedImage((map.getHeight() - 1) * 16, (map.getWidth() - 3) * 16,
                        BufferedImage.TYPE_INT_ARGB);

        BufferedImage lighting =
                new BufferedImage((map.getHeight() - 1) * 16, (map.getWidth() - 3) * 16,
                        BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = off_Image.createGraphics();
        Graphics2D gLighting = lighting.createGraphics();

        // Add some minimal darkness to the world
        gLighting.setColor(new Color(0f, 0f, 0f, 0.1f));
        gLighting.fillRect(0, 0, lighting.getWidth(), lighting.getHeight());

        // Get tiles
        BufferedImage tiles = map.getTilesImage();
        BufferedImage ground = map.getBackgroundImage();
        g2.drawImage(ground, null, 0, 0);

        // Tiles
        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                int tile = map.getTile(x, y);
                if (tile == 0) {
                    continue;
                }
                tile--;

                // Get tile from main tiles
                int tileX = (int) Math.floor(((double) tile) / (tiles.getHeight() / 16));
                int tileY = tile - (tileX * (tiles.getHeight() / 16));
                int absTileX = tileX * 16;
                int absTileY = tileY * 16;

                BufferedImage tileRender = tiles.getSubimage(absTileX, absTileY, 16, 16);

                g2.drawImage(tileRender, null, x * 16, y * 16);
            }
        }

        // Objects
        for (WorldObject object : map.getObjects()) {
            object.draw(g2, gLighting);
        }

        // Zones
        if (showZones) {
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
                g2.setColor(color);
                g2.fillRect(x1, y1, x2 - x1, y2 - y1);
                g2.setColor(Color.BLACK);
                g2.drawString(values.getKey(), x1, y1 + 12);
                if (selectedZone != null && values.getKey().trim().equalsIgnoreCase(selectedZone.trim())) {
                    g2.setColor(Color.RED);
                    g2.drawRect(x1, y1, x2 - x1, y2 - y1);
                }
            }
        }

        // Merge lighting
        g2.drawImage(lighting, null, 0, 0);
        return off_Image;
    }
}
