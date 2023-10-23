package edu.ucsb.ece150.gauchopay;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
public class CardListActivity extends AppCompatActivity {

    private static final int RC_HANDLE_INTERNET_PERMISSION = 2;

    private ArrayList<String> cardArray;
    private ArrayAdapter adapter;

    private ListView cardList;
    private Handler handler = new Handler();
    private Timer timer = new Timer();
    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    // Launch the asynchronous process to grab the web API
                    new ReadWebServer(getApplicationContext()).execute("");
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_list);

        // Ensure that we have Internet permissions
        int internetPermissionGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
        if(internetPermissionGranted != PackageManager.PERMISSION_GRANTED) {
            final String[] permission = new String[] {Manifest.permission.INTERNET};
            ActivityCompat.requestPermissions(this, permission, RC_HANDLE_INTERNET_PERMISSION);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        cardArray = new ArrayList<>();
        cardList = findViewById(R.id.cardList);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, cardArray);
        cardList.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toAddCardActivity = new Intent(getApplicationContext(), AddCardActivity.class);
                startActivity(toAddCardActivity);
                finish();
            }
        });

        cardList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final int posID = (int) id;

                // If "lastAmount > 0" the last API call is a valid request (that the user must
                // respond to.
                Log.d("card click", "lastAmount= " + ReadWebServer.getLastAmount());
                if (ReadWebServer.getLastAmount() != 0) {
                    // [TODO] Send the card information back to the web API. Reference the
                    // WriteWebServer constructor to know what information must be passed.
                    // Get the card number from the cardArray based on the position in the array.
                    String cardNumber = cardArray.get(posID);
                    Log.d("card click", "cardNumber= " + cardNumber);
                    WriteWebServer writeWebServer = new WriteWebServer(getApplicationContext(), cardNumber);
                    writeWebServer.execute("");

                    // Reset the stored information from the last API call
                    ReadWebServer.resetLastAmount();
                }
            }
        });
        cardList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Object item = cardList.getItemAtPosition(position);
                showDeleteConfirmationDialog(item, position);
                return true;
            }
        });
        // Start the timer to poll the webserver every 5000 ms
        timer.schedule(task, 0, 5000);
    }
    private void showDeleteConfirmationDialog(final Object item, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this card?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences preferences = getSharedPreferences("MyPreferences", MODE_PRIVATE);
                String cardJson = preferences.getString("cardJson", null);

                if (cardJson != null) { //If there is a cardJson string, remove the card from the ArrayList
                    Gson gson = new Gson();
                    //Convert the cardJson string to an ArrayList of Cards
                    Type type = new TypeToken<ArrayList<Card>>() {
                    }.getType();
                    ArrayList<Card> tempCardArray = gson.fromJson(cardJson, type);
                    tempCardArray.remove(position); //Remove the card from the ArrayList
                    String updatedCardJson = gson.toJson(tempCardArray); //Convert the ArrayList back to a string

                    //Save the updated cardJson string to the shared preferences
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("cardJson", updatedCardJson);
                    editor.apply();

                    //Update the cardArray
                    cardArray.remove(position);

                }
                //Update the ListView to reflect the current state
                adapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {});
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    @Override
    protected void onResume() {
        super.onResume();

        // [TODO] This is a placeholder. Modify the card information in the cardArray ArrayList
        // accordingly.
        cardArray.clear();
        SharedPreferences preferences = getSharedPreferences("MyPreferences", MODE_PRIVATE);
        String cardJson = preferences.getString("cardJson", null);
        if (cardJson != null) {
            Gson gson = new Gson();
            Log.d("onresume", "cardJson= " + cardJson);
            Card[] cards = gson.fromJson(cardJson, Card[].class);
            for (Card card : cards) {
                cardArray.add(card.getCardNumber());
            }
        }
        // This is how you tell the adapter to update the ListView to reflect the current state
        // of your ArrayList (which holds all of the card information).
        adapter.notifyDataSetChanged();
    }
}
