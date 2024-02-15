package com.ksr.mealhuteats;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.airbnb.lottie.LottieAnimationView;

public class EmptyCartFragment extends Fragment {

    public static final String TAG = EmptyCartFragment.class.getName();
    private static EmptyCartFragment instance;
    LottieAnimationView animationView;

    public EmptyCartFragment() {
        // Required empty public constructor
    }

    public static EmptyCartFragment getInstance(){
        if (instance == null){
            instance = new EmptyCartFragment();
        }

        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_empty_cart, container, false);

        animationView = view.findViewById(R.id.emptyCartAnimation);
        animationView.setAnimation(R.raw.empty_cart);
        return view;
    }
}