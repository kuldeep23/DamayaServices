package com.example.optimas.firebaseconsole;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.optimas.firebaseconsole.Common.Common;
import com.example.optimas.firebaseconsole.Database.Database;
import com.example.optimas.firebaseconsole.Helper.RecyclerItemTouchHelper;
import com.example.optimas.firebaseconsole.Interface.RecyclerItemTouchHelperListener;
import com.example.optimas.firebaseconsole.Model.DataMessage;
import com.example.optimas.firebaseconsole.Model.MyResponse;
import com.example.optimas.firebaseconsole.Model.Order;

import com.example.optimas.firebaseconsole.Model.Request;
import com.example.optimas.firebaseconsole.Model.Token;
import com.example.optimas.firebaseconsole.Model.User;
import com.example.optimas.firebaseconsole.Remote.APIService;
import com.example.optimas.firebaseconsole.Remote.IGoogleService;
import com.example.optimas.firebaseconsole.ViewHolder.CardAdapter;
import com.example.optimas.firebaseconsole.ViewHolder.CardViewHolder;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import info.hoang8f.widget.FButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Cart extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
GoogleApiClient.OnConnectionFailedListener, LocationListener, RecyclerItemTouchHelperListener {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requests;

    public TextView txtTotalPrice;
    FButton btnPlace;

    List<Order> cart=new ArrayList<>();

    CardAdapter adapter;

    APIService mService;

    RelativeLayout rootLayout;

    Place shippingAddress;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private static final int UPDATE_INTERVAL = 5000;
    private static final int FASTEST_INTERVAL = 3000;
    private static final int DISPLACEMENT = 10;

    private  static  final int LOCATION_REQUEST_CODE=9999;

    private  static final  int PLAY_SERVICES_REQUEST = 9997;

    IGoogleService mGoogleMapService;

    String address;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
        .setDefaultFontPath("fonts/restaurant_font.otf")
        .setFontAttrId(R.attr.fontPath)
        .build());*/

        setContentView(R.layout.activity_cart);

        mGoogleMapService = Common.getGoogleMapAPI();

        rootLayout = (RelativeLayout)findViewById(R.id.rootLayout);

        //Runtime permission
        /*if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) !=PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) !=PackageManager.PERMISSION_GRANTED)
        {
          ActivityCompat.requestPermissions(this,new String[]{
                  Manifest.permission.ACCESS_COARSE_LOCATION,
                  Manifest.permission.ACCESS_FINE_LOCATION
          },LOCATION_REQUEST_CODE);
        }else {
            if(checkPlayServices()){
               buildGoogleApiClient();
               createLocationRequest();
            }
        }*/

        mService = Common.getFCMService();

        database=FirebaseDatabase.getInstance();
        requests=database.getReference("Requests");


        recyclerView=(RecyclerView)findViewById(R.id.listCart);
        recyclerView.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //Swipe to delete
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0,ItemTouchHelper.LEFT,this);
        new  ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        txtTotalPrice=(TextView)findViewById(R.id.total);
        btnPlace=(FButton)findViewById(R.id.btnPlaceOrder);

        btnPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(cart.size() >0)
                    showAlertDialog();
                else
                    Toast.makeText(Cart.this,"Your cart is empty !!!",Toast.LENGTH_SHORT).show();
            }
        });

        loadListFood();


    }

    /*private void createLocationRequest() {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private synchronized void buildGoogleApiClient() {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API).build();

            mGoogleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultCode!=ConnectionResult.SUCCESS)
        {
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode,this,PLAY_SERVICES_REQUEST).show();
            else {
                Toast.makeText(this,"This device is not supported",Toast.LENGTH_SHORT).show();
                finish();
            }
            return  false;
        }
        return true;
    }*/

    private void showAlertDialog() {

        AlertDialog.Builder alertDialog= new AlertDialog.Builder(Cart.this);

        LayoutInflater inflater = this.getLayoutInflater();
        View order_address_comment = inflater.inflate(R.layout.order_address_comment,null);

        final MaterialEditText edtAddress12 = (MaterialEditText) order_address_comment.findViewById(R.id.edtAddress1);
        final MaterialEditText edtcomment = (MaterialEditText) order_address_comment.findViewById(R.id.edtComment1);


     /*   final RadioButton  rdiShipToCurrentAddress = (RadioButton) order_address_comment.findViewById(R.id.rdiCustomAddress);
        final RadioButton  rdiShipToAddress = (RadioButton) order_address_comment.findViewById(R.id.rdiShipToAddress);
        final RadioButton  rdiHomeAddress = (RadioButton) order_address_comment.findViewById(R.id.rdiHomeAddress);*/
        final RadioButton  rdiCOD = (RadioButton) order_address_comment.findViewById(R.id.rdiCOD);


       // final RadioButton  rdiBalance = (RadioButton) order_address_comment.findViewById(R.id.rdiEatItBalance);

        /*final PlaceAutocompleteFragment edtAddress = (PlaceAutocompleteFragment)getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        edtAddress.getView().findViewById(R.id.place_autocomplete_search_button).setVisibility(View.GONE);
        ((EditText)edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                .setHint("Enter your address");

        ((EditText)edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                .setTextSize(14);

        edtAddress.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                shippingAddress=place;
            }

            @Override
            public void onError(Status status) {
            Log.e("Error",status.getStatusMessage());
            }
        });*/

        /*rdiShipToCurrentAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
              *//*  if(isChecked){
                    address=edtAddress12.getText().toString();

                }
                else {
                    Toast.makeText(Cart.this, "Please enter the Address", Toast.LENGTH_SHORT).show();

                }*//*
                    // creating the EditText widget programatically
                    final EditText editText = new EditText(Cart.this);

                    // create the AlertDialog as final
                    final AlertDialog dialog = new AlertDialog.Builder(Cart.this)
                            .setMessage("You are ready to type")
                            .setTitle("The Code of a Ninja")
                            .setView(editText)

                            // Set the action buttons
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                        edtAddress12.setText(editText.getText().toString());
                                }
                            })

                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    // removes the AlertDialog in the screen
                                }
                            })
                            .create();

                    // set the focus change listener of the EditText
                    // this part will make the soft keyboard automaticall visible
                    editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                        @Override
                        public void onFocusChange(View v, boolean hasFocus) {
                            if (hasFocus) {
                                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                            }
                        }
                    });

                    dialog.show();

                }


        });

        rdiHomeAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){

                    if(Common.currentUser.getHomeAddress()!=null ||
                            !TextUtils.isEmpty(Common.currentUser.getHomeAddress()))
                    {
                        address=Common.currentUser.getHomeAddress();
                        edtAddress12.setText(address);
                        *//*((EditText)edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                                .setText(address);*//*
                    }
                   else {
                        Toast.makeText(Cart.this, "Please update your Home Address", Toast.LENGTH_SHORT).show();

                    }
                }
            }
        });

        rdiShipToAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {

                    mGoogleMapService.getAddressName(String.format("https://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f&sensor=false&key=AIzaSyC4SevyZzhjY8U69FxBBlzctAfiMW9fpec",
                            mLastLocation.getLatitude(),
                            mLastLocation.getLongitude()))
                            .enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {

                                    try {
                                        JSONObject jsonObject = new JSONObject(response.body().toString());
                                        JSONArray resultArray = jsonObject.getJSONArray("results");

                                        JSONObject firstObject = resultArray.getJSONObject(0);

                                        address = firstObject.getString("formatted_address");
                                        edtAddress12.setText(address);

                                       *//* ((EditText)edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                                                .setText(address);*//*

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {
                                Toast.makeText(Cart.this,""+t.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });*/
        alertDialog.setView(order_address_comment);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_white_24dp);

        alertDialog.setPositiveButton("Place Order", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {



                /*if(!rdiShipToCurrentAddress.isChecked()&&!rdiShipToAddress.isChecked()&& !rdiHomeAddress.isChecked()){

                    if(shippingAddress!=null)
                    {
                        address=shippingAddress.getAddress().toString();
                    }
                    else
                    {
                        Toast.makeText(Cart.this,"Please enter address or select option from address",Toast.LENGTH_SHORT).show();

                       *//* //Remove Fragment
                        getFragmentManager().beginTransaction()
                                .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                                .commit();*//*

                        return;
                    }
                }*/
                if(TextUtils.isEmpty(edtAddress12.getText().toString())&&TextUtils.isEmpty(edtcomment.getText().toString())){

                    Toast.makeText(Cart.this,"Address or Comments is empty",Toast.LENGTH_SHORT).show();

                   /* //Remove Fragment
                    getFragmentManager().beginTransaction()
                            .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                            .commit();*/

                    return;

                }

                //Check Payment
                if(!rdiCOD.isChecked()){

                    Toast.makeText(Cart.this,"Please select payment option",Toast.LENGTH_SHORT).show();

                  /*  //Remove Fragment
                    getFragmentManager().beginTransaction()
                            .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                            .commit();*/

                    return;
                }
                else if(rdiCOD.isChecked()){
                    Request request =new Request(
                            Common.currentUser.getPhone(),
                            Common.currentUser.getName(),
                            edtAddress12.getText().toString(),
                            txtTotalPrice.getText().toString(),
                            "0",
                            edtcomment.getText().toString(),
                            "COD",
                            "Unpaid",
                            String.format("%s,%s",0,0),
                            cart,
                             Common.restaurantSelected
                    );
                    //Submit to Firebase

                    String order_number=String.valueOf(System.currentTimeMillis());
                    requests.child(order_number).setValue(request);

                    //Delete Database
                    new Database(getBaseContext()).clearCart(Common.currentUser.getPhone());
                    sendNotification(order_number);

                  //  Toast.makeText(Cart.this,"Thank you, Order Place",Toast.LENGTH_SHORT).show();
                    //finish();


                }
                /*else if(rdiBalance.isChecked())

                {
                    double amount=0;

                    try {
                        Locale locale = new Locale("en","IN");
                        amount=Common.formatCurrency(txtTotalPrice.getText().toString(),locale).doubleValue();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if(Double.parseDouble(Common.currentUser.getBalance().toString())>= amount)
                    {

                        Request request =new Request(
                                Common.currentUser.getPhone(),
                                Common.currentUser.getName(),
                                address,
                                txtTotalPrice.getText().toString(),
                                "0",
                                edtcomment.getText().toString(),
                                "Hazir Balance",
                                "Paid",
                                String.format("%s,%s",mLastLocation.getLatitude(),mLastLocation.getLongitude()),
                                cart
                        );

                        //Submit to Firebase

                        final String order_number=String.valueOf(System.currentTimeMillis());
                        requests.child(order_number).setValue(request);

                        //Delete Database
                        new Database(getBaseContext()).clearCart(Common.currentUser.getPhone());

                        //Update Balance
                        double balance=Double.parseDouble(Common.currentUser.getBalance().toString())- amount;
                        Map<String,Object> update_balance = new HashMap<>();
                        update_balance.put("balance",balance);

                        FirebaseDatabase.getInstance()
                                .getReference("User")
                                .child(Common.currentUser.getPhone())
                                .updateChildren(update_balance)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){

                                            FirebaseDatabase.getInstance()
                                                    .getReference("User")
                                                    .child(Common.currentUser.getPhone())
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            Common.currentUser=dataSnapshot.getValue(User.class);
                                                            sendNotification(order_number);
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });
                                        }
                                    }
                                });
                    }
                    else
                    {
                        Toast.makeText(Cart.this,"Your balance not enough, please choose other payment",Toast.LENGTH_SHORT).show();
                    }
                }*/
              /*  //Remove Fragment
                getFragmentManager().beginTransaction()
                        .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                        .commit();*/

            }
        });

         alertDialog.setNegativeButton("Back", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 dialog.dismiss();

               /*  //Remove Fragment
                 getFragmentManager().beginTransaction()
                         .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                         .commit();*/
             }
         });

        alertDialog.show();
    }

    /*@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch ((requestCode))
        {
            case LOCATION_REQUEST_CODE:
                {
                    if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED)
                    {
                        if(checkPlayServices()){
                            buildGoogleApiClient();
                            createLocationRequest();
                        }
                    }
                }
                break;
        }
    }*/

    private void sendNotification(final String order_number) {

        DatabaseReference tokens= FirebaseDatabase.getInstance().getReference("Restaurants").child(Common.restaurantSelected).child("Tokens");
        Query data = tokens.orderByChild("isServerToken").equalTo(true);
        data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot:dataSnapshot.getChildren()){
                    Token serverToken = postSnapshot.getValue(Token.class);

                    //Create raw payload

                   /* Notification notification  = new Notification("HAZIR", "You have new order" + order_number);
                    Sender content = new Sender(serverToken.getToken(),notification);*/
                    Map<String,String> dataSend = new HashMap<>();
                    dataSend.put("title","HAZIR");
                    dataSend.put("message","You have new order" + order_number);
                    DataMessage dataMessage =new DataMessage(serverToken.getToken(),dataSend);

                    mService.sendNotification(dataMessage)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        if (response.body().success == 1) {

                                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(Cart.this);
                                            alertDialog.setTitle("Thank you.Order Placed!!!!");
                                            alertDialog.setMessage("Our partner will call you in a moment.\nCheck your order status on My orders");
                                            alertDialog.setPositiveButton("My Order", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                    Intent my_order = new Intent(Cart.this, OrderStatus.class);
                                                    startActivity(my_order);
                                                }
                                            });
                                            alertDialog.setNegativeButton("Home", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                    Intent home = new Intent(Cart.this, Home.class);
                                                    startActivity(home);
                                                    dialog.dismiss();
                                                }
                                            });
                                            alertDialog.show();

                                            //Toast.makeText(Cart.this, "Thank you, Order Place", Toast.LENGTH_SHORT).show();
                                            //finish();
                                        } else {
                                            Toast.makeText(Cart.this, "Failed !!!", Toast.LENGTH_SHORT).show();

                                        }

                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable throwable) {

                                    Log.e("ERROR", throwable.getMessage());

                                }
                            });


                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void loadListFood() {

        cart=new Database(this).getCarts(Common.currentUser.getPhone());
        adapter= new CardAdapter(cart, this);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

        int total=0;
        for (Order order:cart)
            total+=(Integer.parseInt(order.getPrice()))*(Integer.parseInt(order.getQuantity()));
        Locale locale = new Locale("en","IN");
        NumberFormat fnt=NumberFormat.getCurrencyInstance(locale);

        txtTotalPrice.setText(fnt.format(total));

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if(item.getTitle().equals(Common.DELETE))
            deleteCart(item.getOrder());
        return true;
    }

    private void deleteCart(int position) {
        //Remove Item
        cart.remove(position);

        new Database(this).clearCart(Common.currentUser.getPhone());

        for(Order item:cart)
            new Database(this).addToCart(item);

        loadListFood();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) !=PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) !=PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
    }
    private void displayLocation() {
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) !=PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) !=PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mLastLocation!=null){
            Log.d("LOCATION","Your Location :"+mLastLocation.getLatitude()+","+mLastLocation.getLongitude());
        }
        else {
            Log.d("LOCATION","Could not get your location");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation =location;
        displayLocation();

    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if(viewHolder instanceof CardViewHolder)
        {
            String name=((CardAdapter)recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition()).getProductName();

            final Order deleteItem=((CardAdapter)recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition());
            final int deleteIndex = viewHolder.getAdapterPosition();

            adapter.removeItem(deleteIndex);
            new Database(getBaseContext()).removeFromCart(deleteItem.getProductId(),Common.currentUser.getPhone());

            //Update txttotal
            int total=0;
            List<Order> orders = new Database(getBaseContext()).getCarts(Common.currentUser.getPhone());
            for (Order item:orders)
                total+=(Integer.parseInt(item.getPrice()))*(Integer.parseInt(item.getQuantity()));
            Locale locale = new Locale("en","IN");
            NumberFormat fnt=NumberFormat.getCurrencyInstance(locale);

           txtTotalPrice.setText(fnt.format(total));

           //Make Snackbar

            Snackbar snackbar = Snackbar.make(rootLayout,name + " removed from cart!",Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.restoreItem(deleteItem,deleteIndex);
                    new Database(getBaseContext()).addToCart(deleteItem);

                    //Update txttotal
                    int total=0;
                    List<Order> orders = new Database(getBaseContext()).getCarts(Common.currentUser.getPhone());
                    for (Order item:orders)
                        total+=(Integer.parseInt(item.getPrice()))*(Integer.parseInt(item.getQuantity()));
                    Locale locale = new Locale("en","IN");
                    NumberFormat fnt=NumberFormat.getCurrencyInstance(locale);
                    txtTotalPrice.setText(fnt.format(total));

                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();

        }
    }
}
