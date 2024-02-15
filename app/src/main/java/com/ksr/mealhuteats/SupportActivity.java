package com.ksr.mealhuteats;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.appbar.MaterialToolbar;
import com.ksr.mealhuteats.util.NetworkChangeListener;

public class SupportActivity extends AppCompatActivity {

    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        toolbar = findViewById(R.id.supportToolbar);

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

        /*open google map*/
        findViewById(R.id.textCompanyAddress)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                        openGoogleMap();
                        Intent intent = new Intent(SupportActivity.this, MapActivity.class);
                        startActivity(intent);
                    }
                });

        /*open phone app*/
        TextView companyPhone = findViewById(R.id.textCompanyPhone);

        companyPhone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String phone = companyPhone.getText().toString();
                        String modifiedPhone = phone.replaceAll("[^0-9]", "");
                        String actualPhone = modifiedPhone.substring(2);

                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:"+actualPhone));

                        if (intent.resolveActivity(getPackageManager()) != null){
                            startActivity(intent);
                        }
                    }
                });

        /*open email app*/
        TextView companyEmail = findViewById(R.id.textCompanyEmail);

        companyEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = companyEmail.getText().toString();

                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setPackage("com.google.android.gm");

                emailIntent.setData(Uri.parse("mailto:"));
                emailIntent.putExtra(Intent.EXTRA_EMAIL,email);

                if (emailIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(Intent.createChooser(emailIntent,"Choose an application"));
                }
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

//    private void openGoogleMap(){
//        double companyLatitude = 6.901938270357422;
//        double companyLongitude = 79.87274118099094;
//
//        Uri gmmIntentUri = Uri.parse("geo:" + companyLatitude + "," + companyLongitude + "?z=15&q=" + companyLatitude + "," + companyLongitude);
//
//        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
//        mapIntent.setPackage("com.google.android.apps.maps");
//
//        if (mapIntent.resolveActivity(getPackageManager()) != null) {
//            startActivity(mapIntent);
//        }
//    }

}