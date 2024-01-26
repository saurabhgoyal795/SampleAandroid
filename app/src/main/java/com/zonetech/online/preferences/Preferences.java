package com.zonetech.online.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;
import java.util.Set;

public final class Preferences {

	private Preferences() {

	}

	public static final String PREFS_NAME = "ZoneTechUserPref";
	public static final String PREFS_NAME_INDEX = "ZoneTechUserPrefIndex";
	private static SharedPreferences mPrefs;
	private static PreferencesChangeListener mChangeListener;

	public static final String KEY_STUDENT_ID = "STUDENT_ID";
	public static final String KEY_SPEC_ID = "SPEC_ID";
	public static final String KEY_COURSE_ID = "COURSE_ID";
	public static final String KEY_STUDENT_CODE = "STUDENT_CODE";
	public static final String KEY_STUDENT_NAME = "STUDENT_NAME";
	public static final String KEY_STUDENT_EMAIL = "STUDENT_EMAIL";
	public static final String KEY_STUDENT_PHONE = "STUDENT_PHONE";
	public static final String KEY_COURSE_NAME = "COURSE_NAME";
	public static final String KEY_SPEC_NAME = "SPEC_NAME";
	public static final String KEY_STUDENT_ROLLNO = "STUDENT_ROLLNO";
	public static final String KEY_STUDENT_PROFILE_PIC = "STUDENT_PROFILE_PIC";
	public static final String KEY_IS_LOGIN_COMPLTED = "IS_LOGIN_COMPLTED";
	public static final String KEY_CART_COUNT = "CART_COUNT";
	public static final String KEY_IS_WELCOME_MSG_SHOWN = "IS_WELCOME_MSG_SHOWN";
	public static final String KEY_IS_NEW_USER = "IS_NEW_USER";
	public static final String KEY_STUDENT_SESSION_ID = "STUDENT_SESSION_ID";
	public static final String KEY_IS_TEST_FORM_SUBMITTED = "IS_TEST_FORM_SUBMITTED";
	public static final String KEY_USER_TOKEN = "USER_TOKEN";
	public static final String KEY_USER_TOKEN_SAVED = "USER_FCM_TOKEN_SAVED";
	public static final String KEY_INAPP_UPDATE_PRIORITY = "INAPP_UPDATE_PRIORITY";
	public static final String KEY_IS_INAPPUPDATE_ENABLED = "IS_INAPPUPDATE_ENABLED";
	public static final String KEY_IS_CUSTOM_EXTRACTOR_ENABLED = "IS_CUSTOM_EXTRACTOR_ENABLED";
	public static final String KEY_CUSTOM_EXTRACTOR_URL = "CUSTOM_EXTRACTOR_URL";
	public static final String KEY_IS_WEB_PLAYER_ENABLED = "IS_WEB_PLAYER_ENABLED";
	public static final String KEY_LAST_DOWNLOAD_DATA = "LAST_DOWNLOAD_DATA";
	public static final String KEY_FORCE_UPDATE = "FORCE_UPDATE";
	public static final String KEY_VIMEO_TOKENS = "VIMEO_TOKENS";

	private static SharedPreferences getPreferences(Context context) {
        if(mPrefs == null) {
            SharedPreferences pref = context.getApplicationContext().getSharedPreferences(PREFS_NAME_INDEX,
                    Context.MODE_PRIVATE);
            String prefName = pref.getString("current_pref", PREFS_NAME);

            mPrefs = context.getApplicationContext().getSharedPreferences(prefName,
                    Context.MODE_PRIVATE);
            mChangeListener = new PreferencesChangeListener(context);
            mPrefs.registerOnSharedPreferenceChangeListener(mChangeListener);
        }

		return mPrefs;
	}

	public static boolean clear(Context context) {
		return getPreferences(context).edit().clear().commit();
	}

	public static String get(Context context, String key, String defValue) {
		return getPreferences(context).getString(key, defValue);
	}

	public static int get(Context context, String key, int defValue) {
		return getPreferences(context).getInt(key, defValue);
	}

	public static boolean get(Context context, String key, boolean defValue) {
		return getPreferences(context).getBoolean(key, defValue);
	}

	public static float get(Context context, String key, float defValue) {
		return getPreferences(context).getFloat(key, defValue);
	}

	public static long get(Context context, String key, long defValue) {
		return getPreferences(context).getLong(key, defValue);
	}

	public static Set<String> get(Context context, String key, Set<String> defValue) {
		return getPreferences(context).getStringSet(key, defValue);
	}

	public static Map<String, ?> getAll(Context context) {
		return getPreferences(context).getAll();
	}

	public static void put(Context context, String key, boolean value) {
		getPreferences(context).edit().putBoolean(key, value).commit();
	}

	public static void put(Context context, String key, float value) {
		getPreferences(context).edit().putFloat(key, value).commit();
	}

	public static void put(Context context, String key, int value) {
		getPreferences(context).edit().putInt(key, value).commit();
	}

	public static void put(Context context, String key, long value) {
		getPreferences(context).edit().putLong(key, value).commit();
	}

	public static void put(Context context, String key, String value) {
		getPreferences(context).edit().putString(key, value).commit();
	}

	public static void put(Context context, String key, Set<String> value) {
		getPreferences(context).edit().putStringSet(key, value).commit();
	}

	public static void remove(Context context, String key) {
		getPreferences(context).edit().remove(key).commit();
	}

	public static boolean contains(Context context, String key) {
		return getPreferences(context).contains(key);
	}
}