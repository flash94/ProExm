package com.example.proexm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.example.proexm.database.DbHelper;
import com.example.proexm.interfaces.CallBackInterface;
import com.example.proexm.models.BarcodeItemsModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.makeramen.roundedimageview.RoundedImageView;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class AddNewItem extends AppCompatActivity implements CallBackInterface {

    //views
    private RoundedImageView itemImage;
    private EditText itemNameEt, manufacturerEt, priceEt, mfDateEt, expDateEt, descEt;
    private FloatingActionButton saveBtn;

    private String barcodeValue;

    //permission constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 101;

    //image pick constants
    private static final int IMAGE_PICK_CAMERA_CODE = 102;
    private static final int IMAGE_PICK_GALLERY_CODE = 103;

    //array of permissions
    private String[] cameraPermissions; //camera and storage
    private String[] storagePermissions; //only storage
    //variables (Will contain only data to save to database)
    private Uri imageUri;
    private String id, itemName, manufacturer, price, mfDate, expDate, desc, itemStatus, addedTime, updatedTime;
    private boolean isEditMode = false;
    private int daysTOExpiry;

    //db helper
    private DbHelper dbHelper;
    
    //datepicker
    DatePickerDialog datePickerDialog;

   private ArrayList<BarcodeItemsModel>itemsList;

    //actionbar
    private ActionBar actionBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_item);

        //init actionbar
        actionBar = getSupportActionBar();
        //actionbar title
        actionBar.setTitle("Add Item");
        //back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        //init views
        itemImage = findViewById(R.id.itemImage);
        itemNameEt = findViewById(R.id.itemNameEt);
        manufacturerEt = findViewById(R.id.manufacturerEt);
        priceEt = findViewById(R.id.priceEt);
        mfDateEt = (EditText)findViewById(R.id.mfDateEt);
        expDateEt = (EditText) findViewById(R.id.expDateEt);
        descEt = findViewById(R.id.descEt);
        saveBtn = findViewById(R.id.saveBtn);
        mfDateEt.setInputType(InputType.TYPE_NULL);
        expDateEt.setInputType(InputType.TYPE_NULL);

        //get data from Intent
        Intent intent = getIntent();
        isEditMode = intent.getBooleanExtra("isEditMode",false);
        barcodeValue = intent.getStringExtra("barcodeValue");

        //set data to views
        if(isEditMode){
            //update data
            actionBar.setTitle("Update Item");
            id = intent.getStringExtra("ID");
            itemName = intent.getStringExtra("ITEM_NAME");
            imageUri = Uri.parse(intent.getStringExtra("ITEM_IMAGE"));
            price = intent.getStringExtra("ITEM_PRICE");
            manufacturer = intent.getStringExtra("ITEM_MANUFACTURER");
            mfDate = intent.getStringExtra("ITEM_MFD");
            expDate = intent.getStringExtra("ITEM_EXP");
            itemStatus = intent.getStringExtra("ITEM_STATUS");
            desc = intent.getStringExtra("ITEM_DESC");
            addedTime = intent.getStringExtra("ADDED_TIME");
            updatedTime = intent.getStringExtra("UPDATED_TIME");

            //set data to views
            itemNameEt.setText(itemName);
            manufacturerEt.setText(manufacturer);
            priceEt.setText(price);
            mfDateEt.setText(mfDate);
            expDateEt.setText(expDate);
            descEt.setText(desc);

            //if no image was selected while adding data, imageUri value will be "null"
            if(imageUri.toString().equals("null")){
                //no image, set default
                itemImage.setImageResource(R.drawable.ic_add_image);
            }
            else{
                //have image, set image to db image
                itemImage.setImageURI(imageUri);
            }

        }
        else {
            //add data
            actionBar.setTitle("Add Item");
        }
        //init dbhelper
        dbHelper = new DbHelper(this);

        //init permission arrays
        cameraPermissions = new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //click image to show image picker dialog
        itemImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show image pick dialog
                imagePickDialog();

            }
        });
        
        //click to popup date picker for mfDate
        mfDateEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchDatePickerDialog1();
            }
        });

        //click to popup date picker for expDate
        expDateEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchDatePickerDialog2();
            }
        });

        //load barcode data to view
        barcodeData();

        //click button to save item details
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                A obj = new A();
                CallBackInterface startCallback = new B();
                obj.registerCallBackListener(startCallback);
                obj.daysCalc();
                //new Thread(new A());
                //inputData();
            }
        });
    }



    private void launchDatePickerDialog1() {
        // calender class's instance and get current date , month and year from calender
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR); // current year
        int mMonth = c.get(Calendar.MONTH); // current month
        int mDay = c.get(Calendar.DAY_OF_MONTH); // current day
        // date picker dialog
        datePickerDialog = new DatePickerDialog(AddNewItem.this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        // set day of month , month and year value in the edit text

                            mfDateEt.setText(dayOfMonth + "-"
                                    + (monthOfYear + 1) + "-" + year);

                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();


    }

    private void launchDatePickerDialog2() {
        // calender class's instance and get current date , month and year from calender
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR); // current year
        int mMonth = c.get(Calendar.MONTH); // current month
        int mDay = c.get(Calendar.DAY_OF_MONTH); // current day
        // date picker dialog
        datePickerDialog = new DatePickerDialog(AddNewItem.this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        // set day of month , month and year value in the edit text

                        expDateEt.setText(dayOfMonth + "-"
                                + (monthOfYear + 1) + "-" + year);

                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    private void barcodeData(){
        //get barcode data from db
        if(barcodeValue != null){
            if(dbHelper.getBarcodeItem(barcodeValue).size()>0){
                itemsList = dbHelper.getBarcodeItem(barcodeValue);
                System.out.println("Ade");
                manufacturerEt.setText(itemsList.get(0).getItemManufacturer());
               itemImage.setImageURI(Uri.parse(itemsList.get(0).getItemImage()));
               imageUri = Uri.parse(itemsList.get(0).getItemImage());
               itemNameEt.setText(itemsList.get(0).getItemName());
            }
            else {
                Toast.makeText(this, "Product with this barcode does not exist",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void inputData() {
        //get data
        //itemName, manufacturer, price, mfDate, expDate, desc
        itemName = ""+itemNameEt.getText().toString().trim();
        manufacturer = ""+manufacturerEt.getText().toString().trim();
        price = ""+priceEt.getText().toString().trim();
        mfDate = ""+mfDateEt.getText().toString().trim();
        expDate = ""+expDateEt.getText().toString().trim();
        desc = ""+descEt.getText().toString().trim();

        //int daysToExpiry = 0;
        //String expiryStatus = "Not Expired";



        if(isEditMode){
            //update data
            String timestamp = ""+System.currentTimeMillis();
            //get new expiry status on item update

                    //to get the expiry status of the item if expired or not
                    if(daysTOExpiry <= 0){
                        itemStatus = "Expired";
                    }
                    else{
                        itemStatus = "Not Expired";
                    }
            //update item
            dbHelper.updateItemRecord(
                    ""+id,
                    ""+itemName,
                    ""+imageUri,
                    ""+price,
                    ""+manufacturer,
                    ""+desc,
                    ""+expDate,
                    ""+mfDate,
                    ""+daysTOExpiry,
                    ""+itemStatus, //item status will remain the same
                    ""+addedTime, //added time will remain the same
                    ""+timestamp //updated time will be changed
            );
            Toast.makeText(this,"Updated Successfully", Toast.LENGTH_SHORT).show();
        }
        else{
            //new data
            //save to db
            String timestamp = ""+System.currentTimeMillis();
            //get new expiry status on item create
                    //to get the expiry status of the item if expired or not
                    if(daysTOExpiry <= 0){
                        itemStatus = "Expired";
                    }
                    else{
                        itemStatus = "Not Expired";
                    }
            //create item
            long id = dbHelper.insertItemRecord(
                    ""+itemName,
                    ""+imageUri,
                    ""+price,
                    ""+manufacturer,
                    ""+desc,
                    ""+expDate,
                    ""+mfDate,
                    ""+daysTOExpiry,
                    ""+itemStatus,
                    ""+timestamp,
                    ""+timestamp
            );
            //Snackbar.make(this, "Item Added Successfully", Snackbar.LENGTH_LONG).show();
            Toast.makeText(this,"Item Added Successfully", Toast.LENGTH_LONG).show();
        }
        clearFields();
    }

    private void clearFields(){
        itemNameEt.setText(null);
        manufacturerEt.setText(null);
        priceEt.setText(null);
        mfDateEt.setText(null);
        expDateEt.setText(null);
        descEt.setText(null);
    }

    private void imagePickDialog() {
        //options to display in dialog
        String[] options = {"Camera", "Gallery"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //title
        builder.setTitle("Pick Image From");
        //set items/options
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //handle clicks
                if(which == 0){
                    //camera clicked
                    if(!checkCameraPersmissions()){
                        requestCameraPermission();
                    }
                    else {
                        //permission already granted
                        pickFromCamera();
                    }
                }
                else if (which == 1){
                    if(!checkStoragePermission()){
                        requestStoragePermission();
                    }
                    else{
                        //permission already granted
                        pickFromGallery();
                    }
                }
            }
        });
        //create/show dialog
        builder.create().show();
    }

    private void pickFromGallery() {
        //intent to pick image from gallery, the image will be returned in onActivityResult method
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*"); //we want only images
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        //intent to pick image from gallery, the image will be returned in onActivityResult method

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Image title");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Image description");
        //put image uri
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        //intent to open camera for image
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private boolean checkStoragePermission(){
        //check if storage permission is enabled or not

        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return result;
    }

    private void requestStoragePermission(){
        //request storage permission
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPersmissions(){
        //check if camera permissions is enabled or not
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return result && result1;
    }

    private void requestCameraPermission(){
        //request camera permission
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private void copyFileOrDirectory(String srcDir, String desDir){
        //create folder in specific directory
        try {
           File src = new File(srcDir);
           File des = new File(desDir, src.getName());
           if(src.isDirectory()){
               String[] files = src.list();
               int filesLength = files.length;
               for (String file : files){
                   String src1 = new File(src, file).getPath();
                   String dst1 = des.getPath();

                   copyFileOrDirectory(src1, dst1);
               }
           }
           else {
               copyFile(src, des);
           }
        }
        catch (Exception e) {
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void copyFile(File srcDir, File desDir) throws IOException {
        if(!desDir.getParentFile().exists()){
            desDir.mkdirs(); //create if not exists
        }
        if(!desDir.exists()){
            desDir.createNewFile();
        }
        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(srcDir).getChannel();
            destination = new FileOutputStream(desDir).getChannel();
            destination.transferFrom(source, 0, source.size());

            imageUri = Uri.parse(desDir.getPath()); //uri of saved image
            Log.d("ImagePath", "copyFile: "+imageUri);
        }
        catch (Exception e){
            //if there is an error saving the image
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        finally {
            //close resources
            if(source!=null){
                source.close();
            }
            if (destination!=null){
                destination.close();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // go back by clicking back button on actionbar
        return super.onSupportNavigateUp();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //request of permission allowed/denied

        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if(grantResults.length>0){
                    //if allowed returns true otherwise false
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if(cameraAccepted && storageAccepted) {
                        //both permission is allowed
                        pickFromCamera();
                    }
                    else{
                        Toast.makeText(this, "Camera & Storage permissions are required...", Toast.LENGTH_SHORT).show();
                    }
                }

            }
            break;
            case STORAGE_REQUEST_CODE:{
                if(grantResults.length>0){
                    //if allowed returns true otherwise false
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(storageAccepted){
                        //storage permission is allowed
                        pickFromGallery();
                    }
                    else{
                        Toast.makeText(this, "Storage permissions are required...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //image picked from camera or gallery will be recieved here

        if(resultCode == RESULT_OK){
            //image is picked

            if(requestCode == IMAGE_PICK_GALLERY_CODE){
                //picked from gallery
                //crop image
                CropImage.activity(data.getData())
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(this);
            }
            else if(requestCode == IMAGE_PICK_CAMERA_CODE){
                //picked from camera
                //crop image
                CropImage.activity(imageUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(this);
            }
            else if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
                //cropped imaage received
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK){
                    Uri resultUri = result.getUri();
                    imageUri = resultUri;
                    //set image
                    itemImage.setImageURI(resultUri);

                    copyFileOrDirectory(""+imageUri.getPath(), ""+getDir("SQLiteItemImages",MODE_PRIVATE));
                }
                else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                    //error
                    Exception error = result.getError();
                    Toast.makeText(this, " "+error, Toast.LENGTH_SHORT).show();

                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void getDaysBeforeExpiry(int expDate) {

    }

    class B implements CallBackInterface{
        //second synchronous call
        public void getDaysBeforeExpiry(int expDate) {
            inputData();
        }
    }

    class A {
        //first synchronous call
        CallBackInterface startCallBack;
        public void registerCallBackListener(CallBackInterface startCallBack) {
            this.startCallBack = startCallBack;
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        public void daysCalc(){
            //calculate the number of days to expiry on item create
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
            DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("dd-MM-yyyy");

            try {
                Date date = formatter.parse(expDateEt.getText().toString());
                LocalDate now = LocalDate.now();
                String text = now.format(formatter2);
                Date dateNow = formatter.parse(text);
                long diffInMillies = Math.abs(date.getTime() - dateNow.getTime());
                long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
                if(date.compareTo(dateNow) <= 0) {
                    daysTOExpiry = 0;
                } else {
                    daysTOExpiry = (int) (long) diff;
                }
                System.out.println("Number of days between two dates = "+daysTOExpiry);
                startCallBack.getDaysBeforeExpiry(daysTOExpiry);

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

}
