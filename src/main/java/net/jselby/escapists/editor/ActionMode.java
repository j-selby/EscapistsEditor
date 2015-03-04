package net.jselby.escapists.editor;

/**
 * Defines modes used by the editor, in interaction with the screen contents/render.
 *
 * @author j_selby
 */
public enum ActionMode {
    /**
     * Toolbar option for creating objects.
     */
    CREATE_OBJECT("Create Object"),
    /**
     * Toolbar option for deleting objects.
     */
    DELETE_OBJECT("Delete Object"),
    /**
     * Toolbar option for setting tiles within a map.
     */
    SET_TILE("Set Tile"),
    /**
     * Toolbar option for editing map zone.
     */
    ZONE_EDIT("Edit Zones");

    private String title;

    ActionMode(String title) {
        this.title = title;
    }

    /**
     * Gets the user-friendly version of the toolbar option.
     * @return A user-friendly toolbar prompt
     */
    public String getTitle() {
        return title;
    }
}
