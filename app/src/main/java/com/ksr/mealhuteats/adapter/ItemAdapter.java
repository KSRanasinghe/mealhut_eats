package com.ksr.mealhuteats.adapter;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.ksr.mealhuteats.R;
import com.ksr.mealhuteats.model.Cart;
import com.ksr.mealhuteats.model.Item;
import com.ksr.mealhuteats.util.OnCardClickListener;
import com.ksr.mealhuteats.widget.ProcessDialog;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    public static final String TAG = ItemAdapter.class.getName();
    private Context context;
    private List<Item> itemList;
    private OnCardClickListener<Item> onCardClickListener;
    private FirebaseStorage firebaseStorage;

    public ItemAdapter() {
    }

    public ItemAdapter(Context context, List<Item> itemList, OnCardClickListener<Item> onCardClickListener) {
        this.context = context;
        this.itemList = itemList;
        this.onCardClickListener = onCardClickListener;
        this.firebaseStorage = FirebaseStorage.getInstance();
    }

    public void setItemList(List<Item> itemList) {
        this.itemList = itemList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_card, parent, false);
        return new ViewHolder(view, onCardClickListener, itemList, context);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemAdapter.ViewHolder holder, int position) {
        if (itemList != null && position < itemList.size()) {
            Item item = itemList.get(position);

            String itemPrice = "Rs. " + item.getPrice();

            holder.itemName.setText(item.getName());
            holder.itemPrice.setText(itemPrice);

            firebaseStorage.getReference("product-images/" + item.getImage())
                    .getDownloadUrl()
                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.get().load(uri).fit().centerCrop().into(holder.itemImage);
                        }
                    });
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ItemAdapter itemAdapter = new ItemAdapter();
        TextView itemName, itemPrice;
        ImageView itemImage, itemPlus;

        public ViewHolder(@NonNull View itemView, OnCardClickListener<Item> onCardClickListener, List<Item> itemList, Context context) {
            super(itemView);
            itemName = itemView.findViewById(R.id.itemName);
            itemPrice = itemView.findViewById(R.id.itemPrice);
            itemImage = itemView.findViewById(R.id.itemImage);
            itemPlus = itemView.findViewById(R.id.itemPlus);

            /*set onClickListener for item image in item cards*/
            itemImage.setOnClickListener(view -> {
                if (onCardClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onCardClickListener.onCardClick(itemList.get(position));
                    }
                }
            });

            /*set onClickListener for "Add to Cart" button in item cards*/
            itemPlus.setOnClickListener(view -> {
                if (onCardClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Item item = itemList.get(position);

                        //get current user
                        String userId = itemAdapter.getCurrentUserId();
                        Log.i(TAG, userId);
                        itemAdapter.updateCart(userId, item, context);
                    }
                }
            });
        }
    }

    private String getCurrentUserId() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            return user.getUid();
        }
        return null;
    }

    private void updateCart(String uid, Item item, Context context) {
        ProcessDialog processDialog = new ProcessDialog(context, R.layout.custom_processing_dialog, R.raw.add_cart);
        processDialog.show();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference userCartRef = firestore.collection("carts").document(uid);

        userCartRef.collection("items")
                .document(item.getId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot document) {
                        if (document.exists()) {
                            Cart cart = document.toObject(Cart.class);
                            assert cart != null;
                            int currentQty = cart.getQty();

                            if (currentQty < 10) {
                                userCartRef.collection("items")
                                        .document(item.getId())
                                        .update("qty", currentQty + 1)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                processDialog.dismiss();
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(context, "Meal added successfully.", Toast.LENGTH_LONG).show();
                                                } else {
                                                    Toast.makeText(context, "Something went wrong.", Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                            } else {
                                processDialog.dismiss();
                                Toast.makeText(context, "You have added the maximum quantity for this item.", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            userCartRef.collection("items")
                                    .document(item.getId())
                                    .set(new Cart(item.getId(), item.getName(), 1, item.getPrice(), item.getImage()))
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            processDialog.dismiss();
                                            if (task.isSuccessful()) {
                                                Toast.makeText(context, "Meal added successfully.", Toast.LENGTH_LONG).show();
                                            } else {
                                                Toast.makeText(context, "Something went wrong.", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        processDialog.dismiss();
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(context, "Failed to check cart", Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void setOnCardClickListener(OnCardClickListener<Item> listener) {
        this.onCardClickListener = listener;
    }
}
