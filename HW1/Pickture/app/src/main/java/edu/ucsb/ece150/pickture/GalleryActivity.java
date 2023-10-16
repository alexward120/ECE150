package edu.ucsb.ece150.pickture;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

public class GalleryActivity extends AppCompatActivity {
    int[] images_id = {R.id.img0, R.id.img1, R.id.img2, R.id.img3,
                    R.id.img4, R.id.img5};
    int[] images = {R.drawable.pic0, R.drawable.pic1, R.drawable.pic2,
                    R.drawable.pic3, R.drawable.pic4, R.drawable.pic5};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        for (int i=0; i < images.length; i++) {
            int imageResource = images[i];
            ImageView imageView = (ImageView) findViewById(images_id[i]);
            imageView.setOnClickListener(v -> onClickImage(imageResource));
        }
    }
    public void onClickImage(int imageResource) {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("imageResource", imageResource);
        startActivity(intent);
    }
}
