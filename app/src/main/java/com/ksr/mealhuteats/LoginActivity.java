package com.ksr.mealhuteats;

import android.app.PendingIntent;
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

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.identity.GetSignInIntentRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.ksr.mealhuteats.model.User;
import com.ksr.mealhuteats.util.NetworkChangeListener;
import com.ksr.mealhuteats.util.UsernameValidation;
import com.ksr.mealhuteats.widget.CustomDialog;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    public static final String TAG = LoginActivity.class.getName();
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    private static final int EMAIL = 1;
    private static final int PHONE = 2;
    private CustomDialog customProgressDialog;
    private CustomDialog customInputDialog;
    private EditText otpEditText;
    private FirebaseAuth firebaseAuth;
    private SignInClient signInClient;
    private FirebaseFirestore firestore;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken resendingToken;
    private String prefPhone;
    private String prefPassword;
    private Intent homeIntent;
    private Button googleSignInBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();
        signInClient = Identity.getSignInClient(getApplicationContext());
        firestore = FirebaseFirestore.getInstance();
        customProgressDialog = new CustomDialog(LoginActivity.this, R.layout.custome_progress_dialog);
        customInputDialog = new CustomDialog(LoginActivity.this, R.layout.custom_input_dialog_layout);
        Button verifyBtn = customInputDialog.findViewById(R.id.customDialogVerifyBtn);
        Button cancelBtn = customInputDialog.findViewById(R.id.customInputDialogCancelBtn);
        otpEditText = customInputDialog.findViewById(R.id.otpEditText);
        homeIntent = new Intent(LoginActivity.this, MainActivity.class);

        findViewById(R.id.textViewSignupLink).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });

        /*login button programming*/
        EditText emailOrPhoneText = findViewById(R.id.editTextEmailPhone);
        EditText passwordText = findViewById(R.id.editTextPassword);

        findViewById(R.id.btnLogin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailOrPhone = emailOrPhoneText.getText().toString();
                String password = passwordText.getText().toString();

                switch (UsernameValidation.validate(emailOrPhone)) {
                    case EMAIL:
                        userLoginByEmail(emailOrPhone, password);
                        break;
                    case PHONE:
                        userLoginByPhone(emailOrPhone, password);
                        break;
                    default:
                        Toast.makeText(getApplicationContext(), "Please enter a valid email address or phone number.", Toast.LENGTH_LONG).show();
                        break;
                }
            }
        });

        /*OTP verification*/
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
                                            setSharedPreferences(uid, prefPhone, prefPassword);
                                            customProgressDialog.dismiss();
                                            updateUI(uid);
                                        }
                                    } else {
                                        customProgressDialog.dismiss();
                                        String message = "Oops! \uD83D\uDE1F Sign in failed. " +
                                                "Double-check your information and try again. If the issue persists, contact support for assistance. Thank you!";
                                        showDialog(message);
                                    }
                                }
                            });
                } else {
                    Toast.makeText(LoginActivity.this, "Please enter the OTP received on your phone to proceed.", Toast.LENGTH_LONG).show();
                }
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customInputDialog.dismiss();
            }
        });

        /*Google sign in*/
        googleSignInBtn = findViewById(R.id.btnGoogleSignIn);

        googleSignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GetSignInIntentRequest signInIntentRequest = GetSignInIntentRequest.builder()
                        .setServerClientId(getString(R.string.web_client_id)).build();
                Task<PendingIntent> signInIntent = signInClient.getSignInIntent(signInIntentRequest);
                signInIntent.addOnSuccessListener(new OnSuccessListener<PendingIntent>() {
                    @Override
                    public void onSuccess(PendingIntent pendingIntent) {
                        IntentSenderRequest intentSenderRequest =
                                new IntentSenderRequest.Builder(pendingIntent).build();
                        signInLauncher.launch(intentSenderRequest);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
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

    private void userLoginByEmail(String email, String password) {
        View currentFocus = getCurrentFocus();
        if (currentFocus != null) {
            currentFocus.clearFocus();
        }

        customProgressDialog.show();

        if (!password.isEmpty()) {
            /*sign in*/
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = firebaseAuth.getCurrentUser();
                                if (user != null) {
                                    if (user.isEmailVerified()) {
                                        String uid = user.getUid();
                                        setSharedPreferences(uid, email, password);
                                        customProgressDialog.dismiss();
                                        updateUI(uid);
                                    } else {
                                        customProgressDialog.dismiss();
                                        Toast.makeText(LoginActivity.this, "Please verify your email address.", Toast.LENGTH_LONG).show();
                                    }
                                }
                            } else {
                                customProgressDialog.dismiss();
                                Exception exception = task.getException();
                                String message;
                                if (exception instanceof FirebaseAuthInvalidUserException) {
                                    message = "Oops! \uD83D\uDE1F It looks like this email address isn't registered. " +
                                            "Double-check your information and try again. If the issue persists, contact support for assistance. Thank you!";
                                } else {
                                    message = "Oops! \uD83D\uDE1F Sign in failed. Double-check your information and try again. " +
                                            "If the issue persists, contact support for assistance. Thank you!";
                                }
                                showDialog(message);
                            }
                        }
                    });

        } else {
            Toast.makeText(getApplicationContext(), "Please fill mandatory fields.", Toast.LENGTH_LONG).show();
        }
    }

    private void userLoginByPhone(String phone, String password) {
        View currentFocus = getCurrentFocus();
        if (currentFocus != null) {
            currentFocus.clearFocus();
        }

        customProgressDialog.show();

        if (!password.isEmpty()) {
            firestore.collection("users")
                    .whereEqualTo("phone", phone)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                if (!task.getResult().isEmpty()) {
                                    Log.i(TAG, "Phone number is existed.");
                                    sendOtp(phone, password);
                                } else {
                                    customProgressDialog.dismiss();
                                    String message = "Oops! \uD83D\uDE1F It looks like this phone number isn't registered. " +
                                            "Double-check your information and try again. If the issue persists, contact support for assistance. Thank you!";
                                    showDialog(message);
                                }
                            } else {
                                Log.e(TAG, "Error checking phone number: " + task.getException());
                            }
                        }
                    });
        } else {
            customProgressDialog.dismiss();
            Toast.makeText(getApplicationContext(), "Please fill mandatory fields.", Toast.LENGTH_LONG).show();
        }
    }

    private void sendOtp(String phone, String password) {
        PhoneAuthProvider.verifyPhoneNumber(
                PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber("+94" + phone)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(LoginActivity.this)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                Log.i(TAG, "onVerificationCompleted : " + phoneAuthCredential);
                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                Log.i(TAG, "onVerificationFailed : " + e.getMessage());
                                customProgressDialog.dismiss();
                                String message = "Oops! \uD83D\uDE1F Sign in failed. " +
                                        "Double-check your information and try again. If the issue persists, contact support for assistance. Thank you!";
                                showDialog(message);
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

    /*sign in with google*/
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential authCredential = GoogleAuthProvider.getCredential(idToken, null);
        Task<AuthResult> authResultTask = firebaseAuth.signInWithCredential(authCredential);
        authResultTask.addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                        checkFirestoreUsers(user);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void handleSignInResult(Intent intent) {
        try {
            SignInCredential signInCredential = signInClient.getSignInCredentialFromIntent(intent);
            String idToken = signInCredential.getGoogleIdToken();
            firebaseAuthWithGoogle(idToken);
        } catch (ApiException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private final ActivityResultLauncher<IntentSenderRequest> signInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult o) {
                            handleSignInResult(o.getData());
                        }
                    });

    private void checkFirestoreUsers(FirebaseUser user) {
        googleSignInBtn.setEnabled(false);
        customProgressDialog.show();

        firestore.collection("users")
                .whereEqualTo("uuid", user.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().isEmpty()) {
                                createNewUserInFirestore(user);
                            }
                            setSharedPreferences(user.getUid(), user.getEmail(), null);
                            customProgressDialog.dismiss();
                            updateUI(user.getUid());
                        }
                    }
                });
    }

    private void createNewUserInFirestore(FirebaseUser currentUser) {
        User user = new User();
        user.setUuid(currentUser.getUid());
        user.setEmail(currentUser.getEmail());

        firestore.collection("users")
                .add(user)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (!task.isSuccessful()) {
                            googleSignInBtn.setEnabled(true);
                            customProgressDialog.dismiss();
                            String message = "Oops! \uD83D\uDE1F Sign in failed. " +
                                    "Double-check your information and try again. If the issue persists, " +
                                    "contact support for assistance. Thank you!";
                            showDialog(message);
                        }
                    }
                });
    }

    /*sign in with google*/

    private void setSharedPreferences(String uid, String username, String password) {
        final String PREF_NAME = getPackageName() + "_preferences";

        SharedPreferences preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("uid", uid);
        editor.putString("username", username);
        editor.putString("password", password);
        editor.apply();
    }

    private void showDialog(String message) {
        CustomDialog dialog = new CustomDialog(LoginActivity.this, R.layout.custom_dialog_layout);

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

    private void updateUI(String uid) {
        EditText emailOrPhoneText = findViewById(R.id.editTextEmailPhone);
        EditText passwordText = findViewById(R.id.editTextPassword);
        Button loginBtn = findViewById(R.id.btnLogin);

        emailOrPhoneText.getText().clear();
        passwordText.getText().clear();
        loginBtn.setEnabled(false);

        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        homeIntent.putExtra("uid", uid);
        startActivity(homeIntent);
        finish();
    }
}