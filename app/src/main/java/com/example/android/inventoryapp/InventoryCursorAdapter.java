package com.example.android.inventoryapp;

//https://developer.android.com/reference/android/widget/ListView.html
//https://developer.android.com/reference/android/support/v4/widget/SimpleCursorAdapter.html
//https://developer.android.com/reference/android/database/sqlite/package-summary.html
//https://github.com/codepath/android_guides/wiki/Populating-a-ListView-with-a-CursorAdapter
//https://stackoverflow.com/questions/29339820/android-populate-custom-listview-from-sqlite-database
//https://stackoverflow.com/posts/29340128/revisions

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.inventoryapp.data.InventoryContract;
import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

/**
 * {@link InventoryCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of inventory data as its data source. This adapter knows
 * how to create list items for each row of inventory data in the {@link Cursor}.
 */
public class InventoryCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link InventoryCursorAdapter}.
     *
     * @param context The context
     * @param c The cursor from which to get the data.
     */
    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the inventory data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current pet can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        // Find individual views that we want to modify in the list item layout
        TextView supplierTextView = (TextView) view.findViewById(R.id.supplier);
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        final TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        TextView priceTextView = (TextView) view.findViewById (R.id.price);
        ImageView imageImageView = (ImageView) view.findViewById(R.id.image);
        final Button saleButton = (Button) view.findViewById(R.id.saleButton);

        // Find the columns of inventory attributes that we're interested in and read the inventory attributes
        // from the Cursor for the current inventory

        final int _id = cursor.getInt(cursor.getColumnIndex(InventoryContract.InventoryEntry._ID));

        final String supplier = cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_SUPPLIER));
        final String name = cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_NAME));
        final int quantity = cursor.getInt(cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_QUANTITY));
        final int price = cursor.getInt(cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_PRICE));
        final String image = cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_IMAGE));

        // Update the TextViews with the attributes for the current inventory

        //https://stackoverflow.com/questions/47779756/resourcesnotfoundexception-string-resource-id-0x1

        supplierTextView.setText("" + supplier);
        nameTextView.setText("" + name);
        quantityTextView.setText("" + quantity);
        priceTextView.setText("" + price);

        if(image!= null) {
            imageImageView.setVisibility(View.VISIBLE);
            imageImageView.setImageURI(Uri.parse(image));
        }
        else {
            imageImageView.setVisibility(View.GONE);
        }

        final Uri uri = Uri.parse(InventoryEntry.CONTENT_URI + "/" + _id);

        // Sale Button
        //https://stackoverflow.com/questions/48350406/updating-the-sqlite-db-with-a-button-click-on-listview

        saleButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                ContentResolver resolver = view.getContext().getContentResolver();
                ContentValues values = new ContentValues();

                String quantityString = quantityTextView.getText().toString();
                int quantity;
                if (quantityString.isEmpty()) {
                    return;
                } else if (quantityString.equals("0")) {
                   return;
                } else {
                    quantity = Integer.parseInt(quantityString);
                    quantityTextView.setText(String.valueOf(quantity - 1));

                    values.put(InventoryEntry.COLUMN_INVENTORY_QUANTITY, quantity -1);

                    Uri updateUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, _id);
                    context.getContentResolver().update(updateUri, values,null, null);
                }
            }
        });
    }
}
