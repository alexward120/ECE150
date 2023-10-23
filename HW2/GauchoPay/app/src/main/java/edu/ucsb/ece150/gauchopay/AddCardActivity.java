package edu.ucsb.ece150.gauchopay;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.braintreepayments.cardform.view.CardForm;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.w3c.dom.Text;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class AddCardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_card);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Add Card");
        setSupportActionBar(toolbar);

        // Note that the requirements here are just for creating the fields on the form. For
        // example, if the cvvRequired setting was set to "false", the form would not contain
        // a field for CVV. ("Requirement" DOES NOT mean "Valid".)
        final CardForm cardForm = findViewById(R.id.card_form);
        cardForm.cardRequired(true)
                .expirationRequired(true)
                .cvvRequired(true)
                .postalCodeRequired(true)
                .actionLabel("Add Card")
                .setup(this);

        // [TODO] Implement a method of getting card information and sending it to the main activity.
        // You will want to add a new component onto this activity's layout so you can perform this
        // task as a result of a button click.
        //
        //  Get card information from the CardForm view. Refer to the library website
        // https://github.com/braintree/android-card-form/blob/master/README.md.
        //
        // This information has to be sent back to the CardListActivity (to update the
        // list of cards).
        Button submitButton = findViewById(R.id.submit_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickSubmit(v);
            }
        });
    }
    public void onClickSubmit(View view) {
        CardForm cf = findViewById(R.id.card_form);
        Card card = new Card(cf.getCardNumber(), cf.getExpirationMonth(),
                            cf.getExpirationYear(), cf.getCvv(), cf.getPostalCode());
        if (card.getCardNumber().equals("")) {
            Toast.makeText(this, "Card number invalid", Toast.LENGTH_SHORT).show();
            return;
        }

        //Grab the cardArray from the shared preferences
        SharedPreferences preferences = getSharedPreferences("MyPreferences", MODE_PRIVATE);
        String cardJson = preferences.getString("cardJson", null);

        //Add the new card to the cardArray
        if (cardJson != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<Card>>() {}.getType();
            //Convert the cardJson string to an ArrayList of Cards
            ArrayList<Card> cardArray = gson.fromJson(cardJson, type);
            cardArray.add(card); //Add the new card to the ArrayList
            String updatedCardJson = gson.toJson(cardArray); //Convert the ArrayList back to a string
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("cardJson", updatedCardJson);
            editor.apply();
        } else { //If there is no cardJson string, create one
            ArrayList<Card> cardArray = new ArrayList<>();
            cardArray.add(card);
            Gson gson = new Gson();
            String updatedCardJson = gson.toJson(cardArray);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("cardJson", updatedCardJson);
            editor.apply();
        }
        Intent intent = new Intent(this, CardListActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, CardListActivity.class));
        finish();
    }
}
