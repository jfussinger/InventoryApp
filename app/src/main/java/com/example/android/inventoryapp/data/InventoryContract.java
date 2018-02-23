package com.example.android.inventoryapp.data;

import android.net.Uri;
import android.content.ContentResolver;
import android.provider.BaseColumns;

//https://www.androidhive.info/2011/11/android-sqlite-database-tutorial/

/**
 * API Contract for the Inventory app.
 */

public final class InventoryContract {

    int _id;
    String supplier = "";
    String name = "";
    int quantity;
    int price;
    String image = "";

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public InventoryContract() {}

    // constructor
    public InventoryContract(int _id, String supplier, String name, int quantity, int price, String image){
        this._id = _id;
        this.supplier = supplier;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.image = image;
    }

    //Getter methods

    public int getId () {
        return this._id = _id;
    }

    public String getSupplier() {
        return this.supplier = supplier;
    }

    public String getName() {
        return this.name = name;
    }

    public int getQuantity() {
        return this.quantity = quantity;
    }

    public int getPrice() {
        return this.price = price;
    }

    public String getImage() {
        return this.image = image;
    }

    //Setter methods

    public void setId (int _id) {
        this._id = _id;
    }

    public void setSupplier(String supplier){
        this.supplier = supplier;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setQuantity(int quantity){
        this.quantity = quantity;
    }

    public void setPrice(int price){
        this.price = price;
    }

    public void setImage(String image){
        this.image = image;
    }

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.inventoryapp/inventoryapp/ is a valid path for
     * looking at pet data. content://com.example.android.inventoryapp/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_INVENTORY = "inventory";

    /**
     * Inner class that defines constant values for the inventory database table.
     * Each entry in the table represents single inventory.
     */
    public static final class InventoryEntry implements BaseColumns {

        /**
         * The content URI to access the inventory data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of inventory.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        /**
         * The MIME type of the {@link #CONTENT_URI} for single inventory.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        /** Name of database table for inventory */
        public final static String TABLE_NAME = "inventory";

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_INVENTORY_SUPPLIER = "supplier";
        public final static String COLUMN_INVENTORY_NAME = "name";
        public final static String COLUMN_INVENTORY_QUANTITY = "quantity";
        public final static String COLUMN_INVENTORY_PRICE = "price";
        public final static String COLUMN_INVENTORY_IMAGE = "image";

        }
    }


