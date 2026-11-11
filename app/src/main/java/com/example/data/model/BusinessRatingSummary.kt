package com.example.data.model

import com.example.data.local.BusinessReviewEntity

data class BusinessRatingSummary(
  val businessId: String,
  val businessName: String,
  val busyCardNetId: String,
  val averageRating: Float,
  val totalReviewsCount: Int,
  val fiveStarCount: Int,
  val fourStarCount: Int,
  val threeStarCount: Int,
  val twoStarCount: Int,
  val oneStarCount: Int,
  val reviews: List<BusinessReviewEntity>
) {
  val fiveStarPct: Float
    get() = if (totalReviewsCount > 0) (fiveStarCount.toFloat() / totalReviewsCount) else 0f

  val fourStarPct: Float
    get() = if (totalReviewsCount > 0) (fourStarCount.toFloat() / totalReviewsCount) else 0f

  val threeStarPct: Float
    get() = if (totalReviewsCount > 0) (threeStarCount.toFloat() / totalReviewsCount) else 0f

  val twoStarPct: Float
    get() = if (totalReviewsCount > 0) (twoStarCount.toFloat() / totalReviewsCount) else 0f

  val oneStarPct: Float
    get() = if (totalReviewsCount > 0) (oneStarCount.toFloat() / totalReviewsCount) else 0f
}
