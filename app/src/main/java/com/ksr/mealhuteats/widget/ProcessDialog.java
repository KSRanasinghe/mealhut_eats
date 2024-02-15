package com.ksr.mealhuteats.widget;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.airbnb.lottie.LottieAnimationView;
import com.ksr.mealhuteats.R;

public class ProcessDialog extends CustomDialog{
    public ProcessDialog(@NonNull Context context, int layoutResourceId, int animationResourceId) {
        super(context, layoutResourceId);

        LottieAnimationView lav = findViewById(R.id.cartAnimation);
        lav.setAnimation(animationResourceId);
    }
}
