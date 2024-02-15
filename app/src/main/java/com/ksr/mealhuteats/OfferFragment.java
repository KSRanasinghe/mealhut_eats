package com.ksr.mealhuteats;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ksr.mealhuteats.adapter.OfferAdapter;
import com.ksr.mealhuteats.model.Offer;

import java.util.ArrayList;
import java.util.List;

public class OfferFragment extends Fragment {
    public static final String TAG = OfferFragment.class.getName();
    private static OfferFragment instance;
    private RecyclerView recyclerView;
    private OfferAdapter adapter;
    private List<Offer> offerList;
    private FirebaseFirestore firestore;

    public OfferFragment() {
        // Required empty public constructor
    }

    public static OfferFragment getInstance() {
        if (instance == null) {
            instance = new OfferFragment();
        }

        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firestore = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_offer, container, false);

        recyclerView = view.findViewById(R.id.offerRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        adapter = new OfferAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        fetchOfferCard();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void fetchOfferCard() {
        firestore.collection("offers")
                .orderBy("id", Query.Direction.DESCENDING)
                .limit(8)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            offerList = new ArrayList<>();
                            for (QueryDocumentSnapshot snapshot : task.getResult()) {
                                Offer offer = snapshot.toObject(Offer.class);
                                offerList.add(offer);
                            }
                            adapter.setOfferList(offerList);
                        } else {
                            Log.e(TAG, task.getException().getMessage());
                        }
                    }
                });
    }
}