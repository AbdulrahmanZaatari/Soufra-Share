package com.example.project;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// Remove any Glide imports if they exist!
import com.squareup.picasso.Picasso;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PostingAdapter extends RecyclerView.Adapter<PostingAdapter.PostingViewHolder> {

    private static final String TAG = "PostingAdapter";
    private List<Meal> postingList;
    private Context context;
    private OnPostingActionListener listener;

    private String baseUrl = "http://10.0.2.2/Soufra_Share/";

    public interface OnPostingActionListener {
        void onEditClick(Meal meal);
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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_posting_card, parent, false);
        return new PostingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostingViewHolder holder, int position) {
        Meal currentMeal = postingList.get(position);
        if (currentMeal == null) {
            Log.e(TAG, "Meal object at position " + position + " is null.");
            return;
        }

        // Logging details for debugging
        Log.d(TAG, "Binding meal - Name: " + currentMeal.getName() +
                ", Price: " + currentMeal.getPrice() +
                ", Quantity: " + currentMeal.getQuantity() +
                ", Description: " + currentMeal.getDescription());

        holder.textViewName.setText(currentMeal.getName());
        holder.textViewPrice.setText(String.format(Locale.getDefault(), "$%.2f", currentMeal.getPrice()));
        holder.textViewQuantity.setText(String.format(Locale.getDefault(), "Qty: %d", currentMeal.getQuantity()));
        holder.textViewDescription.setText(currentMeal.getDescription());

        // Load the image using Picasso with @drawable/sushi as placeholder and error fallback
        String imagePathsJson = currentMeal.getImagePaths();
        if (imagePathsJson != null && !imagePathsJson.isEmpty() && !imagePathsJson.equals("[]")) {
            try {
                Gson gson = new Gson();
                Type listType = new TypeToken<List<String>>() {}.getType();
                List<String> imagePaths = gson.fromJson(imagePathsJson, listType);

                if (!imagePaths.isEmpty()) {
                    // Build the URL with the first image (adjust as needed if multiple images exist)
                    String imageUrl = baseUrl + "uploads/" + imagePaths.get(0);
                    Log.d(TAG, "Loading posting image from: " + imageUrl);
                    Picasso.get()
                            .load(imageUrl)
                            .placeholder(R.drawable.sushi) // use sushi as placeholder
                            .error(R.drawable.sushi)       // use sushi for error fallback
                            .into(holder.imageViewPosting);
                } else {
                    holder.imageViewPosting.setImageResource(R.drawable.sushi);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing image paths: " + e.getMessage());
                holder.imageViewPosting.setImageResource(R.drawable.sushi);
            }
        } else {
            holder.imageViewPosting.setImageResource(R.drawable.sushi);
        }

        // Set up the Edit button action.
        holder.buttonEdit.setOnClickListener(v -> {
            if (listener != null) {
                Log.d(TAG, "Edit button clicked for meal ID: " + currentMeal.getMealId());
                listener.onEditClick(currentMeal);
            } else {
                Log.w(TAG, "Listener is null, cannot handle edit click.");
            }
        });

        // Set up the Delete button action.
        holder.buttonDelete.setOnClickListener(v -> {
            if (listener != null) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) { // check for valid adapter position
                    Log.d(TAG, "Delete button clicked for meal ID: " + currentMeal.getMealId() + " at position: " + adapterPosition);
                    listener.onDeleteClick(currentMeal, adapterPosition);
                } else {
                    Log.w(TAG, "Delete button clicked but adapter position is invalid.");
                }
            } else {
                Log.w(TAG, "Listener is null, cannot handle delete click.");
            }
        });
    }

    @Override
    public int getItemCount() {
        int count = postingList != null ? postingList.size() : 0;
        Log.d(TAG, "getItemCount() returning: " + count);
        return count;
    }

    public void updatePostings(List<Meal> newPostings) {
        Log.d(TAG, "updatePostings() called with " + (newPostings != null ? newPostings.size() : 0) + " items.");
        this.postingList = new ArrayList<>(newPostings);
        notifyDataSetChanged();
        Log.d(TAG, "updatePostings() - notifyDataSetChanged() called.");
    }

    // Method to remove an item from the postings list after deletion
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
            buttonEdit = itemView.findViewById(R.id.button_edit_posting);
            buttonDelete = itemView.findViewById(R.id.button_delete_posting);
        }
    }
}
