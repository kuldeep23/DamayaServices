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

public class SendPackage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_package);


        Button confirm;

        confirm=(Button)findViewById(R.id.confirmOrder);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText pickup=(EditText)findViewById(R.id.pick_address);
                EditText drop=(EditText)findViewById(R.id.drop_address);
                EditText content=(EditText)findViewById(R.id.drop_content);

               String pickup_String= pickup.getText().toString();
               String drop_String=drop.getText().toString();
               String content_String= content.getText().toString();

                Toast.makeText(SendPackage.this, pickup_String+drop_String+content_String, Toast.LENGTH_SHORT).show();
            }
        });


    }
}
