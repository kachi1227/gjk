package com.gjk;

public interface Constants {
	
	static final long INJECTOR_PERIOD = 2000;
	static final int OFFSCREEN_PAGE_LIMIT = 500;
	static final String BASE_URL = "http://skip2milu.com/gjk/";
	static final String JSON = "json";
	static final String GCM_ID = "GCM_ID";

    static final String EXTRA_MESSAGE = "message";
    static final String PROPERTY_REG_ID = "registration_id";
    static final String PROPERTY_APP_VERSION = "appVersion";
    static final String PROPERTY_SETTING_INTERLEAVING = "interleaving";
    static final boolean PROPERTY_SETTING_INTERLEAVING_DEFAULT = false;
    static final String PROPERTY_SETTING_USE_KACHIS_CACHE = "useKachisCache";
    static final boolean PROPERTY_SETTING_USE_KACHIS_CACHE_DEFAULT = false;
    static final String PROPERTY_SETTING_SHOW_DEBUG_TOASTS = "showDebugToasts";
    static final boolean PROPERTY_SETTING_SHOW_DEBUG_TOASTS_DEFAULT = false;
    static final int GALLERY_REQUEST = 1;
	static final int CAMERA_REQUEST = 2;
	static final int PLAY_SERVICES_RESOLUTION_REQUEST = 3;
	static final String SENDER_ID = "353373511052";
	
	//PREFERENCES//
	static final String PREF_FILE_NAME = "gjk_pref";
}
