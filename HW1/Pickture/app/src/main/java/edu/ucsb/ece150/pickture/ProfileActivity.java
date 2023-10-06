package edu.ucsb.ece150.pickture;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

/*
 * This is the main activity of Pickture. It will should display the user's profile picture
 * and the user's first/last name. An example ImageView and example picture is given.
 *
 * Remember to read through all available documentation (there are so many Android development
 * guides that can be found) and read through your error logs.
 */
public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final ImageView exampleImage = (ImageView) this.findViewById(R.id.exampleImageView);
        exampleImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // [TODO] Implement application behavior when the user clicks the profile picture
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        // [TODO] Hint: You will need this for implementing graceful app shutdown
    }

    @Override
    protected void onResume() {
        super.onResume();

        // [TODO] Hint: You will need this for implementing graceful app shutdown
    }

    /*
     * You may or may not need this function depending on how you decide to pass messages
     * between your activities.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // [TODO] "I bring news from the nether!"
    }
}
