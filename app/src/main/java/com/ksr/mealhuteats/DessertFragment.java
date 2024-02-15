package com.ksr.mealhuteats;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class DessertFragment extends Fragment implements OnCardClickListener<Item> {

    public static final String TAG = DessertFragment.class.getName();
    private static DessertFragment instance;
    private RecyclerView recyclerView;
    private ItemAdapter itemAdapter;
    private String categoryId;
    private List<Item> itemList;
    private FirebaseFirestore firestore;

    public DessertFragment() {
        // Required empty public constructor
    }

    public static DessertFragment getInstance(){
        if (instance == null){
            instance = new DessertFragment();
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
        View view = inflater.inflate(R.layout.fragment_dessert, container, false);

        if (getArguments() != null) {
            categoryId = getArguments().getString("categoryId", "");
        }

        recyclerView = view.findViewById(R.id.dessertRecyclerView);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        itemAdapter = new ItemAdapter(getContext(),new ArrayList<>(),this);
        recyclerView.setAdapter(itemAdapter);

        fetchDataFromFirestore(categoryId);

        return view;
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onCardClick(Item item) {
        Intent intent = new Intent(requireContext(), SingleProductActivity.class);
        intent.putExtra("id",item.getId());
        startActivity(intent);
    }
}