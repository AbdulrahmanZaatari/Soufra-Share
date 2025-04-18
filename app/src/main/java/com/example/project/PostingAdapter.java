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

    private String baseUrl = "http://10.0.2.2/Soufra_Share/"; // <-- Check this URL

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
        // Ensure this matches your layout file name for a single posting item
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_posting_card, parent, false); // <-- Check this layout file name
        return new PostingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostingViewHolder holder, int position) {
        Meal currentMeal = postingList.get(position);
        if (currentMeal == null) {
            Log.e(TAG, "Meal object at position " + position + " is null.");
            return; // Skip binding if the meal object is null
        }

        // Logging details for debugging the data received
        Log.d(TAG, "Binding meal - Name: " + currentMeal.getName() +
                ", ID: " + currentMeal.getMealId() +
                ", Price: " + currentMeal.getPrice() +
                ", Quantity: " + currentMeal.getQuantity() +
                ", ImagePathsJson: " + currentMeal.getImagePaths());


        holder.textViewName.setText(currentMeal.getName());
        holder.textViewPrice.setText(String.format(Locale.getDefault(), "$%.2f", currentMeal.getPrice()));
        holder.textViewQuantity.setText(String.format(Locale.getDefault(), "Qty: %d", currentMeal.getQuantity()));
        holder.textViewDescription.setText(currentMeal.getDescription());

        // Load the image using Picasso with @drawable/sushi as placeholder and error fallback
        String imagePathsJson = currentMeal.getImagePaths();

        List<String> imagePaths = null; // Initialize imagePaths to null
        if (imagePathsJson != null && !imagePathsJson.isEmpty() && !imagePathsJson.equals("[]")) {
            try {
                Gson gson = new Gson();
                Type listType = new TypeToken<List<String>>() {}.getType();
                // gson.fromJson can return null if the JSON string is "null" or malformed
                imagePaths = gson.fromJson(imagePathsJson, listType);

            } catch (Exception e) {
                Log.e(TAG, "Error parsing image paths JSON for meal ID " + currentMeal.getMealId() + ": " + e.getMessage());
                // If parsing fails, imagePaths remains null, which is handled below
            }
        }

        // *** IMPORTANT FIX: Check if imagePaths is null BEFORE calling isEmpty() ***
        if (imagePaths != null && !imagePaths.isEmpty()) {
            // Assuming image filenames are relative to the uploads directory within your base URL
            String imageUrl = baseUrl + "uploads/" + imagePaths.get(0); // Get the first image URL
            Log.d(TAG, "Loading posting image from: " + imageUrl);
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.sushi) // use sushi as placeholder
                    .error(R.drawable.sushi)       // use sushi for error fallback
                    .into(holder.imageViewPosting);
        } else {
            // Set placeholder if imagePaths is null, empty, or the original JSON was "[]"
            Log.d(TAG, "imagePaths is null or empty for meal ID " + currentMeal.getMealId() + ", setting default image.");
            holder.imageViewPosting.setImageResource(R.drawable.sushi); // Set placeholder
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

    // Method to update the list of postings and notify the adapter
    public void updatePostings(List<Meal> newPostings) {
        Log.d(TAG, "updatePostings() called with " + (newPostings != null ? newPostings.size() : 0) + " items.");
        this.postingList = new ArrayList<>(newPostings); // Create a new list to avoid modifying the original
        notifyDataSetChanged();
        Log.d(TAG, "updatePostings() - notifyDataSetChanged() called.");
    }

    // Method to remove an item from the postings list after deletion
    public void removeItem(int position) {
        if (postingList != null && position >= 0 && position < postingList.size()) {
            Log.d(TAG, "Removing item at position: " + position + " (Meal ID: " + postingList.get(position).getMealId() + ")");
            postingList.remove(position);
            notifyItemRemoved(position);
            // You might need notifyItemRangeChanged(position, postingList.size()) if item positions shift visually
        } else {
            Log.w(TAG, "Attempted to remove item at invalid position: " + position);
        }
    }

    // ViewHolder class
    public static class PostingViewHolder extends RecyclerView.ViewHolder {
        // Declare your views here, matching the IDs in item_posting_card.xml
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