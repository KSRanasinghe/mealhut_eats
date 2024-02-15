package com.ksr.mealhuteats;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ksr.mealhuteats.adapter.CartItemAdapter;
import com.ksr.mealhuteats.model.Cart;
import com.ksr.mealhuteats.model.Order;
import com.ksr.mealhuteats.model.User;
import com.ksr.mealhuteats.widget.CustomDialog;
import com.ksr.mealhuteats.widget.ProcessDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class CartFragment extends Fragment implements CartItemAdapter.OnQuantityChangeListener {

    public static final String TAG = CartFragment.class.getName();
    private static CartFragment instance;
    private RecyclerView recyclerView;
    private TextView txtTotalItems, txtSubTotal, txtNetTotal;
    private Button confirmBtn;
    private ProcessDialog processDialog, removeProcessDialog;
    private CustomDialog confirmDialog, customProgressDialog, verifyDialog;
    private CartItemAdapter cartItemAdapter;
    private List<Cart> cartList;
    private FirebaseAuth firebaseAuth;
    private String uid;
    private FirebaseFirestore firestore;
    private double amountToPay;

    public static CartFragment getInstance() {
        if (instance == null) {
            instance = new CartFragment();
        }

        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        recyclerView = view.findViewById(R.id.cartItemRecyclerView);
        txtTotalItems = view.findViewById(R.id.cartTotalItems);
        txtSubTotal = view.findViewById(R.id.cartSubTotal);
        txtNetTotal = view.findViewById(R.id.cartNetTotal);
        confirmBtn = view.findViewById(R.id.btnConfirmOrder);
        processDialog = new ProcessDialog(getContext(), R.layout.custom_processing_dialog, R.raw.add_cart);
        removeProcessDialog = new ProcessDialog(getContext(), R.layout.custom_processing_dialog, R.raw.cart_remove);
        customProgressDialog = new CustomDialog(getContext(), R.layout.custome_progress_dialog);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        cartItemAdapter = new CartItemAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(cartItemAdapter);

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            uid = user.getUid();
            fetchDataFromFirestore(uid);
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customProgressDialog.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showVerifyMsgDialog(uid);
                    }
                }, 6000);
            }
        });
    }

    private void fetchDataFromFirestore(String uid) {
        firestore.collection("carts")
                .document(uid)
                .collection("items")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error == null) {
                            int totalItems = 0;
                            double totalBill = 0.0;
                            if (value != null) {
                                cartList = new ArrayList<>();
                                for (QueryDocumentSnapshot snapshot : value) {
                                    Cart cart = snapshot.toObject(Cart.class);
                                    totalItems += cart.getQty();
                                    totalBill += cart.getQty() * cart.getItemPrice();
                                    cartList.add(cart);
                                }
                                cartItemAdapter.setCartList(cartList);
                                amountToPay = totalBill;
                                generateBill(totalItems, totalBill);
                                confirmBtn.setEnabled(totalItems > 0);
                            }
                        }
                    }
                });
    }

    private void generateBill(int totalItems, double totalBill) {
        String bill = "Rs." + totalBill;
        txtTotalItems.setText(String.valueOf(totalItems));
        txtSubTotal.setText(bill);
        txtNetTotal.setText(bill);
    }

    @Override
    public void onIncrementClick(int position) {
        Cart cart = cartList.get(position);

        if (cart.getQty() < 10) {
            int newQty = cart.getQty() + 1;
            updateFirestoreQuantity(cart, newQty);
        } else {
            String message = "Oops! \uD83D\uDE1F You can add up to 10 servings per order for each meal. " +
                    "If you'd like more than 10 servings, please reach out to our support team. Thank you!";
            showDialog(message);
        }
    }

    @Override
    public void onDecrementClick(int position) {
        Cart cart = cartList.get(position);

        if (cart.getQty() > 1) {
            int newQty = cart.getQty() - 1;
            updateFirestoreQuantity(cart, newQty);
        } else {
            String message = "Oops! \uD83D\uDE1F Last one left! Unable to reduce quantity. Remove directly from your cart if you wish to proceed without this item.";
            showDialog(message);
        }
    }

    @Override
    public void onRemoveClick(int position) {
        Cart cart = cartList.get(position);
        String message = "Are you sure you want to remove " + cart.getItemName() + " from your cart?\uD83E\uDDD0 \nConfirm to proceed or cancel to keep it in your order.";
        showConfirmDialog(message, cart);
    }

    private void updateFirestoreQuantity(Cart cart, int newQty) {
        processDialog.show();
        firestore.collection("carts")
                .document(uid)
                .collection("items")
                .document(cart.getItemId())
                .update("qty", newQty)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        processDialog.dismiss();
                        if (task.isSuccessful()) {
                            Log.i(TAG, "updateFirestoreQuantity successful.");
                        } else {
                            Log.e(TAG, task.getException().getMessage());
                            Toast.makeText(requireContext().getApplicationContext(), "Something went wrong. Please try again later.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void showDialog(String message) {
        CustomDialog dialog = new CustomDialog(getContext(), R.layout.custom_dialog_layout);

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

    private void showConfirmDialog(String message, Cart cart) {
        confirmDialog = new CustomDialog(getContext(), R.layout.custom_confirmation_dialog_layout);

        TextView dialogMsgText = confirmDialog.findViewById(R.id.dialogConfirmMsg);
        Button okBtn = confirmDialog.findViewById(R.id.customDialogOkBtn);
        Button cancelBtn = confirmDialog.findViewById(R.id.customDialogCancelBtn);

        dialogMsgText.setText(message);

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeProductFromCart(cart);
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

    private void removeProductFromCart(Cart cart) {
        confirmDialog.dismiss();
        removeProcessDialog.show();

        firestore.collection("carts")
                .document(uid)
                .collection("items")
                .document(cart.getItemId())
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        removeProcessDialog.dismiss();
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Meal removed successfully.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getContext(), "Something went wrong. Please try again later.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void showVerifyMsgDialog(String uid) {
        verifyDialog = new CustomDialog(getContext(), R.layout.custom_verify_dialog_layout);

        TextView name = verifyDialog.findViewById(R.id.verifyName);
        TextView phone = verifyDialog.findViewById(R.id.verifyPhone);
        TextView address = verifyDialog.findViewById(R.id.verifyAddress);
        Button verifyBtn = verifyDialog.findViewById(R.id.verifyDialogVerifyBtn);
        Button cancelBtn = verifyDialog.findViewById(R.id.verifyDialogCancelBtn);

        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customProgressDialog.show();
                placeOrder(cartList);
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verifyDialog.dismiss();
            }
        });

        firestore.collection("users")
                .whereEqualTo("uuid", uid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        customProgressDialog.dismiss();
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot snapshot : task.getResult()) {
                                User user = snapshot.toObject(User.class);
                                name.setText(user.getFullName());
                                phone.setText(user.getPhone());
                                address.setText(user.getAddress());
                            }
                            verifyBtn.setEnabled(true);
                        } else {
                            Toast.makeText(getContext(), "Something went wrog. Please try again later.", Toast.LENGTH_LONG).show();
                        }
                    }
                });

        verifyDialog.show();
    }

    private void placeOrder(List<Cart> cartList) {
        verifyDialog.dismiss();
        if (cartList != null && !cartList.isEmpty()) {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    String id = String.valueOf(System.currentTimeMillis());
                    String date = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
                    double netTotal = amountToPay;
                    int totalItems = cartList.size();
                    String status = "Processing";

                    DocumentReference userOrderDocRef = firestore.collection("orders")
                            .document(uid);

                    /*make new document with user id under orders collection*/
                    userOrderDocRef.set(new HashMap<>())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {

                                    /*save a new order*/
                                    Order order = new Order(id, date, totalItems, netTotal, status);

                                    DocumentReference myordersRef = userOrderDocRef.collection("myorders")
                                            .document(id);

                                    myordersRef.set(order)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {

                                                    /*save items under this order id*/
                                                    for (Cart cart : cartList) {
                                                        String itemId = cart.getItemId();

                                                        myordersRef.collection("items")
                                                                .document(itemId)
                                                                .set(cart)
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (!task.isSuccessful()) {
                                                                            Toast.makeText(getContext(), "Something went wrong.", Toast.LENGTH_LONG).show();
                                                                        }
                                                                    }
                                                                });
                                                    }
                                                    cartCleanup();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.e(TAG, e.getMessage());
                                                }
                                            });

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(TAG, e.getMessage());
                                }
                            });
                }
            }, 3000);
        }
    }

    private void cartCleanup() {
        firestore.collection("carts")
                .document(uid)
                .collection("items")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        customProgressDialog.dismiss();
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot snapshot : task.getResult()) {
                                snapshot.getReference().delete();
                            }
                            String message = "Hooray! \uD83C\uDF89 Your order has been placed successfully. " +
                                    "Thank you for choosing us. Get ready for a delicious experience!";
                            showDialog(message);
                        } else {
                            Toast.makeText(getContext(), "Something went wrong.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}