package getirhacktathon.getirandroid.rest;

import java.util.List;

import getirhacktathon.getirandroid.model.Location;
import getirhacktathon.getirandroid.model.Request;
import getirhacktathon.getirandroid.model.RequestResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by atakan1 on 17.02.2018.
 */

public interface ApiInterface {

    @POST("/request/getAll")
    Call<List<Request>> getRequest(@Body Location location);

    @POST("/request/create")
    Call<Request> createRequest(@Body Request request);

}
