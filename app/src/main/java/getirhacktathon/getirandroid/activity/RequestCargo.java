package getirhacktathon.getirandroid.activity;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import getirhacktathon.getirandroid.R;
import getirhacktathon.getirandroid.model.Destination;
import getirhacktathon.getirandroid.model.Request;
import getirhacktathon.getirandroid.model.Source;
import getirhacktathon.getirandroid.rest.ApiClient;
import getirhacktathon.getirandroid.rest.ApiInterface;
import getirhacktathon.getirandroid.util.Utils;
import getirhacktathon.getirandroid.util.Constants;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestCargo extends AppCompatActivity {
    private static final String TAG = "abc";

    private Source source;
    private Destination destination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_cargo);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == Constants.SOURCE_DEST_LOC) {
                String source_location_title = data.getStringExtra(Constants.source_location_name);
                LatLng source_location_info = data.getParcelableExtra(Constants.source_lat_long);

                List<Double> source_coordinates = new ArrayList<Double>();
                source_coordinates.add(Utils.round(source_location_info.longitude, 4));
                source_coordinates.add(Utils.round(source_location_info.latitude, 4));

                source = new Source();
                source.setCoordinates(source_coordinates);
                source.setType("Point");

                String destination_location_title = data.getStringExtra(Constants.destination_location_name);
                LatLng destination_location_info = data.getParcelableExtra(Constants.destination_lat_long);

                List<Double> dest_coordinates = new ArrayList<Double>();
                dest_coordinates.add(Utils.round(destination_location_info.longitude, 3));
                dest_coordinates.add(Utils.round(destination_location_info.latitude, 3));

                destination = new Destination();
                destination.setCoordinates(dest_coordinates);
                destination.setType("Point");

                TextView sourceId = findViewById(R.id.source_id);
                sourceId.setText(source_location_title);

                TextView destId = findViewById(R.id.dest_id);
                destId.setText(destination_location_title);

            }
        }
    }

    public void searchPlace(View view) {
        Intent showMap = new Intent(this, GoogleMapsActivity.class);
        startActivityForResult(showMap, Constants.SOURCE_DEST_LOC);


    }

    public void makeRequest(View view) {

        Request request = new Request();
        request.setSource(source);
        request.setDestination(destination);

        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);

        Call<Request> call = apiService.createRequest(request);
        Log.d(TAG, Utils.bodyToString(call.request()));

        call.enqueue(new Callback<Request>() {
            @Override
            public void onResponse(Call<Request> call, Response<Request> response) {
                Log.d(TAG, response.code() + "");
                Log.d(TAG, response.toString());
                Request requests = response.body();
                if (response.code() == 200 || response.code() == 201) {
                    Utils.showToast(getBaseContext(), "Request Added Successfully");
                } else {
                    try {
                        Utils.showToast(getBaseContext(), response.errorBody().string());
                    } catch (Exception e) {
                        Log.d("Exception", e.getMessage());
                    }

                }
            }

            @Override
            public void onFailure(Call<Request> call, Throwable t) {
                // Log error here since request failed
                Log.e(TAG, t.toString());
            }
        });

        finish();
    }
}
