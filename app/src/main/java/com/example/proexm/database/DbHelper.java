package com.example.proexm.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.proexm.interfaces.CallBackBroadcast;
import com.example.proexm.models.BarcodeItemsModel;
import com.example.proexm.models.ModelExpiredItems;
import com.example.proexm.models.ModelItems;
import com.example.proexm.models.UserModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.example.proexm.database.Constants.BARCODE_TABLE;
import static com.example.proexm.database.Constants.B_BARCODE_CODE;
import static com.example.proexm.database.Constants.B_ID;
import static com.example.proexm.database.Constants.LOGIN_TABLE;
import static com.example.proexm.database.Constants.L_EMAIL;
import static com.example.proexm.database.Constants.L_ID;
import static com.example.proexm.database.Constants.L_PASSWORD;
import static com.example.proexm.database.Constants.L_ROLE_ID;
import static com.example.proexm.database.Constants.L_USERNAME;

//Database Helper class that contain all crud methods
public class DbHelper extends SQLiteOpenHelper {

    public DbHelper(@Nullable Context context) {
        super(context, Constants.DB_NAME, null, Constants.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //create table on that db
        db.execSQL(Constants.CREATE_ITEMS_TABLE);
        db.execSQL(Constants.CREATE_LOGIN_TABLE);
        db.execSQL(Constants.CREATE_BARCODE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // upgrade database (if there is any structure change, change db version)

        //drop older table if exists
        db.execSQL("DROP TABLE IF EXISTS "+ Constants.ITEMS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS "+ Constants.LOGIN_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.BARCODE_TABLE);
        //create table again
        onCreate(db);

    }
    //insert record to items database
    public long insertItemRecord(String itemName, String itemImage, String itemPrice, String itemManufacturer,
                                 String itemDesc, String itemExp, String itemMfd, String daysToExpiry, String itemStatus, String addedTime, String updatedTime){
        //get writeable database because we want to write data
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        // id will be inserted automatically as we set AUTOINCREMENT in query

        //insert data
        values.put(Constants.C_ITEM_NAME, itemName);
        values.put(Constants.C_ITEM_IMAGE, itemImage);
        values.put(Constants.C_ITEM_PRICE, itemPrice);
        values.put(Constants.C_ITEM_MANUFACTURER, itemManufacturer);
        values.put(Constants.C_DESC, itemDesc);
        values.put(Constants.C_EXPIRY_DATE, itemExp);
        values.put(Constants.C_MANUFACTURE_DATE, itemMfd);
        values.put(Constants.C_DAYS_TO_EXPIRY, daysToExpiry);
        values.put(Constants.C_STATUS, itemStatus);
        values.put(Constants.C_ADDED_TIMESTAMP, addedTime);
        values.put(Constants.C_UPDATED_TIMESTAMP, updatedTime);

        //insert row, it will return record id of saved record
        long id = db.insert(Constants.ITEMS_TABLE, null, values);
        //close db connection
        db.close();
        //return id of inserted item
        return id;
    }

    //update existing record to items database
    public void updateItemRecord(String id, String itemName, String itemImage, String itemPrice, String itemManufacturer,
                                 String itemDesc, String itemExp, String itemMfd, String daysToExpiry, String itemStatus, String addedTime, String updatedTime){
        //get writeable database because we want to write data
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        // id will be inserted automatically as we set AUTOINCREMENT in query

        //insert data
        values.put(Constants.C_ITEM_NAME, itemName);
        values.put(Constants.C_ITEM_IMAGE, itemImage);
        values.put(Constants.C_ITEM_PRICE, itemPrice);
        values.put(Constants.C_ITEM_MANUFACTURER, itemManufacturer);
        values.put(Constants.C_DESC, itemDesc);
        values.put(Constants.C_EXPIRY_DATE, itemExp);
        values.put(Constants.C_MANUFACTURE_DATE, itemMfd);
        values.put(Constants.C_DAYS_TO_EXPIRY, daysToExpiry);
        values.put(Constants.C_STATUS, itemStatus);
        values.put(Constants.C_ADDED_TIMESTAMP, addedTime);
        values.put(Constants.C_UPDATED_TIMESTAMP, updatedTime);

        //insert row, it will return record id of saved record
        db.update(Constants.ITEMS_TABLE, values, Constants.C_ID +" = ?", new String[] {id});
        //close db connection
        db.close();
    }

    //get all data from sqlite database
    public ArrayList<ModelItems> getAllItems(String orderBy){
        //orderby query will allow to sort data e.g newest/oldest first, name ascending/descending
        //it will return list of items since we have used return type ArrayList<ModelItems>

        ArrayList<ModelItems> itemsList = new ArrayList<>();
        //query to select records
        String selectQuery = "SELECT * FROM " + Constants.ITEMS_TABLE + " ORDER BY " + orderBy;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        //looping through all records and add to list
        if(cursor.moveToFirst()){
            do{
                ModelItems modelItems = new ModelItems(
                        ""+cursor.getInt(cursor.getColumnIndex(Constants.C_ID)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_ITEM_NAME)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_ITEM_IMAGE)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_ITEM_PRICE)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_ITEM_MANUFACTURER)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_DESC)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_EXPIRY_DATE)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_MANUFACTURE_DATE)),
                        Integer.parseInt(""+cursor.getInt(cursor.getColumnIndex(Constants.C_DAYS_TO_EXPIRY))),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_STATUS)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_ADDED_TIMESTAMP)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_UPDATED_TIMESTAMP))
                );
                //add record to list
                itemsList.add(modelItems);
            }while (cursor.moveToNext());
        }
        //close db connection
        db.close();

        //return the list
        return itemsList;
    }

    //get all expiring items (items that have 1 to 7 days to expiry)
    public ArrayList<ModelExpiredItems> getAllExpiringItems(String orderBy){
        //orderby query will allow to sort data e.g newest/oldest first, name ascending/descending
        //it will return list of items since we have used return type ArrayList<ModelItems>

        ArrayList<ModelExpiredItems> itemsList = new ArrayList<>();
        String exp = "Expired";
        //query to select records
        String selectQuery = "SELECT * FROM " + Constants.ITEMS_TABLE + " WHERE " + Constants.C_STATUS + " = " + "\""+exp+"\"" + " ORDER BY " + orderBy;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        //looping through all records and add to list
        if(cursor.moveToFirst()){
            do{
                ModelExpiredItems modelExpiredItems = new ModelExpiredItems(
                        ""+cursor.getInt(cursor.getColumnIndex(Constants.C_ID)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_ITEM_NAME)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_ITEM_IMAGE)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_ITEM_PRICE)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_ITEM_MANUFACTURER)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_DESC)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_EXPIRY_DATE)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_MANUFACTURE_DATE)),
                        Integer.parseInt(""+cursor.getInt(cursor.getColumnIndex(Constants.C_DAYS_TO_EXPIRY))),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_STATUS)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_ADDED_TIMESTAMP)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_UPDATED_TIMESTAMP))
                );
                //add record to list
                itemsList.add(modelExpiredItems);
            }while (cursor.moveToNext());
        }
        //close db connection
        db.close();

        //return the list
        return itemsList;
    }

    //search data
    public ArrayList<ModelItems> searchItems(String query){
        //orderby query will allow to sort data e.g newest/oldest first, name ascending/descending
        //it will return list of items since we have used return type ArrayList<ModelItems>

        ArrayList<ModelItems>itemsList = new ArrayList<>();
        //query to select records
        String selectQuery = "SELECT * FROM " + Constants.ITEMS_TABLE + " WHERE " + Constants.C_ITEM_NAME + " LIKE '%" + query +" '%";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        //looping through all records and add to list
        if(cursor.moveToFirst()){
            do{
                ModelItems modelItems = new ModelItems(
                        ""+cursor.getInt(cursor.getColumnIndex(Constants.C_ID)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_ITEM_NAME)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_ITEM_IMAGE)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_ITEM_PRICE)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_ITEM_MANUFACTURER)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_DESC)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_EXPIRY_DATE)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_MANUFACTURE_DATE)),
                        Integer.parseInt(""+cursor.getInt(cursor.getColumnIndex(Constants.C_DAYS_TO_EXPIRY))),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_STATUS)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_ADDED_TIMESTAMP)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_UPDATED_TIMESTAMP))
                );

                //add record to list
                itemsList.add(modelItems);
            }while (cursor.moveToNext());
        }
        //close db connection
        db.close();

        //return the list
        return itemsList;
    }

    //search Expired Items
    public ArrayList<ModelExpiredItems> searchExpiredItems(String query){
        //orderby query will allow to sort data e.g newest/oldest first, name ascending/descending
        //it will return list of items since we have used return type ArrayList<ModelItems>

        ArrayList<ModelExpiredItems>itemsList = new ArrayList<>();
        //query to select records
        String selectQuery = "SELECT * FROM " + Constants.ITEMS_TABLE + " WHERE " + Constants.C_DAYS_TO_EXPIRY + " =" + 0 + " AND " + Constants.C_ITEM_NAME + " LIKE '%" + query +" '%";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        //looping through all records and add to list
        if(cursor.moveToFirst()){
            do{
                ModelExpiredItems modelExpiredItems = new ModelExpiredItems(
                        ""+cursor.getInt(cursor.getColumnIndex(Constants.C_ID)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_ITEM_NAME)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_ITEM_IMAGE)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_ITEM_PRICE)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_ITEM_MANUFACTURER)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_DESC)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_EXPIRY_DATE)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_MANUFACTURE_DATE)),
                        Integer.parseInt(""+cursor.getInt(cursor.getColumnIndex(Constants.C_DAYS_TO_EXPIRY))),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_STATUS)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_ADDED_TIMESTAMP)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_UPDATED_TIMESTAMP))
                );

                //add record to list
                itemsList.add(modelExpiredItems);
            }while (cursor.moveToNext());
        }
        //close db connection
        db.close();

        //return the list
        return itemsList;
    }

    //delete single data using id
    public void deleteData(String id){
        SQLiteDatabase db = getWritableDatabase();
        db.delete(Constants.ITEMS_TABLE, Constants.C_ID + " = ?", new String[] {id});
        db.close();
    }

    //delete all data from table
    public void deleteAllData(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + Constants.ITEMS_TABLE);
        db.close();
    }

    //delete all expired data from table
    public void deleteAllExpiredData(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + Constants.ITEMS_TABLE);
        db.close();
    }

    //get number of items
    public int getItemsCount(){
        String countQuery = "SELECT * FROM " + Constants.ITEMS_TABLE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void updateExpiryRow(){

        int daysToExpiry = 0;
        String expiryStatus = "Not Expired";
        String selectQuery = "SELECT * FROM " + Constants.ITEMS_TABLE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        //looping through all records and add to list
        if(cursor.moveToFirst()){
            do{
                ModelItems modelItems = new ModelItems(
                        ""+cursor.getInt(cursor.getColumnIndex(Constants.C_ID)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_ITEM_NAME)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_ITEM_IMAGE)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_ITEM_PRICE)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_ITEM_MANUFACTURER)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_DESC)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_EXPIRY_DATE)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_MANUFACTURE_DATE)),
                        Integer.parseInt(""+cursor.getInt(cursor.getColumnIndex(Constants.C_DAYS_TO_EXPIRY))),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_STATUS)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_ADDED_TIMESTAMP)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_UPDATED_TIMESTAMP))
                );

                SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
                DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("dd-MM-yyyy");

                try {
                    Date date = formatter.parse(modelItems.getItemExp());
                    LocalDate now = LocalDate.now();
                    String text = now.format(formatter2);
                    Date dateNow = formatter.parse(text);
                    long diffInMillies = Math.abs(date.getTime() - dateNow.getTime());
                    long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

                    //to get the expiry status of the item if expired or not
                    if(date.compareTo(dateNow) <= 0) {
                        daysToExpiry = 0;
                        expiryStatus = "Expired";
                    } else {
                        daysToExpiry = (int) (long) diff;
                        expiryStatus = "Not Expired";
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                ContentValues values = new ContentValues();
                // id will be inserted automatically as we set AUTOINCREMENT in query

                //insert data
                String timestamp = ""+System.currentTimeMillis();
                values.put(Constants.C_ID, modelItems.getId());
                values.put(Constants.C_ITEM_NAME, modelItems.getItemName());
                values.put(Constants.C_ITEM_IMAGE, modelItems.getItemImage());
                values.put(Constants.C_ITEM_PRICE, modelItems.getItemPrice());
                values.put(Constants.C_ITEM_MANUFACTURER, modelItems.getItemManufacturer());
                values.put(Constants.C_DESC, modelItems.getItemDesc());
                values.put(Constants.C_EXPIRY_DATE, modelItems.getItemExp());
                values.put(Constants.C_MANUFACTURE_DATE, modelItems.getItemMfd());
                values.put(Constants.C_DAYS_TO_EXPIRY, daysToExpiry);
                values.put(Constants.C_STATUS, expiryStatus);
                values.put(Constants.C_ADDED_TIMESTAMP, modelItems.getAddedTime());
                values.put(Constants.C_UPDATED_TIMESTAMP, timestamp);

                //insert row, it will return record id of saved record
                db.update(Constants.ITEMS_TABLE, values, Constants.C_ID +" = ?", new String[] {modelItems.getId()});
                //add record to list
            }while (cursor.moveToNext());
        }
        //close db connection
        db.close();
    }

    //insert record to user Login table
    public long insertUser(UserModel userModel){
        //get writeable database because we want to write data
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values2 = new ContentValues();
        // id will be inserted automatically as we set AUTOINCREMENT in query

        //insert data
        values2.put(Constants.L_EMAIL, userModel.getEmail());
        values2.put(Constants.L_PASSWORD, userModel.getPassword());
        values2.put(Constants.L_USERNAME, userModel.getUserName());
        values2.put(Constants.L_ROLE, userModel.getRole());
        values2.put(Constants.L_ROLE_ID, userModel.getRoleId());
        values2.put(Constants.L_ADDED_TIMESTAMP, userModel.getAddedTimeStamp());


        //insert row, it will return record id of saved record
        long id = db.insert(Constants.LOGIN_TABLE, null, values2);
        //close db connection
        db.close();
        //return id of inserted item
        return id;
    }

    public boolean checkUser(String email){
        String[] column = {L_ID};
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = L_EMAIL + " = ?";
        String[] selectionArgs= {email,};

        Cursor cursor = db.query(LOGIN_TABLE,column,selection,selectionArgs,null,null,null);
        int Cursorcount = cursor.getCount();
        db.close();
        if(Cursorcount>0){
            return true;
        }
        return false;
    }

    public boolean checkUser(String userName, String password, int roleId ){
        String newRoleId = Integer.toString(roleId);
        String[] column = {L_ID};
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = L_USERNAME + " = ?" + " AND " + L_PASSWORD + " = ?" + " AND "+ L_ROLE_ID + " = ?";
        String[] selectionArgs= {userName,password,newRoleId};

        Cursor cursor = db.query(LOGIN_TABLE,column,selection,selectionArgs,null,null,null);
        int Cursorcount = cursor.getCount();
        db.close();
        if(Cursorcount>0){
            return true;
        }
        return false;
    }

    //insert record to user Login table
    public long insertBarcodeItems(BarcodeItemsModel barcodeItemsModel){
        //get writeable database because we want to write data
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        // id will be inserted automatically as we set AUTOINCREMENT in query

        //insert data
        values.put(Constants.B_ITEM_NAME, barcodeItemsModel.getItemName());
        values.put(Constants.B_ITEM_MANUFACTURER, barcodeItemsModel.getItemManufacturer());
        values.put(Constants.B_BARCODE_CODE, barcodeItemsModel.getBarcodeCode());
        values.put(Constants.B_ITEM_IMAGE, barcodeItemsModel.getItemImage());
        values.put(Constants.B_ADDED_TIMESTAMP, barcodeItemsModel.getAddedTimeStamp());


        //insert row, it will return record id of saved record
        long id = db.insert(Constants.BARCODE_TABLE, null, values);
        //close db connection
        db.close();
        //return id of inserted item
        return id;
    }

    public boolean checkBarcodeItemExists(String barcode){
        String[] column = {B_ID};
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = B_BARCODE_CODE + " = ?";
        String[] selectionArgs= {barcode};

        Cursor cursor = db.query(BARCODE_TABLE,column,selection,selectionArgs,null,null,null);
        int Cursorcount = cursor.getCount();
        db.close();
        if(Cursorcount>0){
            return true;
        }
        return false;
    }


    //get item by barcode
    public ArrayList<BarcodeItemsModel> getBarcodeItem(String barcodeValue){

        ArrayList<BarcodeItemsModel>itemsList = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        //query to select records
        String selectQuery = "SELECT * FROM " + BARCODE_TABLE + " WHERE " + B_BARCODE_CODE + " =" + "\""+barcodeValue+"\"";

        Cursor cursor = db.rawQuery(selectQuery, null);

        //looping through all records and add to list
        if(cursor.moveToFirst()){
            do{
                BarcodeItemsModel barcodeItemsModel = new BarcodeItemsModel();
                barcodeItemsModel.setId(cursor.getInt(cursor.getColumnIndex(Constants.B_ID)));
                barcodeItemsModel.setItemName(""+cursor.getString(cursor.getColumnIndex(Constants.B_ITEM_NAME)));
                barcodeItemsModel.setItemImage(""+cursor.getString(cursor.getColumnIndex(Constants.B_ITEM_IMAGE)));
                barcodeItemsModel.setItemManufacturer(""+cursor.getString(cursor.getColumnIndex(Constants.B_ITEM_MANUFACTURER)));
                barcodeItemsModel.setAddedTimeStamp(""+cursor.getString(cursor.getColumnIndex(Constants.B_ADDED_TIMESTAMP)));

                //add record to list
                itemsList.add(barcodeItemsModel);
            }while (cursor.moveToNext());
        }
        //close db connection
        db.close();

        //return the list
        return itemsList;
    }


}
