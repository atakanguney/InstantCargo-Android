package getirhacktathon.getirandroid.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by atakan1 on 17.02.2018.
 */

public class RequestResponse {
    private class Point {
        @SerializedName("coordinates")
        private double[] coordinates;

        public Point(double[] coordinates) {
            this.coordinates = coordinates;
        }

        public double[] getCoordinates() {

            return coordinates;
        }

        public void setCoordinates(double[] coordinates) {
            this.coordinates = coordinates;
        }

    }

    @SerializedName("source")
    private Point source;

    @SerializedName("destination")
    private Point destination;

    @SerializedName("created_at")
    private Date createdAt;

    @SerializedName("_id")
    private String id;

    @SerializedName("__v")
    private int v;

    public RequestResponse() {

    }

    public RequestResponse(Point source, Point destination, Date createdAt, String id, int v) {
        this.source = source;
        this.destination = destination;
        this.createdAt = createdAt;
        this.id = id;
        this.v = v;
    }

    public Point getSource() {

        return source;
    }

    public void setSource(Point source) {
        this.source = source;
    }

    public Point getDestination() {
        return destination;
    }

    public void setDestination(Point destination) {
        this.destination = destination;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getV() {
        return v;
    }

    public void setV(int v) {
        this.v = v;
    }
}
