package com.example.proexm;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.EditText;
import android.widget.TextView;

import com.example.proexm.database.Constants;
import com.example.proexm.database.DbHelper;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.Calendar;
import java.util.Locale;

public class ItemDetailActivity extends AppCompatActivity {
    //views
    private RoundedImageView itemIv;
    private TextView itemNameTv, manufacturerTv, priceTv, mfDateTv, expDateTv, descTv, expDaysTv, itemStatus, dateAddedTv, dateUpdatedTv;

    //Action bar
    private ActionBar actionBar;

    //db helper
    private DbHelper dbHelper;

    private String itemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        //setting up actionbar with title and back button
        actionBar = getSupportActionBar();
        actionBar.setTitle("Item Details");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //get item id froom adapter through intent
        Intent intent = getIntent();
        itemId = intent.getStringExtra("ITEM_ID");

        //init db helper class
        dbHelper = new DbHelper(this);

        //init views
        itemIv = findViewById(R.id.itemIv);
        itemNameTv = findViewById(R.id.itemNameTv);
        manufacturerTv = findViewById(R.id.itemManufacturerTv);
        priceTv = findViewById(R.id.itemPriceTv);
        mfDateTv = findViewById(R.id.itemManufactureDateTv);
        expDateTv = findViewById(R.id.itemExpDateTv);
        descTv = findViewById(R.id.descTv);
        expDaysTv = findViewById(R.id.expDaysTv);
        itemStatus = findViewById(R.id.expStatus);

        dateAddedTv = findViewById(R.id.dateAddedTv);
        dateUpdatedTv = findViewById(R.id.dateUpdatedTv);

        showItemDetails();
    }

    private void showItemDetails() {
        //get item details

        //query to select item based on item id
        String selectQuery = "SELECT * FROM " + Constants.ITEMS_TABLE + " WHERE " + Constants.C_ID +" =\"" + itemId+"\"";

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        //keep checking in the whole db for that record
        if(cursor.moveToNext()){
            do{
                //get data
                String id = ""+cursor.getInt(cursor.getColumnIndex(Constants.C_ID));
                String itemName = ""+cursor.getString(cursor.getColumnIndex(Constants.C_ITEM_NAME));
                String itemImage = ""+cursor.getString(cursor.getColumnIndex(Constants.C_ITEM_IMAGE));
                String itemPrice = ""+cursor.getString(cursor.getColumnIndex(Constants.C_ITEM_PRICE));
                String itemManufacturer = ""+cursor.getString(cursor.getColumnIndex(Constants.C_ITEM_MANUFACTURER));
                String daysToExp = ""+cursor.getInt(cursor.getColumnIndex(Constants.C_DAYS_TO_EXPIRY));
                String expStatus = ""+cursor.getString(cursor.getColumnIndex(Constants.C_STATUS));
                String desc = ""+cursor.getString(cursor.getColumnIndex(Constants.C_DESC));
                String expDate = ""+cursor.getString(cursor.getColumnIndex(Constants.C_EXPIRY_DATE));
                String mfdDate = ""+cursor.getString(cursor.getColumnIndex(Constants.C_MANUFACTURE_DATE));
                String timestampAdded = ""+cursor.getString(cursor.getColumnIndex(Constants.C_ADDED_TIMESTAMP));
                String timestampUpdated = ""+cursor.getString(cursor.getColumnIndex(Constants.C_UPDATED_TIMESTAMP));

                //convert timestamp to dd/mm/yyyy hh:mm aa e.g. 22/0/2020 08:22 AM
                Calendar calendar1 = Calendar.getInstance(Locale.getDefault());
                calendar1.setTimeInMillis(Long.parseLong(timestampAdded));
                String timeAdded = ""+DateFormat.format("dd/MM/yyyy hh:mm:aa", calendar1);

                Calendar calendar2 = Calendar.getInstance(Locale.getDefault());
                calendar2.setTimeInMillis(Long.parseLong(timestampUpdated));
                String timeUpdated = ""+DateFormat.format("dd/MM/yyyy hh:mm:aa", calendar2);

                //set data
                itemNameTv.setText(itemName);
                manufacturerTv.setText(itemManufacturer);
                priceTv.setText(itemPrice);
                mfDateTv.setText(mfdDate);
                expDateTv.setText(expDate);
                descTv.setText(desc);
                expDaysTv.setText(daysToExp);
                itemStatus.setText(expStatus);
                dateAddedTv.setText(timeAdded);
                dateUpdatedTv.setText(timeUpdated);

                //if item has no image
                if (itemImage.equals("null")){
                    //no image in record set default
                    itemIv.setImageResource(R.drawable.ic_no_photo);
                }
                else{
                    //have image in record
                    itemIv.setImageURI(Uri.parse(itemImage));
                }

            }while (cursor.moveToNext());
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();//goto previous activity
        return super.onSupportNavigateUp();
    }
}
