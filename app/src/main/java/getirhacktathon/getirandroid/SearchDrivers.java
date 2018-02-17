package getirhacktathon.getirandroid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SearchDrivers extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_drivers);

        Intent showMap = new Intent(this, GoogleMapsActivity.class);
        startActivity(showMap);
    }
}
