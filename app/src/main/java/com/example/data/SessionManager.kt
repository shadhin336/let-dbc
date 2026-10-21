package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.User
import org.json.JSONArray
import org.json.JSONObject

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("ewallet_session_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LOGGED_IN_USER_ID = "logged_in_user_id"
        private const val KEY_SAVED_PHONE = "saved_user_phone"
        private const val KEY_SAVED_PASSWORD = "saved_user_password"
        private const val KEY_USER_BACKUP_LIST = "user_backup_list"
    }

    fun saveLoggedInUser(user: User) {
        prefs.edit()
            .putLong(KEY_LOGGED_IN_USER_ID, user.id)
            .putString(KEY_SAVED_PHONE, user.phoneNumber)
            .putString(KEY_SAVED_PASSWORD, user.password)
            .apply()

        // Also add or update in backup registry
        backupUser(user)
    }

    fun getLoggedInUserId(): Long? {
        val id = prefs.getLong(KEY_LOGGED_IN_USER_ID, -1L)
        return if (id != -1L) id else null
    }

    fun getSavedPhone(): String? = prefs.getString(KEY_SAVED_PHONE, null)
    fun getSavedPassword(): String? = prefs.getString(KEY_SAVED_PASSWORD, null)

    fun clearSession() {
        prefs.edit()
            .remove(KEY_LOGGED_IN_USER_ID)
            .remove(KEY_SAVED_PHONE)
            .remove(KEY_SAVED_PASSWORD)
            .apply()
    }

    fun backupUser(user: User) {
        try {
            val jsonArray = getBackupUsersArray()
            var found = false
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                if (obj.optString("phoneNumber") == user.phoneNumber) {
                    obj.put("fullName", user.fullName)
                    obj.put("password", user.password)
                    obj.put("balance", user.balance)
                    obj.put("totalDeposit", user.totalDeposit)
                    obj.put("totalWithdraw", user.totalWithdraw)
                    obj.put("todayCommission", user.todayCommission)
                    obj.put("totalCommission", user.totalCommission)
                    obj.put("commissionBalance", user.commissionBalance)
                    obj.put("isAdmin", user.isAdmin)
                    found = true
                    break
                }
            }
            if (!found) {
                val newObj = JSONObject().apply {
                    put("phoneNumber", user.phoneNumber)
                    put("fullName", user.fullName)
                    put("password", user.password)
                    put("balance", user.balance)
                    put("totalDeposit", user.totalDeposit)
                    put("totalWithdraw", user.totalWithdraw)
                    put("todayCommission", user.todayCommission)
                    put("totalCommission", user.totalCommission)
                    put("commissionBalance", user.commissionBalance)
                    put("isAdmin", user.isAdmin)
                }
                jsonArray.put(newObj)
            }
            prefs.edit().putString(KEY_USER_BACKUP_LIST, jsonArray.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getBackupUsers(): List<User> {
        val list = mutableListOf<User>()
        try {
            val jsonArray = getBackupUsersArray()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    User(
                        phoneNumber = obj.getString("phoneNumber"),
                        fullName = obj.optString("fullName", "User"),
                        password = obj.getString("password"),
                        balance = obj.optDouble("balance", 0.0),
                        totalDeposit = obj.optDouble("totalDeposit", 0.0),
                        totalWithdraw = obj.optDouble("totalWithdraw", 0.0),
                        todayCommission = obj.optDouble("todayCommission", 0.0),
                        totalCommission = obj.optDouble("totalCommission", 0.0),
                        commissionBalance = obj.optDouble("commissionBalance", 0.0),
                        isAdmin = obj.optBoolean("isAdmin", false)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun getBackupUsersArray(): JSONArray {
        val raw = prefs.getString(KEY_USER_BACKUP_LIST, null) ?: return JSONArray()
        return try {
            JSONArray(raw)
        } catch (e: Exception) {
            JSONArray()
        }
    }
}
