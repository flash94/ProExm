package com.example.proexm.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proexm.AddNewItem;
import com.example.proexm.ItemDetailActivity;
import com.example.proexm.MainActivity;
import com.example.proexm.R;
import com.example.proexm.database.DbHelper;
import com.example.proexm.models.ModelItems;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

/*Custom Adapter class for recyclerView
* Here we will inflate row_card_record for with items from our model
* get and set data to views*/
public class AdapterItem extends RecyclerView.Adapter<AdapterItem.ItemHolderRecord> {

    //variables
    private Context context;
    private ArrayList<ModelItems>itemsList;

    //DB Helper
    DbHelper dbHelper;

    //constructor
    public AdapterItem(Context context, ArrayList<ModelItems> itemsList) {
        this.context = context;
        this.itemsList = itemsList;
        dbHelper = new DbHelper(context);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemHolderRecord onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_card_record, parent,false);
        return new ItemHolderRecord(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolderRecord holder, final int position) {
        //get data, set data, handle view clicks in this method
        ModelItems model = itemsList.get(position);
        final String id = model.getId();
        final String itemName = model.getItemName();
        final String itemImage = model.getItemImage();
        final String itemPrice = model.getItemPrice();
        final String itemManufacturer = model.getItemManufacturer();
        final String itemDesc = model.getItemDesc();
        final String itemExp = model.getItemExp();
        final String itemMfd = model.getItemMfd();
        final String daysToExpiry = model.getDaysToExpiry();
        final String itemStatus = model.getItemStatus();
        final String addedTime = model.getAddedTime();
        final String updatedTime = model.getUpdatedTime();


        //set data to views
        holder.itemNameTv.setText("Name: "+itemName);
        //holder.itemIv.setImageURI(Uri.parse(itemImage));
        holder.priceTv.setText("Price:" +itemPrice);
        holder.expDateTv.setText("Expiry Date: "+itemExp);
        holder.mfdDateTv.setText("Status: "+itemStatus);

        String exp = "EXPIRED";
        if(itemStatus.matches("Expired")){
            holder.expDateTv.setTextColor(Color.RED);
            holder.mfdDateTv.setTextColor(Color.RED);
        } else {
            holder.expDateTv.setTextColor(Color.GREEN);
            holder.mfdDateTv.setTextColor(Color.GREEN);
        }


        //if item has no image
        if (itemImage.equals("null")){
            //no image in record set default
            holder.itemIv.setImageResource(R.drawable.ic_no_photo);
        }
        else{
            //have image in record
            holder.itemIv.setImageURI(Uri.parse(itemImage));
        }

        //handle onclick (go to detail record activity)
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ItemDetailActivity.class);
                intent.putExtra("ITEM_ID", id);
                context.startActivity(intent);
                //will later implement

            }
        });

        //handle more button click listener (show options like edit, delete etc)
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show option menu
                showMoreDialog(
                        ""+position,
                        ""+id,
                        ""+itemName,
                        ""+itemImage,
                        ""+itemPrice,
                        ""+itemManufacturer,
                        ""+itemMfd,
                        ""+itemExp,
                        ""+itemDesc,
                        ""+daysToExpiry,
                        ""+itemStatus,
                        ""+addedTime,
                        ""+updatedTime
                );
            }
        });

        Log.d("ImagePath", "onBindViewHolder: "+itemImage);

    }

    private void showMoreDialog(String position, final String id, final String itemName, final String itemImage, final String itemPrice, final String itemManufacturer, final String itemMfd,
                                final String itemExp, final String itemDesc, final String daysToExpiry, final String itemStatus, final String addedTime, final String updatedTime) {
        //options to display in dialog
        String[] options = {"Edit", "Delete"};
        //dialog
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        //add items to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //handle item clicks
                if (which==0){
                    //Edit is clicked
                    //start AddUpdateItemActivity to update existing item
                    Intent intent = new Intent(context, AddNewItem.class);
                    intent.putExtra("ID",id);
                    intent.putExtra("ITEM_NAME",itemName);
                    intent.putExtra("ITEM_IMAGE",itemImage);
                    intent.putExtra("ITEM_PRICE",itemPrice);
                    intent.putExtra("ITEM_MANUFACTURER",itemManufacturer);
                    intent.putExtra("ITEM_MFD",itemMfd);
                    intent.putExtra("ITEM_EXP",itemExp);
                    intent.putExtra("ITEM_DESC",itemDesc);
                    intent.putExtra("DAYS_TO_EXPIRY",daysToExpiry);
                    intent.putExtra("ITEM_STATUS",itemStatus);
                    intent.putExtra("ADDED_TIME",addedTime);
                    intent.putExtra("UPDATED_TIME",updatedTime);
                    intent.putExtra("isEditMode",true);//need to update existing data, set true
                    context.startActivity(intent);
                }
                else if(which==1){
                    //delete is clicked
                    dbHelper.deleteData(id);
                    //refresh record by calling activity's onResume method
                    ((MainActivity)context).onResume();
                }
            }
        });
        //show dialog
        builder.create().show();
    }

    @Override
    public int getItemCount() {
        return itemsList.size(); //return size of list/number of items
    }

    class ItemHolderRecord extends RecyclerView.ViewHolder{

        //variables
        private Context context;
        private ArrayList<ModelItems>itemsList;

        //views
        ImageView itemIv;
        TextView itemNameTv, priceTv, mfdDateTv, expDateTv;
        ImageButton moreBtn;

        public ItemHolderRecord(@NonNull View itemView) {
            super(itemView);

            //init views
            itemIv = itemView.findViewById(R.id.itemIv);
            itemNameTv = itemView.findViewById(R.id.itemNameTv);
            priceTv = itemView.findViewById(R.id.priceTv);
            mfdDateTv = itemView.findViewById(R.id.mfdDateTv);
            expDateTv = itemView.findViewById(R.id.expDateTv);
            moreBtn = itemView.findViewById(R.id.moreBtn);
        }
    }
}
