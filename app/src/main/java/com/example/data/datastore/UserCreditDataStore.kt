package com.example.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.BusinessCertificateType
import com.example.data.model.CreditTransaction
import com.example.data.model.CreditTransactionType
import com.example.data.model.GovernmentIdType
import com.example.data.model.KycVerificationData
import com.example.data.model.KycVerificationStatus
import com.example.data.model.SocietyMaintenancePaymentRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

private val Context.creditDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_credits_and_kyc")

/**
 * DataStore implementation to track user credit balance, marketing expenditures,
 * app sharing rewards, and multi-step KYC verification records.
 */
class UserCreditDataStore(private val context: Context) {

  companion object {
    private val KEY_CREDIT_BALANCE = intPreferencesKey("credit_balance")
    private val KEY_TOTAL_EARNED = intPreferencesKey("total_earned_credits")
    private val KEY_TOTAL_SPENT = intPreferencesKey("total_spent_credits")
    private val KEY_SHARE_COUNT = intPreferencesKey("app_share_count")
    private val KEY_TRANSACTIONS_JSON = stringPreferencesKey("transactions_json")
    private val KEY_SOCIETY_PAYMENTS_JSON = stringPreferencesKey("society_maintenance_payments_json")

    // KYC Status and Documents Keys
    private val KEY_KYC_STATUS = stringPreferencesKey("kyc_verification_status")
    private val KEY_GOV_ID_TYPE = stringPreferencesKey("kyc_gov_id_type")
    private val KEY_GOV_ID_NUMBER = stringPreferencesKey("kyc_gov_id_number")
    private val KEY_GOV_ID_PHOTO = stringPreferencesKey("kyc_gov_id_photo_uri")
    private val KEY_FACE_FRONT_PHOTO = stringPreferencesKey("kyc_face_front_photo_uri")
    private val KEY_FACE_LEFT_PHOTO = stringPreferencesKey("kyc_face_left_photo_uri")
    private val KEY_FACE_RIGHT_PHOTO = stringPreferencesKey("kyc_face_right_photo_uri")
    private val KEY_BIZ_CERT_TYPE = stringPreferencesKey("kyc_biz_cert_type")
    private val KEY_BIZ_CERT_NUMBER = stringPreferencesKey("kyc_biz_cert_number")
    private val KEY_BIZ_CERT_PHOTO = stringPreferencesKey("kyc_biz_cert_photo_uri")
    private val KEY_APPLICANT_NAME = stringPreferencesKey("kyc_applicant_name")
    private val KEY_BUSINESS_NAME = stringPreferencesKey("kyc_business_name")
    private val KEY_SUBMITTED_TIME = longPreferencesKey("kyc_submitted_time")
    private val KEY_COMPLIANCE_NOTES = stringPreferencesKey("kyc_compliance_notes")

    const val DEFAULT_STARTING_CREDITS = 100
    const val DEFAULT_SHARE_REWARD_CREDITS = 50
  }

  val creditBalance: Flow<Int> = context.creditDataStore.data.map { preferences ->
    preferences[KEY_CREDIT_BALANCE] ?: DEFAULT_STARTING_CREDITS
  }

  val totalEarned: Flow<Int> = context.creditDataStore.data.map { preferences ->
    preferences[KEY_TOTAL_EARNED] ?: DEFAULT_STARTING_CREDITS
  }

  val totalSpent: Flow<Int> = context.creditDataStore.data.map { preferences ->
    preferences[KEY_TOTAL_SPENT] ?: 0
  }

  val shareCount: Flow<Int> = context.creditDataStore.data.map { preferences ->
    preferences[KEY_SHARE_COUNT] ?: 0
  }

  val kycStatus: Flow<KycVerificationStatus> = context.creditDataStore.data.map { preferences ->
    val statusStr = preferences[KEY_KYC_STATUS] ?: KycVerificationStatus.UNVERIFIED.name
    try {
      KycVerificationStatus.valueOf(statusStr)
    } catch (_: Exception) {
      KycVerificationStatus.UNVERIFIED
    }
  }

