package com.example.project;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso; // Make sure to add Picasso dependency in your build.gradle (app level)

import java.util.List;

public class MealAdapter extends RecyclerView.Adapter<MealAdapter.MealViewHolder> {

    private List<Meal> mealList;

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
        holder.mealQuantityText.setText("Qty: " + currentMeal.getQuantity()); // Set the quantity

        // Placeholder for image loading
        holder.mealImageView.setImageResource(R.drawable.sushi);

        if (currentMeal.getProfilePicture() != null && !currentMeal.getProfilePicture().isEmpty()) {
            // Placeholder for Picasso (commented out)
            // Picasso.get().load(currentMeal.getProfilePicture()).placeholder(R.drawable.ic_person).into(holder.profileImage);
            holder.profileImage.setImageResource(R.drawable.ic_person); // Placeholder
        } else {
            holder.profileImage.setImageResource(R.drawable.ic_person);
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
        public ImageView mealImageView; // Add ImageView for the meal
        public TextView mealQuantityText; // Add TextView for quantity

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
            mealImageView = itemView.findViewById(R.id.meal_image); // Assuming you have an ImageView with this ID in your item_meal.xml
            mealQuantityText = itemView.findViewById(R.id.meal_quantity_text); // Initialize the quantity TextView
        }
    }
}