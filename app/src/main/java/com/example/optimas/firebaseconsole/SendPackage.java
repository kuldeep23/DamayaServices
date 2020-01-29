package com.example.optimas.firebaseconsole;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.optimas.firebaseconsole.Common.Common;
import com.example.optimas.firebaseconsole.Model.SendPackageRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SendPackage extends AppCompatActivity {

    DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_package);

        mDatabase= FirebaseDatabase.getInstance().getReference().child("SendPackageRequest");
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
                        number
                );

               String order_number=String.valueOf(System.currentTimeMillis());
                mDatabase.child(order_number).setValue(request);

                Toast.makeText(SendPackage.this, pickup_String+drop_String+content_String, Toast.LENGTH_SHORT).show();
            }
        });


    }
}
