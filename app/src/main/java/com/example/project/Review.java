package com.example.project;

public class Review {
    private int reviewId;
    private int reviewerId;
    private String reviewerUsername;
    private int revieweeId;
    private int rating;
    private String comment;
    private String reviewDate;

    private String reviewerProfilePicture;

    public Review(int reviewId, int reviewerId, String reviewerUsername, int revieweeId, int rating, String comment, String reviewDate, String reviewerProfilePicture) {
        this.reviewId = reviewId;
        this.reviewerId = reviewerId;
        this.reviewerUsername = reviewerUsername;
        this.revieweeId = revieweeId;
        this.rating = rating;
        this.comment = comment;
        this.reviewDate = reviewDate;
        this.reviewerProfilePicture = reviewerProfilePicture;
    }

    public int getReviewId() {
        return reviewId;
    }

    public int getReviewerId() {
        return reviewerId;
    }

    public String getReviewerUsername() {
        return reviewerUsername;
    }

    public int getRevieweeId() {
        return revieweeId;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public String getReviewDate() {
        return reviewDate;
    }
    public String getReviewerProfilePicture() {
        return reviewerProfilePicture;
    }
}