package edu.ucsb.ece150.pickture;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.Image;
import android.os.Bundle;
//import android.view.View;
import android.view.View;
import android.widget.ImageView;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

/*
 * This is the main activity of Pickture. It will should display the user's profile picture
 * and the user's first/last name. An example ImageView and example picture is given.
 *
 * Remember to read through all available documentation (there are so many Android development
 * guides that can be found) and read through your error logs.
 */
public class ProfileActivity extends AppCompatActivity {

    private static final String PREFERENCES_KEY = "MyPreferences";
    private static final String IMAGE_RESOURCE_KEY = "imageResource";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        int imageResource = getIntent().getIntExtra("imageResource", -1);
        //Log.d("Image Resource", "Got " + imageResource);
        if (imageResource == -1) {
            SharedPreferences preferences = getSharedPreferences(PREFERENCES_KEY, MODE_PRIVATE);
            imageResource = preferences.getInt(IMAGE_RESOURCE_KEY, R.drawable.pic0);
        }

        ImageView imageView = (ImageView) this.findViewById(R.id.exampleImageView);
        imageView.setImageResource(imageResource);
        int finalImageResource = imageResource;
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, GalleryActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences preferences = getSharedPreferences(PREFERENCES_KEY, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        int currentImageResource = getIntent().getIntExtra("imageResource", -1);
        if (currentImageResource == -1) {
            currentImageResource = R.drawable.pic0;
        }
        Log.d("On pause", "inserting val as currentIMgResource" + currentImageResource);
        editor.putInt(IMAGE_RESOURCE_KEY, currentImageResource);
        editor.apply();
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
