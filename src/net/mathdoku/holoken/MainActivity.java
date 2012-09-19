package net.mathdoku.holoken;

import android.app.ActionBar;
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
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;


public class MainActivity extends Activity {
    
	// Define variables
	public SharedPreferences preferences;
	Button number[] = new Button[9];
	ImageButton mode[] = new ImageButton[3];
	ImageButton action[] = new ImageButton[4];
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Set up preferences
        PreferenceManager.setDefaultValues(this, R.layout.activity_settings, false);
        this.preferences = PreferenceManager.getDefaultSharedPreferences(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Associate variables with views
        number[0] = (Button)findViewById(R.id.button1);
        number[1] = (Button)findViewById(R.id.button2);
        number[2] = (Button)findViewById(R.id.button3);
        number[3] = (Button)findViewById(R.id.button4);
        number[4] = (Button)findViewById(R.id.button5);
        number[5] = (Button)findViewById(R.id.button6);
        number[6] = (Button)findViewById(R.id.button7);
        number[7] = (Button)findViewById(R.id.button8);
        number[8] = (Button)findViewById(R.id.button9);
        
        mode[0]= (ImageButton)findViewById(R.id.button_pen);
        mode[1]= (ImageButton)findViewById(R.id.button_pencil);
        mode[2]= (ImageButton)findViewById(R.id.button_eraser);
        
        action[0]= (ImageButton)findViewById(R.id.icon_new);
        action[1]= (ImageButton)findViewById(R.id.icon_save);
        action[2]= (ImageButton)findViewById(R.id.icon_hint);
        action[3]= (ImageButton)findViewById(R.id.icon_overflow);
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
    

    // Create a Save Game dialog
    public void saveGameDialog() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

    	builder.setTitle(R.string.dialog_save_title)
    		   .setMessage(R.string.dialog_save_msg)
    	       .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	                dialog.cancel();
    	           }
    	       })
    	       .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	        	   MainActivity.this.createSaveGame();
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