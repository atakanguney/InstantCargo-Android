package getirhacktathon.getirandroid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void driveCargo(View view) {
        Intent driveCargoIntent = new Intent(this, DriveCargo.class);
        startActivity(driveCargoIntent);
    }

    public void requestCargo(View view) {
        Intent requestCargoIntent = new Intent(this, RequestCargo.class);
        startActivity(requestCargoIntent);
    }

    public void searchDrivers(View view) {
        Intent searchDrivers = new Intent(this, SearchDrivers.class);
        startActivity(searchDrivers);
    }
}
