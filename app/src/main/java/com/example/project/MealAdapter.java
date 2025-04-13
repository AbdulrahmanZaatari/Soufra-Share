package com.example.project;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;
import java.util.List;

public class MealAdapter extends RecyclerView.Adapter<MealAdapter.MealViewHolder> {

    private List<Meal> mealList;
    private String baseUrl = "http://10.0.2.2/soufra_share/uploads/";

    public MealAdapter(List<Meal> mealList) {
        this.mealList = mealList;
    }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_meal, parent, false);
        return new MealViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        Meal currentMeal = mealList.get(position);
        holder.usernameText.setText(currentMeal.getUsername());
        holder.ratingBar.setRating((float) currentMeal.getRating());
        holder.mealNameText.setText(currentMeal.getName());
        holder.mealDescriptionText.setText(currentMeal.getDescription());
        holder.mealPriceText.setText("$" + String.format("%.2f", currentMeal.getPrice()));
        holder.mealLocationText.setText("Location: " + currentMeal.getLocation());
        holder.mealDeliveryOptionText.setText("Delivery: " + (currentMeal.getDeliveryOption() == 1 ? "Yes" : "No"));
        holder.mealQuantityText.setText("Qty: " + currentMeal.getQuantity());

        // Load meal image
        String imagePathsJson = currentMeal.getImagePaths();
        if (imagePathsJson != null && !imagePathsJson.isEmpty() && !imagePathsJson.equals("[]")) {
            try {
                Gson gson = new Gson();
                Type listType = new TypeToken<List<String>>() {}.getType();
                List<String> imagePaths = gson.fromJson(imagePathsJson, listType);

                if (!imagePaths.isEmpty()) {
                    String imageUrl = baseUrl + imagePaths.get(0);
                    Log.d("MealAdapter", "Loading meal image from: " + imageUrl);
                    Picasso.get()
                            .load(imageUrl)
                            .placeholder(R.drawable.sushi)
                            .error(R.drawable.sushi)
                            .into(holder.mealImageView);
                } else {
                    holder.mealImageView.setImageResource(R.drawable.sushi);
                }

            } catch (Exception e) {
                Log.e("MealAdapter", "Error parsing image paths: " + e.getMessage());
                holder.mealImageView.setImageResource(R.drawable.sushi);
            }
        } else {
            holder.mealImageView.setImageResource(R.drawable.sushi);
        }

        // Load profile picture
        String profilePictureUrl = currentMeal.getProfilePicture();
        Log.d("MealAdapter", "Profile picture URL from Meal object: " + profilePictureUrl);

        if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
            String fullProfilePictureUrl = baseUrl + profilePictureUrl;
            Log.d("MealAdapter", "Loading profile picture from: " + fullProfilePictureUrl);
            Picasso.get()
                    .load(fullProfilePictureUrl)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(holder.profileImage);
        } else {
            holder.profileImage.setImageResource(R.drawable.ic_person); // Placeholder if URL is null or empty
        }

        // Set OnClickListener for the item view
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MealDetailActivity.class);
                intent.putExtra("meal", currentMeal); // Pass the Meal object
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mealList.size();
    }

    public static class MealViewHolder extends RecyclerView.ViewHolder {
        public ImageView profileImage;
        public TextView usernameText;
        public RatingBar ratingBar;
        public TextView mealNameText;
        public TextView mealDescriptionText;
        public TextView mealPriceText;
        public TextView mealLocationText;
        public TextView mealDeliveryOptionText;
        public ImageView mealImageView;
        public TextView mealQuantityText;

        public MealViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profile_image);
            usernameText = itemView.findViewById(R.id.username_text);
            ratingBar = itemView.findViewById(R.id.rating_bar);
            mealNameText = itemView.findViewById(R.id.meal_name_text);
            mealDescriptionText = itemView.findViewById(R.id.meal_description_text);
            mealPriceText = itemView.findViewById(R.id.meal_price_text);
            mealLocationText = itemView.findViewById(R.id.meal_location_text);
            mealDeliveryOptionText = itemView.findViewById(R.id.meal_delivery_option_text);
            mealImageView = itemView.findViewById(R.id.meal_image);
            mealQuantityText = itemView.findViewById(R.id.meal_quantity_text);
        }
    }
}