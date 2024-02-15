package com.ksr.mealhuteats;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.ksr.mealhuteats.model.User;
import com.ksr.mealhuteats.util.NetworkChangeListener;
import com.ksr.mealhuteats.widget.CustomDialog;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, NavigationBarView.OnItemSelectedListener {

    public static final String TAG = MainActivity.class.getName();
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar toolbar;
    private ActionBarDrawerToggle toggle;
    private BottomNavigationView bottomNavigationView;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private FirebaseFirestore firestore;
    private FirebaseStorage firebaseStorage;
    private CircleImageView proPicIcon;
    private CustomDialog customProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.sideNavigationView);
        toolbar = findViewById(R.id.toolbar);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        proPicIcon = findViewById(R.id.proPicIcon);

        customProgressDialog = new CustomDialog(MainActivity.this, R.layout.custome_progress_dialog);

        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        // Get UID from putExtra
        user = firebaseAuth.getCurrentUser();
        if (user != null) {
            setDetailsToSideDrawer(user);
            checkProfileCondition(user.getUid());
        }

        toggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.open();
            }
        });

        navigationView.setNavigationItemSelectedListener(this);
        bottomNavigationView.setOnItemSelectedListener(this);

        // Initialize and set the initial fragment
        if (savedInstanceState == null) {
            loadFragment(HomeFragment.getInstance());
            navigationView.setCheckedItem(R.id.sideNavHome);
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (bottomNavigationView.getSelectedItemId() != R.id.bottomNavHome) {
                    bottomNavigationView.setSelectedItemId(R.id.bottomNavHome);
                }
            }
        });

    }

    @Override
    protected void onStart() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener, filter);
        super.onStart();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(networkChangeListener);
        super.onStop();
    }

    private void setDetailsToSideDrawer(FirebaseUser user) {
        if (user != null) {
            TextView textUser = navigationView.getHeaderView(0).findViewById(R.id.txtUser);
            TextView textAsUsername = navigationView.getHeaderView(0).findViewById(R.id.txtAsUsername);
            CircleImageView proPic = navigationView.getHeaderView(0).findViewById(R.id.sideDrawerProPic);

            if (user.getEmail() != null) {
                textAsUsername.setText(user.getEmail());
            } else if (user.getPhoneNumber() != null) {
                textAsUsername.setText(user.getPhoneNumber());
            }

            firestore.collection("users")
                    .whereEqualTo("uuid", user.getUid())
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            if (error == null && value != null) {
                                for (QueryDocumentSnapshot snapshot : value) {
                                    User currentUser = snapshot.toObject(User.class);
                                    if (currentUser.getFullName() != null) {
                                        textUser.setText(currentUser.getFullName());
                                        firebaseStorage.getReference("user-images/" + currentUser.getImage())
                                                .getDownloadUrl()
                                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        Picasso.get().load(uri).fit().centerCrop().into(proPic);
                                                    }
                                                });
                                    }
                                }

                            } else {
                                Log.e(TAG, error.getMessage());
                            }
                        }
                    });
        }
    }

    private void checkProfileCondition(String uuid) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                firestore.collection("users")
                        .whereEqualTo("uuid", uuid)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot snapshot : task.getResult()) {
                                        User user = snapshot.toObject(User.class);
                                        if (user.getFullName() == null) {
                                            String message = "Looks like your profile isn't set up yet. \uD83E\uDDD0 Let's make it uniquely yours!";
                                            showMessageDialog(message, true);
                                        }

                                        if (user.getImage() != null) {
                                            firebaseStorage.getReference("user-images/" + user.getImage())
                                                    .getDownloadUrl()
                                                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                        @Override
                                                        public void onSuccess(Uri uri) {
                                                            Picasso.get().load(uri).fit().into(proPicIcon);
                                                        }
                                                    });
                                        }
                                    }
                                }
                            }
                        });
            }
        }, 5000);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.bottomNavHome) {
            loadFragment(HomeFragment.getInstance());
        } else if (item.getItemId() == R.id.bottomNavOffers) {
            loadFragment(OfferFragment.getInstance());
        } else if (item.getItemId() == R.id.bottomNavCart) {
            if (user != null) {
                firestore.collection("carts")
                        .document(user.getUid())
                        .collection("items")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    QuerySnapshot result = task.getResult();
                                    if (result.size() != 0) {
                                        loadFragment(CartFragment.getInstance());
                                    } else {
                                        loadFragment(EmptyCartFragment.getInstance());
                                    }
                                }
                            }
                        });
            }
        } else if (item.getItemId() == R.id.bottomNavProfile) {
            loadFragment(ProfileFragment.getInstance());
        } else if (item.getItemId() == R.id.sideNavHome) {
            drawerLayout.close();
            loadFragment(HomeFragment.getInstance());
            bottomNavigationView.setSelectedItemId(R.id.bottomNavHome);
        } else if (item.getItemId() == R.id.sideNavOffers) {
            drawerLayout.close();
            loadFragment(OfferFragment.getInstance());
            bottomNavigationView.setSelectedItemId(R.id.bottomNavOffers);
        } else if (item.getItemId() == R.id.sideNavOrderHistory) {
            drawerLayout.close();
            Intent intent = new Intent(MainActivity.this, OrderHistoryActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.sideNavProfile) {
            drawerLayout.close();
            loadFragment(ProfileFragment.getInstance());
            bottomNavigationView.setSelectedItemId(R.id.bottomNavProfile);
        } else if (item.getItemId() == R.id.sideNavSupport) {
            drawerLayout.close();
            Intent intent = new Intent(MainActivity.this, SupportActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.sideNavLogout) {
            drawerLayout.close();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    showConfirmMsg();
                }
            }, 200);
        }

        return true;
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainer, fragment);
        fragmentTransaction.commit();
        updateToolbar(fragment);
    }

    private void updateToolbar(Fragment fragment) {

        ImageView leftImage = findViewById(R.id.proPicIcon);

        if (fragment instanceof ProfileFragment) {
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_green));
            leftImage.setVisibility(View.INVISIBLE);
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.primary_green));
            getWindow().getDecorView().setSystemUiVisibility(0);

            // Change the color of the menu navigation icon
            Drawable drawable = toggle.getDrawerArrowDrawable().mutate();
            drawable.setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_ATOP);
        } else {
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
            leftImage.setVisibility(View.VISIBLE);
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

            // Change the color of the menu navigation icon to the default color
            Drawable drawable = toggle.getDrawerArrowDrawable().mutate();
            drawable.setColorFilter(null);
        }
    }

    private void showMessageDialog(String message, boolean isEmptyProfile) {
        CustomDialog dialog = new CustomDialog(MainActivity.this, R.layout.custom_dialog_layout);

        TextView dialogMsgText = dialog.findViewById(R.id.dialogMsg);
        Button dialogBtn = dialog.findViewById(R.id.customDialogBtn);

        dialogMsgText.setText(message);

        dialogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if (isEmptyProfile) {
                    Intent intent = new Intent(MainActivity.this, EditProfileActivity.class);
                    startActivity(intent);
                }
            }
        });

        dialog.show();
    }

    private void showConfirmMsg() {
        CustomDialog confirmDialog = new CustomDialog(MainActivity.this, R.layout.custom_confirmation_dialog_layout);

        TextView dialogMsgText = confirmDialog.findViewById(R.id.dialogConfirmMsg);
        Button okBtn = confirmDialog.findViewById(R.id.customDialogOkBtn);
        Button cancelBtn = confirmDialog.findViewById(R.id.customDialogCancelBtn);

        String message = "Are you sure you want to sign out?\uD83E\uDDD0" +
                "\nConfirm to log out or cancel to stay connected." +
                "\nSee you again soon!";
        dialogMsgText.setText(message);

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmDialog.dismiss();
                customProgressDialog.show();
                clearSharedPreferences();
                navigateToWelcomeActivity();
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

    private void clearSharedPreferences() {
        final String PREF_NAME = getPackageName() + "_preferences";

        SharedPreferences preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }

    private void navigateToWelcomeActivity() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                customProgressDialog.dismiss();
                Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        }, 2500);
    }
}