  val kycData: Flow<KycVerificationData> = context.creditDataStore.data.map { preferences ->
    val status = try {
      KycVerificationStatus.valueOf(preferences[KEY_KYC_STATUS] ?: KycVerificationStatus.UNVERIFIED.name)
    } catch (_: Exception) {
      KycVerificationStatus.UNVERIFIED
    }

    val idType = try {
      GovernmentIdType.valueOf(preferences[KEY_GOV_ID_TYPE] ?: GovernmentIdType.AADHAAR.name)
    } catch (_: Exception) {
      GovernmentIdType.AADHAAR
    }

    val certType = try {
      BusinessCertificateType.valueOf(preferences[KEY_BIZ_CERT_TYPE] ?: BusinessCertificateType.GST_REGISTRATION.name)
    } catch (_: Exception) {
      BusinessCertificateType.GST_REGISTRATION
    }

    KycVerificationData(
      idType = idType,
      idNumber = preferences[KEY_GOV_ID_NUMBER] ?: "",
      governmentIdPhotoUri = preferences[KEY_GOV_ID_PHOTO] ?: "",
      frontFacePhotoUri = preferences[KEY_FACE_FRONT_PHOTO] ?: "",
      leftProfilePhotoUri = preferences[KEY_FACE_LEFT_PHOTO] ?: "",
      rightProfilePhotoUri = preferences[KEY_FACE_RIGHT_PHOTO] ?: "",
      businessCertificateType = certType,
      businessCertificateNumber = preferences[KEY_BIZ_CERT_NUMBER] ?: "",
      businessCertificatePhotoUri = preferences[KEY_BIZ_CERT_PHOTO] ?: "",
      applicantFullName = preferences[KEY_APPLICANT_NAME] ?: "",
      businessName = preferences[KEY_BUSINESS_NAME] ?: "",
      submittedTimestamp = preferences[KEY_SUBMITTED_TIME] ?: 0L,
      status = status,
      complianceNotes = preferences[KEY_COMPLIANCE_NOTES] ?: ""
    )
  }

  val transactions: Flow<List<CreditTransaction>> = context.creditDataStore.data.map { preferences ->
    val jsonStr = preferences[KEY_TRANSACTIONS_JSON] ?: "[]"
    parseTransactions(jsonStr)
  }

  val societyMaintenancePayments: Flow<List<SocietyMaintenancePaymentRecord>> =
    context.creditDataStore.data.map { preferences ->
      val jsonStr = preferences[KEY_SOCIETY_PAYMENTS_JSON] ?: "[]"
      parseSocietyPayments(jsonStr)
    }

  /**
   * Increments credits whenever the user shares the app or digital card.
   */
  suspend fun incrementCreditsForShare(
    platform: String = "App Share",
    amount: Int = DEFAULT_SHARE_REWARD_CREDITS
  ): Int {
    var newBalance = 0
    context.creditDataStore.edit { preferences ->
      val currentBalance = preferences[KEY_CREDIT_BALANCE] ?: DEFAULT_STARTING_CREDITS
      val currentEarned = preferences[KEY_TOTAL_EARNED] ?: DEFAULT_STARTING_CREDITS
      val currentShareCount = preferences[KEY_SHARE_COUNT] ?: 0

      newBalance = currentBalance + amount
      preferences[KEY_CREDIT_BALANCE] = newBalance
      preferences[KEY_TOTAL_EARNED] = currentEarned + amount
      preferences[KEY_SHARE_COUNT] = currentShareCount + 1

      // Add to transaction ledger
      val currentJson = preferences[KEY_TRANSACTIONS_JSON] ?: "[]"
      val newTransaction = CreditTransaction(
        id = UUID.randomUUID().toString(),
        timestamp = System.currentTimeMillis(),
        amount = amount,
        type = CreditTransactionType.APP_SHARE_REWARD,
        description = "Reward for sharing app via $platform (+${amount} Credits)",
        balanceAfter = newBalance
      )
      preferences[KEY_TRANSACTIONS_JSON] = appendTransaction(currentJson, newTransaction)
    }
    return newBalance
  }

