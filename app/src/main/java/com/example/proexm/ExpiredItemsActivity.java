package com.example.proexm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.proexm.adapters.ExpiredItemsAdapter;
import com.example.proexm.database.Constants;
import com.example.proexm.database.DbHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class ExpiredItemsActivity extends AppCompatActivity {

    private RecyclerView expiredItemsRv;

    //sort options
    String orderByNewest = Constants.C_ADDED_TIMESTAMP + " DESC";
    String orderByOldest = Constants.C_ADDED_TIMESTAMP + " ASC";
    String orderByTitleAsc = Constants.C_ITEM_NAME + " ASC";
    String orderByTitleDesc = Constants.C_ITEM_NAME + " DESC";

    //for refreshing items, refresh with last choosen sort option
    String currentOrderByStatus = orderByNewest;

    //db helper
    private DbHelper dbHelper;

    //ACTION BAR
    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Constants.ROLE_ID == 9){
            setContentView(R.layout.activity_expired_items);
        }
        else if(Constants.ROLE_ID == 1) {
            setContentView(R.layout.activity_expired_items_admin);
        }
        //init actionbar
        actionBar = getSupportActionBar();
        //actionbar title
        actionBar.setTitle("Expired Items");
        //back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        //init db helper class
        dbHelper = new DbHelper(this);

        //init views
        expiredItemsRv = findViewById(R.id.expiredItemsRv);

        //load records (default newest first)
        loadExpiredItems(orderByNewest);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dbHelper.updateExpiryRow();
        }

        //set bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        //set home selected
        bottomNavigationView.setSelectedItemId(R.id.expItems);
        //perform itemselected listener to navigate activity
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.home:
                        Intent intent = new Intent(ExpiredItemsActivity.this, MainActivity.class);
                        intent.putExtra("isEditMode", false); //want to add new data, set false
                        startActivity(intent);
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.expItems:
                        return true;
                    case R.id.baseItems:
                        Intent intent2 = new Intent(ExpiredItemsActivity.this, AddBarcodeItem.class);
                        intent2.putExtra("isEditMode", false); //want to add new data, set false
                        startActivity(intent2);
                        overridePendingTransition(0,0);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        final IntentResult result = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if(result != null){
            if(result.getContents() != null){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Scan Result");
                builder.setMessage(result.getContents());
                builder.setPositiveButton("Add Product", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(ExpiredItemsActivity.this, AddNewItem.class);
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
        }else{
            super.onActivityResult(requestCode,resultCode,data);
        }
    }


    private void loadExpiredItems(String orderBy){
        currentOrderByStatus = orderBy;
        ExpiredItemsAdapter expiredItemsAdapter = new ExpiredItemsAdapter(ExpiredItemsActivity.this,
                dbHelper.getAllExpiringItems(orderBy));
        expiredItemsRv.setAdapter(expiredItemsAdapter);
    }

    private void searchExpiredItems(String query){
        ExpiredItemsAdapter expiredItemsAdapter = new ExpiredItemsAdapter(ExpiredItemsActivity.this,
                dbHelper.searchExpiredItems(query));
        expiredItemsRv.setAdapter(expiredItemsAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflate menu
        getMenuInflater().inflate(R.menu.menu_main, menu);

        //searchView
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView)item.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchExpiredItems(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchExpiredItems(newText);
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        //handle menu items
        int id = item.getItemId();
        if(id==R.id.action_sort){
            //show sort options(show in dialog)
            sortOptionDialog();
        }
        else if (id == R.id.action_delete_all){
            //show dialog asking if to delete all items
            confirmDeleteDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    private void sortOptionDialog() {
        //options to display in dialog
        String[] options = {"Title Ascending", "Title Descending", "Newest", "Oldest"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sort By")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle options click
                        if(which == 0){
                            //title ascending
                            loadExpiredItems(orderByTitleAsc);
                        }
                        else if(which == 1){
                            //title descending
                            loadExpiredItems(orderByTitleDesc);
                        }
                        else if (which == 2){
                            //newest
                            loadExpiredItems(orderByNewest);
                        }
                        else if (which == 3){
                            //oldest
                            loadExpiredItems(orderByOldest);
                        }
                    }
                })
                .create().show();//show dialog
    }

    private void confirmDeleteDialog() {
        //options to display in dialog
        String[] options = {"Yes", "No"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("You are about to delete all items")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle options click
                        if(which == 0){
                            //delete all items
                            dbHelper.deleteAllExpiredData();
                            onResume();
                        }
                        else if(which == 1){
                            //cancel delete dialog
                            onResume();
                        }

                    }
                })
                .create().show();//show dialog
    }

    @Override
    public void onResume() {
        super.onResume();
        loadExpiredItems(currentOrderByStatus);

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // go back by clicking back button on actionbar
        return super.onSupportNavigateUp();
    }
}
