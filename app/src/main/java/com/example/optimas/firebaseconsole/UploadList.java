package com.example.optimas.firebaseconsole;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.optimas.firebaseconsole.Common.Common;
import com.example.optimas.firebaseconsole.Model.DataMessage;
import com.example.optimas.firebaseconsole.Model.MyResponse;
import com.example.optimas.firebaseconsole.Model.Token;
import com.example.optimas.firebaseconsole.Model.UploadImageList;
import com.example.optimas.firebaseconsole.Remote.APIService;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadList extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private ImageButton mSelectImage;
    private static final int GALLERY_REQUEST =1;
    private EditText mPostComments;
    private Button mSubmitButton;
    private Uri mImageUri=null;

    private StorageReference mStorage;

    private ProgressDialog mProgress;
    private DatabaseReference mDatabase;
    String item;

    APIService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_list);

        mService = Common.getFCMService();

        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("UploadImageList");


        mSelectImage = (ImageButton)findViewById(R.id.imageSelect);
        mPostComments = (EditText) findViewById(R.id.comments);
        mSubmitButton = (Button)findViewById(R.id.submit);

        // Spinner element
        Spinner spinner = (Spinner) findViewById(R.id.spinner);

        // Spinner click listener
        spinner.setOnItemSelectedListener(this);

        // Spinner Drop down elements
        List<String> categories = new ArrayList<String>();
        categories.add("Grocery");
        categories.add("Fruits");
        categories.add("Vegetables");


        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);

        mProgress = new ProgressDialog(this);


        mSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent galleryIntent= new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GALLERY_REQUEST);
            }
        });

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              
                startPosting();
            }
        });
    }

    private void startPosting() {

        mProgress.setMessage("Uploading List....");
        mProgress.show();

        final String comments_val = mPostComments.getText().toString().trim();

        if(!TextUtils.isEmpty(comments_val)&& mImageUri!=null){

            StorageReference filepath = mStorage.child("Upload_List").child(mImageUri.getLastPathSegment());
            filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Uri downloadUri = taskSnapshot.getDownloadUrl();
                    DatabaseReference newList= mDatabase.push();

                    UploadImageList list=new UploadImageList(
                            Common.currentUser.getPhone(),
                            Common.currentUser.getName(),
                            comments_val,
                            item,
                            downloadUri.toString(),
                            "Pending"
                    );
                    String order_number=String.valueOf(System.currentTimeMillis());
                    mDatabase.child(order_number).setValue(list);
                   /* newList.child("comments").setValue(comments_val);
                    newList.child("imageURL").setValue(downloadUri.toString());
                    newList.child("category").setValue(item);*/
                    mProgress.dismiss();

                    sendNotification(order_number);
                    Toast.makeText(UploadList.this, "List uploaded...", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK){

            mImageUri = data.getData();

            mSelectImage.setImageURI(mImageUri);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
// On selecting a spinner item
        item = adapterView.getItemAtPosition(i).toString();

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

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
                    dataSend.put("title","Damaya Upload List Service");
                    dataSend.put("message","You have new List" + order_number);
                    DataMessage dataMessage =new DataMessage(serverToken.getToken(),dataSend);

                    mService.sendNotification(dataMessage)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        if (response.body().success == 1) {

                                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(UploadList.this);
                                            alertDialog.setTitle("Thank you.Order Placed!!!!");
                                            alertDialog.setMessage("Our partner will call you in a moment.");
                                            alertDialog.setPositiveButton("Order Status", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                    Intent home = new Intent(UploadList.this, MainMenu.class);
                                                    startActivity(home);
                                                    dialog.dismiss();
                                                }
                                            });
                                            alertDialog.setNegativeButton("Home", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                    Intent home = new Intent(UploadList.this, MainMenu.class);
                                                    startActivity(home);
                                                    dialog.dismiss();
                                                }
                                            });
                                            alertDialog.show();

                                            //Toast.makeText(Cart.this, "Thank you, Order Place", Toast.LENGTH_SHORT).show();
                                            //finish();
                                        } else {
                                            Toast.makeText(UploadList.this, "Failed !!!", Toast.LENGTH_SHORT).show();

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
