package com.example.optimas.firebaseconsole;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import com.example.optimas.firebaseconsole.Model.Restaurant;
import com.example.optimas.firebaseconsole.ViewHolder.MenuViewHolder;
import com.example.optimas.firebaseconsole.ViewHolder.RestaurantViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class RestaurantList extends AppCompatActivity {

        AlertDialog waitingDialog;
        RecyclerView recyclerView;
        SwipeRefreshLayout swipeRefreshLayout;

        FirebaseDatabase database;

        DatabaseReference category;

    HashMap<String,String> image_list;
    SliderLayout mSlider;

    FirebaseRecyclerOptions<Restaurant> options = new  FirebaseRecyclerOptions.Builder<Restaurant>()
            .setQuery(FirebaseDatabase.getInstance()
                    .getReference()
                    .child("Restaurants")
                    ,Restaurant.class)
            .build();

    FirebaseRecyclerAdapter<Restaurant,RestaurantViewHolder> adapter= new FirebaseRecyclerAdapter<Restaurant, RestaurantViewHolder>(options) {


        @Override
        protected void onBindViewHolder(@NonNull RestaurantViewHolder viewHolder, int position, @NonNull Restaurant model) {
            viewHolder.txt_restaurant_name.setText(model.getName());
            Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.img_restaurant);


            final Restaurant clickItem=model;
            viewHolder.setItemClickListener(new ItemClickListener() {
                @Override
                public void onClick(View view, int position, boolean isLongClick) {
                    // Toast.makeText(Home.this,""+clickItem.getName(),Toast.LENGTH_SHORT).show();
                    //Get CategoryID
                    Intent foodList=new Intent(RestaurantList.this,Home.class);
                    Common.restaurantSelected=adapter.getRef(position).getKey();
                    startActivity(foodList);
                }
            });
        }
        @Override
        public RestaurantViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.content_restaurant,parent,false);

            return  new RestaurantViewHolder(itemView);
        }



    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resturant_list);
        database=FirebaseDatabase.getInstance();
       // setupSlider();

        //View
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(Common.isConnectedToInternet(getBaseContext()))
                    loadRestaurant();
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
                    loadRestaurant();
                else

                {
                    Toast.makeText(getBaseContext(), "Please check your connection !!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });


        //Load menu
        recyclerView=(RecyclerView)findViewById(R.id.recycle_restaurant);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupSlider() {

        mSlider=(SliderLayout)findViewById(R.id.main_slider);
        image_list = new HashMap<>();

        final DatabaseReference banners = database.getReference("Banner");
        banners.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot:dataSnapshot.getChildren())
                {
                    MainBanner banner= postSnapshot.getValue(MainBanner.class);

                    image_list.put(banner.getName()+"@@@"+banner.getId(),banner.getImage());
                }
                    for(String key:image_list.keySet()){
                    String[] keySplit = key.split("@@@");
                    String nameofFood = keySplit[0];
                    String idofFood = keySplit[1];

                    final TextSliderView textSliderView = new TextSliderView(getBaseContext());
                    textSliderView
                            .description(nameofFood)
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

    private void loadRestaurant() {



        adapter.startListening();
        recyclerView.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);


        //Animation
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }

    @Override
    protected void onStop() {
        super.onStop();
//        mSlider.stopAutoCycle();
        adapter.stopListening();

    }
    public void onResume(){
        super.onResume();

        loadRestaurant();

        if(adapter!=null)
            adapter.startListening();
        //will be executed onResume

    }
}