  /**
   * Decrements credits when initiating marketing or premium service tasks.
   * Returns true if debit succeeded, false if insufficient credits.
   */
  suspend fun spendCreditsForMarketing(
    serviceId: String,
    serviceTitle: String,
    creditCost: Int
  ): Boolean {
    var success = false
    context.creditDataStore.edit { preferences ->
      val currentBalance = preferences[KEY_CREDIT_BALANCE] ?: DEFAULT_STARTING_CREDITS
      if (currentBalance >= creditCost) {
        val currentSpent = preferences[KEY_TOTAL_SPENT] ?: 0
        val newBalance = currentBalance - creditCost

        preferences[KEY_CREDIT_BALANCE] = newBalance
        preferences[KEY_TOTAL_SPENT] = currentSpent + creditCost

        val currentJson = preferences[KEY_TRANSACTIONS_JSON] ?: "[]"
        val newTransaction = CreditTransaction(
          id = UUID.randomUUID().toString(),
          timestamp = System.currentTimeMillis(),
          amount = -creditCost,
          type = CreditTransactionType.MARKETING_BOOST_SPEND,
          description = "Marketing Campaign: $serviceTitle (-${creditCost} Credits)",
          balanceAfter = newBalance
        )
        preferences[KEY_TRANSACTIONS_JSON] = appendTransaction(currentJson, newTransaction)
        success = true
      }
    }
    return success
  }

  /**
   * Top-up digital wallet credits via simulated UPI / Card recharge.
   */
  suspend fun addWalletCredits(
    amount: Int,
    paymentMethod: String = "UPI (Instant)",
    packName: String = "Wallet Recharge"
  ): Int {
    var newBalance = 0
    context.creditDataStore.edit { preferences ->
      val currentBalance = preferences[KEY_CREDIT_BALANCE] ?: DEFAULT_STARTING_CREDITS
      val currentEarned = preferences[KEY_TOTAL_EARNED] ?: DEFAULT_STARTING_CREDITS

      newBalance = currentBalance + amount
      preferences[KEY_CREDIT_BALANCE] = newBalance
      preferences[KEY_TOTAL_EARNED] = currentEarned + amount

      val currentJson = preferences[KEY_TRANSACTIONS_JSON] ?: "[]"
      val newTransaction = CreditTransaction(
        id = UUID.randomUUID().toString(),
        timestamp = System.currentTimeMillis(),
        amount = amount,
        type = CreditTransactionType.WALLET_TOPUP,
        description = "Recharge via $paymentMethod: $packName (+${amount} Credits)",
        balanceAfter = newBalance
      )
      preferences[KEY_TRANSACTIONS_JSON] = appendTransaction(currentJson, newTransaction)
    }
    return newBalance
  }

  /**
   * Deducts credits for launching advertising campaigns.
   */
  suspend fun spendCreditsForAdCampaign(
    campaignTitle: String,
    creditCost: Int
  ): Boolean {
    var success = false
    context.creditDataStore.edit { preferences ->
      val currentBalance = preferences[KEY_CREDIT_BALANCE] ?: DEFAULT_STARTING_CREDITS
      if (currentBalance >= creditCost) {
        val currentSpent = preferences[KEY_TOTAL_SPENT] ?: 0
        val newBalance = currentBalance - creditCost

        preferences[KEY_CREDIT_BALANCE] = newBalance
        preferences[KEY_TOTAL_SPENT] = currentSpent + creditCost

        val currentJson = preferences[KEY_TRANSACTIONS_JSON] ?: "[]"
        val newTransaction = CreditTransaction(
          id = UUID.randomUUID().toString(),
          timestamp = System.currentTimeMillis(),
          amount = -creditCost,
          type = CreditTransactionType.AD_CAMPAIGN_SPEND,
          description = "Ad Campaign Launch: $campaignTitle (-${creditCost} Credits)",
          balanceAfter = newBalance
        )
        preferences[KEY_TRANSACTIONS_JSON] = appendTransaction(currentJson, newTransaction)
        success = true
      }
    }
    return success
  }

