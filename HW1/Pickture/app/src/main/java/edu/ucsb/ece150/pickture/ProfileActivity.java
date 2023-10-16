package edu.ucsb.ece150.pickture;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

    private static final String PREFERENCES_KEY = "MyPreferences";
    private static final String IMAGE_RESOURCE_KEY = "imageResource";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ImageView imageView = this.findViewById(R.id.exampleImageView);

        imageView.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, GalleryActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences preferences = getSharedPreferences(PREFERENCES_KEY, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        ImageView imageView = this.findViewById(R.id.exampleImageView);
        String currentImageTag = (String) imageView.getTag();
        int currentImageResource = Integer.parseInt(currentImageTag);
        editor.putInt(IMAGE_RESOURCE_KEY, currentImageResource);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ImageView imageView = this.findViewById(R.id.exampleImageView);
        int imageResource = getIntent().getIntExtra("imageResource", -1);
        if (imageResource == -1) {
            SharedPreferences preferences = getSharedPreferences(PREFERENCES_KEY, MODE_PRIVATE);
            imageResource = preferences.getInt(IMAGE_RESOURCE_KEY, R.drawable.pic0);
        }

        imageView.setTag(String.valueOf(imageResource));
        getIntent().removeExtra("imageResource");
        imageView.setImageResource(imageResource);
    }
}
