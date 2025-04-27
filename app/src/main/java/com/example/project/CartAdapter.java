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
import com.bumptech.glide.Glide;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private Context context;
    private List<CartItem> cartItems;
    private OnItemClickListener deleteClickListener;
    private String baseUrl = "http://10.0.2.2/Soufra_Share/uploads/"; 

    public interface OnItemClickListener {
        void onDeleteClick(int position);
    }

    public void setOnDeleteClickListener(OnItemClickListener listener) {
        this.deleteClickListener = listener;
    }

    public CartAdapter(Context context, List<CartItem> cartItems) {
        this.context = context;
        this.cartItems = cartItems;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem currentItem = cartItems.get(position);
        holder.mealNameTextView.setText(currentItem.getMealName());
        holder.mealPriceTextView.setText(String.format("$%.2f", currentItem.getPrice()));
        holder.quantityTextView.setText("Qty: " + currentItem.getQuantity());

        String imageUrl = currentItem.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.equals("[]")) {
            try {
                String cleanedImageUrl = imageUrl.replaceAll("\\[\"", "").replaceAll("\"\\]", "");
                String fullImageUrl = baseUrl + cleanedImageUrl;
                Log.d("CartAdapter", "Loading image from: " + fullImageUrl);
                Glide.with(context)
                        .load(fullImageUrl)
                        .placeholder(R.drawable.sushi)
                        .error(R.drawable.meal)
                        .into(holder.mealImageView);
            } catch (Exception e) {
                Log.e("CartAdapter", "Error loading image for meal: " + currentItem.getMealName(), e);
                Glide.with(context)
                        .load(R.drawable.sushi)
                        .into(holder.mealImageView);
            }
        } else {
            Glide.with(context)
                    .load(R.drawable.sushi)
                    .into(holder.mealImageView);
        }

        holder.deleteButton.setOnClickListener(v -> {
            if (deleteClickListener != null) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    deleteClickListener.onDeleteClick(adapterPosition);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        public ImageView mealImageView;
        public TextView mealNameTextView;
        public TextView mealPriceTextView;
        public TextView quantityTextView;
        public ImageButton deleteButton;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            mealImageView = itemView.findViewById(R.id.meal_image_view);
            mealNameTextView = itemView.findViewById(R.id.meal_name_text_view);
            mealPriceTextView = itemView.findViewById(R.id.meal_price_text_view);
            quantityTextView = itemView.findViewById(R.id.quantity_text_view);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}