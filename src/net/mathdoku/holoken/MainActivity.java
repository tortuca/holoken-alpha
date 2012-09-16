package net.mathdoku.holoken;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements ActionBar.OnNavigationListener {
public SharedPreferences preferences;
	
    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up preferences
        PreferenceManager.setDefaultValues(this, R.layout.settings_view, false);
        this.preferences = PreferenceManager.getDefaultSharedPreferences(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        // Set up the dropdown list navigation in the action bar.
        actionBar.setListNavigationCallbacks(
                // Specify a SpinnerAdapter to populate the dropdown list.
                new ArrayAdapter(
                        actionBar.getThemedContext(),
                        android.R.layout.simple_list_item_1,
                        android.R.id.text1,
                        new String[]{
                                getString(R.string.main_section_title),
                                getString(R.string.game_section_title),
                                getString(R.string.saves_section_title),
                        }),
                this);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
            getActionBar().setSelectedNavigationItem(
                    savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_SELECTED_NAVIGATION_ITEM,
                getActionBar().getSelectedNavigationIndex());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	 switch (item.getItemId()) {
         	case R.id.menu_new:
         		//int gridSize = this.preferences.getInt("defaultgamegrid", 0);
         		int gridSize = 0;
         		//newGameDialog calls createNewGame at end of dialog
         		if (gridSize == 0)	
         			newGameDialog();
         		else
         			createNewGame(gridSize);
         		break;
         	case R.id.menu_save:
         		saveGameDialog();
         		break;
         	case R.id.menu_settings:
	        	startActivity(new Intent(this, SettingsActivity.class));
	            break;
	        case R.id.menu_help:
	        	openHelpDialog();
	            break;
	        default:
	    	     return super.onOptionsItemSelected(item);
    	 }
    	 return true;
    }

    @Override
    public boolean onNavigationItemSelected(int position, long id) {
        // When the given tab is selected, show the tab contents in the container
        Fragment fragment = new DummySectionFragment();
        Bundle args = new Bundle();
        args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
        return true;
    }

    /**
     * A dummy fragment representing a section of the app, but that simply displays dummy text.
     */
    public static class DummySectionFragment extends Fragment {
        public DummySectionFragment() {
        }

        public static final String ARG_SECTION_NUMBER = "section_number";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            TextView textView = new TextView(getActivity());
            textView.setGravity(Gravity.CENTER);
            Bundle args = getArguments();
            textView.setText(Integer.toString(args.getInt(ARG_SECTION_NUMBER)));
            return textView;
        }
    }

    // Create a new game dialog menu and return default grid size
    private void newGameDialog() {
    	final CharSequence[] items = { 
    		getString(R.string.game_grid_4),
    		getString(R.string.game_grid_5),
    		getString(R.string.game_grid_6),
    		getString(R.string.game_grid_7),
    		getString(R.string.game_grid_8),
    		getString(R.string.game_grid_9),
    	};
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(R.string.menu_new)
    		   .setItems(items, new DialogInterface.OnClickListener() {
    			   public void onClick(DialogInterface dialog, int item) {	
    				   createNewGame(item+4);
    			   }
    		   })
    		   .show();
    }
    
    private void createNewGame(int gridSize) {
    	Toast.makeText(getApplicationContext(), "Size of grid: "+gridSize, Toast.LENGTH_SHORT).show();
    }
    
    // Create a Save Game dialog
    private void saveGameDialog() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);

    	builder.setTitle(R.string.dialog_save_title)
    		   .setMessage(R.string.dialog_save_game)
    	       .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	                dialog.cancel();
    	           }
    	       })
    	       .setPositiveButton(R.string.dialog_save_now, new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	        	   Toast.makeText(getApplicationContext(), R.string.dialog_save_toast, Toast.LENGTH_SHORT).show();
    	           }
    	       })
    	       .show();
    }
    // Create a Help dialog
    private void openHelpDialog() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	LayoutInflater inflater = LayoutInflater.from(this);
    	View layout = inflater.inflate(R.layout.help_dialog,
    	                               (ViewGroup) findViewById(R.id.help_layout));

    	builder.setTitle(R.string.help_section_title)
    		   .setView(layout)
    	       .setNeutralButton(R.string.about_section_title, new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	                MainActivity.this.openAboutDialog();
    	           }
    	       })
    	       .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	                dialog.cancel();
    	           }
    	       })
    	       .show();
    }

    
    // Create a About dialog
    private void openAboutDialog() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	LayoutInflater inflater = LayoutInflater.from(this);
    	View layout = inflater.inflate(R.layout.about_dialog,
    	                               (ViewGroup) findViewById(R.id.about_layout));

    	builder.setTitle(R.string.about_section_title)
    		   .setView(layout)
    	       .setNeutralButton(R.string.help_section_title, new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	                MainActivity.this.openHelpDialog();
    	           }
    	       })
    	       .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	                dialog.cancel();
    	           }
    	       })
    	       .show();
    }
    
    public void debugStr(String string) {
    	Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT).show();
    }

}
