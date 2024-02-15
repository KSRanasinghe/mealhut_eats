package com.ksr.mealhuteats.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.ksr.mealhuteats.R;
import com.ksr.mealhuteats.model.Offer;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PromoAdapter extends RecyclerView.Adapter<PromoAdapter.ViewHolder> {

    private List<Offer> offerList;
    private FirebaseStorage firebaseStorage;

    public PromoAdapter(List<Offer> offerList) {
        this.offerList = offerList;
        this.firebaseStorage = FirebaseStorage.getInstance();
    }

    public void setOfferList(List<Offer> offerList) {
        this.offerList = offerList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PromoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.promotion_card, parent, false);
        return new PromoAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PromoAdapter.ViewHolder holder, int position) {
        Offer offer = offerList.get(position);

        firebaseStorage.getReference("offer-images/" + offer.getImage())
                .getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).fit().into(holder.promoImage);
                    }
                });

    }

    @Override
    public int getItemCount() {
        return offerList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView promoImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            promoImage = itemView.findViewById(R.id.promoImg);
        }
    }
}
