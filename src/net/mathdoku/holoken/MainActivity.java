package net.mathdoku.holoken;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    
	// Define constants
	public static final int ERASER = 0;
	public static final int PEN = 1;
	public static final int PENCIL = 2;

	public static final int UPDATE_RATE = 500;
	
	// Define variables
	public SharedPreferences preferences;
	public static int theme;
	int bgColours[] = {0xFFFFFFFF, 0xFF343434};
	
	Button numbers[] = new Button[9];
	ImageButton actions[] = new ImageButton[4];
	RadioButton modes[] = new RadioButton[3];
	int modeColours[] = {0xFF99cc00,0xFF33b5e5,0x9033b5e5}; // holoblue/green

	LinearLayout topLayout, controlKeypad;
	RelativeLayout titleContainer;
	TextView timeView;
	long starttime = 0;
	String solveTime;

    public GridView kenKenGrid;
    public GridCell selectedCell;
    ProgressDialog mProgressDialog;
    final Handler mHandler = new Handler();
	final Handler mTimerHandler = new Handler();
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set up preferences
        PreferenceManager.setDefaultValues(this, R.xml.activity_settings, false);
        this.preferences = PreferenceManager.getDefaultSharedPreferences(this);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        String themePref = this.preferences.getString("alternatetheme", "0");
        theme = Integer.parseInt(themePref);
        
        setContentView(R.layout.activity_main);

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

        actions[0]= (ImageButton)findViewById(R.id.icon_new);
        actions[1]= (ImageButton)findViewById(R.id.icon_replay);
        actions[2]= (ImageButton)findViewById(R.id.icon_hint);
        actions[3]= (ImageButton)findViewById(R.id.icon_overflow);
        
        this.kenKenGrid = (GridView)findViewById(R.id.gridview);
        this.kenKenGrid.mContext = this;
        this.controlKeypad = (LinearLayout)findViewById(R.id.controls);
        this.topLayout = (LinearLayout)findViewById(R.id.container);
        this.titleContainer = (RelativeLayout)findViewById(R.id.titlecontainer);
        this.timeView = (TextView)titleContainer.findViewById(R.id.playtime);
        
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
        
        // Pen in all pencil marks/maybes on a long click
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
				    //MainActivity.this.controlKeypad.setVisibility(View.GONE);
    				MainActivity.this.makeToast(getString(R.string.puzzle_solved));
    				MainActivity.this.titleContainer.setBackgroundColor(0xFF33B5E5);
    				//kenKenGrid.mSelectorShown = false;
    				MainActivity.this.kenKenGrid.mPlayTime = System.currentTimeMillis() - starttime;
    				mTimerHandler.removeCallbacks(playTimer);
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
							if(MainActivity.this.kenKenGrid.mActive)
								MainActivity.this.newGameDialog();
							else
								MainActivity.this.createNewGame();
        					break;
        				case R.id.icon_replay:
							if(MainActivity.this.kenKenGrid.mGridSize > 3)
								MainActivity.this.restartGameDialog();
        					break;
        				case R.id.icon_hint:
        					if(MainActivity.this.kenKenGrid.mActive)
        						MainActivity.this.checkProgress();
        					break;
        				case R.id.icon_overflow:
        					MainActivity.this.openOptionsMenu();
        			}
        		}
        	});
        
        SaveGame saver = new SaveGame();
        restoreSaveGame(saver);
	    this.topLayout.setBackgroundColor(bgColours[theme]);
    	this.kenKenGrid.setTheme(theme);
        
    }
    
    protected void onActivityResult(int requestCode, int resultCode,
    	      Intent data) {
  	    if (requestCode != 7 || resultCode != Activity.RESULT_OK)
  	      return;
  	    Bundle extras = data.getExtras();
  	    String filename = extras.getString("filename");
  	    Log.d("HoloKen", "Loading game: " + filename);
    	SaveGame saver = new SaveGame(filename);
    	restoreSaveGame(saver);
    }
    
    public void onPause() {
    	if (this.kenKenGrid.mGridSize > 3) {
        	this.kenKenGrid.mPlayTime = System.currentTimeMillis() - starttime;
        	mTimerHandler.removeCallbacks(playTimer);
        	// NB: saving solved games messes up the timer?
        	SaveGame saver = new SaveGame();
        	saver.Save(this.kenKenGrid);

    	}
    	super.onPause();
    }
    
    public void onResume() {
    	loadPreferences();
    
	    this.kenKenGrid.mDupedigits = this.preferences.getBoolean("duplicates", true);
	    this.kenKenGrid.mBadMaths = this.preferences.getBoolean("badmaths", true);
	    this.kenKenGrid.mShowOperators = this.preferences.getBoolean("showoperators", true);
	    //alternatetheme
	    if (this.kenKenGrid.mActive) {
	    	this.kenKenGrid.requestFocus();
	    	this.kenKenGrid.invalidate();
	    	starttime = System.currentTimeMillis() - this.kenKenGrid.mPlayTime;
	    	mTimerHandler.postDelayed(playTimer, 0);
	    }
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
                Intent i = new Intent(this, SaveGameListActivity.class);
                startActivityForResult(i, 7);
         		break;
         	case R.id.menu_share:
         		if(kenKenGrid.mGridSize > 3)
         			getScreenShot();
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
 			case R.id.menu_show_mistakes:
 				this.kenKenGrid.markInvalidChoices();
 				break;
			case R.id.menu_reveal_cell:
				selectedCell.setUserValue(selectedCell.mValue);
				selectedCell.mCheated = true;
				this.kenKenGrid.invalidate();
				break;
   	 		case R.id.menu_reveal_cage:
   	 			this.kenKenGrid.Solve(this.kenKenGrid.mCages.get(
						selectedCell.mCageId).mCells);
   	 			break;
   	 		case R.id.menu_show_solution:
				this.kenKenGrid.Solve(this.kenKenGrid.mCells);
		   		break;
 		}
 		
   		Toast.makeText(this, R.string.toast_cheated, Toast.LENGTH_SHORT).show();
		return super.onContextItemSelected(item);
    }
   
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && 
        		keyCode == KeyEvent.KEYCODE_BACK && this.kenKenGrid.mSelectorShown) {
	      	this.kenKenGrid.requestFocus();
	      	this.kenKenGrid.mSelectorShown = false;
	      	this.kenKenGrid.invalidate();
	      	return true;
        }
    	return super.onKeyDown(keyCode, event);
    }
  
    /***************************
     * Helper functions to create new game
     ***************************/  

    public void loadPreferences() {
    	// Re-check preferences
        String themePref = this.preferences.getString("alternatetheme", "0");
        int newtheme = Integer.parseInt(themePref);
        if (theme != newtheme) {
		    this.topLayout.setBackgroundColor(bgColours[newtheme]);
	    	this.kenKenGrid.setTheme(newtheme);
	    	theme = newtheme;
        }
        
	    if (this.preferences.getBoolean("keepscreenon", true))
	    	this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	    else
	    	this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	    
	    if (this.preferences.getBoolean("showfullscreen", true)) {
	    	this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
	    	this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
	    }
	    else {
	    	this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
	    	this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
	    }
	    
	    if (this.preferences.getBoolean("showtimer", true))
		    this.timeView.setVisibility(View.VISIBLE);
	    else
	    	this.timeView.setVisibility(View.INVISIBLE);
	    
    }
    
    public void createNewGame() {
    	// Check preferences for new game
 		String gridSizePref = this.preferences.getString("defaultgamegrid", "ask");		
 		if (gridSizePref.equals("ask")) {
 			newGameDialog();
 		}
 		else {
 			int gridSize = Integer.parseInt(gridSizePref); 	
 			startNewGame(gridSize);
 		}
    }

	public void startNewGame(final int gridSize) {
    	kenKenGrid.mGridSize = gridSize;
		//titleContainer.setBackgroundColor(@);
    	titleContainer.setBackgroundResource(R.drawable.menu_button);
    	showDialog(0);
    	Thread t = new Thread() {
			public void run() {
				MainActivity.this.kenKenGrid.reCreate();
				MainActivity.this.mHandler.post(newGameReady);
			}
    	};
    	t.start();
    }
	
    // Create runnable for posting
    final Runnable newGameReady = new Runnable() {
        public void run() {
        	MainActivity.this.dismissDialog(0);	    
        	MainActivity.this.topLayout.setBackgroundColor(bgColours[theme]);
        	MainActivity.this.kenKenGrid.setTheme(theme);
        	MainActivity.this.setButtonVisibility(kenKenGrid.mGridSize);
        	MainActivity.this.kenKenGrid.invalidate();
        	starttime = System.currentTimeMillis();
        	mTimerHandler.postDelayed(playTimer, 0);
        }
    };
        
	public void setButtonVisibility(int gridSize) {
    	for (int i=0; i<9; i++) {
    		this.numbers[i].setEnabled(true);
    		if (i>=gridSize)
    			this.numbers[i].setEnabled(false);
    	}	
		this.controlKeypad.setVisibility(View.VISIBLE);
    }
	
	//runs without timer be reposting self
	Runnable playTimer = new Runnable() {
        @Override
        public void run() {
           long millis = System.currentTimeMillis() - starttime;
           convertTimetoStr(millis);
           mTimerHandler.postDelayed(this, UPDATE_RATE);
        }
    };

	public void convertTimetoStr(long time) {
        int seconds = (int) (time / 1000);
        int minutes = seconds / 60 % 60;
        int hours   = seconds / 3600;
        seconds     = seconds % 60;

        timeView.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
	}
	
	public void restoreSaveGame(SaveGame saver) {
	    if (saver.Restore(this.kenKenGrid)) {
	    	this.setButtonVisibility(this.kenKenGrid.mGridSize);
	    	if(!this.kenKenGrid.isSolved()) {
	    		this.kenKenGrid.mActive = true;
	    		titleContainer.setBackgroundResource(R.drawable.menu_button);
	    	}
	    	else {
	    		this.kenKenGrid.mActive = false;
	    		titleContainer.setBackgroundColor(0xFF33B5E5);
	    		mTimerHandler.removeCallbacks(playTimer);
	    	}
	    	this.kenKenGrid.invalidate();
	    }
	}
    /***************************
     * Helper functions to modify KenKen grid cells
     ***************************/  
    
    public void enterNumber (int number) {
    	GridCell selectedCell = this.kenKenGrid.mSelectedCell;
    	if (!kenKenGrid.mActive)
    		return;
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
    	selectedCell = this.kenKenGrid.mSelectedCell;
    	if (!kenKenGrid.mActive)
    		return;
    	if (selectedCell == null)
    		return;
    	
    	if (modes[PEN].isChecked()) {
    		selectedCell.mSelectedPaint.setColor(modeColours[PEN]); //blue
    		if (selectedCell.mPossibles.size() == 1)
    			selectedCell.setUserValue(selectedCell.mPossibles.get(0));
    	}
    	else if (modes[PENCIL].isChecked()) {
    		selectedCell.mSelectedPaint.setColor(modeColours[PENCIL]); //blue
    		if (selectedCell.isUserValueSet()) {
				int x = selectedCell.mUserValue;
				selectedCell.clearUserValue();
				selectedCell.togglePossible(x);
    		}
    	}
    	else if (modes[ERASER].isChecked()) {
    		selectedCell.mSelectedPaint.setColor(modeColours[ERASER]); //green
    		selectedCell.mPossibles.clear();
    		selectedCell.clearUserValue();
    	}
    	
    	this.kenKenGrid.requestFocus();
    	this.kenKenGrid.invalidate();
    }       
    
    
    public void checkProgress() {
    	String string = kenKenGrid.countMistakes() + " " +
    			getString(R.string.toast_progress);
		makeToast(string);
    }
    
    
    public void getScreenShot() {
    	//File file = new File(Environment.getExternalStorageDirectory() + "/HoloKen/");
        File path = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES)+"/HoloKen/");
        if (!path.exists()) 
        	   path.mkdir();

        GridView grid= (GridView)findViewById(R.id.gridview);
		for (GridCell cell : grid.mCells)
			cell.mSelected = false;
        grid.setDrawingCacheEnabled(true);
    	String filename = "/holoken_"+ grid.mGridSize + "_" +
    			new SimpleDateFormat("yyMMddHHmm").format(new Date())+".png";

        //Bitmap bitmap = loadBitmapFromView(grid);
		Bitmap bitmap = grid.getDrawingCache();
        File file = new File(path,filename);
        //File file = new File("/mnt/sdcard/Pictures/test.png");
        try  {
            file.createNewFile();
            FileOutputStream ostream = new FileOutputStream(file);
            bitmap.compress(CompressFormat.PNG, 90, ostream);
            ostream.flush();
            ostream.close();
        } 
        catch (Exception e)          {
            e.printStackTrace();
        }
        grid.destroyDrawingCache();
		makeToast(getString(R.string.puzzle_screenshot)+path);
		
		// Initiate sharing dialog
		Intent share = new Intent(Intent.ACTION_SEND);
		share.setType("image/png");
		share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
		startActivity(Intent.createChooser(share, getString(R.string.menu_share)));

    }
    
    
    /***************************
     * Functions to create various alert dialogs
     ***************************/   
    
    @Override
    protected Dialog onCreateDialog(int id) {
	    mProgressDialog = new ProgressDialog(this);
	    mProgressDialog.setMessage(getResources().getString(R.string.dialog_building_msg));
	    mProgressDialog.setIndeterminate(false);
	    mProgressDialog.setCancelable(false);
	    return mProgressDialog;
    }
    
    // Create a new game dialog menu and return default grid size
    public void newGameDialog() {
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
    				   MainActivity.this.startNewGame(item+4);
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
    	        	    MainActivity.this.kenKenGrid.mActive = true;
        				MainActivity.this.titleContainer.setBackgroundResource(R.drawable.menu_button);
        	        	starttime = System.currentTimeMillis();
        	        	mTimerHandler.postDelayed(playTimer, 0);
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


    public void makeToast(String string) {
    	Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT).show();
    }

}