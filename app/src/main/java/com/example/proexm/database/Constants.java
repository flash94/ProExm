package com.example.proexm.database;

public class Constants {

    //db name
    public static final String DB_NAME = "PROEXM_DB";
    //db version
    public static final int DB_VERSION = 1;
    //table name
    public static final String ITEMS_TABLE = "ITEMS_TABLE";
    public static final String LOGIN_TABLE = "USER_LOGIN_TABLE";
    public static final String BARCODE_TABLE = "BARCODE_ITEM_TABLE";


    //login role id
    public static int ROLE_ID = 0;


    //columns/fields of ITEMS TABLE
    public static final  String C_ID = "ID";
    public static final  String C_ITEM_NAME = "ITEM_NAME";
    public static final  String C_ITEM_IMAGE = "ITEM_IMAGE";
    public static final  String C_ITEM_PRICE = "ITEM_PRICE";
    public static final  String C_ITEM_MANUFACTURER = "ITEM_MANUFACTURER";
    public static final  String C_DESC = "ITEM_DESCRIPTION";
    public static final  String C_EXPIRY_DATE = "ITEM_EXPIRY_DATE";
    public static final  String C_MANUFACTURE_DATE = "ITEM_MANUFACTURE_DATE";
    public static final  String C_DAYS_TO_EXPIRY = "ITEM_DAYS_TO_EXPIRY";
    public static final String C_STATUS = "STATUS";
    public static final  String C_ADDED_TIMESTAMP = "ADDED_TIME_STAMP";
    public static final  String C_UPDATED_TIMESTAMP = "UPDATED_TIME_STAMP";


    //COLUMNS/FIELDS OF USER LOGIN
    public static final  String L_ID = "ID";
    public static final  String L_EMAIL = "EMAIL";
    public static final String L_USERNAME = "USERNAME";
    public static final String L_ROLE = "ROLE";
    public static final String L_ROLE_ID = "ROLE_ID";
    public static final  String L_PASSWORD = "PASSWORD";
    public static final  String L_ADDED_TIMESTAMP = "ADDED_TIME_STAMP";

    //columns/fields of ITEMS TABLE
    public static final  String B_ID = "ID";
    public static final  String B_ITEM_NAME = "ITEM_NAME";
    public static final  String B_ITEM_IMAGE = "ITEM_IMAGE";
    public static final  String B_ITEM_MANUFACTURER = "ITEM_MANUFACTURER";
    public static final  String B_BARCODE_CODE = "ITEM_BARCODE";
    public static final  String B_ADDED_TIMESTAMP = "ADDED_TIME_STAMP";


    //Create ITEMS table query
    public static final  String CREATE_ITEMS_TABLE = "CREATE TABLE " + ITEMS_TABLE + "("
            + C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + C_ITEM_NAME + " TEXT,"
            + C_ITEM_IMAGE + " TEXT,"
            + C_ITEM_PRICE + " TEXT,"
            + C_ITEM_MANUFACTURER + " TEXT,"
            + C_DESC + " TEXT,"
            + C_EXPIRY_DATE + " TEXT,"
            + C_MANUFACTURE_DATE + " TEXT,"
            + C_DAYS_TO_EXPIRY + " INTEGER, "
            + C_STATUS + " TEXT,"
            + C_ADDED_TIMESTAMP + " TEXT,"
            + C_UPDATED_TIMESTAMP + " TEXT"
            + ")";

    //Create USER LOGIN table query
    public static final  String CREATE_LOGIN_TABLE = "CREATE TABLE " + LOGIN_TABLE + "("
            + L_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + L_EMAIL + " TEXT,"
            + L_USERNAME + " TEXT,"
            + L_ROLE + " TEXT,"
            + L_ROLE_ID + " INTEGER,"
            + L_PASSWORD + " TEXT,"
            + L_ADDED_TIMESTAMP + " TEXT"
            + ")";

    //Create USER Barcode Items table query
    public static final  String CREATE_BARCODE_TABLE = "CREATE TABLE " + BARCODE_TABLE + "("
            + B_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + B_ITEM_NAME + " TEXT,"
            + B_ITEM_IMAGE + " TEXT,"
            + B_ITEM_MANUFACTURER + " TEXT,"
            + B_BARCODE_CODE + " TEXT,"
            + B_ADDED_TIMESTAMP + " TEXT"
            + ")";

}
