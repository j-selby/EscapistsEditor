package net.jselby.escapists.editor;

/**
 * Defines modes used by the editor.
 *
 * @author j_selby
 */
public enum ActionMode {
    CREATE_OBJECT("Create Object"),
    DELETE_OBJECT("Delete Object"),
    INFO_OBJECT("Object Info"),
    SET_TILE("Set Tile"),
    ZONE_EDIT("Edit Zones"),
    CLEAR_ALL_TILES("Clear ALL Tiles");

    private String title;

    ActionMode(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
