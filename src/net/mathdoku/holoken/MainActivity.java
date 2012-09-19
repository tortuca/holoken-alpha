package net.mathdoku.holoken;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;


public class MainActivity extends Activity {
    
	// Define constants
	public static final int ERASER = 0;
	public static final int PEN = 1;
	public static final int PENCIL = 2;

	public static final int REPLAY = 0;
	public static final int PAUSE = 1;
	public static final int HINT = 2;
	public static final int OVERFLOW = 3;
	
	// Define variables
	public SharedPreferences preferences;
	Button numbers[] = new Button[9];
	ImageButton actions[] = new ImageButton[4];
	RadioButton modes[] = new RadioButton[3];

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Set up preferences
        PreferenceManager.setDefaultValues(this, R.layout.activity_settings, false);
        this.preferences = PreferenceManager.getDefaultSharedPreferences(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Associate variables with views
        numbers[0] = (Button)findViewById(R.id.button1);
        numbers[1] = (Button)findViewById(R.id.button2);
        numbers[2] = (Button)findViewById(R.id.button3);
        numbers[3] = (Button)findViewById(R.id.button4);
        numbers[4] = (Button)findViewById(R.id.button5);
        numbers[5] = (Button)findViewById(R.id.button6);
        numbers[6] = (Button)findViewById(R.id.button7);
        numbers[7] = (Button)findViewById(R.id.button8);
        numbers[8] = (Button)findViewById(R.id.button9);
        
        modes[ERASER] = (RadioButton)findViewById(R.id.button_eraser);
        modes[PEN] = (RadioButton)findViewById(R.id.button_pen);
        modes[PENCIL] = (RadioButton)findViewById(R.id.button_pencil);

        actions[REPLAY]= (ImageButton)findViewById(R.id.icon_replay);
        actions[PAUSE]= (ImageButton)findViewById(R.id.icon_pause);
        actions[HINT]= (ImageButton)findViewById(R.id.icon_hint);
        actions[OVERFLOW]= (ImageButton)findViewById(R.id.icon_overflow);
        
        // Set up listeners
        for (int i = 0; i<numbers.length; i++)
        	this.numbers[i].setOnClickListener(new OnClickListener() {
        		public void onClick(View v) {
        			// If in eraser mode, automatically change to pen mode
        			if (MainActivity.this.checkMode() == ERASER)
        				modes[PENCIL].toggle();
        			// Convert text of button (number) to Integer
        			int d = Integer.parseInt(((Button)v).getText().toString());
        			MainActivity.this.enterNumber(d);
        		}
        	});
        
        for (int i = 0; i<modes.length; i++)
        	this.modes[i].setOnClickListener(new OnClickListener() {
        		public void onClick(View v) {
        			switch(((RadioButton)v).getId()) {
        				case R.id.button_pen:
        					debugStr("if mark=1, pen it it, else do nothing");
        					break;
        				case R.id.button_pencil:
        					debugStr("if pen, unmark");
        					break;
        				case R.id.button_eraser:
        					MainActivity.this.enterNumber(ERASER);
        			}
        		}
        	});
        
        for (int i = 0; i<actions.length; i++)
        	this.actions[i].setOnClickListener(new OnClickListener() {
        		public void onClick(View v) {
        			switch(((ImageButton)v).getId()) {
        				case R.id.icon_replay:
        					MainActivity.this.restartGameDialog();
        					break;
        				case R.id.icon_pause:
        					debugStr("Pause game");
        					break;
        				case R.id.icon_hint:
        					debugStr("Menu to check progress, reveal cell/cage/solution");
        					break;
        				case R.id.icon_overflow:
        					MainActivity.this.openOptionsMenu();
        			}
        		}
        	});
        
    }
    
    
    public void onPause() {
    	/*if (this.kenKenGrid.mGridSize > 3) {
	    	SaveGame saver = new SaveGame();
	    	saver.Save(this.kenKenGrid);
    	}*/
    	super.onPause();
    }
    
    public void onResume() {
    	// Re-check preferences
	    if (this.preferences.getBoolean("keepscreenon", false))
	    	this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	    else
	    	this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	    
	    //alternatetheme, dupedigits, badmaths, showoperators
	    super.onResume();
    }
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	
    	switch (item.getItemId()) {
    		/*case android.R.id.home:
				// app icon in action bar clicked; go home
				Intent intent = new Intent(this, MainActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				break;*/
         	case R.id.menu_new:
         		createNewGame();
         		break;
         	case R.id.menu_save:
         		startActivity(new Intent(this, SaveGameList.class));
         		//saveGameDialog();
         		break;
         	case R.id.menu_stats:
	        	startActivity(new Intent(this, StatsActivity.class));
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
    
    
    public void createNewGame() {
    	// Check preferences for new game
    	Boolean showOperators = this.preferences.getBoolean("showoperators", true);
 		String gridSizePref = this.preferences.getString("defaultgamegrid", "ask");
 		
 		if (gridSizePref.equals("ask")) {
 			MainActivity.this.newGameDialog(showOperators);
 		}
 		else {
 			int gridSize = Integer.parseInt(gridSizePref);
 			MainActivity.this.constructNewGame(gridSize, showOperators);
 		}
    }
    
    public void constructNewGame(int gridSize, boolean showOperators) {
    	debugStr("Size of grid: "+gridSize);
    }
    public void createSaveGame() {
    	debugStr("Saved Game");
    }
    
    public int checkMode() {
		RadioGroup modes = (RadioGroup)findViewById(R.id.modebuttons);
		switch (modes.getCheckedRadioButtonId()) {
			case R.id.button_pencil:
				return(PENCIL);
			case R.id.button_eraser:
				return(ERASER);
			default:
				return(PEN);
		}
    }
    
    public void enterNumber (int number) {
    	debugStr("Button pressed: "+number);
    }   
 
    /**
     * Functions to create various alert dialogs
     */   
    // Create a new game dialog menu and return default grid size
   public void newGameDialog(final boolean showOperators) {
    	final CharSequence[] items = { 
    		getString(R.string.grid_size_4),
    		getString(R.string.grid_size_5),
    		getString(R.string.grid_size_6),
    		getString(R.string.grid_size_7),
    		getString(R.string.grid_size_8),
    		getString(R.string.grid_size_9),
    	};
 
    	AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
    	builder.setTitle(R.string.menu_new)
    		   .setItems(items, new DialogInterface.OnClickListener() {
    			   public void onClick(DialogInterface dialog, int item) {	
    				   MainActivity.this.constructNewGame(item+4, showOperators);
    			   }
    		   })
    		   .show();
    }
    

    // Create a Restart Game dialog
    public void restartGameDialog() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

    	builder.setTitle(R.string.dialog_restart_title)
    	       .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	                dialog.cancel();
    	           }
    	       })
    	       .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	        	   debugStr("Game restarted");
    	           }
    	       })
    	       .show();
    }
    

    // Create a Help dialog
    public void openHelpDialog() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
    	LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
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
    public void openAboutDialog() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
    	LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
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
    
	public void setButtonVisibility(int gridSize) {
    	
    	for (int i=4; i<9; i++) {
    		this.numbers[i].setVisibility(View.VISIBLE);
    		if (i>=gridSize)
    			this.numbers[i].setEnabled(false);
    	}	
		/*this.solvedText.setVisibility(View.GONE);
    	if (!MainActivity.this.preferences.getBoolean("hideselector", false)) {
			this.controls.setVisibility(View.VISIBLE);
    	}*/
  
    }
	
    public void debugStr(String string) {
    	Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT).show();
    }

}