  /**
   * Deducts credits when purchasing products, templates, or services in the marketplace.
   */
  suspend fun spendCreditsForMarketplace(
    itemTitle: String,
    creditCost: Int,
    orderId: String = ""
  ): Boolean {
    var success = false
    context.creditDataStore.edit { preferences ->
      val currentBalance = preferences[KEY_CREDIT_BALANCE] ?: DEFAULT_STARTING_CREDITS
      if (currentBalance >= creditCost) {
        val currentSpent = preferences[KEY_TOTAL_SPENT] ?: 0
        val newBalance = currentBalance - creditCost

        preferences[KEY_CREDIT_BALANCE] = newBalance
        preferences[KEY_TOTAL_SPENT] = currentSpent + creditCost

        val currentJson = preferences[KEY_TRANSACTIONS_JSON] ?: "[]"
        val newTransaction = CreditTransaction(
          id = UUID.randomUUID().toString(),
          timestamp = System.currentTimeMillis(),
          amount = -creditCost,
          type = CreditTransactionType.MARKETPLACE_PURCHASE_SPEND,
          description = "Marketplace Purchase: $itemTitle (-${creditCost} Credits)",
          balanceAfter = newBalance
        )
        preferences[KEY_TRANSACTIONS_JSON] = appendTransaction(currentJson, newTransaction)
        success = true
      }
    }
    return success
  }

  /**
   * Transfers credits to another verified cardholder by BusyCardNet ID.
   */
  suspend fun transferCreditsToUser(
    recipientBcnId: String,
    amount: Int,
    note: String = ""
  ): Boolean {
    var success = false
    context.creditDataStore.edit { preferences ->
      val currentBalance = preferences[KEY_CREDIT_BALANCE] ?: DEFAULT_STARTING_CREDITS
      if (currentBalance >= amount) {
        val currentSpent = preferences[KEY_TOTAL_SPENT] ?: 0
        val newBalance = currentBalance - amount

        preferences[KEY_CREDIT_BALANCE] = newBalance
        preferences[KEY_TOTAL_SPENT] = currentSpent + amount

        val currentJson = preferences[KEY_TRANSACTIONS_JSON] ?: "[]"
        val noteSuffix = if (note.isNotBlank()) " ($note)" else ""
        val newTransaction = CreditTransaction(
          id = UUID.randomUUID().toString(),
          timestamp = System.currentTimeMillis(),
          amount = -amount,
          type = CreditTransactionType.WALLET_TRANSFER_OUT,
          description = "Transfer to $recipientBcnId: -${amount} Credits$noteSuffix",
          balanceAfter = newBalance
        )
        preferences[KEY_TRANSACTIONS_JSON] = appendTransaction(currentJson, newTransaction)
        success = true
      }
    }
    return success
  }

  /**
   * Pays Society Maintenance bill using BusyCardNet Digital Wallet Credits (1 Credit = ₹1).
   * Returns the generated verified receipt on success, or null if insufficient credits.
   */
  suspend fun paySocietyMaintenanceWithCredits(
    societyName: String,
    registrationNumber: String,
    unitOrFlatNumber: String,
    memberName: String,
    memberBcnId: String,
    billingPeriod: String,
    amountRupees: Int,
    breakdownSummary: String
  ): SocietyMaintenancePaymentRecord? {
    var createdReceipt: SocietyMaintenancePaymentRecord? = null
    context.creditDataStore.edit { preferences ->
      val currentBalance = preferences[KEY_CREDIT_BALANCE] ?: DEFAULT_STARTING_CREDITS
      if (currentBalance >= amountRupees && amountRupees > 0) {
        val currentSpent = preferences[KEY_TOTAL_SPENT] ?: 0
        val newBalance = currentBalance - amountRupees

        preferences[KEY_CREDIT_BALANCE] = newBalance
        preferences[KEY_TOTAL_SPENT] = currentSpent + amountRupees

        val receiptId = "RCPT-CHS-${UUID.randomUUID().toString().take(6).uppercase()}"
        val now = System.currentTimeMillis()

        val txJson = preferences[KEY_TRANSACTIONS_JSON] ?: "[]"
        val newTransaction = CreditTransaction(
          id = UUID.randomUUID().toString(),
          timestamp = now,
          amount = -amountRupees,
          type = CreditTransactionType.SOCIETY_MAINTENANCE_SPEND,
          description = "Society Maintenance ($unitOrFlatNumber • $billingPeriod) - $societyName",
          balanceAfter = newBalance
        )
        preferences[KEY_TRANSACTIONS_JSON] = appendTransaction(txJson, newTransaction)

        val record = SocietyMaintenancePaymentRecord(
          receiptId = receiptId,
          societyName = societyName,
          registrationNumber = registrationNumber,
          unitOrFlatNumber = unitOrFlatNumber,
          memberName = memberName,
          memberBcnId = memberBcnId,
          billingPeriod = billingPeriod,
          amountRupees = amountRupees,
          paymentMethod = "BusyCardNet Wallet Credits",
          breakdownSummary = breakdownSummary,
          timestamp = now,
          status = "PAID & VERIFIED"
        )
        val socJson = preferences[KEY_SOCIETY_PAYMENTS_JSON] ?: "[]"
        preferences[KEY_SOCIETY_PAYMENTS_JSON] = appendSocietyPayment(socJson, record)
        createdReceipt = record
      }
    }
    return createdReceipt
  }

