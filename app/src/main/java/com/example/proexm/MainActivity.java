package com.example.proexm;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.proexm.adapters.AdapterItem;
import com.example.proexm.broadcastReceivers.CheckExpiredProductsBroadcast;
import com.example.proexm.broadcastReceivers.ExpiryBroadcast;
import com.example.proexm.database.Constants;
import com.example.proexm.database.DbHelper;
import com.example.proexm.models.ModelExpiredItems;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    //views
    private FloatingActionButton addItemBtn;
    private RecyclerView itemsRv;
    private final String CHANNEL_ID = "expiring_items";
    private final int NOTIFICATION_ID = 200;

    //db helper
    private DbHelper dbHelper;

    //action bar
    ActionBar actionBar;

    //pending intent for daily refresh
    AlarmManager alarmManager;
    PendingIntent pendingIntent;
    Intent intent;

    //sort options
    String orderByNewest = Constants.C_ADDED_TIMESTAMP + " DESC";
    String orderByOldest = Constants.C_ADDED_TIMESTAMP + " ASC";
    String orderByTitleAsc = Constants.C_ITEM_NAME + " ASC";
    String orderByTitleDesc = Constants.C_ITEM_NAME + " DESC";

    //for refreshing items, refresh with last choosen sort option
    String currentOrderByStatus = orderByNewest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        int roleId = intent.getIntExtra("RoleID",0);
        if(Constants.ROLE_ID==0){
            Constants.ROLE_ID = roleId;
        }
        if(Constants.ROLE_ID == 9){
            setContentView(R.layout.activity_main);
        }
        else if(Constants.ROLE_ID == 1) {
            setContentView(R.layout.activity_main_admin);
        }

        actionBar = getSupportActionBar();
        actionBar.setTitle("All Items");


        //init views
        addItemBtn = findViewById(R.id.addItemBtn);
        itemsRv = findViewById(R.id.itemsRv);

        //init db helper class
        dbHelper = new DbHelper(this);

        //load records (default newest first)
        loadItems(orderByNewest);

        //update Expiry Row on application start
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dbHelper.updateExpiryRow();
            dbHelper.getAllExpiringItems(currentOrderByStatus);
        }

        //click to start add item activity
        addItemBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                //start add new item activity
                Intent intent = new Intent(MainActivity.this, AddNewItem.class);
                intent.putExtra("isEditMode", false); //want to add new data, set false
                startActivity(intent);

//                Intent intent = new Intent(MainActivity.this, ExpiredItemsActivity.class);
//                //intent.putExtra("isEditMode", false); //want to add new data, set false
//                startActivity(intent);

            }
        });

        //set alarm to particular time
        //we cancel the alarm
       // cancelAlert();
        //we set the alarm
        startAlertAtParticularTime();
        createNotificationChannel();

        //set bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        //set home selected
        bottomNavigationView.setSelectedItemId(R.id.home);
        //perform itemselected listener to navigate activity
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.home:
                        return true;
                    case R.id.expItems:
                        Intent intent = new Intent(MainActivity.this, ExpiredItemsActivity.class);
                        intent.putExtra("isEditMode", false); //want to add new data, set false
                        startActivity(intent);
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.baseItems:
                        Intent intent2 = new Intent(MainActivity.this, AddBarcodeItem.class);
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

    private void loadItems(String orderBy){
        currentOrderByStatus = orderBy;
        AdapterItem adapterItem = new AdapterItem(MainActivity.this,
                dbHelper.getAllItems(orderBy));
        itemsRv.setAdapter(adapterItem);
    }

    private void searchItems(String query){
        AdapterItem adapterItem = new AdapterItem(MainActivity.this,
                dbHelper.searchItems(query));
        itemsRv.setAdapter(adapterItem);
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
                        Intent intent = new Intent(MainActivity.this, AddNewItem.class);
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

    @Override
    public void onResume(){
        super.onResume();
        loadItems(currentOrderByStatus); // refresh Item list
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
                searchItems(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchItems(newText);
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
                            loadItems(orderByTitleAsc);
                        }
                        else if(which == 1){
                            //title descending
                            loadItems(orderByTitleDesc);
                        }
                        else if (which == 2){
                            //newest
                            loadItems(orderByNewest);
                        }
                        else if (which == 3){
                            //oldest
                            loadItems(orderByOldest);
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
                            dbHelper.deleteAllData();
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

    public void startAlertAtParticularTime() {

        if(dbHelper.getAllExpiringItems(currentOrderByStatus).size()>0){
            Intent alertIntent = new Intent(this, ExpiryBroadcast.class);

            //String name = "ProExm Product Expiry";
            //String content = "Some products will soon expire, check now...";
            alertIntent.putExtra("name", "ProExm Product Expiry");
            alertIntent.putExtra("content", "Some products have expired, check now...");
            // if there are product that have expired notification show everyday
            //intent = new Intent(this, CheckExpiredProductsBroadcast.class);
            int uniqueInt = (int) (System.currentTimeMillis() & 0xfffffff);
            pendingIntent = PendingIntent.getBroadcast(
                    this.getApplicationContext(), uniqueInt, alertIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Calendar calendar = Calendar.getInstance();
            //calendar.set(Calendar.DAY_OF_WEEK, 2);
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, 11);
            calendar.set(Calendar.MINUTE, 55);

            alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, pendingIntent);

            Toast.makeText(this, "Alarm will vibrate at time specified",
                    Toast.LENGTH_SHORT).show();

        }
        else{
            // if there are no expired product notification show everyweek
            Intent alertIntent = new Intent(this, ExpiryBroadcast.class);

            //String name = "ProExm Product Expiry";
            //String content = "Some products will soon expire, check now...";
            alertIntent.putExtra("name", "ProExm Expiry Refresh Alert");
            alertIntent.putExtra("content", "Some products will soon expire, check now...");
            intent = new Intent(this, ExpiryBroadcast.class);
            int uniqueInt = (int) (System.currentTimeMillis() & 0xfffffff);
            pendingIntent = PendingIntent.getBroadcast(
                    this.getApplicationContext(), uniqueInt, alertIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_WEEK, 2);
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, 11);
            calendar.set(Calendar.MINUTE, 55);

            alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, pendingIntent);

            Toast.makeText(this, "Alarm will vibrate at time specified",
                    Toast.LENGTH_SHORT).show();


        }
    }

    public void cancelAlert() {

        // alarm first vibrate at 14 hrs and 40 min and repeat itself at ONE_HOUR interval

        intent = new Intent(this, ExpiryBroadcast.class);
        pendingIntent = PendingIntent.getBroadcast(
                this.getApplicationContext(), 280192, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        if(alarmManager != null){
            alarmManager.cancel(pendingIntent);
        }
        Toast.makeText(this, "Notification cancelled",
                Toast.LENGTH_SHORT).show();

    }

    private void createNotificationChannel(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            CharSequence name = "Application Notifications";
            String description = "Include all the application notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            notificationChannel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);

        }
    }

}

