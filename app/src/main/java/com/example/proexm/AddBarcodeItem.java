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
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.proexm.database.DbHelper;
import com.example.proexm.interfaces.CallBackInterface;
import com.example.proexm.models.BarcodeItemsModel;
import com.example.proexm.models.UserModel;
import com.example.proexm.validations.InputValidation;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.makeramen.roundedimageview.RoundedImageView;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class AddBarcodeItem extends AppCompatActivity implements View.OnClickListener {

    private RoundedImageView itemImage;
    private EditText itemNameEt, manufacturerEt, barcodeEt;
    private FloatingActionButton saveBtn;

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
    private String id, itemName, manufacturer, price, barcode, mfDate, expDate, desc, itemStatus, addedTime, updatedTime;
    private boolean isEditMode = false;
    private int daysTOExpiry;

    //db helper
    private DbHelper dbHelper;
    //actionbar
    private ActionBar actionBar;
    //user model
    private BarcodeItemsModel barcodeItemsModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_barcode_item);

        //init actionbar
        actionBar = getSupportActionBar();
        //actionbar title
        actionBar.setTitle("Add Base Products");
        //back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        initViews();
        initObject();
        initPermissions();
        initListeners();
        initbottomNavigation();

    }

    public void initViews(){
        //init views
        itemImage = findViewById(R.id.itemImage);
        itemNameEt = findViewById(R.id.itemNameEt);
        manufacturerEt = findViewById(R.id.manufacturerEt);
        barcodeEt = findViewById(R.id.barcodeEt);
        saveBtn = findViewById(R.id.saveBtn);
    }

    private void initObject(){
        dbHelper = new DbHelper(this);
        barcodeItemsModel = new BarcodeItemsModel();
    }

    private void initListeners(){
        saveBtn.setOnClickListener(this);
        itemImage.setOnClickListener(this);
    }

    private void initPermissions(){
        //init permission arrays
        cameraPermissions = new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    }

    private void initbottomNavigation(){
        //set bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        //set home selected
        bottomNavigationView.setSelectedItemId(R.id.baseItems);
        //perform itemselected listener to navigate activity
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.home:
                        Intent intent = new Intent(AddBarcodeItem.this, MainActivity.class);
                        intent.putExtra("isEditMode", false); //want to add new data, set false
                        startActivity(intent);
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.expItems:
                        Intent intent2 = new Intent(AddBarcodeItem.this, ExpiredItemsActivity.class);
                        intent2.putExtra("isEditMode", false); //want to add new data, set false
                        startActivity(intent2);
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.baseItems:
                        return true;
                    case R.id.scannerBtn:
                        scanCode();
                }
                return false;
            }
        });
    }

    private void scanCode(){
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(CaptureActivity.class);
        integrator.setOrientationLocked(false);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Scanning Code");
        integrator.initiateScan();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.saveBtn:
                //add new item to barcode item table
                addBarcodeItem();
                break;
            case R.id.itemImage:
                //launch image picker dialog
                imagePickDialog();
                break;
        }
    }

    //add new item to barcode item table
    private void addBarcodeItem() {
        String timestamp = ""+System.currentTimeMillis();
        if(!dbHelper.checkBarcodeItemExists(barcodeEt.getText().toString().trim())){
            barcodeItemsModel.setItemName(itemNameEt.getText().toString().trim());
            barcodeItemsModel.setItemImage(""+imageUri);
            barcodeItemsModel.setBarcodeCode(barcodeEt.getText().toString().trim());
            barcodeItemsModel.setItemManufacturer(manufacturerEt.getText().toString().trim());
            barcodeItemsModel.setAddedTimeStamp(timestamp);

            dbHelper.insertBarcodeItems(barcodeItemsModel);
            Toast.makeText(this, itemNameEt.getText().toString().trim() + "Added", Toast.LENGTH_SHORT).show();
            emptyInputEditText();

        }
        else{
            Toast.makeText(this, "Product Not Added! Barcode Already Exists", Toast.LENGTH_LONG).show();
        }
    }
    // refresh inputfields
    private void emptyInputEditText() {
        barcodeEt.setText(null);
        itemNameEt.setText(null);
        manufacturerEt.setText(null);
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
        final IntentResult result = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
                if(result != null){
                    if(result.getContents() != null){
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Scan Result");
                        builder.setMessage(result.getContents());
                        builder.setPositiveButton("Add Product", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(AddBarcodeItem.this, AddNewItem.class);
                                String barcodeValue = result.getContents();
                                intent.putExtra("barcodeValue", barcodeValue); //want to add new data, set false
                                startActivity(intent);

                            }
                        }).setNegativeButton("Scan Again", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                scanCode();
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                    else{
                        Toast.makeText(this, "Invalid barcode", Toast.LENGTH_LONG).show();
                    }
                }


        super.onActivityResult(requestCode, resultCode, data);
    }

}
