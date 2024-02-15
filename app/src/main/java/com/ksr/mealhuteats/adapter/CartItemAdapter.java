package com.ksr.mealhuteats.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.ksr.mealhuteats.R;
import com.ksr.mealhuteats.model.Cart;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.ViewHolder> {

    private List<Cart> cartList;
    private OnQuantityChangeListener listener;
    private FirebaseStorage firebaseStorage;

    public CartItemAdapter(List<Cart> cartList, OnQuantityChangeListener listener) {
        this.cartList = cartList;
        this.listener = listener;
        this.firebaseStorage = FirebaseStorage.getInstance();
    }

    public void setCartList(List<Cart> cartList){
        this.cartList = cartList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.cart_item_card,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartItemAdapter.ViewHolder holder, int position) {
        Cart cart = cartList.get(position);

        String itemPrice = "Rs. "+ cart.getItemPrice();

        holder.cartItemName.setText(cart.getItemName());
        holder.cartItemPrice.setText(itemPrice);
        holder.cartItemQty.setText(String.valueOf(cart.getQty()));

        firebaseStorage.getReference("product-images/" + cart.getItemImage())
                .getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).fit().centerCrop().into(holder.cartItemImage);
                    }
                });

        holder.cartItemMinus.setOnClickListener(view -> listener.onDecrementClick(position));
        holder.cartItemPlus.setOnClickListener(view -> listener.onIncrementClick(position));
        holder.cartItemDelete.setOnClickListener(view -> listener.onRemoveClick(position));
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView cartItemName, cartItemPrice, cartItemQty;
        ImageView cartItemImage,cartItemMinus,cartItemPlus,cartItemDelete;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cartItemName = itemView.findViewById(R.id.cartItemName);
            cartItemPrice = itemView.findViewById(R.id.cartItemPrice);
            cartItemQty = itemView.findViewById(R.id.cartItemQty);
            cartItemImage = itemView.findViewById(R.id.cartItemImg);
            cartItemMinus = itemView.findViewById(R.id.cartItemMinus);
            cartItemPlus = itemView.findViewById(R.id.cartItemPlus);
            cartItemDelete = itemView.findViewById(R.id.cartItemDeleteButton);
        }
    }

    public interface OnQuantityChangeListener {
        void onIncrementClick(int position);
        void onDecrementClick(int position);
        void onRemoveClick(int position);
    }
}
