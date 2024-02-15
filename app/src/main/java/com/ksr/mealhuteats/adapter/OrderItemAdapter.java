package com.ksr.mealhuteats.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ksr.mealhuteats.R;
import com.ksr.mealhuteats.model.Cart;

import java.util.List;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.ViewHolder> {

    private List<Cart> cartList;

    public OrderItemAdapter(List<Cart> cartList) {
        this.cartList = cartList;
    }

    public void setCartList(List<Cart> cartList) {
        this.cartList = cartList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.order_history_item_layout, parent, false);
        return new OrderItemAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemAdapter.ViewHolder holder, int position) {
        Cart cartItem = cartList.get(position);

        holder.itemName.setText(cartItem.getItemName());
        holder.itemQty.setText(String.valueOf(cartItem.getQty()));
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView itemName, itemQty;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            itemName = itemView.findViewById(R.id.orderItemName);
            itemQty = itemView.findViewById(R.id.orderItemQty);
        }
    }
}
