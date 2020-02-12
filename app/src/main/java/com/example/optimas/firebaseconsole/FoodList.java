package com.example.optimas.firebaseconsole;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.optimas.firebaseconsole.Common.Common;
import com.example.optimas.firebaseconsole.Database.Database;
import com.example.optimas.firebaseconsole.Interface.ItemClickListener;
import com.example.optimas.firebaseconsole.Model.Category;
import com.example.optimas.firebaseconsole.Model.Favorites;
import com.example.optimas.firebaseconsole.Model.Food;
import com.example.optimas.firebaseconsole.Model.Order;
import com.example.optimas.firebaseconsole.ViewHolder.FoodViewHolder;
import com.facebook.CallbackManager;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.squareup.picasso.Transformation;

import java.util.ArrayList;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class FoodList extends AppCompatActivity  {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference foodList;

    String categoryId = "";
    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;

    //Search Fuctionality

    FirebaseRecyclerAdapter<Food, FoodViewHolder> searchadapter;
    List<String> suggestList= new ArrayList<>();
    MaterialSearchBar materialSearchbar;

    //Favroties
    Database localDB;

    //Facebook Share
    CallbackManager callbackManager;
    ShareDialog shareDialog;

    SwipeRefreshLayout swipeRefreshLayout;
    //Create target from Picaso

    Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

            //Create photo from Bitmap

            SharePhoto photo = new SharePhoto.Builder()
                    .setBitmap(bitmap)
                    .build();

            if(ShareDialog.canShow(SharePhotoContent.class)){

                SharePhotoContent content = new SharePhotoContent.Builder()
                        .addPhoto(photo)
                        .build();
                shareDialog.show(content);
            }
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //fonts
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/OpenSans-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());


        setContentView(R.layout.activity_cart);
        setContentView(R.layout.activity_food_list);

        //Init Facebook
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);



        database = FirebaseDatabase.getInstance();
        foodList = database.getReference("Restaurants").child(Common.restaurantSelected)
                .child("details").child("Foods");

        //Local DB
        localDB = new Database(this);

        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if (getIntent() != null)
                    categoryId = getIntent().getStringExtra("CategoryId");

                switch (categoryId) {
                    case "05":
                        Intent sendpackage=new Intent(FoodList.this,HouseHolds.class);
                        sendpackage.putExtra("CategoryId",categoryId);
                        startActivity(sendpackage);
                        break;
                    case "02":
                        if (!categoryId.isEmpty() && categoryId != null) {

                            if(Common.isConnectedToInternet(getBaseContext()))
                                loadListFood(categoryId);
                            else
                            {
                                Toast.makeText(FoodList.this, "Please check your connection !!", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            ///   loadFood(categoryId);


                        }
                        break;

                }


            }
        });

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (getIntent() != null)
                    categoryId = getIntent().getStringExtra("CategoryId");
                if (!categoryId.isEmpty() && categoryId != null) {

                    if(Common.isConnectedToInternet(getBaseContext()))
                        loadListFood(categoryId);
                    else
                    {
                        Toast.makeText(FoodList.this, "Please check your connection !!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    ///   loadFood(categoryId);
                }

                //Search
                materialSearchbar=(MaterialSearchBar)findViewById(R.id.searchBar);
                materialSearchbar.setHint("Enter your food");
                //materialSearchbar.setSpeechMode(false);
                loadSuggest();

                materialSearchbar.setCardViewElevation(10);
                materialSearchbar.addTextChangeListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        // When user type text suggest

                        List<String> suggest=new ArrayList<String>();
                        for(String search:suggestList){

                            if(search.toLowerCase().contains(materialSearchbar.getText().toLowerCase()))
                                suggest.add(search);
                        }
                        materialSearchbar.setLastSuggestions(suggest);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
                materialSearchbar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
                    @Override
                    public void onSearchStateChanged(boolean enabled) {
                        if(!enabled)
                            recyclerView.setAdapter(adapter);
                    }

                    @Override
                    public void onSearchConfirmed(CharSequence text) {

                        startSearch(text);
                    }

                    @Override
                    public void onButtonClicked(int buttonCode) {

                    }
                });

            }
        });
        recyclerView = (RecyclerView) findViewById(R.id.recycle_food);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);



    }

    private void startSearch(CharSequence text) {

        Query searchByName = foodList.orderByChild("name").equalTo(text.toString());

        FirebaseRecyclerOptions<Food> foodOptions = new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(searchByName,Food.class)
                .build();

        searchadapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(foodOptions) {
            @Override
            protected void onBindViewHolder(@NonNull FoodViewHolder viewHolder, int position, @NonNull Food model) {
                viewHolder.food_name.setText(model.getName());
                viewHolder.food_description.setText(model.getDescription());
                viewHolder.food_price.setText(model.getPrice());

                Transformation transformation = new RoundedTransformationBuilder()
                        .borderColor(Color.BLACK)
                        .borderWidthDp(3)
                        .cornerRadiusDp(30)
                        .oval(false)
                        .build();
                Picasso.with(getBaseContext())
                        .load(model.getImage())
                        .fit()
                        .transform(transformation)
                        .into(viewHolder.food_image);

                final Food local = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        // Toast.makeText(Home.this,""+clickItem.getName(),Toast.LENGTH_SHORT).show();
                        //Get CategoryID
                        Intent foodDetail = new Intent(FoodList.this, FoodDetail.class);
                        foodDetail.putExtra("foodId", searchadapter.getRef(position).getKey());
                        startActivity(foodDetail);
                    }
                });
            }

            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.food_item1,parent,false);
                return new FoodViewHolder(itemView);
            }
        };
        searchadapter.startListening();
        recyclerView.setAdapter(searchadapter);
    }

    private void loadSuggest() {
    foodList.orderByChild("menuId").equalTo(categoryId).addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            for (DataSnapshot postSnapshot:dataSnapshot.getChildren()){

                Food item= postSnapshot.getValue(Food.class);
                suggestList.add(item.getName());

            }
            materialSearchbar.setLastSuggestions(suggestList);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    });
    }

    private void loadListFood(String categoryId) {

        Query searchByName = foodList.orderByChild("menuId").equalTo(categoryId);

        FirebaseRecyclerOptions<Food> foodOptions = new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(searchByName,Food.class)
                .build();
      adapter=new FirebaseRecyclerAdapter<Food, FoodViewHolder>(foodOptions) {
          @Override
          protected void onBindViewHolder(@NonNull final FoodViewHolder viewHolder, final int position, @NonNull final Food model) {
              viewHolder.food_name.setText(model.getName());
              viewHolder.food_price.setText(String.format("Rs. %s", model.getPrice().toString()));
              viewHolder.food_description.setText(model.getDescription());

              Transformation transformation = new RoundedTransformationBuilder()
                      .cornerRadiusDp(8)
                      .oval(false)
                      .build();
              Picasso.with(getBaseContext())
                      .load(model.getImage())
                      .fit()
                      .transform(transformation)
                      .into(viewHolder.food_image);
             // Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.food_image);


              viewHolder.quick_cart.setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                      //Quick Cart
                      boolean isExists = new Database(getBaseContext()).checkFoodExist(adapter.getRef(position).getKey(), Common.currentUser.getPhone());

                      if (!isExists) {
                          new Database(getBaseContext()).addToCart(new Order(
                                  Common.currentUser.getPhone(),
                                  adapter.getRef(position).getKey(),
                                  model.getName(),
                                  "1",
                                  model.getPrice(),
                                  model.getDiscount(),
                                  model.getImage()
                          ));

                      } else {
                          new Database(getBaseContext()).increaseCart(Common.currentUser.getPhone(), adapter.getRef(position).getKey());
                      }
                      Toast.makeText(FoodList.this, "Added to Cart", Toast.LENGTH_SHORT).show();
                  }
              });



              //Add Favroties
              if(localDB.isFavorites(adapter.getRef(position).getKey(),Common.currentUser.getPhone()))
                  viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);

              //Click to Share

              /*viewHolder.share_image.setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                      Picasso.with(getApplicationContext())
                              .load(model.getImage())
                              .into(target);
                  }
              });*/
              //Click to change state of favroties
              viewHolder.fav_image.setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {

                      Favorites favorites = new Favorites();
                      favorites.setFoodId( adapter.getRef(position).getKey());
                      favorites.setFoodName(model.getName());
                      favorites.setFoodDescription(model.getDescription());
                      favorites.setFoodDiscount(model.getDiscount());
                      favorites.setFoodImage(model.getImage());
                      favorites.setFoodMenuId(model.getMenuId());
                      favorites.setUserPhone(Common.currentUser.getPhone());
                      favorites.setFoodPrice(model.getPrice());

                      if(!localDB.isFavorites(adapter.getRef(position).getKey(),Common.currentUser.getPhone())){

                          localDB.addToFavorites(favorites);
                          viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);
                          Toast.makeText(FoodList.this,""+model.getName()+" was added to Favorites",Toast.LENGTH_LONG).show();

                      }
                      else{
                          localDB.removeFromFavorites(adapter.getRef(position).getKey(),Common.currentUser.getPhone());
                          viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                          Toast.makeText(FoodList.this,""+model.getName()+" was removed from Favorites",Toast.LENGTH_LONG).show();
                      }
                  }
              });

              final Food local=model;
              viewHolder.setItemClickListener(new ItemClickListener() {
                  @Override
                  public void onClick(View view, int position, boolean isLongClick) {
                      // Toast.makeText(Home.this,""+clickItem.getName(),Toast.LENGTH_SHORT).show();
                      //Get CategoryID
                      Intent foodDetail=new Intent(FoodList.this,FoodDetail.class);
                      foodDetail.putExtra("foodId",adapter.getRef(position).getKey());
                      startActivity(foodDetail);
                  }
              });

          }

          @Override
          public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
              View itemView = LayoutInflater.from(parent.getContext())
                      .inflate(R.layout.food_item1,parent,false);
              return new FoodViewHolder(itemView);
          }
      };
      adapter.startListening();
      recyclerView.setAdapter(adapter);
      swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onResume(){
        super.onResume();
        loadListFood(categoryId);
        //will be executed onResume
        if(adapter!=null)
            adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(adapter!=null)
        adapter.stopListening();

        if(searchadapter!=null)
        searchadapter.stopListening();
    }



}

