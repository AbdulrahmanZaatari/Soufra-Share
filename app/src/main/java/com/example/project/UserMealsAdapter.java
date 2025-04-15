package com.example.project;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;
import java.util.List;

public class UserMealsAdapter extends RecyclerView.Adapter<UserMealsAdapter.MealViewHolder> {

    private Context context;
    private List<Meal> mealList;
    private String baseUrl = "http://10.0.2.2/Soufra_Share/";

    public UserMealsAdapter(Context context, List<Meal> mealList) {
        this.context = context;
        this.mealList = mealList;
    }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_meal_horizontal, parent, false); // Inflate the horizontal layout
        return new MealViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        Meal currentMeal = mealList.get(position);
        holder.mealNameTextView.setText(currentMeal.getName());
        holder.mealPriceTextView.setText("$" + String.format("%.2f", currentMeal.getPrice()));
        holder.mealDescriptionTextView.setText(currentMeal.getDescription());

        // Load meal image
        String imagePathsJson = currentMeal.getImagePaths();
        if (imagePathsJson != null && !imagePathsJson.isEmpty() && !imagePathsJson.equals("[]")) {
            try {
                Gson gson = new Gson();
                Type listType = new TypeToken<List<String>>() {
                }.getType();
                List<String> imagePaths = gson.fromJson(imagePathsJson, listType);

                if (!imagePaths.isEmpty()) {
                    String imageUrl = baseUrl + "uploads/" + imagePaths.get(0);
                    Log.d("UserMealsAdapter", "Loading meal image from: " + imageUrl);
                    Picasso.get()
                            .load(imageUrl)
                            .placeholder(R.drawable.sushi) // Replace with your placeholder
                            .error(R.drawable.sushi)       // Replace with your error image
                            .into(holder.mealImageView);
                } else {
                    holder.mealImageView.setImageResource(R.drawable.sushi); // Default image if no path
                }

            } catch (Exception e) {
                Log.e("UserMealsAdapter", "Error parsing image paths: " + e.getMessage());
                holder.mealImageView.setImageResource(R.drawable.sushi); // Error fallback image
            }
        } else {
            holder.mealImageView.setImageResource(R.drawable.sushi); // Default image if no JSON
        }
    }

    @Override
    public int getItemCount() {
        return mealList.size();
    }

    public static class MealViewHolder extends RecyclerView.ViewHolder {
        ImageView mealImageView;
        TextView mealNameTextView;
        TextView mealPriceTextView;
        TextView mealDescriptionTextView;

        public MealViewHolder(@NonNull View itemView) {
            super(itemView);
            mealImageView = itemView.findViewById(R.id.mealImageView);
            mealNameTextView = itemView.findViewById(R.id.mealNameTextView);
            mealPriceTextView = itemView.findViewById(R.id.mealPriceTextView);
            mealDescriptionTextView = itemView.findViewById(R.id.mealDescriptionTextView);
        }
    }
}