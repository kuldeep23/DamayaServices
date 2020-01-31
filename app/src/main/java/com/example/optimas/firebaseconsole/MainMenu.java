package com.example.optimas.firebaseconsole;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.example.optimas.firebaseconsole.Common.Common;
import com.example.optimas.firebaseconsole.Database.Database;
import com.example.optimas.firebaseconsole.Interface.ItemClickListener;
import com.example.optimas.firebaseconsole.Model.Banner;
import com.example.optimas.firebaseconsole.Model.Category;
import com.example.optimas.firebaseconsole.Model.MainBanner;
import com.example.optimas.firebaseconsole.Model.Token;
import com.example.optimas.firebaseconsole.ViewHolder.RestaurantViewHolder;
import com.facebook.accountkit.AccountKit;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainMenu extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    FirebaseDatabase database;
    DatabaseReference category;
    TextView txtFullName;

    RecyclerView recycler_menu;
    RecyclerView.LayoutManager layoutManager;

    FirebaseRecyclerAdapter<Category,RestaurantViewHolder> adapter;

    SwipeRefreshLayout swipeRefreshLayout;
    CounterFab fab;

    HashMap<String,String> image_list;
    SliderLayout mSlider;

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
        setContentView(R.layout.activity_home);

        FirebaseMessaging.getInstance().subscribeToTopic(Common.topicName);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Menu");
        setSupportActionBar(toolbar);


        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(Common.isConnectedToInternet(getBaseContext()))
                    loadMenu();
                else

                {
                    Toast.makeText(getBaseContext(), "Please check your connection !!", Toast.LENGTH_SHORT).show();
                    return;
                }

            }
        });

        //Default load first time
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if(Common.isConnectedToInternet(getBaseContext()))
                    loadMenu();
                else

                {
                    Toast.makeText(getBaseContext(), "Please check your connection !!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
        //Init Firebase
        database=FirebaseDatabase.getInstance();
        category=database.getReference("Restaurants")/*.child(Common.restaurantSelected)
                .child("details").child("Category")*/;


        FirebaseRecyclerOptions<Category> options = new  FirebaseRecyclerOptions.Builder<Category>()
                .setQuery(category,Category.class)
                .build();
        adapter= new FirebaseRecyclerAdapter<Category, RestaurantViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull RestaurantViewHolder viewHolder, int position, @NonNull Category model) {
                viewHolder.txt_restaurant_name.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.img_restaurant);


                final Category clickItem=model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        // Toast.makeText(Home.this,""+clickItem.getName(),Toast.LENGTH_SHORT).show();
                        //Get CategoryID
                        Common.restaurantSelected=adapter.getRef(position).getKey();
                        switch (Common.restaurantSelected) {
                            case "01":
                                Intent sendpackage=new Intent(MainMenu.this,SendPackage.class);
                                startActivity(sendpackage);
                                break;
                            case "02":
                                Intent foodList=new Intent(MainMenu.this,Home.class);
                                startActivity(foodList);
                                break;
                            case "03":
                                Intent fruitsList=new Intent(MainMenu.this,Home.class);
                                startActivity(fruitsList);
                                break;
                            case "04":
                                Intent vegetablesList=new Intent(MainMenu.this,Home.class);
                                startActivity(vegetablesList);
                                break;
                            case "07":
                                Intent uploadList=new Intent(MainMenu.this,UploadList.class);
                                startActivity(uploadList);
                                break;
                        }
                    }
                });
            }

            @NonNull
            @Override
            public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.resturant_item,parent,false);

                return  new RestaurantViewHolder(itemView);
            }
        };


        Paper.init(this);


        /*fab = (CounterFab) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cartIntent = new Intent(MainMenu.this,Cart.class);
                startActivity(cartIntent);
            }
        });

        fab.setCount( new Database(this).getCountCart(Common.currentUser.getPhone()));*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Set Name for user
        View headerView= navigationView.getHeaderView(0);
        txtFullName=(TextView)headerView.findViewById(R.id.txtFullName);

        txtFullName.setText("Welcome "+Common.currentUser.getName());

        //Load menu
        recycler_menu=(RecyclerView)findViewById(R.id.recycle_menu);
        recycler_menu.setLayoutManager(new GridLayoutManager(this,3));
        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(recycler_menu.getContext(),
                R.anim.layout_fall_down);
        recycler_menu.setLayoutAnimation(controller);


        updateToken(FirebaseInstanceId.getInstance().getToken());

       setupSlider();

    }

    private void setupSlider() {


        mSlider=(SliderLayout)findViewById(R.id.slider);
        image_list = new HashMap<>();


        final DatabaseReference banners = database.getReference("Banner");

        banners.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot:dataSnapshot.getChildren())
                {
                    Banner banner= postSnapshot.getValue(Banner.class);
                    image_list.put(banner.getName()+"@@@"+banner.getId(),banner.getImage());
                }
                for(String key:image_list.keySet()){
                    String[] keySplit = key.split("@@@");
                    String nameofFood = keySplit[0];
                    String idofFood = keySplit[1];

                    final TextSliderView textSliderView = new TextSliderView(getBaseContext());
                    textSliderView.description(nameofFood)
                            .image(image_list.get(key))
                            .setScaleType(BaseSliderView.ScaleType.Fit);
                           /* .setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                                @Override
                                public void onSliderClick(BaseSliderView slider) {
                                     Intent intent = new Intent(Home.this,FoodDetail.class);
                                     intent.putExtras(textSliderView.getBundle());
                                     startActivity(intent);
                                }
                            });*/

                    textSliderView.bundle(new Bundle());
                    textSliderView.getBundle().putString("foodId",idofFood);

                    mSlider.addSlider(textSliderView);

                    banners.removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mSlider.setPresetTransformer(SliderLayout.Transformer.Background2Foreground);
        mSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        mSlider.setCustomAnimation(new DescriptionAnimation());
        mSlider.setDuration(4000);

    }

        private void updateToken(String token) {
        FirebaseDatabase db= FirebaseDatabase.getInstance();
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(/*"Restaurants").child(Common.restaurantSelected).child(*/"Tokens");
        Token data = new Token(token,false);
        tokens.child(Common.currentUser.getPhone()).setValue(data);
    }

    private void loadMenu() {



        adapter.startListening();
        recycler_menu.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);


        //Animation
        recycler_menu.getAdapter().notifyDataSetChanged();
        recycler_menu.scheduleLayoutAnimation();
    }


    @Override
    protected void onStop() {
        super.onStop();

        adapter.stopListening();
 //       mSlider.stopAutoCycle();
    }
    @Override
    public void onResume(){
        super.onResume();
//        mSlider.startAutoCycle();
        loadMenu();
//        fab.setCount( new Database(this).getCountCart(Common.currentUser.getPhone()));
        if(adapter!=null)
            adapter.startListening();
        //will be executed onResume

    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.menu_search)
            startActivity(new Intent(MainMenu.this,SearchActivity.class));


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_menu ) {
            // Handle the camera action
        } else if (id == R.id.nav_cart) {

            Intent cartIntent= new Intent(MainMenu.this,Cart.class);
            startActivity(cartIntent);

        } else if (id == R.id.nav_orders) {

            Intent orderIntent= new Intent(MainMenu.this,OrderStatus.class);
            startActivity(orderIntent);

        } else if (id == R.id.nav_logout) {

            AccountKit.logOut();


            //Logout
            Intent signIn= new Intent(MainMenu.this,MainActivity.class);
            signIn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(signIn);

        }

        else if (id==R.id.nav_update_name){
            showChangePasswordDialog();
        }
        else if (id==R.id.nav_share){
            showSharedDialog();
        }
        else if (id==R.id.nav_contactus){
            showContactDialog();
        }
        else if (id==R.id.nav_rate){
            Toast.makeText(this,"Coming Soon",Toast.LENGTH_LONG).show();

        }
        else if(id==R.id.nav_policy){
            Toast.makeText(this,"Coming Soon",Toast.LENGTH_LONG).show();

        }

       /* else if (id==R.id.nav_home_address){
            showHomeAddressDialog();
        }*/



        else if (id==R.id.nav_fav){
            startActivity(new Intent(MainMenu.this,FavoritesActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showContactDialog() {
        Toast.makeText(this,"Coming Soon",Toast.LENGTH_LONG).show();
    }

    private void showSharedDialog() {
        Toast.makeText(this,"Coming Soon",Toast.LENGTH_LONG).show();
    }


   /* private void showHomeAddressDialog() {

            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
            alertDialog.setTitle("Change Home Address");
            alertDialog.setMessage("Please fill all information");

            LayoutInflater inflater = LayoutInflater.from(this);
            View layout_home = inflater.inflate(R.layout.home_address_layout,null);

        final MaterialEditText edtHomeAddress = (MaterialEditText)layout_home.findViewById(R.id.edtHomeAddress);
        alertDialog.setView(layout_home);
        alertDialog.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                Common.currentUser.setHomeAddress(edtHomeAddress.getText().toString());

                FirebaseDatabase.getInstance().getReference("User")
                        .child(Common.currentUser.getPhone())
                        .setValue(Common.currentUser)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(Home.this,"Update Address Successfully",Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        });
        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }*/

    private void showChangePasswordDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainMenu.this);
        alertDialog.setTitle("Change Name");
        alertDialog.setMessage("Please fill all information");

        LayoutInflater inflater = LayoutInflater.from(this);
        View layout_name = inflater.inflate(R.layout.update_name_layout,null);

        final MaterialEditText edtName = (MaterialEditText)layout_name.findViewById(R.id.edtName);

        alertDialog.setView(layout_name);

        alertDialog.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                final android.app.AlertDialog waitingDialog = new SpotsDialog(MainMenu.this);
                waitingDialog.show();

                //UpdateName
                Map<String,Object> update_name=new HashMap<>();
                update_name.put("name",edtName.getText().toString());

                FirebaseDatabase.getInstance()
                        .getReference("User")
                        .child(Common.currentUser.getPhone())
                        .updateChildren(update_name)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                waitingDialog.dismiss();
                                if (task.isSuccessful())
                                    Toast.makeText(MainMenu.this, "Name was Updated", Toast.LENGTH_SHORT).show();


                            }
                        });


            }
        });

        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        alertDialog.show();
    }


}
