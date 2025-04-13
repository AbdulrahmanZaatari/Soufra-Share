package com.example.project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private List<Review> reviewList;

    public ReviewAdapter(List<Review> reviewList) {
        this.reviewList = reviewList;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);
        holder.reviewerUsernameTextView.setText(review.getReviewerUsername());
        holder.ratingBar.setRating(review.getRating());
        holder.commentTextView.setText(review.getComment());
        holder.reviewDateTextView.setText(review.getReviewDate());
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        public TextView reviewerUsernameTextView;
        public RatingBar ratingBar;
        public TextView commentTextView;
        public TextView reviewDateTextView;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            reviewerUsernameTextView = itemView.findViewById(R.id.text_reviewer_username);
            ratingBar = itemView.findViewById(R.id.rating_bar_review);
            commentTextView = itemView.findViewById(R.id.text_review_comment);
            reviewDateTextView = itemView.findViewById(R.id.text_review_date);
        }
    }
}