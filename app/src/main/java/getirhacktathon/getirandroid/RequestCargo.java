package getirhacktathon.getirandroid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

public class RequestCargo extends AppCompatActivity {

    public static final int LOCATION = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_cargo);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == LOCATION) {
                String location_title = data.getStringExtra("LOCATION_NAME");
                LatLng location_info = data.getParcelableExtra("LAT_LONG");

                TextView titleView = findViewById(R.id.location_title);
                titleView.setText(location_title);

                TextView latLongView = findViewById(R.id.location_lat_long);
                latLongView.setText("Lat: " + location_info.latitude + ", Long: " + location_info.longitude);

            }
        }
    }

    public void searchPlace(View view) {
        Intent showMap = new Intent(this, GoogleMapsActivity.class);
        startActivityForResult(showMap, LOCATION);


    }
}
