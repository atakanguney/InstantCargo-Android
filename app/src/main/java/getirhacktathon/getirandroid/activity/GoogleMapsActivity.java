package getirhacktathon.getirandroid.activity;


import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import getirhacktathon.getirandroid.R;

import getirhacktathon.getirandroid.model.Request;
import getirhacktathon.getirandroid.remote.GoogleMapsLocationAsyncTask;
import getirhacktathon.getirandroid.rest.ApiClient;
import getirhacktathon.getirandroid.rest.ApiInterface;
import getirhacktathon.getirandroid.util.Constants;
import getirhacktathon.getirandroid.util.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class GoogleMapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMapsLocationAsyncTask.ResultCallback {

    private static final int MAX_SEARCH_RESULTS = 5;
    private static final float DEFAULT_SEARCH_ZOOM = 8f;
    private static final String TAG = GoogleMapsActivity.class.getSimpleName();

    private GoogleMap mGoogleMap = null;
    private CameraPosition mCameraPosition;

    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;

    private FusedLocationProviderClient mFusedLocationProviderClient;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    // Used for selecting the current place.
    private static final int M_MAX_ENTRIES = 5;
    private String[] mLikelyPlaceNames;
    private String[] mLikelyPlaceAddresses;
    private String[] mLikelyPlaceAttributions;
    private LatLng[] mLikelyPlaceLatLngs;

    private SearchView mSearchView;
    private Geocoder mGeoCoder;

    public Intent getResultData() {
        return resultData;
    }

    public void setResultData(Intent resultData) {
        this.resultData = resultData;
    }

    private Intent resultData;
    private boolean getRequest;
    private HashMap<String, Request> requests;
    private int range;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_maps);

        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this, null);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        Intent intent;
        try {
            intent = getIntent();

            if (intent.hasExtra("GET_REQUEST")) {
                getRequest = intent.getBooleanExtra("GET_REQUEST", false);
                requests = new HashMap<String, Request>();
                range = intent.getIntExtra("RANGE", -1);
            }

        } catch (Exception e) {
            // Do nothing
        }
        mGeoCoder = new Geocoder(this);
        resultData = new Intent();

        Toolbar toolbar = findViewById(R.id.google_maps_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
    }

    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mGoogleMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mGoogleMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.action_google_maps, menu);

        // Searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView = (SearchView) menu.findItem(R.id.search).getActionView();
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        setupSearchView();

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Handles a click on the menu option to get a place.
     *
     * @param item The menu item to handle.
     * @return Boolean.
     */
    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.option_get_place) {
            showCurrentPlace();
        }
        return true;
    }*/


    /**
     * If search bar is open, close it instead of going back.
     */
    @Override
    public void onBackPressed() {
        if (!mSearchView.isIconified()) {
            mSearchView.setQuery("", false);
            mSearchView.setIconified(true);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Set up the functionality of mSearchView.
     */
    private void setupSearchView() {
        // when a query is submitted, clear and load items from mSearchItemsFragment.
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                // close the keyboard
                View v = getCurrentFocus();
                if (v != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }

                // search the location
                new GoogleMapsLocationAsyncTask(mGeoCoder, MAX_SEARCH_RESULTS, GoogleMapsActivity.this).execute(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // do nothing
                return true;
            }
        });
    }

    /**
     * Callback method that will be called by GoogleMapsLocationAsyncTask when it
     * completes its task.
     *
     * @param resultList List of results for the given query.
     */
    @Override
    public void onDataRetrieved(List<Address> resultList) {
        if (resultList == null) {
            //System.out.println("Connection Failure");
            Utils.showToast(this, getString(R.string.connection_failure));
        } else if (resultList.isEmpty()) {
            //System.out.println("Unable to get location");
            Utils.showToast(this, getString(R.string.unable_get_location));
        } else {
            // show all the markers on the map
            for (Address address : resultList) {
                addMarker(address);
            }

            // change the camera to the first result
            Address firstResult = resultList.get(0);
            LatLng cameraCenter = new LatLng(firstResult.getLatitude(), firstResult.getLongitude());
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(cameraCenter, DEFAULT_SEARCH_ZOOM));
        }
    }

    /**
     * Add a new marker on the given location to mGoogleMap.
     *
     * @param address Address object containing information about a location.
     * @return Marker object representing the location on the map.
     */
    public Marker addMarker(Address address) {
        MarkerOptions options = new MarkerOptions();
        double lat = address.getLatitude();
        double lon = address.getLongitude();

        options.position(new LatLng(lat, lon));

        if (address.getFeatureName() == null) {
            options.title(getString(R.string.lat_long_location, lat, lon));
        } else {
            options.title(address.getFeatureName());
        }

        return mGoogleMap.addMarker(options);
    }

    /**
     * Callback method that will be called when SupportMapFragment is ready and can be
     * used.
     *
     * @param googleMap GoogleMap object that handles settings objects on GoogleMap fragment.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        // Use a custom info window adapter to handle multiple lines of text in the
        // info window contents.
        mGoogleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            // Return null here, so that getInfoContents() is called next.
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Inflate the layouts for the info window, title and snippet.
                View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents,
                        (FrameLayout) findViewById(R.id.map), false);

                TextView title = ((TextView) infoWindow.findViewById(R.id.title));
                title.setText(marker.getTitle());

                TextView snippet = ((TextView) infoWindow.findViewById(R.id.snippet));
                snippet.setText(marker.getSnippet());

                return infoWindow;
            }
        });

        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        if (getRequest) {
            findNearbyRequests();
        }

        // show information about location
        mGoogleMap.setOnMarkerClickListener((Marker marker) -> {
            showMarkerInformation(marker);
            return true;
        });

        // add a marker when map is clicked and show information
        if (!getRequest) {
            mGoogleMap.setOnMapClickListener((LatLng latLng) -> {
                Address address = new Address(Locale.getDefault());
                address.setLatitude(latLng.latitude);
                address.setLongitude(latLng.longitude);

                Marker newMarker = addMarker(address);
                showMarkerInformation(newMarker);
            });
        }
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));

                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mGoogleMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void findNearbyRequests() {

        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();

                            CircleOptions circleOptions = new CircleOptions();
                            circleOptions.center(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()));
                            circleOptions.radius(range);
                            circleOptions.fillColor(Color.parseColor("#500084d3"));
                            circleOptions.strokeColor(Color.BLUE);
                            circleOptions.strokeWidth(2);
                            mGoogleMap.addCircle(circleOptions);

                            getirhacktathon.getirandroid.model.Location location = new getirhacktathon.getirandroid.model.Location();
                            location.setLatitude(Utils.round(mLastKnownLocation.getLatitude(), 3));
                            location.setLongitude(Utils.round(mLastKnownLocation.getLongitude(), 3));
                            location.setDistance(range);

                            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
                            Call<List<Request>> call = apiService.getRequest(location);

                            Log.d("abc", Utils.bodyToString(call.request()));

                            call.enqueue(new Callback<List<Request>>() {
                                @Override
                                public void onResponse(Call<List<Request>> call, Response<List<Request>> response) {

                                    List<Request> requestsAll = response.body();

                                    if (response.code() == 200 || response.code() == 201) {
                                        Utils.showToast(getBaseContext(), "Request Added Successfully");
                                    } else {
                                        try {
                                            Utils.showToast(getBaseContext(), response.errorBody().string());
                                        } catch (Exception e) {
                                            Log.d("Exception", e.getMessage());
                                        }

                                    }
                                    if (requestsAll == null) return;

                                    for (Request req : requestsAll) {
                                        Log.d("abc", req.getDestination().getCoordinates().get(0) + ", " + req.getDestination().getCoordinates().get(1));
                                        requests.put(req.getId(), req);
                                        mGoogleMap.addMarker(new MarkerOptions()
                                                .title(req.getId())
                                                .position(new LatLng(req.getDestination().getCoordinates().get(1), req.getDestination().getCoordinates().get(0)))
                                                .snippet(getString(R.string.default_info_snippet)));
                                    }
                                }

                                @Override
                                public void onFailure(Call<List<Request>> call, Throwable t) {
                                    // Log error here since request failed
                                    Log.e(TAG, t.toString());
                                }
                            });

                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mGoogleMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    /**
     * Prompts the user to select the current place from a list of likely places, and shows the
     * current place on the map - provided the user has granted location permission.
     */
    private void showCurrentPlace() {
        if (mGoogleMap == null) {
            return;
        }

        if (mLocationPermissionGranted) {
            // Get the likely places - that is, the businesses and other points of interest that
            // are the best match for the device's current location.
            @SuppressWarnings("MissingPermission") final Task<PlaceLikelihoodBufferResponse> placeResult =
                    mPlaceDetectionClient.getCurrentPlace(null);
            placeResult.addOnCompleteListener
                    (new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
                        @Override
                        public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();

                                // Set the count, handling cases where less than 5 entries are returned.
                                int count;
                                if (likelyPlaces.getCount() < M_MAX_ENTRIES) {
                                    count = likelyPlaces.getCount();
                                } else {
                                    count = M_MAX_ENTRIES;
                                }

                                int i = 0;
                                mLikelyPlaceNames = new String[count];
                                mLikelyPlaceAddresses = new String[count];
                                mLikelyPlaceAttributions = new String[count];
                                mLikelyPlaceLatLngs = new LatLng[count];

                                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                                    // Build a list of likely places to show the user.
                                    mLikelyPlaceNames[i] = (String) placeLikelihood.getPlace().getName();
                                    mLikelyPlaceAddresses[i] = (String) placeLikelihood.getPlace()
                                            .getAddress();
                                    mLikelyPlaceAttributions[i] = (String) placeLikelihood.getPlace()
                                            .getAttributions();
                                    mLikelyPlaceLatLngs[i] = placeLikelihood.getPlace().getLatLng();

                                    i++;
                                    if (i > (count - 1)) {
                                        break;
                                    }
                                }

                                // Release the place likelihood buffer, to avoid memory leaks.
                                likelyPlaces.release();

                                // Show a dialog offering the user the list of likely places, and add a
                                // marker at the selected place.
                                openPlacesDialog();

                            } else {
                                Log.e(TAG, "Exception: %s", task.getException());
                            }
                        }
                    });
        } else {
            // The user has not granted permission.
            Log.i(TAG, "The user did not grant location permission.");

            // Add a default marker, because the user hasn't selected a place.
            mGoogleMap.addMarker(new MarkerOptions()
                    .title(getString(R.string.default_info_title))
                    .position(mDefaultLocation)
                    .snippet(getString(R.string.default_info_snippet)));

            // Prompt the user for permission.
            getLocationPermission();
        }
    }

    /**
     * Displays a form allowing the user to select a place from a list of likely places.
     */
    private void openPlacesDialog() {
        // Ask the user to choose the place where they are now.
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // The "which" argument contains the position of the selected item.
                LatLng markerLatLng = mLikelyPlaceLatLngs[which];
                String markerSnippet = mLikelyPlaceAddresses[which];
                if (mLikelyPlaceAttributions[which] != null) {
                    markerSnippet = markerSnippet + "\n" + mLikelyPlaceAttributions[which];
                }

                // Add a marker for the selected place, with an info window
                // showing information about that place.
                mGoogleMap.addMarker(new MarkerOptions()
                        .title(mLikelyPlaceNames[which])
                        .position(markerLatLng)
                        .snippet(markerSnippet));

                // Position the map's camera at the location of the marker.
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng,
                        DEFAULT_ZOOM));
            }
        };

        // Display the dialog.
        android.support.v7.app.AlertDialog dialog = new android.support.v7.app.AlertDialog.Builder(this)
                .setTitle(R.string.pick_place)
                .setItems(mLikelyPlaceNames, listener)
                .show();
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (mGoogleMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mGoogleMap.setMyLocationEnabled(true);
                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mGoogleMap.setMyLocationEnabled(false);
                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /**
     * Show information related to location
     *
     * @param marker Marker object that contains LatLang and name about its location.
     */
    private void showMarkerInformation(Marker marker) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(marker.getTitle());

        if (getRequest) {
            builder.setPositiveButton("OK", (DialogInterface dialog, int which) -> {

                resultData.putExtra(Constants.request, marker.getTitle());

                LatLng source = new LatLng(requests.get(marker.getTitle()).getSource().getCoordinates().get(1), requests.get(marker.getTitle()).getSource().getCoordinates().get(0));
                LatLng destination = new LatLng(requests.get(marker.getTitle()).getDestination().getCoordinates().get(1), requests.get(marker.getTitle()).getDestination().getCoordinates().get(0));

                resultData.putExtra(Constants.source_lat_long, source);
                resultData.putExtra(Constants.destination_lat_long, destination);

                setResult(RESULT_OK, resultData);
                finish();
            });

            builder.setNegativeButton("Cancel", (DialogInterface dialog, int which) -> {
                return;
            });

        } else {
            builder.setPositiveButton("Destination", (DialogInterface dialog, int which) -> {
                if (!resultData.hasExtra(Constants.source_location_name) && !resultData.hasExtra(Constants.destination_location_name)) {
                    resultData.putExtra(Constants.destination_location_name, marker.getTitle());
                    resultData.putExtra(Constants.destination_lat_long, marker.getPosition());
                } else if (!resultData.hasExtra(Constants.destination_location_name)) {
                    if (resultData.getStringExtra(Constants.source_location_name).contentEquals(marker.getTitle())) {
                        Utils.showToast(this, "Destination cannot be source!!");
                    } else {
                        resultData.putExtra(Constants.destination_location_name, marker.getTitle());
                        resultData.putExtra(Constants.destination_lat_long, marker.getPosition());
                    }
                } else if (!resultData.hasExtra(Constants.source_location_name)) {
                    if (!resultData.getStringExtra(Constants.destination_location_name).contentEquals(marker.getTitle())) {
                        Utils.showToast(this, "Only one destination can be selected!!");
                        marker.remove();
                    }
                } else {
                    if (!resultData.getStringExtra(Constants.source_location_name).contentEquals(marker.getTitle()) && !resultData.getStringExtra(Constants.destination_location_name).contentEquals(marker.getTitle())) {
                        Utils.showToast(this, "Only one destination can be selected!!");
                        marker.remove();
                    } else if (resultData.getStringExtra(Constants.source_location_name).contentEquals(marker.getTitle())) {
                        Utils.showToast(this, "Destination cannot be source!!");
                    }
                }
            });
            builder.setNeutralButton("Source", (DialogInterface dialog, int which) -> {
                if (!resultData.hasExtra(Constants.source_location_name) && !resultData.hasExtra(Constants.destination_location_name)) {
                    resultData.putExtra(Constants.source_location_name, marker.getTitle());
                    resultData.putExtra(Constants.source_lat_long, marker.getPosition());
                } else if (!resultData.hasExtra(Constants.source_location_name)) {
                    if (resultData.getStringExtra(Constants.destination_location_name).contentEquals(marker.getTitle())) {
                        Utils.showToast(this, "Source cannot be destination!!");
                    } else {
                        resultData.putExtra(Constants.source_location_name, marker.getTitle());
                        resultData.putExtra(Constants.source_lat_long, marker.getPosition());
                    }
                } else if (!resultData.hasExtra(Constants.destination_location_name)) {
                    if (!resultData.getStringExtra(Constants.source_location_name).contentEquals(marker.getTitle())) {
                        Utils.showToast(this, "Only one source can be selected!!");
                        marker.remove();
                    }
                } else {
                    if (!resultData.getStringExtra(Constants.source_location_name).contentEquals(marker.getTitle()) && !resultData.getStringExtra(Constants.destination_location_name).contentEquals(marker.getTitle())) {
                        Utils.showToast(this, "Only one source can be selected!!");
                        marker.remove();
                    } else if (resultData.getStringExtra(Constants.destination_location_name).contentEquals(marker.getTitle())) {
                        Utils.showToast(this, "Source cannot be destination!!");
                    }
                }
            });

            builder.setNegativeButton(R.string.cancel, (DialogInterface dialog, int which) -> {
                if (resultData.hasExtra(Constants.destination_location_name)) {
                    if (resultData.getStringExtra(Constants.destination_location_name).contentEquals(marker.getTitle())) {
                        resultData.removeExtra(Constants.destination_location_name);
                        resultData.removeExtra(Constants.destination_lat_long);
                    }
                } else if (resultData.hasExtra(Constants.source_location_name)) {
                    if (resultData.getStringExtra(Constants.source_location_name).contentEquals(marker.getTitle())) {
                        resultData.removeExtra(Constants.source_location_name);
                        resultData.removeExtra(Constants.source_lat_long);
                    }
                }
                marker.remove();
                return;
            });

            builder.setOnCancelListener((DialogInterface dialog) -> {
                if (resultData.hasExtra(Constants.destination_location_name)) {
                    if (resultData.getStringExtra(Constants.destination_location_name).contentEquals(marker.getTitle())) {
                        return;
                    }
                }

                if (resultData.hasExtra(Constants.source_location_name)) {
                    if (resultData.getStringExtra(Constants.source_location_name).contentEquals(marker.getTitle())) {
                        return;
                    }
                }
                marker.remove();
                return;
            });
        }
        builder.create().show();
    }

    /*
     * RequestResponse location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */

    private void getLocationPermission() {

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    public void makeRequest(View view) {
        if (!resultData.hasExtra(Constants.destination_location_name) || !resultData.hasExtra(Constants.source_location_name)) {
            Utils.showToast(this, "Destination and Source Must be selected!!");
        } else {
            setResult(RESULT_OK, resultData);
            finish();
        }

    }
}