package com.example.android.inventoryapp;

//https://developer.android.com/guide/topics/permissions/requesting.html
//https://github.com/mitchtabian/CheckPermissions/blob/master/CheckPermissions/app/src/main/java/tabian/com/checkpermissions/MainActivity.java

//https://discussions.udacity.com/t/unofficial-how-to-pick-an-image-from-the-gallery/314971
//https://developer.android.com/reference/android/graphics/BitmapFactory.Options.html#inBitmap
//https://github.com/crlsndrsjmnz/MyShareImageExample
//https://developer.android.com/training/camera/photobasics.html

//https://stackoverflow.com/questions/8701634/send-email-intent

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Allows user to create new inventory or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = EditorActivity.class.getSimpleName();

    private static final String TAG = "EditoryActivity";

    private static final String[] STORAGE_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    //Code referenced from: https://github.com/crlsndrsjmnz/MyShareImageExample/blob/master/app/src/main/java/co/carlosandresjimenez/android/myshareimageexample/MainActivity.java

    private static final int PICK_IMAGE_REQUEST = 0;

    private static final String IMAGE_URI = "IMAGE_URI";

    /** Identifier for the inventory data loader */
    private static final int EXISTING_INVENTORY_LOADER = 0;

    /** Content URI for the existing inventory (null if it's a new inventory) */
    private Uri CurrentInventoryUri;

    /** EditText field to enter the supplier of the product */
    private EditText SupplierEditText;

    /** EditText field to enter the name of the product */
    private EditText NameEditText;

    /** EditText field to enter the quanity of the product */
    private EditText QuantityEditText;

    /** EditText field to enter the price of the product */
    private EditText PriceEditText;

    Button SelectImage;

    /** ImageView field to select the photo of the product */
    private ImageView ImageImageView;

    final Context context = this;

    private Uri imageUri;

    private String imageString;

    /** Boolean flag that keeps track of whether the inventory has been edited (true) or not (false) */
    private boolean InventoryHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mInventoryHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            InventoryHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        //Verify Permissions

        verifyPermissions();

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating new inventory or editing an existing one.
        Intent intent = getIntent();
        CurrentInventoryUri = intent.getData();

        // If the intent DOES NOT contain a inventory content URI, then we know that we are
        // creating new inventory.
        if (CurrentInventoryUri == null) {
            // This is new inventory, so change the app bar to say "Add Inventory"
            setTitle(getString(R.string.editor_activity_title_new_inventory));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete inventory that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing inventory, so change app bar to say "Edit Inventory"
            setTitle(getString(R.string.editor_activity_title_edit_inventory));

            // Initialize a loader to read the inventory data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_INVENTORY_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        SupplierEditText = (EditText) findViewById(R.id.editInventorySupplier);
        NameEditText = (EditText) findViewById(R.id.editInventoryName);
        QuantityEditText = (EditText) findViewById(R.id.editInventoryQuantity);
        PriceEditText = (EditText) findViewById(R.id.editInventoryPrice);
        SelectImage = (Button) findViewById(R.id.selectImage);
        ImageImageView = (ImageView) findViewById(R.id.editInventoryImage);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.

        SupplierEditText.setOnTouchListener(mTouchListener);
        NameEditText.setOnTouchListener(mTouchListener);
        PriceEditText.setOnTouchListener(mTouchListener);}

    public void selectImage(View view) {

        //Open Image Selector
        openImageSelector();

        InventoryHasChanged = true;

    }

    //Code to verify permissions for read and write external storage

    private void verifyPermissions() {
        Log.d(TAG, "verifyPermissions: Checking Permissions.");

        int permissionReadExternalStorage = ActivityCompat.checkSelfPermission(EditorActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);

        int permissionWriteExternalStorage = ActivityCompat.checkSelfPermission(EditorActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionReadExternalStorage != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    EditorActivity.this,
                    STORAGE_PERMISSIONS,
                    1
            );
        }

        if (permissionWriteExternalStorage != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    EditorActivity.this,
                    STORAGE_PERMISSIONS,
                    1
            );
        }
    }

    //https://github.com/crlsndrsjmnz/MyShareImageExample/blob/master/app/src/main/java/co/carlosandresjimenez/android/myshareimageexample/MainActivity.java

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (imageUri != null){
            outState.putString(IMAGE_URI, imageUri.toString());}

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState.containsKey(IMAGE_URI) &&
                !savedInstanceState.getString(IMAGE_URI).equals("")) {
            imageUri = Uri.parse(savedInstanceState.getString(IMAGE_URI));
            //ImageUriTextView.setText(imageUri.toString());

            ViewTreeObserver viewTreeObserver = ImageImageView.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    ImageImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    ImageImageView.setImageBitmap(getBitmapFromUri(imageUri));
                }
            });
        }
    }

    public void openImageSelector() {
        Intent intent;

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {

            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.  Pull that uri using "resultData.getData()"

            if (resultData != null) {
                Uri imageUri = resultData.getData();
                Log.i(LOG_TAG, "Uri: " + imageUri.toString());
                //ImageUriTextView.setText(imageUri.toString());
                ImageImageView.setImageBitmap(getBitmapFromUri(imageUri));
                imageString = imageUri.toString();
                //Glide.with(this).load(imageString)
                        //.placeholder(R.drawable.imagenotfound)
                        //.crossFade()
                        //.fitCenter()
                        //.into(ImageImageView);

                //https://stackoverflow.com/questions/27394016/how-does-one-use-glide-to-download-an-image-into-a-bitmap

                Glide.with(this)
                        .load(imageString)
                        .asBitmap()
                        .placeholder(R.drawable.imagenotfound)
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                ImageImageView.setImageBitmap(resource);
                            }
                        });
            }
        }
    }

    public Bitmap getBitmapFromUri(Uri uri) {

        if (uri == null || uri.toString().isEmpty())
            return null;

        //Get the dimensions of the view
        int targetW = ImageImageView.getWidth();
        int targetH = ImageImageView.getHeight();

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH );

            // Decode the image file into a Bitmap sized to fill the view
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            input = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, "Failed to load image.", fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {

            }
        }
    }

    /**
     * This method is called when the decrease quantity button is clicked.
     */

    public void decreaseQuantity(View view) {
        String quantityString = QuantityEditText.getText().toString();
        int quantity;
        if (quantityString.isEmpty()) {
            return;
        } else if (quantityString.equals("0")) {
            return;
        } else {
            quantity = Integer.parseInt(quantityString);
            QuantityEditText.setText(String.valueOf(quantity - 1));
        }
    }

    /**
     * This method is called when the increase quantity button is clicked.
     */

    public void increaseQuantity(View view) {
        String quantityString = QuantityEditText.getText().toString();
        int quantity;
        if (quantityString.isEmpty()) {
            quantity = 0;
        } else {
            quantity = Integer.parseInt(quantityString);
        }
        QuantityEditText.setText(String.valueOf(quantity + 1));
    }

    /**
     * Get user input from editor and save inventory into database.
     */

    //https://stackoverflow.com/questions/218384/what-is-a-nullpointerexception-and-how-do-i-fix-it

    private void saveInventory() {

        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String supplierString = SupplierEditText.getText().toString().trim();
        String nameString = NameEditText.getText().toString().trim();
        String quantityString = QuantityEditText.getText().toString().trim();
        String priceString = PriceEditText.getText().toString().trim();
        ImageImageView.setImageBitmap(getBitmapFromUri(imageUri));

        //https://discussions.udacity.com/t/error-while-saving-image/241036/21
        if(imageUri == null){
            //Toast.makeText(this, "please select image", Toast.LENGTH_SHORT).show();
            return;
        }
        String imageString = imageUri.toString();

        //https://stackoverflow.com/questions/36004584/how-to-show-database-image-in-glide-on-android
        //https://github.com/codepath/android_guides/wiki/Displaying-Images-with-the-Glide-Library

        //Glide.with(context)
                //.load(imageString)
                //.error(R.drawable.imagenotfound)
                //.placeholder(R.drawable.imagenotfound)
                //.into(ImageImageView);

        //https://stackoverflow.com/questions/27394016/how-does-one-use-glide-to-download-an-image-into-a-bitmap

        Glide.with(this)
                .load(imageString)
                .asBitmap()
                .placeholder(R.drawable.imagenotfound)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        ImageImageView.setImageBitmap(resource);
                    }
                });

        // Check if this is supposed to be a new inventory
        // and check if all the fields in the editor are blank
        if (CurrentInventoryUri == null &&
                TextUtils.isEmpty(nameString) || TextUtils.isEmpty(quantityString) ||
                TextUtils.isEmpty(priceString) || TextUtils.isEmpty(supplierString) ||
                TextUtils.isEmpty(imageString)) {
            // Since no fields were modified, we can return early without creating new inventory.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            {Toast.makeText(this, getString(R.string.editor_insert_inventory),
                    Toast.LENGTH_SHORT).show();
            }return;
        }

        // Create a ContentValues object where column names are the keys,
        // and inventory attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_INVENTORY_SUPPLIER, "" + supplierString);
        values.put(InventoryEntry.COLUMN_INVENTORY_NAME, "" + nameString);
        values.put(InventoryEntry.COLUMN_INVENTORY_QUANTITY, "" + quantityString);
        values.put(InventoryEntry.COLUMN_INVENTORY_PRICE, "" + priceString);
        values.put(InventoryEntry.COLUMN_INVENTORY_IMAGE, "" + imageString);

        // Determine if this is a new or existing inventory by checking if mCurrentInventoryUri is null or not
        if (CurrentInventoryUri == null) {

            // This is a NEW inventory, so insert new inventory into the provider,
            // returning the content URI for the new inventory.
            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_inventory_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_inventory_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {

            // Otherwise this is an EXISTING inventory, so update the inventory with content URI: mCurrentInventoryUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentInventoryUri will already identify the correct row in the database that
            // we want to modify.

            int rowsAffected = getContentResolver().update(CurrentInventoryUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.

            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.

                Toast.makeText(this, getString(R.string.editor_update_inventory_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.

                Toast.makeText(this, getString(R.string.editor_update_inventory_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        //Exit activity
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // If this is new inventory, hide the "Delete" menu item.
        if (CurrentInventoryUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu

        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save inventory to database
                saveInventory();
                return true;
            // Respond to a click on "Order More" menu option
            case R.id.order_more:
                // Pop up confirmation for order more
                createOrderMore();
                return true;
                // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the inventory hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!InventoryHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the inventory hasn't changed, continue with handling back button press
        if (!InventoryHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all inventory attributes, define a projection that contains
        // all columns from the inventory table

        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_INVENTORY_SUPPLIER,
                InventoryEntry.COLUMN_INVENTORY_NAME,
                InventoryEntry.COLUMN_INVENTORY_QUANTITY,
                InventoryEntry.COLUMN_INVENTORY_PRICE,
                InventoryEntry.COLUMN_INVENTORY_IMAGE };

        // This loader will execute the ContentProvider's query method on a background thread

        return new CursorLoader(this,   // Parent activity context
                CurrentInventoryUri,         // Query the content URI for the current inventory
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)

        if (cursor.moveToFirst()) {

            // Find the columns of inventory attributes that we're interested in
            // Extract out the value from the Cursor for the given column index
            // Update the views on the screen with the values from the database

            SupplierEditText.setText(cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_SUPPLIER)));
            NameEditText.setText(cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_NAME)));
            QuantityEditText.setText(cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_QUANTITY)));
            PriceEditText.setText(cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_PRICE)));
            ImageImageView.setImageURI(Uri.parse(cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_IMAGE))));
        }

        //https://discussions.udacity.com/t/error-while-saving-image/241036/21

        if(ImageImageView != null){
            imageUri = Uri.parse(cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_IMAGE)));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        // If the loader is invalidated, clear out all the data from the input fields.
        SupplierEditText.setText("");
        NameEditText.setText("");
        QuantityEditText.setText("");
        PriceEditText.setText("");
        ImageImageView.setImageBitmap(null);
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //https://stackoverflow.com/questions/8701634/send-email-intent
    //https://github.com/udacity/Just-Java/blob/master/app/src/main/java/com/example/android/justjava/MainActivity.java

    /**
     * Email intent to Order More of a particular product
     */
    private void createOrderMore() {

        // Use an intent to launch an email app.
        // Send the order more in the email body.

        String orderMoreName = NameEditText.getText().toString().trim();
        String createOrderMoreMessage = createOrderMoreSummary (orderMoreName);

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_SUBJECT, "Order more summary for" + SupplierEditText.getText().toString().trim() + NameEditText.getText().toString().trim());
        intent.putExtra(Intent.EXTRA_TEXT, createOrderMoreMessage);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public String createOrderMoreSummary(String orderMoreName) {
        String orderMoreMessage = "";
        orderMoreMessage += "Supplier:" + SupplierEditText.getText().toString().trim() + "\n";
        orderMoreMessage += "Purchase:" + NameEditText.getText().toString().trim() + "\n";
        return orderMoreMessage;
    }

    /**
     * Prompt the user to confirm that they want to delete this inventory.
     */
    private void showDeleteConfirmationDialog() {

        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the inventory.
                deleteInventory();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the inventory.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the inventory in the database.
     */
    private void deleteInventory() {
        // Only perform the delete if this is an existing inventory.
        if (CurrentInventoryUri != null) {

            // Call the ContentResolver to delete the inventory at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentInventoryUri
            // content URI already identifies the inventory that we want.

            int rowsDeleted = getContentResolver().delete(CurrentInventoryUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_inventory_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_inventory_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }
}