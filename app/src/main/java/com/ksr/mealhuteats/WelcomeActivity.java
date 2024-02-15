package com.ksr.mealhuteats;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.ksr.mealhuteats.util.NetworkChangeListener;

public class WelcomeActivity extends AppCompatActivity {

    public static final String TAG = WelcomeActivity.class.getName();

    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean userAuthenticated = isUserAuthenticated();

        if (userAuthenticated) {
            navigateToHome();
        } else {
            setContentView(R.layout.activity_welcome);

            /*set full screen*/
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                getWindow().setStatusBarColor(Color.BLACK);
            } else {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }

            /*off night mode for the app*/
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

            findViewById(R.id.btnGetStarted).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        }
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

    private void navigateToHome() {
        final String PREF_NAME = getPackageName() + "_preferences";
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String uid = preferences.getString("uid", "");
        Log.i(TAG, uid);
        Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
        intent.putExtra("uid", uid);
        startActivity(intent);
        finish();
    }

    private boolean isUserAuthenticated() {
        final String PREF_NAME = getPackageName() + "_preferences";
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.contains("uid");
    }

    //    private void showNoInternetDialog() {
//
//        CustomDialog dialog = new CustomDialog(WelcomeActivity.this, R.layout.custom_dialog_layout);
//        String message = getString(R.string.wifi_alert_msg);
//
//        TextView dialogMsgText = dialog.findViewById(R.id.dialogMsg);
//        Button dialogBtn = dialog.findViewById(R.id.customDialogBtn);
//
//        dialogMsgText.setText(message);
//
//        dialogBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                dialog.dismiss();
//            }
//        });
//
//        dialog.show();
//    }

//    private boolean isInternetAvailable() {
//        ConnectivityManager connectivityManager =
//                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//
//        if (connectivityManager != null) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
//                return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
//            } else {
//                // For devices below M, we use the deprecated method
//                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
//                return activeNetworkInfo != null && activeNetworkInfo.isConnected();
//            }
//        }
//        return false;
//    }
}