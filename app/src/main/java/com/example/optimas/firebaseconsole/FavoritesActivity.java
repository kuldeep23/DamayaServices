package com.example.optimas.firebaseconsole;

import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.RelativeLayout;

import com.example.optimas.firebaseconsole.Common.Common;
import com.example.optimas.firebaseconsole.Database.Database;
import com.example.optimas.firebaseconsole.Helper.RecyclerItemTouchHelper;
import com.example.optimas.firebaseconsole.Interface.RecyclerItemTouchHelperListener;
import com.example.optimas.firebaseconsole.Model.Favorites;
import com.example.optimas.firebaseconsole.Model.Order;
import com.example.optimas.firebaseconsole.ViewHolder.FavoritesAdapter;
import com.example.optimas.firebaseconsole.ViewHolder.FavoritesViewHolder;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class FavoritesActivity extends AppCompatActivity implements RecyclerItemTouchHelperListener {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

  FavoritesAdapter adapter;

  RelativeLayout rootLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        rootLayout =(RelativeLayout)findViewById(R.id.root_layout);

        recyclerView = (RecyclerView) findViewById(R.id.recycle_fav);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(recyclerView.getContext(),
                R.anim.layout_fall_down);
        recyclerView.setLayoutAnimation(controller);

        //Swipe to delete
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0,ItemTouchHelper.LEFT,this);
        new  ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);
        
        loadFavorites();

    }

    private void loadFavorites() {
        adapter = new FavoritesAdapter(this,new Database(this).getAllFavorites(Common.currentUser.getPhone()));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if(viewHolder instanceof FavoritesViewHolder){

            String name= ((FavoritesAdapter)recyclerView.getAdapter()).getItem(position).getFoodName();

            final Favorites deleteItem = ((FavoritesAdapter)recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition());
            final int deleteIndex = viewHolder.getAdapterPosition();

            adapter.removeItem(viewHolder.getAdapterPosition());

            new Database(getBaseContext()).removeFromFavorites(deleteItem.getFoodId(),Common.currentUser.getPhone());

            //Make Snackbar

            Snackbar snackbar = Snackbar.make(rootLayout,name + " removed from cart!",Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.restoreItem(deleteItem,deleteIndex);
                    new Database(getBaseContext()).addToFavorites(deleteItem);


                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }
}
