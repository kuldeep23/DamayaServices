package com.example.optimas.firebaseconsole.Common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.format.DateFormat;

import java.text.ParseException;

import com.example.optimas.firebaseconsole.Model.Request;
import com.example.optimas.firebaseconsole.Model.Shipper;
import com.example.optimas.firebaseconsole.Model.User;
import com.example.optimas.firebaseconsole.Remote.APIService;
import com.example.optimas.firebaseconsole.Remote.IGoogleService;
import com.example.optimas.firebaseconsole.Remote.RetrofitClient;
import com.example.optimas.firebaseconsole.Remote.GoogleRetrofitClient;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Optimas on 30-10-2018.
 */

public class  Common {

    public static String topicName = "News";

    public static User currentUser;
    public static Request currentRequest;
    public static String currentKey;
    public static String restaurantSelected="";
    public static final String ORDER_NEED_SHIP_TABLE = "OrdersNeedShip";
    public static final int REQUEST_CODE=1000;
    public  static Shipper currentShipper;
    public static final String SHIPPER_TABLE = "Shippers";
    public  static  String PHONE_TEXT="userPhone";


    public static final String INTENT_FODD_ID  = "FoodId";

    private static final  String BASE_URL = "https://fcm.googleapis.com/";

    private static final  String GOOGLE_API_URL = "https://maps.googleapis.com/";

    public  static APIService getFCMService(){
        return RetrofitClient.getClient(BASE_URL).create(APIService.class);
    }

    public  static IGoogleService getGoogleMapAPI(){
        return GoogleRetrofitClient.getGoogleClient(GOOGLE_API_URL).create(IGoogleService.class);
    }

    public static String convertCodeToStatus(String code) {

        if(code.equals("0"))
            return "Order Placed";
        else if(code.equals("1"))
            return "Order Accepted";
        else if(code.equals("2"))
            return "Order in Shipping";
        else
            return "Shipped";
    }

    public static final String DELETE = "Delete";
    public static final String USER_KEY = "User";
    public static final String PWD_KEY = "Password";


    public static boolean isConnectedToInternet(Context context){

        ConnectivityManager connectivityManager =(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager!=null){

            NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
            if(info!=null){
                for(int i=0;i<info.length;i++){

                    if(info[i].getState()==NetworkInfo.State.CONNECTED)
                        return true;

                }
            }
        }
        return false;

    }

    public  static BigDecimal formatCurrency(String amount, Locale locale) throws ParseException
    {
        NumberFormat format= NumberFormat.getCurrencyInstance(locale);
        if (format instanceof DecimalFormat)
            ((DecimalFormat) format).setParseBigDecimal(true);
            return (BigDecimal)format.parse(amount.replace("[^\\d.,]",""));
    }

    public static String getDate(long time){
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(time);
        StringBuilder date = new StringBuilder(DateFormat.format("dd-MM-yyyy HH:mm"
                ,calendar)
                .toString());

        return  date.toString();

    }

}