  /**
   * Records a direct Society Maintenance bill payment via UPI / Card / NetBanking
   * and rewards +15 Loyalty Credits to the user's digital wallet.
   */
  suspend fun recordDirectSocietyMaintenancePayment(
    societyName: String,
    registrationNumber: String,
    unitOrFlatNumber: String,
    memberName: String,
    memberBcnId: String,
    billingPeriod: String,
    amountRupees: Int,
    paymentMethod: String,
    breakdownSummary: String
  ): SocietyMaintenancePaymentRecord {
    val receiptId = "RCPT-CHS-${UUID.randomUUID().toString().take(6).uppercase()}"
    val now = System.currentTimeMillis()
    val record = SocietyMaintenancePaymentRecord(
      receiptId = receiptId,
      societyName = societyName,
      registrationNumber = registrationNumber,
      unitOrFlatNumber = unitOrFlatNumber,
      memberName = memberName,
      memberBcnId = memberBcnId,
      billingPeriod = billingPeriod,
      amountRupees = amountRupees,
      paymentMethod = paymentMethod,
      breakdownSummary = breakdownSummary,
      timestamp = now,
      status = "PAID & VERIFIED"
    )

    context.creditDataStore.edit { preferences ->
      val socJson = preferences[KEY_SOCIETY_PAYMENTS_JSON] ?: "[]"
      preferences[KEY_SOCIETY_PAYMENTS_JSON] = appendSocietyPayment(socJson, record)

      // Award +15 cashback credits for paying society maintenance digitally
      val cashback = 15
      val currentBalance = preferences[KEY_CREDIT_BALANCE] ?: DEFAULT_STARTING_CREDITS
      val currentEarned = preferences[KEY_TOTAL_EARNED] ?: DEFAULT_STARTING_CREDITS
      val newBalance = currentBalance + cashback

      preferences[KEY_CREDIT_BALANCE] = newBalance
      preferences[KEY_TOTAL_EARNED] = currentEarned + cashback

      val txJson = preferences[KEY_TRANSACTIONS_JSON] ?: "[]"
      val rewardTx = CreditTransaction(
        id = UUID.randomUUID().toString(),
        timestamp = now,
        amount = cashback,
        type = CreditTransactionType.REFERRAL_REWARD,
        description = "Cashback: Society Maintenance Paid via $paymentMethod (+$cashback Cr)",
        balanceAfter = newBalance
      )
      preferences[KEY_TRANSACTIONS_JSON] = appendTransaction(txJson, rewardTx)
    }

    return record
  }

