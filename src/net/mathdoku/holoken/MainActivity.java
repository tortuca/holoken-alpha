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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements ActionBar.OnNavigationListener {
	
	public SharedPreferences preferences;
	
    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

	Button numberButtons[] = new Button[9];
	ImageButton actionButtons[] = new ImageButton[6];
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Define variables
        
        // Set up preferences
        PreferenceManager.setDefaultValues(this, R.layout.activity_settings, false);
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
                                getString(R.string.saves_section_title),
                                getString(R.string.stats_section_title),
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
    		case android.R.id.home:
				// app icon in action bar clicked; go home
				Intent intent = new Intent(this, MainActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				break;
         	case R.id.menu_new:
         		// Check preferences for new game
            	Boolean showOperators = this.preferences.getBoolean("showoperators", true);
         		String gridSizePref = this.preferences.getString("defaultgamegrid", "ask");
         		
         		if (gridSizePref.equals("ask")) {
         			newGameDialog(showOperators);
         		}
         		else {
         			int gridSize = Integer.parseInt(gridSizePref);
         			createNewGame(gridSize, showOperators);
         		}
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
        //actionBar.setSelectedNavigationItem(position);
		switch(position) {
			case(0): setContentView(R.layout.activity_main);
		}
		
		/* When the given tab is selected, show the tab contents in the container
        Fragment fragment = new DummySectionFragment();
        Bundle args = new Bundle();
        args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();*/
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
    
    
    private void createNewGame(int gridSize, boolean showOperators) {
    	debugStr("Size of grid: "+gridSize);
    }
    private void createSavedGame() {
    	debugStr("Saved Game");
    }
    
    
 
    /**
     * Functions to create various alert dialogs
     */   
    // Create a new game dialog menu and return default grid size
   private void newGameDialog(final boolean showOperators) {
    	final CharSequence[] items = { 
    		getString(R.string.grid_size_4),
    		getString(R.string.grid_size_5),
    		getString(R.string.grid_size_6),
    		getString(R.string.grid_size_7),
    		getString(R.string.grid_size_8),
    		getString(R.string.grid_size_9),
    	};
 
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(R.string.menu_new)
    		   .setItems(items, new DialogInterface.OnClickListener() {
    			   public void onClick(DialogInterface dialog, int item) {	
    				   createNewGame(item+4, showOperators);
    			   }
    		   })
    		   .show();
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
    	        	   createSavedGame();
    	           }
    	       })
    	       .show();
    }
    

    // Create a Help dialog
    private void openHelpDialog() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	LayoutInflater inflater = LayoutInflater.from(this);
    	View layout = inflater.inflate(R.layout.dialog_help,
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
    	View layout = inflater.inflate(R.layout.dialog_about,
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
    
	/*public void setButtonVisibility(int gridSize) {
    	
    	for (int i=4; i<9; i++) {
    		this.digits[i].setVisibility(View.VISIBLE);
    		if (i>=gridSize)
    			this.digits[i].setEnabled(false);
    	}	
		this.solvedText.setVisibility(View.GONE);
		this.pressMenu.setVisibility(View.GONE);
    	if (!MainActivity.this.preferences.getBoolean("hideselector", false)) {
			this.controls.setVisibility(View.VISIBLE);
    	}
  
    }*/
	
    public void debugStr(String string) {
    	Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT).show();
    }

}
