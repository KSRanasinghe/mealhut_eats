package com.ksr.mealhuteats.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.ksr.mealhuteats.R;
import com.ksr.mealhuteats.model.Offer;
import com.squareup.picasso.Picasso;

import java.util.List;

public class OfferAdapter extends RecyclerView.Adapter<OfferAdapter.ViewHolder> {

    private List<Offer> offerList;
    private FirebaseStorage firebaseStorage;

    public OfferAdapter(List<Offer> offerList) {
        this.offerList = offerList;
        this.firebaseStorage = FirebaseStorage.getInstance();
    }

    public void setOfferList(List<Offer> offerList) {
        this.offerList = offerList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OfferAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.offer_card, parent, false);
        return new OfferAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OfferAdapter.ViewHolder holder, int position) {
        Offer offer = offerList.get(position);

        String startDate = "Starts on : " + offer.getStart();
        String endDate = "Ends on : " + offer.getEnd();

        holder.offerStartDate.setText(startDate);
        holder.offerEndDate.setText(endDate);

        firebaseStorage.getReference("offer-images/" + offer.getImage())
                .getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).fit().into(holder.offerImg);
                    }
                });
    }

    @Override
    public int getItemCount() {
        return offerList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView offerStartDate, offerEndDate;
        private ImageView offerImg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            offerImg = itemView.findViewById(R.id.offerImg);
            offerStartDate = itemView.findViewById(R.id.offerStartDate);
            offerEndDate = itemView.findViewById(R.id.offerEndDate);
        }
    }
}