  /**
   * Adds reward credits for activity (e.g. check-in, KYC reward).
   */
  suspend fun earnFreeCredits(
    amount: Int,
    source: String
  ): Int {
    var newBalance = 0
    context.creditDataStore.edit { preferences ->
      val currentBalance = preferences[KEY_CREDIT_BALANCE] ?: DEFAULT_STARTING_CREDITS
      val currentEarned = preferences[KEY_TOTAL_EARNED] ?: DEFAULT_STARTING_CREDITS

      newBalance = currentBalance + amount
      preferences[KEY_CREDIT_BALANCE] = newBalance
      preferences[KEY_TOTAL_EARNED] = currentEarned + amount

      val currentJson = preferences[KEY_TRANSACTIONS_JSON] ?: "[]"
      val newTransaction = CreditTransaction(
        id = UUID.randomUUID().toString(),
        timestamp = System.currentTimeMillis(),
        amount = amount,
        type = CreditTransactionType.REFERRAL_REWARD,
        description = "$source (+${amount} Credits)",
        balanceAfter = newBalance
      )
      preferences[KEY_TRANSACTIONS_JSON] = appendTransaction(currentJson, newTransaction)
    }
    return newBalance
  }

  /**
   * Saves multi-step KYC submission and updates status to PENDING_REVIEW.
   */
  suspend fun submitKycVerification(data: KycVerificationData) {
    context.creditDataStore.edit { preferences ->
      preferences[KEY_KYC_STATUS] = KycVerificationStatus.PENDING_REVIEW.name
      preferences[KEY_GOV_ID_TYPE] = data.idType.name
      preferences[KEY_GOV_ID_NUMBER] = data.idNumber
      preferences[KEY_GOV_ID_PHOTO] = data.governmentIdPhotoUri
      preferences[KEY_FACE_FRONT_PHOTO] = data.frontFacePhotoUri
      preferences[KEY_FACE_LEFT_PHOTO] = data.leftProfilePhotoUri
      preferences[KEY_FACE_RIGHT_PHOTO] = data.rightProfilePhotoUri
      preferences[KEY_BIZ_CERT_TYPE] = data.businessCertificateType.name
      preferences[KEY_BIZ_CERT_NUMBER] = data.businessCertificateNumber
      preferences[KEY_BIZ_CERT_PHOTO] = data.businessCertificatePhotoUri
      preferences[KEY_APPLICANT_NAME] = data.applicantFullName
      preferences[KEY_BUSINESS_NAME] = data.businessName
      preferences[KEY_SUBMITTED_TIME] = System.currentTimeMillis()
      preferences[KEY_COMPLIANCE_NOTES] = "Submitted for verification. BusyCardNet Compliance Team reviewing documents."
    }
  }

  /**
   * Approves the submitted KYC documents (e.g. by BusyCardNet compliance team).
   * Unlocks verified badge and awards verification bonus credits!
   */
  suspend fun approveKycVerification(notes: String = "Identity and Business Certificate verified by BusyCardNet Compliance Team.") {
    context.creditDataStore.edit { preferences ->
      preferences[KEY_KYC_STATUS] = KycVerificationStatus.APPROVED.name
      preferences[KEY_COMPLIANCE_NOTES] = notes

      // Reward bonus credits for verified business completion
      val currentBalance = preferences[KEY_CREDIT_BALANCE] ?: DEFAULT_STARTING_CREDITS
      val currentEarned = preferences[KEY_TOTAL_EARNED] ?: DEFAULT_STARTING_CREDITS
      val bonus = 100
      val newBalance = currentBalance + bonus

      preferences[KEY_CREDIT_BALANCE] = newBalance
      preferences[KEY_TOTAL_EARNED] = currentEarned + bonus

      val currentJson = preferences[KEY_TRANSACTIONS_JSON] ?: "[]"
      val newTransaction = CreditTransaction(
        id = UUID.randomUUID().toString(),
        timestamp = System.currentTimeMillis(),
        amount = bonus,
        type = CreditTransactionType.ADMIN_ADJUSTMENT,
        description = "KYC Verification Completed Bonus (+${bonus} Credits)",
        balanceAfter = newBalance
      )
      preferences[KEY_TRANSACTIONS_JSON] = appendTransaction(currentJson, newTransaction)
    }
  }

  /**
   * Rejects the submitted KYC verification with reason.
   */
  suspend fun rejectKycVerification(reason: String) {
    context.creditDataStore.edit { preferences ->
      preferences[KEY_KYC_STATUS] = KycVerificationStatus.REJECTED.name
      preferences[KEY_COMPLIANCE_NOTES] = reason
    }
  }

