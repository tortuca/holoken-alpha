package net.mathdoku.holoken;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    
	// Define constants
	public static final int ERASER = 0;
	public static final int PEN = 1;
	public static final int PENCIL = 2;

	public static final int NEW = 0;
	public static final int REPLAY = 1;
	public static final int HINT = 2;
	public static final int OVERFLOW = 3;
	
	// Define variables
	public SharedPreferences preferences;
	Button numbers[] = new Button[9];
	ImageButton actions[] = new ImageButton[4];
	RadioButton modes[] = new RadioButton[3];

    public GridView kenKenGrid;
    public GridCell selectedCell;
    ProgressDialog mProgressDialog;
    final Handler mHandler = new Handler();
    
    
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

        actions[NEW]= (ImageButton)findViewById(R.id.icon_new);
        actions[REPLAY]= (ImageButton)findViewById(R.id.icon_replay);
        actions[HINT]= (ImageButton)findViewById(R.id.icon_hint);
        actions[OVERFLOW]= (ImageButton)findViewById(R.id.icon_overflow);
        
        this.kenKenGrid = (GridView)findViewById(R.id.gridview);
        this.kenKenGrid.mContext = this;
        //this.selectedCell = this.kenKenGrid.mSelectedCell;
        
        // Set up listeners
        for (int i = 0; i<numbers.length; i++)
        	this.numbers[i].setOnClickListener(new OnClickListener() {
        		public void onClick(View v) {
        			// If in eraser mode, automatically change to pencil mode
        			if (modes[ERASER].isChecked())
        				modes[PENCIL].toggle();
        			// Convert text of button (number) to Integer
        			int d = Integer.parseInt(((Button)v).getText().toString());
        			MainActivity.this.enterNumber(d);
        		}
        	});
        
        for (int i = 0; i<modes.length; i++)
        	this.modes[i].setOnClickListener(new OnClickListener() {
        		public void onClick(View v) {
        			MainActivity.this.modifyCell();
        		}
        	});
        
        // Pen in all pencil marks/maybes
        this.modes[PEN].setOnLongClickListener(new OnLongClickListener() { 
            @Override
            public boolean onLongClick(View v) {
            	for (GridCell cell : MainActivity.this.kenKenGrid.mCells)
            		if (cell.mPossibles.size() == 1)
            			cell.setUserValue(cell.mPossibles.get(0));
            		MainActivity.this.kenKenGrid.invalidate();
            		return true;
            }
        });


        this.kenKenGrid.setOnGridTouchListener(this.kenKenGrid.new OnGridTouchListener() {
			@Override
			public void gridTouched(GridCell cell) {
				kenKenGrid.mSelectorShown = true;
				MainActivity.this.modifyCell();
			}
		});
        
        this.kenKenGrid.setSolvedHandler(this.kenKenGrid.new OnSolvedListener() {
    			@Override
    			public void puzzleSolved() {
    				MainActivity.this.makeToast("Puzzle solved");

    			}
        });
        this.kenKenGrid.setFocusable(true);
        this.kenKenGrid.setFocusableInTouchMode(true);
        registerForContextMenu(this.kenKenGrid);
        
        for (int i = 0; i<actions.length; i++)
        	this.actions[i].setOnClickListener(new OnClickListener() {
        		public void onClick(View v) {
        			switch(((ImageButton)v).getId()) {
        				case R.id.icon_new:
        					MainActivity.this.createNewGame();
        					break;
        				case R.id.icon_replay:
        					MainActivity.this.restartGameDialog();
        					break;
        				case R.id.icon_hint:
        					MainActivity.this.checkProgress();
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
         	case R.id.menu_new:
         		createNewGame();
         		break;
         	case R.id.menu_save:
         		startActivity(new Intent(this, SaveGameList.class));
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
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
    	super.onCreateContextMenu(menu, v, menuInfo);
    	if (!kenKenGrid.mActive)
    		return;
    	getMenuInflater().inflate(R.menu.solutions, menu);
        return;
    }
    
    public boolean onContextItemSelected(MenuItem item) {
   	 	GridCell selectedCell = this.kenKenGrid.mSelectedCell;
 		if (selectedCell == null)
 			return super.onContextItemSelected(item);
 		
 		switch (item.getItemId()) {
			case R.id.menu_reveal_cell: // Reveal cell
				selectedCell.setUserValue(selectedCell.mValue);
				selectedCell.mCheated = true;
				Toast.makeText(this, R.string.toast_cheated, Toast.LENGTH_SHORT).show();
				this.kenKenGrid.invalidate();
				break;
   	 		case R.id.menu_reveal_cage:
   	 			for (GridCell cell : this.kenKenGrid.mCages.get(
   	 					selectedCell.mCageId).mCells) {
					cell.setUserValue(cell.mValue);
					cell.mCheated = true;
				}
  	   		  Toast.makeText(this, R.string.toast_cheated, Toast.LENGTH_SHORT).show();
  	   		  this.kenKenGrid.invalidate();
  	   		  break;
			case R.id.menu_show_solution:
		   		  this.kenKenGrid.Solve();
		   		  break;

 		}
		return super.onContextItemSelected(item);
   }
    
    protected void onActivityResult(int requestCode, int resultCode,
  	      Intent data) {
	    if (requestCode != 7 || resultCode != Activity.RESULT_OK)
	      return;
	    Bundle extras = data.getExtras();
	    String filename = extras.getString("filename");
	    Log.d("Mathdoku", "Loading game: " + filename);
  	/*SaveGame saver = new SaveGame(filename);
      if (saver.Restore(this.kenKenGrid)) {
      	this.setButtonVisibility(this.kenKenGrid.mGridSize);
      	this.kenKenGrid.mActive = true;
      }*/
  }
  
    /***************************
     * Helper functions
     ***************************/  
    
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
    	MainActivity.this.setButtonVisibility(gridSize);
    	this.startNewGame(gridSize, false);
    }
    public void createSaveGame() {
    	makeToast("Saved Game");
    }
    
    public void enterNumber (int number) {
    	GridCell selectedCell = this.kenKenGrid.mSelectedCell;
    	if (selectedCell == null)
    		return;
    	if (modes[PENCIL].isChecked()) {
    		if (selectedCell.isUserValueSet())
    			selectedCell.clearUserValue();
    		selectedCell.togglePossible(number);
    	}
    	else {
    		selectedCell.setUserValue(number);
    		selectedCell.mPossibles.clear();
    	}
    	this.kenKenGrid.requestFocus();
    	this.kenKenGrid.invalidate();
    }   
 
    public void modifyCell() {
    	GridCell selectedCell = this.kenKenGrid.mSelectedCell;
    	if (selectedCell == null)
    		return;
    	
    	if (modes[PEN].isChecked()) {
    		if(selectedCell.mPossibles.size() == 1)
    			selectedCell.setUserValue(selectedCell.mPossibles.get(0));
    	}
    	else if (modes[PENCIL].isChecked()) {
    		if (selectedCell.isUserValueSet()) {
    			int x = selectedCell.mUserValue;
    			selectedCell.clearUserValue();
    			selectedCell.togglePossible(x);
    		}
    	}
    	else if (modes[ERASER].isChecked()) {
    		selectedCell.mPossibles.clear();
    		selectedCell.clearUserValue();
    	}
    	
    	this.kenKenGrid.requestFocus();
    	this.kenKenGrid.invalidate();
    }       
    

    // Create runnable for posting
    final Runnable newGameReady = new Runnable() {
        public void run() {
    	    //if (MainActivity.this.preferences.getBoolean("alternatetheme", true)) {
    	    	//MainActivity.this.topLayout.setBackgroundDrawable(null);
    	    	//MainActivity.this.topLayout.setBackgroundColor(0xFFA0A0CC);
    	    	///MainActivity.this.topLayout.setBackgroundResource(R.drawable.background);
    	    	MainActivity.this.kenKenGrid.setTheme(GridView.THEME_NEWSPAPER);
    	    //} else {
    	    	///MainActivity.this.topLayout.setBackgroundResource(R.drawable.background);
    	    //	MainActivity.this.kenKenGrid.setTheme(GridView.THEME_CARVED);
    	    //}
        	MainActivity.this.setButtonVisibility(kenKenGrid.mGridSize);
        	MainActivity.this.kenKenGrid.invalidate();
        }
    };
    
    public void startNewGame(final int gridSize, final boolean hideOperators) {
    	kenKenGrid.mGridSize = gridSize;

    	Thread t = new Thread() {
			public void run() {
				//MainActivity.this.kenKenGrid.reCreate(hideOperators);
				MainActivity.this.kenKenGrid.reCreate(false);
				MainActivity.this.mHandler.post(newGameReady);
			}
    	};
    	t.start();
    }
    
    public void checkProgress() {
    	String string = kenKenGrid.countMistakes() + 
    			getString(R.string.toast_progress);
		makeToast(string);
    }
    
    /***************************
     * Functions to create various alert dialogs
     ***************************/   
    
    @Override
    protected Dialog onCreateDialog(int id) {
	    mProgressDialog = new ProgressDialog(this);
	    mProgressDialog.setTitle("Building puzzle");
	    //mProgressDialog.setMessage(getResources().getString(R.string.main_ui_building_puzzle_message));
	    mProgressDialog.setIcon(android.R.drawable.ic_dialog_info);
	    mProgressDialog.setIndeterminate(false);
	    mProgressDialog.setCancelable(false);
	    return mProgressDialog;
    }
    
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
    		   .setMessage(R.string.dialog_restart_msg)
    		   .setIcon(android.R.drawable.ic_dialog_alert)
    	       .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	                dialog.cancel();
    	           }
    	       })
    	       .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	        	    MainActivity.this.kenKenGrid.clearUserValues();
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
    	
    	for (int i=0; i<9; i++) {
    		this.numbers[i].setEnabled(true);
    		if (i>=gridSize)
    			this.numbers[i].setEnabled(false);
    	}	
		/*this.solvedText.setVisibility(View.GONE);
    	if (!MainActivity.this.preferences.getBoolean("hideselector", false)) {
			this.controls.setVisibility(View.VISIBLE);
    	}*/
  
    }
	
    public void makeToast(String string) {
    	Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT).show();
    }

}