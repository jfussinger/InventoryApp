package com.example.android.inventoryapp.data;

import android.database.sqlite.SQLiteOpenHelper;

//SQLite CRUD Method examples
//
//https://www.androidhive.info/2011/11/android-sqlite-database-tutorial/
//https://java2blog.com/android-sqlite-database-crud-example/
//https://github.com/kotlintpoint/SQLite-Android

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Database helper for Inventory app. Manages database creation and version management.
 */
public class InventoryDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = InventoryDbHelper.class.getSimpleName();

    /**
     * Name of the database file
     */
    private static final String DATABASE_NAME = "inventory";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 2;

    /**
     * Name of database table for inventory
     */
    public final static String TABLE_NAME = "inventory";

    /**
     * Constructs a new instance of {@link InventoryDbHelper}.
     *
     * @param context of the app
     */
    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        // Create a String that contains the SQL statement to create the inventory table
        String SQL_CREATE_INVENTORY_TABLE = "CREATE TABLE " + InventoryContract.InventoryEntry.TABLE_NAME + " ("
                + InventoryContract.InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + InventoryContract.InventoryEntry.COLUMN_INVENTORY_SUPPLIER + " TEXT NOT NULL, "
                + InventoryContract.InventoryEntry.COLUMN_INVENTORY_NAME + " TEXT NOT NULL, "
                + InventoryContract.InventoryEntry.COLUMN_INVENTORY_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                + InventoryContract.InventoryEntry.COLUMN_INVENTORY_PRICE + " INTEGER NOT NULL DEFAULT 0, "
                + InventoryContract.InventoryEntry.COLUMN_INVENTORY_IMAGE + " TEXT NOT NULL);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_INVENTORY_TABLE);
    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + InventoryEntry.TABLE_NAME);

        // Create tables again
        onCreate(db);
    }

    // Adding new inventory
    public void addInventory(InventoryContract InventoryContract) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_INVENTORY_SUPPLIER, InventoryContract.getSupplier()); // Inventory Supplier
        values.put(InventoryEntry.COLUMN_INVENTORY_NAME, InventoryContract.getName()); // Inventory Name
        values.put(InventoryEntry.COLUMN_INVENTORY_QUANTITY, InventoryContract.getQuantity()); // Inventory Quantity
        values.put(InventoryEntry.COLUMN_INVENTORY_PRICE, InventoryContract.getPrice()); // Inventory Price
        values.put(InventoryEntry.COLUMN_INVENTORY_IMAGE, InventoryContract.getImage()); // Inventory Image

        // Inserting Row
        db.insert(InventoryEntry.TABLE_NAME, null, values);
        db.close(); // Closing database connection

    }

    // Getting single inventory
    public Cursor getInventory(int id) {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                InventoryContract.InventoryEntry._ID,
                InventoryContract.InventoryEntry.COLUMN_INVENTORY_SUPPLIER,
                InventoryContract.InventoryEntry.COLUMN_INVENTORY_NAME,
                InventoryContract.InventoryEntry.COLUMN_INVENTORY_QUANTITY,
                InventoryContract.InventoryEntry.COLUMN_INVENTORY_PRICE,
                InventoryContract.InventoryEntry.COLUMN_INVENTORY_IMAGE,
        };
        String selection = InventoryContract.InventoryEntry._ID + "=?";
        String[] selectionArgs = new String[]{String.valueOf(null)};

        Cursor cursor = db.query(
                InventoryContract.InventoryEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
        return cursor;
    }

    // Getting All inventory
    public List<InventoryContract> getAllInventory() {
        List<InventoryContract> inventoryContractList = new ArrayList<InventoryContract>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                InventoryContract inventoryContract = new InventoryContract();
                inventoryContract.setSupplier(cursor.getString(0));
                inventoryContract.setName(cursor.getString(1));
                inventoryContract.setQuantity(cursor.getInt(2));
                inventoryContract.setPrice(cursor.getInt(3));
                inventoryContract.setImage(cursor.getString(4));

                // Adding inventory to list
                inventoryContractList.add(inventoryContract);
            } while (cursor.moveToNext());
        }

        // return inventory list
        return inventoryContractList;
    }

    // Updating single inventory
    public int updateInventory(InventoryContract InventoryContract) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_INVENTORY_SUPPLIER, InventoryContract.getSupplier());
        values.put(InventoryEntry.COLUMN_INVENTORY_NAME, InventoryContract.getName());
        values.put(InventoryEntry.COLUMN_INVENTORY_QUANTITY, InventoryContract.getQuantity());
        values.put(InventoryEntry.COLUMN_INVENTORY_PRICE, InventoryContract.getPrice());
        values.put(InventoryEntry.COLUMN_INVENTORY_IMAGE, InventoryContract.getImage());

        // updating row
        return db.update(TABLE_NAME, values, InventoryEntry._ID + " = ?",
                new String[]{String.valueOf(InventoryContract.getId())});

    }

    // Deleting single inventory
    public void deleteAllInventory(InventoryContract InventoryContract) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, InventoryEntry._ID + " = ?",
                new String[]{String.valueOf(InventoryContract.getId())});
        db.close();
    }
}