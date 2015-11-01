package net.jselby.escapists.editor.objects.impl;

import net.jselby.escapists.editor.objects.WorldObject;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * A light, rendered using a gradient onto the screen.
 *
 * @author j_selby
 */
public class Light extends WorldObject {
    private int argument;

    /**
     * Creates a new Light.
     *
     * @param x The X position of this object
     * @param y The Y position of this object
     */
    public Light(int x, int y, int argument) {
        super(x, y);
        this.argument = argument;
    }

    @Override
    public int getID() {
        return 49;
    }

    @Override
    public int getIDArgument() {
        return argument;
    }

    @Override
    public void draw(Graphics2D g, Graphics2D gLighting) {
        int radius = 3;
        // The random addition (16, 0) is due to how The Escapists renders them
        int centerX = getX() * 16 + 16;
        int centerY = getY() * 16;

        Point2D center = new Point2D.Float(centerX, centerY);
        float[] dist = {0.0f, 0.2f, 0.9f};
        Color[] colors = {new Color(1f, 1f, 1f, 0.4f), new Color(1f, 1f, 1f, 0.3f), new Color(1f, 1f, 1f, 0f)};
        RadialGradientPaint p =
                new RadialGradientPaint(center, (radius - 1) * 16, dist,
                        colors, MultipleGradientPaint.CycleMethod.NO_CYCLE);

        int width = ((radius + 1) * 16) / 2;
        gLighting.setPaint(p);
        gLighting.fillRect(centerX - width, centerY - width, width * 2, width * 2);
        gLighting.setPaint(null);
    }
}
