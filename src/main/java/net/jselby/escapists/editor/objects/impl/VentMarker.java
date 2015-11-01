package net.jselby.escapists.editor.objects.impl;

import net.jselby.escapists.editor.objects.WorldObject;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Marks where vents are.
 *
 * @author j_selby
 */
public class VentMarker extends WorldObject {
    private int argument;

    /**
     * Creates a new VentMarker.
     *
     * @param x The X position of this object
     * @param y The Y position of this object
     */
    public VentMarker(int x, int y, int argument) {
        super(x, y);
        this.argument = argument;
    }

    @Override
    public int getID() {
        return 50;
    }

    @Override
    public int getIDArgument() {
        return argument;
    }

    @Override
    public void draw(Graphics2D g, Graphics2D gLighting) {
        BufferedImage img = asWorldDictionary() != null ? asWorldDictionary().getTexture() : null;
        if (img != null) {
            int relativeDrawX = (int) (asWorldDictionary().getDrawX() * 16);
            int relativeDrawY = (int) (asWorldDictionary().getDrawY() * 16);
            g.drawImage(img,
                    null,
                    getX() * 16 + relativeDrawX,
                    getY() * 16 + relativeDrawY);
        }

        int x1 = getX() * 16;
        int y1 = getY() * 16;
        int x2 = x1 + 16;
        int y2 = y1 + 16;

        g.setColor(Color.orange);
        g.drawLine(x1, y1, x2, y2);
        g.drawLine(x1, y2, x2, y1);
    }
}