  private fun parseTransactions(jsonStr: String): List<CreditTransaction> {
    val list = mutableListOf<CreditTransaction>()
    try {
      val jsonArray = JSONArray(jsonStr)
      for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.getJSONObject(i)
        list.add(
          CreditTransaction(
            id = obj.optString("id", UUID.randomUUID().toString()),
            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
            amount = obj.optInt("amount", 0),
            type = try {
              CreditTransactionType.valueOf(obj.optString("type", CreditTransactionType.APP_SHARE_REWARD.name))
            } catch (_: Exception) {
              CreditTransactionType.APP_SHARE_REWARD
            },
            description = obj.optString("description", ""),
            balanceAfter = obj.optInt("balanceAfter", 0)
          )
        )
      }
    } catch (_: Exception) {
      // return empty if parsing fails
    }
    return list.reversed() // most recent first
  }

  private fun appendTransaction(currentJson: String, newTransaction: CreditTransaction): String {
    return try {
      val array = JSONArray(currentJson)
      val obj = JSONObject().apply {
        put("id", newTransaction.id)
        put("timestamp", newTransaction.timestamp)
        put("amount", newTransaction.amount)
        put("type", newTransaction.type.name)
        put("description", newTransaction.description)
        put("balanceAfter", newTransaction.balanceAfter)
      }
      array.put(obj)
      // Keep at most 50 recent records
      if (array.length() > 50) {
        val trimmed = JSONArray()
        for (i in (array.length() - 50) until array.length()) {
          trimmed.put(array.get(i))
        }
        trimmed.toString()
      } else {
        array.toString()
      }
    } catch (_: Exception) {
      "[]"
    }
  }

  private fun parseSocietyPayments(jsonStr: String): List<SocietyMaintenancePaymentRecord> {
    val list = mutableListOf<SocietyMaintenancePaymentRecord>()
    try {
      val jsonArray = JSONArray(jsonStr)
      for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.getJSONObject(i)
        list.add(
          SocietyMaintenancePaymentRecord(
            receiptId = obj.optString("receiptId", "RCPT-CHS-0001"),
            societyName = obj.optString("societyName", "Co-op Housing Society"),
            registrationNumber = obj.optString("registrationNumber", ""),
            unitOrFlatNumber = obj.optString("unitOrFlatNumber", ""),
            memberName = obj.optString("memberName", ""),
            memberBcnId = obj.optString("memberBcnId", ""),
            billingPeriod = obj.optString("billingPeriod", ""),
            amountRupees = obj.optInt("amountRupees", 0),
            paymentMethod = obj.optString("paymentMethod", "UPI"),
            breakdownSummary = obj.optString("breakdownSummary", ""),
            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
            status = obj.optString("status", "PAID & VERIFIED")
          )
        )
      }
    } catch (_: Exception) {
    }
    return list.reversed()
  }

  private fun appendSocietyPayment(
    currentJson: String,
    record: SocietyMaintenancePaymentRecord
  ): String {
    return try {
      val array = JSONArray(currentJson)
      val obj = JSONObject().apply {
        put("receiptId", record.receiptId)
        put("societyName", record.societyName)
        put("registrationNumber", record.registrationNumber)
        put("unitOrFlatNumber", record.unitOrFlatNumber)
        put("memberName", record.memberName)
        put("memberBcnId", record.memberBcnId)
        put("billingPeriod", record.billingPeriod)
        put("amountRupees", record.amountRupees)
        put("paymentMethod", record.paymentMethod)
        put("breakdownSummary", record.breakdownSummary)
        put("timestamp", record.timestamp)
        put("status", record.status)
      }
      array.put(obj)
      if (array.length() > 50) {
        val trimmed = JSONArray()
        for (i in (array.length() - 50) until array.length()) {
          trimmed.put(array.get(i))
        }
        trimmed.toString()
      } else {
        array.toString()
      }
    } catch (_: Exception) {
      "[]"
    }
  }
}
