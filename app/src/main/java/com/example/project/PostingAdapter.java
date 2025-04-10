package com.example.project;

import android.content.Context;
import android.util.Log; // Add Log import
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// TODO: Optional: Import Glide or Picasso

public class PostingAdapter extends RecyclerView.Adapter<PostingAdapter.PostingViewHolder> {

    private static final String TAG = "PostingAdapter"; // Add TAG for logging
    private List<Meal> postingList;
    private Context context;
    private OnPostingActionListener listener;

    public interface OnPostingActionListener {
        void onEditClick(Meal meal); // <<< ADDED BACK
        void onDeleteClick(Meal meal, int position);
    }

    public PostingAdapter(Context context, List<Meal> postingList, OnPostingActionListener listener) {
        this.context = context;
        this.postingList = postingList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PostingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_posting_card, parent, false);
        return new PostingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostingViewHolder holder, int position) {
        Meal currentMeal = postingList.get(position);
        if (currentMeal == null) {
            Log.e(TAG, "Meal object at position " + position + " is null.");
            return;
        }

        Log.d(TAG, "Binding meal - Name: " + currentMeal.getName() +
                ", Price: " + currentMeal.getPrice() +
                ", Quantity: " + currentMeal.getQuantity() +
                ", Description: " + currentMeal.getDescription());

        holder.textViewName.setText(currentMeal.getName());
        holder.textViewPrice.setText(String.format(Locale.getDefault(), "$%.2f", currentMeal.getPrice()));
        holder.textViewQuantity.setText(String.format(Locale.getDefault(), "Qty: %d", currentMeal.getQuantity()));
        holder.textViewDescription.setText(currentMeal.getDescription());

        holder.imageViewPosting.setImageResource(R.drawable.meal);
        /* Example with Glide:
        String imageUrl = ... // Get appropriate image URL from currentMeal.getImagePaths()
        Glide.with(context)
             .load(imageUrl)
             .placeholder(R.drawable.ic_placeholder_meal)
             .error(R.drawable.ic_placeholder_meal)
             .into(holder.imageViewPosting);
        */

        holder.buttonEdit.setOnClickListener(v -> {
            if (listener != null) {
                Log.d(TAG, "Edit button clicked for meal ID: " + currentMeal.getMealId());
                listener.onEditClick(currentMeal); // <<< ADDED BACK
            } else {
                Log.w(TAG, "Listener is null, cannot handle edit click.");
            }
        });

        holder.buttonDelete.setOnClickListener(v -> {
            if (listener != null) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) { // Check for valid position
                    Log.d(TAG, "Delete button clicked for meal ID: " + currentMeal.getMealId() + " at position: " + adapterPosition);
                    listener.onDeleteClick(currentMeal, adapterPosition);
                } else {
                    Log.w(TAG, "Delete button clicked but adapter position is invalid.");
                }
            } else {
                Log.w(TAG, "Listener is null, cannot handle delete click.");
            }
        });
        // -----------------------------------------
    }

    @Override
    public int getItemCount() {
        int count = postingList != null ? postingList.size() : 0;
        Log.d(TAG, "getItemCount() returning: " + count); // Added log
        return count;
    }

    public void updatePostings(List<Meal> newPostings) {
        Log.d(TAG, "updatePostings() called with " + (newPostings != null ? newPostings.size() : 0) + " items.");
        this.postingList = new ArrayList<>(newPostings);
        notifyDataSetChanged();
        Log.d(TAG, "updatePostings() - notifyDataSetChanged() called.");
    }

    // Method to remove an item after deletion
    public void removeItem(int position) {
        if (postingList != null && position >= 0 && position < postingList.size()) {
            postingList.remove(position);
            notifyItemRemoved(position);
        } else {
            Log.w(TAG, "Attempted to remove item at invalid position: " + position);
        }
    }

    public static class PostingViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewPosting;
        TextView textViewName, textViewPrice, textViewQuantity, textViewDescription;
        ImageButton buttonEdit, buttonDelete;

        public PostingViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewPosting = itemView.findViewById(R.id.image_view_posting);
            textViewName = itemView.findViewById(R.id.text_view_posting_name);
            textViewPrice = itemView.findViewById(R.id.text_view_posting_price);
            textViewQuantity = itemView.findViewById(R.id.text_view_posting_quantity);
            textViewDescription = itemView.findViewById(R.id.text_view_posting_description);
            buttonEdit = itemView.findViewById(R.id.button_edit_posting); // <<< Find the Edit button
            buttonDelete = itemView.findViewById(R.id.button_delete_posting);
        }
    }
}