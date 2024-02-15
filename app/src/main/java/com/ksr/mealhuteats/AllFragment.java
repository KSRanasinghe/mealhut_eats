package com.ksr.mealhuteats;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ksr.mealhuteats.adapter.ItemAdapter;
import com.ksr.mealhuteats.adapter.OfferAdapter;
import com.ksr.mealhuteats.adapter.PromoAdapter;
import com.ksr.mealhuteats.model.Item;
import com.ksr.mealhuteats.model.Offer;
import com.ksr.mealhuteats.util.OnCardClickListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AllFragment extends Fragment implements OnCardClickListener<Item> {

    public static final String TAG = AllFragment.class.getName();
    private static AllFragment instance;
    private RecyclerView popularItemRecyclerView, promotionRecyclerView;
    private PromoAdapter promoAdapter;
    private ItemAdapter itemAdapter;
    private List<Offer> offerList;
    private List<Item> itemList;
    private FirebaseFirestore firestore;

    public AllFragment() {
    }

    public static AllFragment getInstance() {
        if (instance == null) {
            instance = new AllFragment();
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
        View view = inflater.inflate(R.layout.fragment_all, container, false);

        /*setup promo cards*/
        promotionRecyclerView = view.findViewById(R.id.homePromoRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        promotionRecyclerView.setLayoutManager(linearLayoutManager);

        promoAdapter = new PromoAdapter(new ArrayList<>());
        promotionRecyclerView.setAdapter(promoAdapter);

        fetchPromoCard();

        /*setup popular items*/
        popularItemRecyclerView = view.findViewById(R.id.popularItemRecyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 2);
        popularItemRecyclerView.setLayoutManager(gridLayoutManager);

        itemAdapter = new ItemAdapter(getContext(),new ArrayList<>(), this);
        popularItemRecyclerView.setAdapter(itemAdapter);

        fetchPopularProducts();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void fetchPromoCard(){
        firestore.collection("offers")
                .orderBy("id", Query.Direction.DESCENDING)
                .limit(3)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            offerList = new ArrayList<>();
                            for (QueryDocumentSnapshot snapshot : task.getResult()) {
                                Offer offer = snapshot.toObject(Offer.class);
                                offerList.add(offer);
                            }
                            promoAdapter.setOfferList(offerList);
                        }else {
                            Log.e(TAG,task.getException().getMessage());
                        }
                    }
                });
    }

    private void fetchPopularProducts(){
        firestore.collection("products")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            itemList = new ArrayList<>();
                            for (QueryDocumentSnapshot snapshot : task.getResult()) {
                                Item item = snapshot.toObject(Item.class);
                                itemList.add(item);
                            }
                            Collections.shuffle(itemList);
                            int limit = Math.min(itemList.size(), 4);
                            List<Item> randomItems = itemList.subList(0, limit);
                            itemAdapter.setItemList(randomItems);

                        }else {
                            Log.e(TAG,task.getException().getMessage());
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