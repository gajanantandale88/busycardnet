package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BusyCardNetDao {

  // Users
  @Query("SELECT * FROM users LIMIT 1")
  fun getCurrentUser(): Flow<UserEntity?>

  @Query("SELECT * FROM users WHERE internalUserId = :userId")
  suspend fun getUserById(userId: String): UserEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertUser(user: UserEntity)

  @Update
  suspend fun updateUser(user: UserEntity)

  @Query("UPDATE users SET availabilityStatus = :status WHERE internalUserId = :userId")
  suspend fun updateAvailabilityStatus(userId: String, status: String)

  // Digital Cards
  @Query("SELECT * FROM digital_cards ORDER BY isPrimary DESC, viewsCount DESC")
  fun getAllCards(): Flow<List<DigitalCardEntity>>

  @Query("SELECT * FROM digital_cards WHERE isPrimary = 1 LIMIT 1")
  fun getPrimaryCard(): Flow<DigitalCardEntity?>

  @Query("SELECT * FROM digital_cards WHERE busyCardNetId = :id LIMIT 1")
  suspend fun getCardByBusyCardNetId(id: String): DigitalCardEntity?

  @Query("SELECT * FROM digital_cards WHERE cardId = :id LIMIT 1")
  suspend fun getCardById(id: String): DigitalCardEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCard(card: DigitalCardEntity)

  @Update
  suspend fun updateCard(card: DigitalCardEntity)

  @Query("UPDATE digital_cards SET viewsCount = viewsCount + 1 WHERE cardId = :cardId")
  suspend fun incrementCardViews(cardId: String)

  @Query("UPDATE digital_cards SET sharesCount = sharesCount + 1 WHERE cardId = :cardId")
  suspend fun incrementCardShares(cardId: String)

  @Query("UPDATE digital_cards SET scansCount = scansCount + 1 WHERE cardId = :cardId")
  suspend fun incrementCardScans(cardId: String)

  // Professions
  @Query("SELECT * FROM professions WHERE isActive = 1 ORDER BY title ASC")
  fun getAllProfessions(): Flow<List<ProfessionCategoryEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProfessions(professions: List<ProfessionCategoryEntity>)

  // Feed Posts
  @Query("SELECT * FROM feed_posts ORDER BY timestamp DESC")
  fun getAllPosts(): Flow<List<FeedPostEntity>>

  @Query("SELECT * FROM feed_posts WHERE postScope = 'ADVERTISEMENT' OR mediaType = 'AD' OR mediaType = 'VIDEO' ORDER BY timestamp DESC")
  fun getAdPosts(): Flow<List<FeedPostEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPost(post: FeedPostEntity)

  @Query("UPDATE feed_posts SET likesCount = likesCount + 1, isLikedByMe = 1 WHERE id = :postId")
  suspend fun likePost(postId: String)

  // Service Requests
  @Query("SELECT * FROM service_requests ORDER BY createdAt DESC")
  fun getAllServiceRequests(): Flow<List<ServiceRequestEntity>>

  @Query("SELECT * FROM service_requests WHERE category = :category ORDER BY createdAt DESC")
  fun getServiceRequestsByCategory(category: String): Flow<List<ServiceRequestEntity>>

  @Query("SELECT * FROM service_requests WHERE requestId = :id LIMIT 1")
  suspend fun getServiceRequestById(id: String): ServiceRequestEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertServiceRequest(request: ServiceRequestEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertServiceRequests(requests: List<ServiceRequestEntity>)

  @Update
  suspend fun updateServiceRequest(request: ServiceRequestEntity)

  @Query("UPDATE service_requests SET status = :status WHERE requestId = :requestId")
  suspend fun updateServiceRequestStatus(requestId: String, status: String)

  @Query("UPDATE service_requests SET radiusMeters = :newRadius WHERE requestId = :requestId")
  suspend fun expandServiceRequestRadius(requestId: String, newRadius: Int)

  @Query("DELETE FROM service_requests WHERE requestId = :requestId")
  suspend fun deleteServiceRequestById(requestId: String)

  @Delete
  suspend fun deleteServiceRequest(request: ServiceRequestEntity)

  // Marketplace Products
  @Query("SELECT * FROM marketplace_products ORDER BY createdAt DESC")
  fun getAllProducts(): Flow<List<MarketplaceProductEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProduct(product: MarketplaceProductEntity)

  // Card Templates
  @Query("SELECT * FROM card_templates ORDER BY downloadsCount DESC")
  fun getAllTemplates(): Flow<List<CardTemplateEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTemplates(templates: List<CardTemplateEntity>)

  // Physical Card Suppliers
  @Query("SELECT * FROM physical_suppliers ORDER BY rating DESC")
  fun getAllSuppliers(): Flow<List<PhysicalSupplierEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSuppliers(suppliers: List<PhysicalSupplierEntity>)

  // Video Posts & Advertisements
  @Query("SELECT * FROM feed_posts WHERE mediaType = 'VIDEO' OR mediaType = 'PAGE' OR postScope = 'VIDEO' ORDER BY timestamp DESC")
  fun getVideoPosts(): Flow<List<FeedPostEntity>>

  // Card Connections (Bridges)
  @Query("SELECT * FROM card_connections ORDER BY createdAt DESC")
  fun getAllConnections(): Flow<List<CardConnectionEntity>>

  @Query("SELECT * FROM card_connections WHERE status = 'CONNECTED' ORDER BY createdAt DESC")
  fun getConnectedBridges(): Flow<List<CardConnectionEntity>>

  @Query("SELECT * FROM card_connections WHERE status = 'PENDING' ORDER BY createdAt DESC")
  fun getPendingRequests(): Flow<List<CardConnectionEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertConnection(connection: CardConnectionEntity)

  @Query("SELECT * FROM card_connections WHERE targetBusyCardNetId = :bcnId OR requesterBusyCardNetId = :bcnId LIMIT 1")
  suspend fun getConnectionByBusyCardNetId(bcnId: String): CardConnectionEntity?

  @Query("UPDATE card_connections SET status = :status WHERE id = :connectionId")
  suspend fun updateConnectionStatus(connectionId: String, status: String)

  @Query("DELETE FROM card_connections WHERE id = :connectionId")
  suspend fun deleteConnection(connectionId: String)

  @Query("UPDATE digital_cards SET isConnected = :connected, connectionStatus = :status WHERE cardId = :cardId")
  suspend fun updateCardConnectionState(cardId: String, connected: Boolean, status: String)

  // User Statuses (24h shared status across bridges)
  @Query("SELECT * FROM user_statuses ORDER BY createdAt DESC")
  fun getAllStatuses(): Flow<List<UserStatusEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertStatus(status: UserStatusEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTemplate(template: CardTemplateEntity)

  // Chat Messages
  @Query("SELECT * FROM chat_messages WHERE serviceRequestId = :requestId ORDER BY timestamp ASC")
  fun getMessagesForRequest(requestId: String): Flow<List<ChatMessageEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertChatMessage(message: ChatMessageEntity)

  // Saved Businesses (Bookmarks)
  @Query("SELECT * FROM saved_businesses ORDER BY savedAt DESC")
  fun getAllSavedBusinesses(): Flow<List<SavedBusinessEntity>>

  @Query("SELECT * FROM saved_businesses WHERE userId = :userId ORDER BY savedAt DESC")
  fun getSavedBusinessesByUser(userId: String): Flow<List<SavedBusinessEntity>>

  @Query("SELECT * FROM saved_businesses WHERE businessId = :businessId LIMIT 1")
  suspend fun getSavedBusinessById(businessId: String): SavedBusinessEntity?

  @Query("SELECT EXISTS(SELECT 1 FROM saved_businesses WHERE businessId = :businessId)")
  fun isBusinessSaved(businessId: String): Flow<Boolean>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSavedBusiness(business: SavedBusinessEntity)

  @Query("DELETE FROM saved_businesses WHERE businessId = :businessId")
  suspend fun deleteSavedBusiness(businessId: String)

  @Query("DELETE FROM saved_businesses WHERE businessId = :businessId AND userId = :userId")
  suspend fun deleteSavedBusinessByUser(businessId: String, userId: String)

  // ==========================================
  // AUCTION ITEMS & BIDDING ESCROW
  // ==========================================
  @Query("SELECT * FROM auction_items ORDER BY createdAt DESC")
  fun getAllAuctionItems(): Flow<List<AuctionItemEntity>>

  @Query("SELECT * FROM auction_items WHERE status = 'ACTIVE' ORDER BY endTime ASC")
  fun getActiveAuctionItems(): Flow<List<AuctionItemEntity>>

  @Query("SELECT * FROM auction_items WHERE id = :id LIMIT 1")
  suspend fun getAuctionItemById(id: String): AuctionItemEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAuctionItem(item: AuctionItemEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAuctionItems(items: List<AuctionItemEntity>)

  @Update
  suspend fun updateAuctionItem(item: AuctionItemEntity)

  @Query("UPDATE auction_items SET status = :status, winningBidderId = :winnerId, winningBidderName = :winnerName, logisticsStatus = :logisticsStatus WHERE id = :id")
  suspend fun closeAuctionItem(id: String, status: String, winnerId: String?, winnerName: String?, logisticsStatus: String = "AWAITING_SELLER_HANDOVER")

  @Query("UPDATE auction_items SET logisticsStatus = :logisticsStatus, storageReceivedAt = :receivedAt, storageShelfSlot = :shelfSlot WHERE id = :id")
  suspend fun updateAuctionStorageHandover(id: String, logisticsStatus: String = "IN_BCN_STORAGE", receivedAt: Long, shelfSlot: String)

  @Query("UPDATE auction_items SET logisticsStatus = :logisticsStatus, dispatchTrackingId = :trackingId WHERE id = :id")
  suspend fun updateAuctionStorageDispatch(id: String, logisticsStatus: String = "OUT_FOR_DELIVERY", trackingId: String)

  @Query("UPDATE auction_items SET logisticsStatus = :logisticsStatus, deliveredAt = :deliveredAt, escrowReleased = 1 WHERE id = :id")
  suspend fun updateAuctionBuyerDelivered(id: String, logisticsStatus: String = "DELIVERED_TO_BUYER", deliveredAt: Long)

  // Auction Bids
  @Query("SELECT * FROM auction_bids WHERE auctionItemId = :itemId ORDER BY bidAmount DESC")
  fun getBidsForItem(itemId: String): Flow<List<AuctionBidEntity>>

  @Query("SELECT * FROM auction_bids WHERE auctionItemId = :itemId ORDER BY bidAmount DESC")
  suspend fun getBidsForItemSync(itemId: String): List<AuctionBidEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAuctionBid(bid: AuctionBidEntity)

  @Query("UPDATE auction_bids SET isWinningBid = 0 WHERE auctionItemId = :itemId")
  suspend fun clearWinningBids(itemId: String)

  @Query("UPDATE auction_bids SET refundStatus = :refundStatus WHERE auctionItemId = :itemId AND bidderUserId != :winnerId")
  suspend fun refundNonWinningBids(itemId: String, winnerId: String, refundStatus: String = "REFUNDED")

  @Query("UPDATE auction_bids SET refundStatus = 'REFUNDED' WHERE auctionItemId = :itemId AND bidderUserId = :outbidUserId")
  suspend fun markBidRefunded(itemId: String, outbidUserId: String)

  // Product Quotations / Offers
  @Query("SELECT * FROM product_quotations WHERE productId = :productId ORDER BY timestamp DESC")
  fun getQuotationsForProduct(productId: String): Flow<List<ProductQuotationEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProductQuotation(quotation: ProductQuotationEntity)

  // Auction Notifications
  @Query("SELECT * FROM auction_notifications WHERE userId = :userId ORDER BY timestamp DESC")
  fun getAuctionNotifications(userId: String): Flow<List<AuctionNotificationEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAuctionNotification(notification: AuctionNotificationEntity)

  @Query("UPDATE auction_notifications SET isRead = 1 WHERE id = :notificationId")
  suspend fun markNotificationRead(notificationId: String)

  // ==========================================
  // BUSINESS STAR RATINGS & REVIEWS
  // ==========================================
  @Query("SELECT * FROM business_reviews ORDER BY timestamp DESC")
  fun getAllReviews(): Flow<List<BusinessReviewEntity>>

  @Query("SELECT * FROM business_reviews WHERE businessId = :businessId OR busyCardNetId = :businessId ORDER BY timestamp DESC")
  fun getReviewsForBusiness(businessId: String): Flow<List<BusinessReviewEntity>>

  @Query("SELECT * FROM business_reviews WHERE businessId = :businessId OR busyCardNetId = :businessId ORDER BY timestamp DESC")
  suspend fun getReviewsForBusinessSync(businessId: String): List<BusinessReviewEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertReview(review: BusinessReviewEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertReviews(reviews: List<BusinessReviewEntity>)

  @Query("UPDATE business_reviews SET helpfulCount = helpfulCount + 1 WHERE id = :reviewId")
  suspend fun incrementReviewHelpful(reviewId: String)

  @Query("DELETE FROM business_reviews WHERE id = :reviewId")
  suspend fun deleteReview(reviewId: String)

  // ==========================================
  // CACHED BUSINESS CARDS (OFFLINE READY)
  // ==========================================
  @Query("SELECT * FROM cached_business_cards ORDER BY lastVisitedTimestamp DESC")
  fun getAllCachedBusinessCards(): Flow<List<CachedBusinessCardEntity>>

  @Query("SELECT * FROM cached_business_cards WHERE busyCardNetId = :bcnId LIMIT 1")
  suspend fun getCachedBusinessCard(bcnId: String): CachedBusinessCardEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCachedBusinessCard(card: CachedBusinessCardEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCachedBusinessCards(cards: List<CachedBusinessCardEntity>)

  @Query("DELETE FROM cached_business_cards WHERE busyCardNetId = :bcnId")
  suspend fun deleteCachedBusinessCard(bcnId: String)
}
