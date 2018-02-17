package getirhacktathon.getirandroid.model;

/**
 * Created by atakan1 on 17.02.2018.
 */

import com.google.gson.annotations.SerializedName;

public class Location {
    @SerializedName("location_name")
    private String locationName;

    @SerializedName("longitude")
    private double longitude;

    @SerializedName("latitude")
    private double lattitude;

    @SerializedName("distance")
    private double distance;

    public Location() {

    }

    public Location(String locationName, double longitude, double lattitude, double distance) {
        this.locationName = locationName;
        this.longitude = longitude;
        this.lattitude = lattitude;
        this.distance = distance;
    }

    public double getDistance() {

        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLattitude() {
        return lattitude;
    }

    public void setLattitude(double lattitude) {
        this.lattitude = lattitude;
    }
}
