package com.example.project;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private List<Review> reviewList;
    private Context context;

    public ReviewAdapter(Context context, List<Review> reviewList) {
        this.context = context;
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

        String profilePictureUrl = review.getReviewerProfilePicture();
        if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
            Picasso.get()
                    .load("http://10.0.2.2/Soufra_Share/" + profilePictureUrl)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(holder.reviewerProfileImageView);
        } else {
            holder.reviewerProfileImageView.setImageResource(R.drawable.ic_person);
        }
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        public ImageView reviewerProfileImageView;
        public TextView reviewerUsernameTextView;
        public RatingBar ratingBar;
        public TextView commentTextView;
        public TextView reviewDateTextView;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            reviewerProfileImageView = itemView.findViewById(R.id.image_view_reviewer_profile);
            reviewerUsernameTextView = itemView.findViewById(R.id.text_view_reviewer_username);
            ratingBar = itemView.findViewById(R.id.rating_bar_review);
            commentTextView = itemView.findViewById(R.id.text_view_review_comment);
            reviewDateTextView = itemView.findViewById(R.id.text_view_review_date);
        }
    }
}
