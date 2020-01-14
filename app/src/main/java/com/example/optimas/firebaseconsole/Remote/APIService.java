package com.example.optimas.firebaseconsole.Remote;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

import com.example.optimas.firebaseconsole.Model.DataMessage;
import com.example.optimas.firebaseconsole.Model.MyResponse;

public interface APIService {
    @Headers(

            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAA-nmNrFA:APA91bEA8Ad78nHkVOE8UWJIQs1W-4P0W171ougMan4OsaP_pzjCpsXKbD6mqoBMqkdtOMkXZS5wXpcRXDF7eYSrAMpRQ_Rj9NyeXlkQh38XfpA_63GEGjp23ic21lSTq6WkIzCnGSVx"

            }

    )
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body DataMessage body);

}