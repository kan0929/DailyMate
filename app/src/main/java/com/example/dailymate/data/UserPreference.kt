package com.example.dailymate.data

import android.content.Context

// SharedPreferences의 키 값들을 상수로 정의
object PrefKeys {
    const val PREFS_NAME = "user_prefs"
    const val USER_ID = "USER_ID"
    const val FULL_NAME = "FULL_NAME"
    const val USER_EMAIL = "USER_EMAIL"
}

// 사용자 세션 정보를 저장하고 불러오는 클래스
class UserPreferences(context: Context) {

    private val sharedPref = context.getSharedPreferences(PrefKeys.PREFS_NAME, Context.MODE_PRIVATE)

    // 사용자 정보 저장
    fun saveUserInfo(userId: Int, fullName: String, userEmail: String) {
        with(sharedPref.edit()) {
            putInt(PrefKeys.USER_ID, userId)
            putString(PrefKeys.FULL_NAME, fullName)
            putString(PrefKeys.USER_EMAIL, userEmail)
            apply()
        }
    }

    // 사용자 ID 불러오기
    fun getUserId(): Int {
        return sharedPref.getInt(PrefKeys.USER_ID, -1) // 기본값 -1
    }

    // 사용자 이름 불러오기
    fun getFullName(): String {
        return sharedPref.getString(PrefKeys.FULL_NAME, "사용자") ?: "사용자"
    }

    // 사용자 이메일 불러오기
    fun getUserEmail(): String {
        return sharedPref.getString(PrefKeys.USER_EMAIL, "이메일 없음") ?: "이메일 없음"
    }
}