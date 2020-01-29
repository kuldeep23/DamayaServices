package com.example.optimas.firebaseconsole;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.optimas.firebaseconsole.Common.Common;
import com.example.optimas.firebaseconsole.Model.DataMessage;
import com.example.optimas.firebaseconsole.Model.MyResponse;
import com.example.optimas.firebaseconsole.Model.SendPackageRequest;
import com.example.optimas.firebaseconsole.Model.Token;
import com.example.optimas.firebaseconsole.Remote.APIService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SendPackage extends AppCompatActivity {

    DatabaseReference mDatabase;
    APIService mService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_package);

        mDatabase= FirebaseDatabase.getInstance().getReference().child("SendPackageRequest");
        mService = Common.getFCMService();
        Button confirm;


        confirm=(Button)findViewById(R.id.confirmOrder);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText pickup=(EditText)findViewById(R.id.pick_address);
                EditText drop=(EditText)findViewById(R.id.drop_address);
                EditText content=(EditText)findViewById(R.id.drop_content);
                EditText contact_name=(EditText)findViewById(R.id.contact_name);
                EditText content_number=(EditText)findViewById(R.id.contact_number);

                String pickup_String= pickup.getText().toString();
                String drop_String=drop.getText().toString();
                String content_String= content.getText().toString();
                String name=contact_name.getText().toString();
                String number= content_number.getText().toString();


                SendPackageRequest request = new SendPackageRequest(
                        Common.currentUser.getPhone(),
                        Common.currentUser.getName(),
                        pickup_String,
                        drop_String,
                        content_String,
                        name,
                        number,
                        "Pending"
                );

               String order_number=String.valueOf(System.currentTimeMillis());
                mDatabase.child(order_number).setValue(request);

                sendNotification(order_number);
            }
        });


    }
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
                    dataSend.put("title","Damaya Package Service");
                    dataSend.put("message","You have new Package" + order_number);
                    DataMessage dataMessage =new DataMessage(serverToken.getToken(),dataSend);

                    mService.sendNotification(dataMessage)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        if (response.body().success == 1) {

                                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(SendPackage.this);
                                            alertDialog.setTitle("Thank you.Order Placed!!!!");
                                            alertDialog.setMessage("Our partner will call you in a moment.");
                                            alertDialog.setPositiveButton("Call us", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                    Intent home = new Intent(SendPackage.this, MainMenu.class);
                                                    startActivity(home);
                                                    dialog.dismiss();
                                                }
                                            });
                                            alertDialog.setNegativeButton("Home", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                    Intent home = new Intent(SendPackage.this, MainMenu.class);
                                                    startActivity(home);
                                                    dialog.dismiss();
                                                }
                                            });
                                            alertDialog.show();

                                            //Toast.makeText(Cart.this, "Thank you, Order Place", Toast.LENGTH_SHORT).show();
                                            //finish();
                                        } else {
                                            Toast.makeText(SendPackage.this, "Failed !!!", Toast.LENGTH_SHORT).show();

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
}
