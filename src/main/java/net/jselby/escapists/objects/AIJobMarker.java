package net.jselby.escapists.objects;

import net.jselby.escapists.WorldObject;

import java.awt.*;

/**
 * Marks where NPCs have to go for their jobs.
 *
 * @author j_selby
 */
public class AIJobMarker extends WorldObject {
    /**
     * Creates a new JobMarker.
     *
     * @param x The X position of this object
     * @param y The Y position of this object
     */
    public AIJobMarker(int x, int y) {
        super(x, y);
    }

    @Override
    public String getName() {
        return "AI_JobMarker";
    }

    @Override
    public int getID() {
        return 50;
    }

    @Override
    public int getIDArgument() {
        return 1;
    }

    @Override
    public void draw(Graphics2D g, Graphics2D gLighting) {
        int x1 = getX() * 16 + 4;
        int y1 = getY() * 16 + 4;
        int x2 = x1 + 16 - 4;
        int y2 = y1 + 16 - 4;

        g.setColor(Color.orange);
        g.drawLine(x1, y1, x2, y2);
        g.drawLine(x1, y2, x2, y1);
    }
}
