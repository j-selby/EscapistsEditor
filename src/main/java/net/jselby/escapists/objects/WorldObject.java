package net.jselby.escapists.objects;

import net.jselby.escapists.EscapistsEditor;
import net.jselby.escapists.utils.StringUtils;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * A world object is a editable entity in the world, which can be interacted with, open, close, or configure
 * game logic.
 *
 * @author j_selby
 */
public abstract class WorldObject {
    private int x;
    private int y;

    /**
     * Creates a new WorldObject.
     * @param x The X position of this object
     * @param y The Y position of this object
     */
    public WorldObject(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Returns the human-readable name of this object.
     * @return A constant String name
     */
    public String getName() {
        String providedName = null;
        for (Objects object : Objects.values()) {
            if (object.getID() == getID()) {
                providedName = StringUtils.capitalize(object.name().toLowerCase().replace("_", " "));
            }
        }
        if (providedName == null) {
            providedName = StringUtils.capitalize(getClass().getSimpleName().toLowerCase().replace("_", " "))
                    + " (ID: " + getID() + ")";
        }
        return getX() + ":" + getY() + " = " + providedName;
    }

    /**
     * Returns the ID of this object.
     * @return A constant ID, conforming to the IDs used by The Escapists.
     */
    public abstract int getID();

    /**
     * Returns the additional argument of this object.
     * @return A constant argument, conforming to the arguments used by The Escapists.
     */
    public abstract int getIDArgument();

    @Override
    public String toString() {
        return "ID: " + getID();
    }

    /**
     * Draw this object. Must be relative to coords.
     * @param g Graphics place to draw onto
     * @param gLighting Graphics to draw lighting onto
     */
    public abstract void draw(Graphics2D g, Graphics2D gLighting);

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Objects asWorldDictionary() {
        for (Objects object : Objects.values()) {
            if (object.getID() == getID()) {
                return object;
            }
        }
        return null;
    }

    public static class Unknown extends WorldObject {

        private int id;
        private int argument;

        /**
         * Creates a new Unknown object representation.
         *
         * @param x The X position of this object
         * @param y The Y position of this object
         */
        public Unknown(int x, int y, int id, int argument) {
            super(x, y);
            this.id = id;
            this.argument = argument;
        }

        @Override
        public int getIDArgument() {
            return this.argument;
        }

        @Override
        public int getID() {
            return id;
        }

        @Override
        public void draw(Graphics2D g, Graphics2D gLighting) {
            // Try to grab the texture for this asset
            BufferedImage img = asWorldDictionary() != null ? asWorldDictionary().getTexture() : null;
            if (img != null) {
                int relativeDrawX = (int) (asWorldDictionary().getDrawX() * 16);
                int relativeDrawY = (int) (asWorldDictionary().getDrawY() * 16);
                g.drawImage(img,
                        null,
                        getX() * 16 + relativeDrawX,
                        getY() * 16 + relativeDrawY);
            } else {
                if (asWorldDictionary() != null
                        && asWorldDictionary().name().toLowerCase().startsWith("ai")
                        && !EscapistsEditor.showGUI) {
                    return;
                }
                g.setColor(new Color(1f, 0f, 0f, 1f));
                g.drawRect(getX() * 16, getY() * 16, 16, 16);
                if (EscapistsEditor.DRAW_TEXT) {
                    g.drawString(getID() + "", getX() * 16 + 2, getY() * 16 + 12);
                }
            }
        }
    }
}
