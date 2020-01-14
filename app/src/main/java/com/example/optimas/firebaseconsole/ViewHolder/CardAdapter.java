package com.example.optimas.firebaseconsole.ViewHolder;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.optimas.firebaseconsole.Cart;
import com.example.optimas.firebaseconsole.Common.Common;
import com.example.optimas.firebaseconsole.Database.Database;
import com.example.optimas.firebaseconsole.Interface.ItemClickListener;
import com.example.optimas.firebaseconsole.Model.Order;
import com.example.optimas.firebaseconsole.R;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Optimas on 07-11-2018.
 */




public class CardAdapter extends RecyclerView.Adapter<CardViewHolder>{

    private List<Order> listData=new ArrayList<>();
    private Cart cart;

    public CardAdapter(List<Order> listData, Cart cart) {
        this.listData = listData;
        this.cart = cart;
    }

    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(cart);
        View itemView=inflater.inflate(R.layout.card_layout,parent,false);
        return new CardViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CardViewHolder holder, final int position) {
       /* TextDrawable drawable= TextDrawable.builder().buildRound(""+listData.get(position).getQuantity(), Color.RED);
        holder.img_cart_count.setImageDrawable(drawable);*/


        Picasso.with(cart.getBaseContext())
                .load(listData.get(position).getImage())
                .resize(70,70)
                .centerCrop()
                .into(holder.cart_image);

        holder.btn_quantity.setNumber(listData.get(position).getQuantity());
       holder.btn_quantity.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
           @Override
           public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {
               Order order = listData.get(position);
               order.setQuantity(String.valueOf(newValue));
               new Database(cart).updateCart(order);

               int total=0;
               List<Order> orders = new Database(cart).getCarts(Common.currentUser.getPhone());
               for (Order item:orders)
                   total+=(Integer.parseInt(item.getPrice()))*(Integer.parseInt(item.getQuantity()));
               Locale locale = new Locale("en","IN");
               NumberFormat fnt=NumberFormat.getCurrencyInstance(locale);

               cart.txtTotalPrice.setText(fnt.format(total));



           }
       });

        Locale locale = new Locale("en","IN");
        NumberFormat fnt=NumberFormat.getCurrencyInstance(locale);
        int price=(Integer.parseInt(listData.get(position).getPrice()))*(Integer.parseInt(listData.get(position).getQuantity()));
        holder.txt_price.setText(fnt.format(price));
        holder.txt_cart_name.setText(listData.get(position).getProductName());

    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    public void removeItem(int posistion){
        listData.remove(posistion);
        notifyItemRemoved(posistion);
    }

    public void restoreItem(Order item,int posistion){
        listData.add(posistion,item);
        notifyItemInserted(posistion);
    }

    public Order getItem(int position)
    {
        return listData.get(position);
    }
}
