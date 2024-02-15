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
import com.ksr.mealhuteats.model.Category;
import com.ksr.mealhuteats.util.OnCardClickListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private List<Category> categoryList;
    private OnCardClickListener<Category> onCardClickListener;
    private int selectedPosition = 0;
    private FirebaseStorage firebaseStorage;

    public CategoryAdapter(List<Category> categoryList) {
        this.categoryList = categoryList;
        this.firebaseStorage = FirebaseStorage.getInstance();
    }

    public void setCategoryList(List<Category> categoryList) {
        this.categoryList = categoryList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.category_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryAdapter.ViewHolder holder, int position) {
        Category category = categoryList.get(holder.getAdapterPosition());
        holder.categoryName.setText(category.getName());

        firebaseStorage.getReference("category-images/" + category.getImage())
                .getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).fit().centerCrop().into(holder.categoryImage);
                    }
                });

        /*set onClickListener for the category cards*/
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                selectedPosition = holder.getAdapterPosition();

                if (onCardClickListener != null) {
                    onCardClickListener.onCardClick(category);
                }

                notifyDataSetChanged();
            }
        });

        /*set all_card by default*/
        holder.itemView.setSelected(position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView categoryName;
        ImageView categoryImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.categoryName);
            categoryImage = itemView.findViewById(R.id.categoryImage);
        }
    }

    public void setOnCardClickListener(OnCardClickListener<Category> listener) {
        this.onCardClickListener = listener;
    }
}
