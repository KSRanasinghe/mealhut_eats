package com.ksr.mealhuteats;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ksr.mealhuteats.util.NetworkChangeListener;
import com.ksr.mealhuteats.util.UsernameValidation;
import com.ksr.mealhuteats.widget.CustomDialog;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    public static final String TAG = EditProfileActivity.class.getName();
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    private MaterialToolbar toolbar;
    private EditText editName, editEmail, editPhone, editAddress;
    private CircleImageView dpPreview;
    private CustomDialog customProgressDialog;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private FirebaseStorage firebaseStorage;
    private Uri imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        toolbar = findViewById(R.id.editProfileToolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        customProgressDialog = new CustomDialog(EditProfileActivity.this, R.layout.custome_progress_dialog);
        editName = findViewById(R.id.editFullName);
        editEmail = findViewById(R.id.editEmail);
        editPhone = findViewById(R.id.editPhone);
        editAddress = findViewById(R.id.editBillingAddress);
        dpPreview = findViewById(R.id.circleImagePreview);

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

        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user != null) {

            if (user.getEmail() != null) {
                editEmail.setText(user.getEmail());
                editEmail.setFocusable(false);
                editEmail.setCursorVisible(false);
            } else if (user.getPhoneNumber() != null) {
                String phoneNumber = user.getPhoneNumber();
                String formattedPhone = "0" + phoneNumber.substring(3);
                editPhone.setText(formattedPhone);
                editPhone.setFocusable(false);
                editPhone.setCursorVisible(false);
            }

            checkProfileCondition(user.getUid());
        }

        /*btn save changes process*/
        findViewById(R.id.btnSaveChanges).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = editName.getText().toString();
                String email = editEmail.getText().toString();
                String phone = editPhone.getText().toString();
                String address = editAddress.getText().toString();
                String image = UUID.randomUUID().toString();

                if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please fill all mandatory fields.", Toast.LENGTH_LONG).show();
                } else if (!UsernameValidation.isValidEmail(email)) {
                    Toast.makeText(getApplicationContext(), "Please enter valid email address.", Toast.LENGTH_LONG).show();
                } else if (!UsernameValidation.isValidPhone(phone)) {
                    Toast.makeText(getApplicationContext(), "Please enter valid phone number.", Toast.LENGTH_LONG).show();
                } else {
                    Map<String, Object> newUser = new HashMap<>();
                    newUser.put("fullName", name);
                    newUser.put("email", email);
                    newUser.put("phone", phone);
                    newUser.put("address", address);

                    if (imagePath != null) {
                        newUser.put("image", image);
                    }

                    saveChanges(newUser, user.getUid());
                }
            }
        });

        /*select image process*/
        ImageButton imageButton = findViewById(R.id.btnSelectImage);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);

                activityResultLauncher.launch(Intent.createChooser(intent, "Select Image"));
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

    private void checkProfileCondition(String uuid) {
        firestore.collection("users")
                .whereEqualTo("uuid", uuid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot snapshot : task.getResult()) {
                                String fullName = snapshot.getString("fullName");
                                if (fullName != null) {
                                    editName.setText(snapshot.getString("fullName"));
                                    editEmail.setText(snapshot.getString("email"));
                                    editPhone.setText(snapshot.getString("phone"));
                                    editAddress.setText(snapshot.getString("address"));

                                    String imageId = snapshot.getString("image");
                                    if (imageId != null) {
                                        firebaseStorage.getReference("user-images/" + imageId)
                                                .getDownloadUrl()
                                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        Picasso.get().load(uri).fit().into(dpPreview);
                                                    }
                                                });
                                    }
                                }
                            }

                        } else {
                            Exception exception = task.getException();
                            if (exception != null) {
                                Log.e(TAG, exception.getMessage());
                            }
                        }
                    }
                });
    }

    /*helper method for select image process*/
    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        imagePath = result.getData().getData();
                        Log.i(TAG, "image path: " + imagePath.getPath());

                        Picasso.get().load(imagePath).fit().into(dpPreview);
                    }
                }
            });

    private void saveChanges(Map<String, Object> user, String uuid) {
        View currentFocus = getCurrentFocus();
        if (currentFocus != null) {
            currentFocus.clearFocus();
        }

        customProgressDialog.show();
        firestore.collection("users")
                .whereEqualTo("uuid", uuid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                DocumentReference reference = document.getReference();
                                reference.update(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Log.i(TAG, "User successfully updated.");

                                                /*upload user image*/
                                                if (imagePath != null) {
                                                    String imageId = String.valueOf(user.get("image"));
                                                    uploadUserImage(imageId);
                                                } else {
                                                    customProgressDialog.dismiss();
                                                    Toast.makeText(EditProfileActivity.this, "Profile has been updated successfully.", Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.e(TAG, "User update failed: " + e.getMessage());
                                                customProgressDialog.dismiss();
                                                String message = "Oops! \uD83D\uDE1F Something went wrong while updating your profile. " +
                                                        "Please try again later. If the issue persists, contact support. Thank you!";
                                                showMessageDialog(message);
                                            }
                                        });
                            }
                        }
                    }
                });
    }

    private void uploadUserImage(String imageId) {
        StorageReference reference = firebaseStorage
                .getReference("user-images").child(imageId);


        reference.putFile(imagePath)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        customProgressDialog.dismiss();
                        Toast.makeText(EditProfileActivity.this, "Profile has been updated successfully.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Profile image upload failed: " + e.getMessage());
                        customProgressDialog.dismiss();
                        String message = "Oops! \uD83D\uDE1F Something went wrong while updating your profile image. " +
                                "Please try again later. If the issue persists, contact support. Thank you!";
                        showMessageDialog(message);
                    }
                });
    }

    private void showMessageDialog(String message) {
        CustomDialog dialog = new CustomDialog(EditProfileActivity.this, R.layout.custom_dialog_layout);

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