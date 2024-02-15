package com.ksr.mealhuteats.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ksr.mealhuteats.R;
import com.ksr.mealhuteats.model.Cart;
import com.ksr.mealhuteats.model.Order;
import com.ksr.mealhuteats.widget.CustomDialog;

import java.util.ArrayList;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    public static final String TAG = OrderAdapter.class.getName();
    private List<Order> orderList;
    private FirebaseFirestore firestore;
    private String uid;

    public OrderAdapter(List<Order> orderList, String uid) {
        this.orderList = orderList;
        this.uid = uid;
        this.firestore = FirebaseFirestore.getInstance();
    }

    public void setOrderList(List<Order> orderList) {
        this.orderList = orderList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.order_histor_card, parent, false);
        return new OrderAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderAdapter.ViewHolder holder, int position) {
        Order order = orderList.get(position);

        String orderId = "#" + order.getId();
        String netTotal = "Rs." + order.getNetTotal();

        holder.orderDate.setText(order.getDatetime());
        holder.orderId.setText(orderId);
        holder.netTotal.setText(netTotal);
        holder.orderStatus.setText(order.getStatus());

        if (!order.getStatus().equals("Processing")) {
            holder.button.setVisibility(View.GONE);
        }

        /*setup ordered items list*/
        LinearLayoutManager layoutManager = new LinearLayoutManager(holder.itemView.getContext());
        holder.itemsRecyclerView.setLayoutManager(layoutManager);

        OrderItemAdapter orderItemAdapter = fetchOrderedItems(order.getId());
        holder.itemsRecyclerView.setAdapter(orderItemAdapter);

        /*order cancelling*/
        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showConfirmDialog(holder.itemView.getContext(), order.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    private OrderItemAdapter fetchOrderedItems(String orderId) {
        OrderItemAdapter orderItemAdapter = new OrderItemAdapter(new ArrayList<>());

        firestore.collection("orders")
                .document(uid)
                .collection("myorders")
                .document(orderId)
                .collection("items")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Cart> cartList = new ArrayList<>();
                            for (QueryDocumentSnapshot snapshot : task.getResult()) {
                                Cart cart = snapshot.toObject(Cart.class);
                                cartList.add(cart);
                            }
                            orderItemAdapter.setCartList(cartList);
                        }
                    }
                });

        return orderItemAdapter;
    }

    private void showConfirmDialog(Context context, String orderId) {
        CustomDialog confirmDialog = new CustomDialog(context, R.layout.custom_confirmation_dialog_layout);

        TextView dialogMsgText = confirmDialog.findViewById(R.id.dialogConfirmMsg);
        Button okBtn = confirmDialog.findViewById(R.id.customDialogOkBtn);
        Button cancelBtn = confirmDialog.findViewById(R.id.customDialogCancelBtn);

        String message = "Are you sure you want to cancel this order?\uD83E\uDDD0 \nConfirm to proceed or cancel to keep it in your order.";
        dialogMsgText.setText(message);

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmDialog.dismiss();
                cancelOrder(context, orderId);
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmDialog.dismiss();
            }
        });

        confirmDialog.show();
    }

    private void cancelOrder(Context context, String orderId) {
        CollectionReference collectionReference = firestore.collection("orders")
                .document(uid)
                .collection("myorders")
                .document(orderId)
                .collection("items");

        collectionReference.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot snapshot : task.getResult()) {
                                snapshot.getReference().delete();
                            }
                            collectionReference.getParent().delete();
                            Toast.makeText(context,"Your order has been canceled successfully.",Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView orderDate, orderId, netTotal, orderStatus;
        private RecyclerView itemsRecyclerView;
        private Button button;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            orderDate = itemView.findViewById(R.id.orderDate);
            orderId = itemView.findViewById(R.id.orderId);
            netTotal = itemView.findViewById(R.id.orderHistoryNetTotal);
            itemsRecyclerView = itemView.findViewById(R.id.orderedItemsRecyclerView);
            orderStatus = itemView.findViewById(R.id.orderStatus);
            button = itemView.findViewById(R.id.orderCancelBtn);
        }
    }
}
