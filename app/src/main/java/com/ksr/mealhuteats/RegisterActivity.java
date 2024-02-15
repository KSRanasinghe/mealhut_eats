package com.ksr.mealhuteats;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ksr.mealhuteats.model.User;
import com.ksr.mealhuteats.util.NetworkChangeListener;
import com.ksr.mealhuteats.util.UsernameValidation;
import com.ksr.mealhuteats.widget.CustomDialog;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RegisterActivity extends AppCompatActivity {

    public static final String TAG = RegisterActivity.class.getName();
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    private static final int EMAIL = 1;
    private static final int PHONE = 2;
    private Button signupBtn;
    private TextView signUpLink;
    private CustomDialog customProgressDialog;
    private CustomDialog customInputDialog;
    private EditText otpEditText;
    private PhoneAuthProvider.ForceResendingToken resendingToken;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private String mVerificationId;
    private String prefPhone;
    private String prefPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        customProgressDialog = new CustomDialog(RegisterActivity.this, R.layout.custome_progress_dialog);
        customInputDialog = new CustomDialog(RegisterActivity.this, R.layout.custom_input_dialog_layout);
        Button verifyBtn = customInputDialog.findViewById(R.id.customDialogVerifyBtn);
        Button cancelBtn = customInputDialog.findViewById(R.id.customInputDialogCancelBtn);
        otpEditText = customInputDialog.findViewById(R.id.otpEditText);

        signupBtn = findViewById(R.id.btnSignUp);
        signUpLink = findViewById(R.id.textViewSigninLink);

        signUpLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });

        /*register button programing*/
        EditText emailOrPhoneText = findViewById(R.id.editTextRegisterEmailPhone);
        EditText passwordText = findViewById(R.id.editTextRegisterPassword);
        EditText passwordConfirmText = findViewById(R.id.editTextPasswordConfirm);

        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailOrPhone = emailOrPhoneText.getText().toString();
                String password = passwordText.getText().toString();
                String passwordConfirm = passwordConfirmText.getText().toString();

                switch (UsernameValidation.validate(emailOrPhone)) {
                    case EMAIL:
                        userRegistrationByEmail(emailOrPhone, password, passwordConfirm);
                        break;
                    case PHONE:
                        userRegistrationByPhone(emailOrPhone, password, passwordConfirm);
                        break;
                    default:
                        Toast.makeText(getApplicationContext(), "Please enter a valid email address or phone number.", Toast.LENGTH_LONG).show();
                        break;
                }

            }
        });

        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String otp = otpEditText.getText().toString();
                if (!otp.isEmpty()) {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otp);
                    customInputDialog.dismiss();
                    customProgressDialog.show();

                    firebaseAuth.signInWithCredential(credential)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseUser user = firebaseAuth.getCurrentUser();

                                        if (user != null) {
                                            String uid = user.getUid();
                                            createUserInFirestore(uid, prefPhone);

                                            Map<String,Object> signInMap = new HashMap<>();
                                            signInMap.put("uid",uid);
                                            signInMap.put("username",prefPhone);
                                            signInMap.put("password",prefPassword);

                                            String message = "Congratulations! \uD83C\uDF89 Registration complete. Enjoy your delicious meals!";
                                            showDialog(message, signInMap);
                                        }
                                    } else {
                                        String message = "Oops! \uD83D\uDE1F Registration failed. " +
                                                "Double-check your information and try again. If the issue persists, contact support for assistance. Thank you!";
                                        showDialog(message,null);
                                    }
                                }
                            });
                } else {
                    Toast.makeText(RegisterActivity.this, "Please enter the OTP received on your phone to proceed.", Toast.LENGTH_LONG).show();
                }
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customInputDialog.dismiss();
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

    private void userRegistrationByEmail(String email, String password, String passwordConfirm) {

        View currentFocus = getCurrentFocus();
        if (currentFocus != null) {
            currentFocus.clearFocus();
        }

        customProgressDialog.show();

        if (!password.isEmpty()) {
            if (password.equals(passwordConfirm)) {
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()) {

                                    FirebaseUser user = firebaseAuth.getCurrentUser();
                                    if (user != null) {
                                        user.sendEmailVerification();
                                        String uid = user.getUid();
                                        createUserInFirestore(uid, email);

                                        Map<String,Object> signInMap = new HashMap<>();
                                        signInMap.put("uid",uid);
                                        signInMap.put("username",email);
                                        signInMap.put("password",password);

                                        String message = "Congratulations! \uD83C\uDF89 Registration complete. " +
                                                "Please verify your email for exclusive offers. Enjoy your delicious meals!";
                                        showDialog(message, signInMap);
                                    }
                                } else {
                                    String message = "Oops! \uD83D\uDE1F Registration failed. " +
                                            "Double-check your information and try again. If the issue persists, contact support for assistance. Thank you!";
                                    showDialog(message,null);
                                }
                            }
                        });
            }
        } else {
            customProgressDialog.dismiss();
            Toast.makeText(getApplicationContext(), "Please fill mandatory fields.", Toast.LENGTH_LONG).show();
        }
    }

    private void userRegistrationByPhone(String phone, String password, String passwordConfirm) {
        View currentFocus = getCurrentFocus();
        if (currentFocus != null) {
            currentFocus.clearFocus();
        }

        customProgressDialog.show();

        if (!password.isEmpty()) {
            if (password.equals(passwordConfirm)) {

                /*otp sending process*/
                PhoneAuthProvider.verifyPhoneNumber(
                        PhoneAuthOptions.newBuilder(firebaseAuth)
                                .setPhoneNumber("+94" + phone)
                                .setTimeout(60L, TimeUnit.SECONDS)
                                .setActivity(RegisterActivity.this)
                                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                    @Override
                                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                        Log.i(TAG, "onVerificationCompleted : " + phoneAuthCredential);
                                    }

                                    @Override
                                    public void onVerificationFailed(@NonNull FirebaseException e) {
                                        Log.i(TAG, "onVerificationFailed : " + e.getMessage());
                                        customProgressDialog.dismiss();
                                        String message = "Oops! \uD83D\uDE1F Registration failed. " +
                                                "Double-check your information and try again. If the issue persists, contact support for assistance. Thank you!";
                                        showDialog(message,null);
                                    }

                                    @Override
                                    public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                        Log.i(TAG, "onCodeSent : " + verificationId);

                                        /*when code sent successfully*/
                                        customProgressDialog.dismiss();
                                        customInputDialog.show();

                                        prefPhone = phone;
                                        prefPassword = password;
                                        mVerificationId = verificationId;
                                        resendingToken = forceResendingToken;
                                    }
                                }).build()
                );
            }
        } else {
            customProgressDialog.dismiss();
            Toast.makeText(getApplicationContext(), "Please fill mandatory fields.", Toast.LENGTH_LONG).show();
        }
    }

    private void createUserInFirestore(String uid, String asUsername) {
        User user = new User();
        user.setUuid(uid);

        switch (UsernameValidation.validate(asUsername)) {
            case EMAIL:
                user.setEmail(asUsername);
                break;
            case PHONE:
                user.setPhone(asUsername);
                break;
        }

        /*create profile sub collection inside of user and insert data*/
        firestore.collection("users")
                .add(user)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        customProgressDialog.dismiss();
                        if (task.isSuccessful()) {
                            Log.i(TAG, "createUserInFirestore : " + task.isSuccessful());
                        } else {
                            Log.i(TAG, "createUserInFirestore : " + task.isSuccessful());
                            String message = "Oops! \uD83D\uDE1F Registration failed. " +
                                    "Double-check your information and try again. If the issue persists, contact support for assistance. Thank you!";
                            showDialog(message,null);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                });
    }

    private void showDialog(String message, Map<String,Object> signIn) {
        CustomDialog dialog = new CustomDialog(RegisterActivity.this, R.layout.custom_dialog_layout);

        TextView dialogMsgText = dialog.findViewById(R.id.dialogMsg);
        Button dialogBtn = dialog.findViewById(R.id.customDialogBtn);

        dialogMsgText.setText(message);

        dialogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if (signIn !=null){
                    String uid = String.valueOf(signIn.get("uid"));
                    String username = String.valueOf(signIn.get("username"));
                    String password = String.valueOf(signIn.get("password"));

                    signupBtn.setVisibility(View.GONE);
                    signUpLink.setText(R.string.after_signup_text);
                    clearEditTextFields();
                    autoSignIn(uid,username,password);
                }
            }
        });

        dialog.show();
    }

    private void autoSignIn(String uid, String asUsername, String password) {
        setSharedPreferences(uid, asUsername, password);
        updateUI(uid);
    }

    private void setSharedPreferences(String uid, String username, String password) {
        final String PREF_NAME = getPackageName()+"_preferences";

        SharedPreferences preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("uid", uid);
        editor.putString("username", username);
        editor.putString("password", password);
        editor.apply();
    }

    private void updateUI(String uid) {
        customProgressDialog.dismiss();
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("uid", uid);
        startActivity(intent);
        finish();
    }

    private void clearEditTextFields() {
        EditText emailOrPhoneText = findViewById(R.id.editTextRegisterEmailPhone);
        EditText passwordText = findViewById(R.id.editTextRegisterPassword);
        EditText passwordConfirmText = findViewById(R.id.editTextPasswordConfirm);

        emailOrPhoneText.getText().clear();
        passwordText.getText().clear();
        passwordConfirmText.getText().clear();

        emailOrPhoneText.setFocusable(false);
        passwordText.setFocusable(false);
        passwordConfirmText.setFocusable(false);

        emailOrPhoneText.setCursorVisible(false);
        passwordText.setCursorVisible(false);
        passwordConfirmText.setCursorVisible(false);
    }
}