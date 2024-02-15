package com.ksr.mealhuteats;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ksr.mealhuteats.adapter.CategoryAdapter;
import com.ksr.mealhuteats.model.Category;
import com.ksr.mealhuteats.util.OnCardClickListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements OnCardClickListener<Category> {

    public static final String TAG = HomeFragment.class.getName();
    private static HomeFragment instance;
    private RecyclerView recyclerView;
    private CategoryAdapter adapter;
    private List<Category> categoryList;
    private long backPressedTime = 0;
    private FirebaseFirestore firestore;

    public HomeFragment() {
    }

    public static HomeFragment getInstance() {
        if (instance == null) {
            instance = new HomeFragment();
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
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        loadFragment(AllFragment.getInstance(),null);

        recyclerView = view.findViewById(R.id.categoryRecyclerView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        adapter = new CategoryAdapter(new ArrayList<>());
        adapter.setOnCardClickListener(this);
        recyclerView.setAdapter(adapter);

        fetchDataFromFirestore();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Add an onBackPressed callback
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Call onBackPressed logic
                onBackPressed();
            }
        });

    }

    private void onBackPressed() {

        if (getChildFragmentManager().getBackStackEntryCount() > 0) {
            getChildFragmentManager().popBackStack();

            if (adapter.getSelectedPosition() != 0) {
                adapter.setSelectedPosition(0);
                adapter.notifyDataSetChanged();
                onCardClick(categoryList.get(0));
            }
        } else {

            long currentTime = System.currentTimeMillis();

            if (currentTime - backPressedTime < 2000) {
                requireActivity().finish();
            } else {
                Toast.makeText(requireContext(), "Press back again to exit", Toast.LENGTH_SHORT).show();
                backPressedTime = currentTime;
            }
        }
    }

    private void fetchDataFromFirestore() {
        firestore.collection("categories")
                .orderBy("id", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            categoryList = new ArrayList<>();
                            for (QueryDocumentSnapshot snapshot : task.getResult()) {
                                Category category = snapshot.toObject(Category.class);
                                categoryList.add(category);
                            }

                            //updating the adapter
                            adapter.setCategoryList(categoryList);
                        } else {
                            Log.e(TAG, "fetching failed: " + task.getException().getMessage());
                        }
                    }
                });
    }

    @Override
    public void onCardClick(Category category) {
        if (category.getName().equalsIgnoreCase("all")) {
            loadFragment(AllFragment.getInstance(),null);
        } else if (category.getName().equalsIgnoreCase("rice")) {
            loadFragment(RiceFragment.getInstance(),category.getId());
        } else if (category.getName().equalsIgnoreCase("kottu")) {
            loadFragment(KottuFragment.getInstance(),category.getId());
        } else if (category.getName().equalsIgnoreCase("pasta")) {
            loadFragment(PastaFragment.getInstance(),category.getId());
        } else if (category.getName().equalsIgnoreCase("dessert")) {
            loadFragment(DessertFragment.getInstance(),category.getId());
        } else if (category.getName().equalsIgnoreCase("beverage")) {
            loadFragment(BeverageFragment.getInstance(),category.getId());
        }
    }

    private void loadFragment(Fragment fragment,String categoryId) {
        FragmentManager childFragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = childFragmentManager.beginTransaction();

        Bundle args = new Bundle();
        args.putString("categoryId",categoryId);
        fragment.setArguments(args);
        fragmentTransaction.replace(R.id.homeFragmentContainer, fragment);

        if (!(fragment instanceof AllFragment)) {
            fragmentTransaction.addToBackStack(null);
        }

        fragmentTransaction.commit();
    }
}