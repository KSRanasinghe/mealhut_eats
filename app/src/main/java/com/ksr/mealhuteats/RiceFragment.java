package com.ksr.mealhuteats;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ksr.mealhuteats.adapter.ItemAdapter;
import com.ksr.mealhuteats.model.Item;
import com.ksr.mealhuteats.util.OnCardClickListener;

import java.util.ArrayList;
import java.util.List;

public class RiceFragment extends Fragment implements OnCardClickListener<Item> {

    public static final String TAG = RiceFragment.class.getName();
    private static RiceFragment instance;
    private RecyclerView recyclerView;
    private ItemAdapter itemAdapter;
    private String categoryId;
    private List<Item> itemList;
    private FirebaseFirestore firestore;

    public RiceFragment() {
        // Required empty public constructor
    }

    public static RiceFragment getInstance() {
        if (instance == null) {
            instance = new RiceFragment();
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
        View view = inflater.inflate(R.layout.fragment_rice, container, false);

        if (getArguments() != null) {
            categoryId = getArguments().getString("categoryId", "");
        }

        recyclerView = view.findViewById(R.id.riceRecyclerView);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        itemAdapter = new ItemAdapter(getContext(),new ArrayList<>(), this);
        recyclerView.setAdapter(itemAdapter);

        fetchDataFromFirestore(categoryId);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void fetchDataFromFirestore(String categoryId) {
        firestore.collection("products")
                .whereEqualTo("categoryId", categoryId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error == null && value != null) {
                            itemList = new ArrayList<>();
                            for (QueryDocumentSnapshot snapshot : value) {
                                Item item = snapshot.toObject(Item.class);
                                itemList.add(item);
                            }
                            itemAdapter.setItemList(itemList);
                        } else {
                            Log.e(TAG,error.getMessage());
                        }
                    }
                });
    }

    @Override
    public void onCardClick(Item item) {
        Intent intent = new Intent(requireContext(), SingleProductActivity.class);
        intent.putExtra("id",item.getId());
        startActivity(intent);
    }
}