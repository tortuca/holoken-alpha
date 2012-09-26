package net.mathdoku.holoken;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.WindowManager;

public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
    
  @Override
  public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
	    if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("showfullscreen", true)) {
	    	this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
	    	this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
	    }
	    else {
	    	this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
	    	this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
	    }
      // Deprecated addPreferencesFromResources, use fragments instead?
      setTitle(R.string.settings_title);
      addPreferencesFromResource(R.xml.activity_settings); 
  }
  
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

  }
}
