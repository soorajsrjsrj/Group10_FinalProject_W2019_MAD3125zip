package com.example.group10_finalproject_w2019_mad3125zip;

import android.content.Intent;
import android.database.Cursor;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;


import com.facebook.accountkit.AccountKit;
import com.facebook.login.LoginManager;

import java.math.BigDecimal;
import java.sql.SQLException;

public class HomePageActivity extends AppCompatActivity {
    private StoreDatabase dbHelper;
    private DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();



        dbHelper = new StoreDatabase(this);
        try {
            dbHelper.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //check if items are available
        int total = dbHelper.getTotalItemsCount();
        if (total<= 0){
            //Add some data
            dbHelper.insertMyShopItems();
        }
        //Generate ListView from SQLite Database
        displayListView();

        int num = dbHelper.getCartItemsRowCount(1);
        int amount = dbHelper.getAmount();
        BigDecimal priceVal;
        if (total == num){
            double tAmount = amount - (0.2 * amount);
            priceVal = BigDecimal.valueOf((long) tAmount, 2);
        } else {
            priceVal = BigDecimal.valueOf(amount, 2);
        }


        TextView numItemsBought = (TextView)findViewById(R.id.cart);
        numItemsBought.setText(num+" of "+ total+" items");

        TextView totalAmount = (TextView)findViewById(R.id.total);
        totalAmount.setText("Total Amount: $"+priceVal);

        LinearLayout cart = (LinearLayout)findViewById(R.id.linearLayout);
        assert cart != null;
        cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePageActivity.this, ShoppingCartActivity.class);
                startActivity(intent);
            }
        });
    }
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void displayListView() {
        Cursor cursor = dbHelper.fetchAllItems("0"); // 0 is used to denote an item yet to be bought

        // Display name of item to be sold
        String[] columns = new String[] {
                StoreDatabase.KEY_NAME
        };

        // the XML defined view which the data will be bound to
        int[] to = new int[] {
                R.id.name,
        };

        // create the adapter using the cursor pointing to the desired data
        //as well as the layout information
        SimpleCursorAdapter dataAdapter = new SimpleCursorAdapter(
                this, R.layout.item_layout,
                cursor,
                columns,
                to,
                0);

        ListView listView = (ListView) findViewById(R.id.listView);
        // Assign adapter to ListView
        assert listView != null;
        listView.setAdapter(dataAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listView, View view, int position, long id) {
                // Get the cursor, positioned to the corresponding row in the result set
                Cursor cursor = (Cursor) listView.getItemAtPosition(position);

                // Get the item attributes to be sent to details activity from this row in the database.
                String name =  cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String description =  cursor.getString(cursor.getColumnIndexOrThrow("description"));
                int price =  cursor.getInt(cursor.getColumnIndexOrThrow("price"));
                int itemId =  cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
                Intent intent = new Intent(HomePageActivity.this, DetailsActivity.class);
                intent.putExtra("name", name);
                intent.putExtra("description", description);
                intent.putExtra("price", price);
                intent.putExtra("_id", itemId);
                startActivity(intent);

            }
        });
    }



    public void logout(View view) {
        LoginManager.getInstance().logOut();
        AccountKit.logOut();
        startActivity(new Intent(HomePageActivity.this, HomePageActivity.class));
        finish();
    }
}
