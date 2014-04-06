package org.namelessrom.devicecontrol.database;

public class TaskerItem {

    public static final String CATEGORY_CPU = "cpu";

    private int    _id;
    private String _category;
    private String _name;
    private String _filename;
    private String _value;
    private String _enabled;

    public TaskerItem() { }

    public TaskerItem(final String category, final String name, final String filename,
            final String value, final boolean enabled) {
        this._id = -1;
        this._category = category;
        this._name = name;
        this._value = value;
        this._filename = filename;
        this._enabled = (enabled ? "1" : "0");
    }

    public TaskerItem(final int id, final String category, final String name, final String filename,
            final String value, final boolean enabled) {
        this._id = id;
        this._category = category;
        this._name = name;
        this._value = value;
        this._filename = filename;
        this._enabled = (enabled ? "1" : "0");
    }

    public int getID() {
        return this._id;
    }

    public void setID(final int id) {
        this._id = id;
    }

    public String getCategory() {
        return this._category;
    }

    public void setCategory(final String category) {
        this._category = category;
    }

    public String getName() {
        return this._name;
    }

    public void setName(final String name) {
        this._name = name;
    }

    public String getValue() {
        return this._value;
    }

    public void setValue(final String value) {
        this._value = value;
    }

    public String getFileName() {
        return this._filename;
    }

    public void setFileName(final String filename) {
        this._filename = filename;
    }

    public boolean getEnabled() {
        return this._enabled.equals("1");
    }

    public void setEnabled(final boolean isEnabled) {
        this._enabled = (isEnabled ? "1" : "0");
    }

}
