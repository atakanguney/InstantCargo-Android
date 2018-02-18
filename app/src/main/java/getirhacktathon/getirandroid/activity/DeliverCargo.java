package getirhacktathon.getirandroid.activity;

import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import getirhacktathon.getirandroid.R;
import getirhacktathon.getirandroid.util.Constants;
import getirhacktathon.getirandroid.util.Utils;

public class DeliverCargo extends AppCompatActivity {

    private String request_id;
    private String request_source_id;
    private LatLng request_source_loc_inf;

    private String request_dest_id;
    private LatLng request_dest_loc_inf;

    private EditText mEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deliver_cargo);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == Constants.GET_REQUEST) {
                request_id = data.getStringExtra(Constants.request);
                request_source_loc_inf = data.getParcelableExtra(Constants.source_lat_long);
                request_dest_loc_inf = data.getParcelableExtra(Constants.destination_lat_long);


                TextView textView = findViewById(R.id.cargo_place);
                textView.setText(request_id);
            }
        }
    }

    public void searchCargo(View view) {
        mEditText = findViewById(R.id.range_input);
        String range_raw = mEditText.getText().toString();

        int range;

        try {
            range = Integer.parseInt(range_raw);
        } catch (Exception e) {
            Utils.showToast(this, "Range must be integer and in meters!!");
            return;
        }

        Intent searchCargoIntent = new Intent(this, GoogleMapsActivity.class);
        searchCargoIntent.putExtra("GET_REQUEST", true);
        searchCargoIntent.putExtra("RANGE", range);
        startActivityForResult(searchCargoIntent, Constants.GET_REQUEST);
    }

    public void applyCargo(View view) {
        if (request_dest_loc_inf == null) {
            Utils.showToast(this, "Cargo must be selected before continue!!");
            return;
        } else {
            Intent in = new Intent(this, MapsActivity.class);
            in.putExtra("SOURCE", request_source_loc_inf);
            in.putExtra("DESTINATION", request_dest_loc_inf);
            startActivity(in);
        }
    }
}
