package com.ksr.mealhuteats;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.ksr.mealhuteats.model.User;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    public static final String TAG = ProfileFragment.class.getName();
    private static ProfileFragment instance;
    private TextView name, email, phone, address;
    private CircleImageView dpPreview;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private FirebaseStorage firebaseStorage;

    public ProfileFragment() {
    }

    public static ProfileFragment getInstance() {
        if (instance == null) {
            instance = new ProfileFragment();
        }

        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        name = view.findViewById(R.id.profileYourName);
        email = view.findViewById(R.id.profileYourEmail);
        phone = view.findViewById(R.id.profileYourPhone);
        address = view.findViewById(R.id.profileYourAddress);
        dpPreview = view.findViewById(R.id.circleImageView);

        view.findViewById(R.id.btnEditProfile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(requireActivity(), EditProfileActivity.class);
                startActivity(intent);
            }
        });

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            checkProfileCondition(user.getUid());
        }
    }

    private void checkProfileCondition(String uuid) {
        firestore.collection("users")
                .whereEqualTo("uuid", uuid)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error == null && value != null) {
                            for (QueryDocumentSnapshot snapshot : value) {
                                User user = snapshot.toObject(User.class);

                                if (user.getFullName() != null) {
                                    name.setText(user.getFullName());
                                    email.setText(user.getEmail());
                                    phone.setText(user.getPhone());
                                    address.setText(user.getAddress());

                                    if (user.getImage() != null) {
                                        firebaseStorage.getReference("user-images/" + user.getImage())
                                                .getDownloadUrl()
                                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        Picasso.get().load(uri).fit().into(dpPreview);
                                                    }
                                                });
                                    }
                                } else if (user.getEmail() != null) {
                                    email.setText(user.getEmail());
                                } else if (user.getPhone() != null) {
                                    phone.setText(user.getPhone());
                                }
                            }
                        }
                    }
                });
    }
}