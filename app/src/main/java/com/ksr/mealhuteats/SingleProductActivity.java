package com.ksr.mealhuteats;

import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.ksr.mealhuteats.model.Cart;
import com.ksr.mealhuteats.model.Item;
import com.ksr.mealhuteats.util.NetworkChangeListener;
import com.ksr.mealhuteats.widget.CustomDialog;
import com.ksr.mealhuteats.widget.ProcessDialog;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;

public class SingleProductActivity extends AppCompatActivity {

    public static final String TAG = SingleProductActivity.class.getName();
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    private MaterialToolbar toolbar;
    private ImageView qtyMinus, qtyPlus, productImage;
    private TextView productName, productPrice, productDesc, productQtyText;
    private Button productAddBtn, productDropBtn;
    private String itemId;
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;
    private String uid;
    private FirebaseStorage storage;
    private ProcessDialog processDialog, removeProcessDialog;
    private CustomDialog confirmDialog;
    private Item thisItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_product);

        toolbar = findViewById(R.id.singleProductToolbar);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        productName = findViewById(R.id.singleProductName);
        productPrice = findViewById(R.id.singleProductPrice);
        productDesc = findViewById(R.id.singleProductInfo);
        productImage = findViewById(R.id.singleProductImage);
        qtyMinus = findViewById(R.id.singleProductMinus);
        qtyPlus = findViewById(R.id.singleProductPlus);
        productQtyText = findViewById(R.id.singleProductQty);
        productAddBtn = findViewById(R.id.singleProductBtnAddCart);
        productDropBtn = findViewById(R.id.singleProductBtnDropCart);
        processDialog = new ProcessDialog(this, R.layout.custom_processing_dialog, R.raw.add_cart);
        removeProcessDialog = new ProcessDialog(this, R.layout.custom_processing_dialog, R.raw.cart_remove);

        itemId = getIntent().getStringExtra("id");

        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        // Enable the back button in the toolbar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Set a click listener for the back button
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });

        /*fetching data from firestore*/
        fetchDataFromFirestore(itemId);

        /*decrease qty*/
        qtyMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int qty = Integer.parseInt(productQtyText.getText().toString());
                if (qty > 1) {
                    int newQty = qty - 1;
                    productQtyText.setText(String.valueOf(newQty));
                }
            }
        });

        /*increase qty*/
        qtyPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int qty = Integer.parseInt(productQtyText.getText().toString());
                if (qty < 10) {
                    int newQty = qty + 1;
                    productQtyText.setText(String.valueOf(newQty));
                }
            }
        });

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            uid = user.getUid();
        }

        /*add to cart*/
        productAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int qty = Integer.parseInt(productQtyText.getText().toString());
                updateCart(uid, thisItem, qty);
            }
        });

        /*drop all from cart*/
        productDropBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkProductAvailability(uid, thisItem);
            }
        });

    }

    @Override
    protected void onStart() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener,filter);
        super.onStart();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(networkChangeListener);
        super.onStop();
    }

    private void fetchDataFromFirestore(String itemId) {
        firestore.collection("products")
                .whereEqualTo("id", itemId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot snapshot : task.getResult()) {
                                Item item = snapshot.toObject(Item.class);
                                fetchImageFromFirebaseStorage(item);
                            }
                        } else {
                            Log.e(TAG, "fetchDataFromFirestore: " + task.getException().getMessage());
                            Toast.makeText(getApplicationContext(), "Something went wrong.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void fetchImageFromFirebaseStorage(Item item) {
        storage.getReference("product-images/" + item.getImage())
                .getDownloadUrl()
                .addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri uri = task.getResult();
                            processData(item, uri);
                        } else {
                            Log.e(TAG, "fetchImageFromFirebaseStorage: " + task.getException().getMessage());
                            Toast.makeText(getApplicationContext(), "Something went wrong.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void processData(Item item, Uri uri) {
        thisItem = item;
        productName.setText(item.getName());
        String price = "Rs." + new DecimalFormat("#.##").format(item.getPrice());
        productPrice.setText(price);
        productDesc.setText(item.getDescription());
        Picasso.get().load(uri).fit().centerCrop().into(productImage);
        productAddBtn.setEnabled(true);
        productDropBtn.setEnabled(true);
    }

    private void updateCart(String uid, Item item, int selectedQty) {
        processDialog.show();

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
                            int newQty = currentQty + selectedQty;

                            if (newQty < 11) {
                                userCartRef.collection("items")
                                        .document(item.getId())
                                        .update("qty", newQty)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                processDialog.dismiss();
                                                if (task.isSuccessful()) {
                                                    productQtyText.setText("1");
                                                    Toast.makeText(getApplicationContext(), "Meal added successfully.", Toast.LENGTH_LONG).show();
                                                } else {
                                                    Toast.makeText(getApplicationContext(), "Something went wrong.", Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                            } else {
                                int remainQty = 0;

                                if (10 - currentQty > 0) {
                                    remainQty = 10 - currentQty;
                                }
                                processDialog.dismiss();

                                String message = "Oops! \uD83D\uDE1F You can add up to 10 servings per order for each meal. " +
                                        "Currently, you have " + remainQty + " servings available. If you'd like more than 10 servings, " +
                                        "please reach out to our support team. Thank you!";
                                showDialog(message);
                            }
                        } else {
                            userCartRef.collection("items")
                                    .document(item.getId())
                                    .set(new Cart(item.getId(), item.getName(), selectedQty, item.getPrice(), item.getImage()))
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            processDialog.dismiss();
                                            if (task.isSuccessful()) {
                                                productQtyText.setText("1");
                                                Toast.makeText(getApplicationContext(), "Meal added successfully.", Toast.LENGTH_LONG).show();
                                            } else {
                                                Toast.makeText(getApplicationContext(), "Something went wrong.", Toast.LENGTH_LONG).show();
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
                        Toast.makeText(getApplicationContext(), "Failed to check cart", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void checkProductAvailability(String uid, Item item) {
        removeProcessDialog.show();
        firestore.collection("carts")
                .document(uid)
                .collection("items")
                .document(item.getId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().exists()) {
                                String message = "Are you sure you want to remove " + item.getName() + " from your cart?\uD83E\uDDD0 \nConfirm to proceed or cancel to keep it in your order.";
                                showConfirmDialog(message);
                            } else {
                                removeProcessDialog.dismiss();
                                Toast.makeText(SingleProductActivity.this, "You don't have added this product.", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            removeProcessDialog.dismiss();
                            Toast.makeText(SingleProductActivity.this, "Something went wrong. Please try again later.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void showConfirmDialog(String message) {
        confirmDialog = new CustomDialog(SingleProductActivity.this, R.layout.custom_confirmation_dialog_layout);

        TextView dialogMsgText = confirmDialog.findViewById(R.id.dialogConfirmMsg);
        Button okBtn = confirmDialog.findViewById(R.id.customDialogOkBtn);
        Button cancelBtn = confirmDialog.findViewById(R.id.customDialogCancelBtn);

        dialogMsgText.setText(message);

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dropProduct(uid, thisItem);
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

    private void dropProduct(String uid, Item item) {
        confirmDialog.dismiss();
        removeProcessDialog.show();

        firestore.collection("carts")
                .document(uid)
                .collection("items")
                .document(item.getId())
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        removeProcessDialog.dismiss();
                        if (task.isSuccessful()) {
                            Toast.makeText(SingleProductActivity.this, "Meal removed successfully.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(SingleProductActivity.this, "Something went wrong. Please try again later.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void showDialog(String message) {
        CustomDialog dialog = new CustomDialog(SingleProductActivity.this, R.layout.custom_dialog_layout);

        TextView dialogMsgText = dialog.findViewById(R.id.dialogMsg);
        Button dialogBtn = dialog.findViewById(R.id.customDialogBtn);

        dialogMsgText.setText(message);

        dialogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

}