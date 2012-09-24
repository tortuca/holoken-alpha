package net.mathdoku.holoken;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.app.Activity;
import android.content.Intent;

public class StatsActivity extends Activity {
	//private SavedGameListAdapter mAdapter;
	//public boolean mCurrentSaved;
	ImageButton actions[] = new ImageButton[4];
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stats);
		
        actions[0]= (ImageButton)findViewById(R.id.icon_back);
        actions[1]= (ImageButton)findViewById(R.id.icon_new);
        actions[2]= (ImageButton)findViewById(R.id.icon_save);
        actions[3]= (ImageButton)findViewById(R.id.icon_overflow);
        
        for (int i = 0; i<actions.length; i++)
        	this.actions[i].setOnClickListener(new OnClickListener() {
        		public void onClick(View v) {
        			switch(((ImageButton)v).getId()) {
        				case R.id.icon_back:
							//return back
        					break;
        				case R.id.icon_new:
							
        					break;
        				case R.id.icon_save:
        					//Intent i = new Intent(this, SaveGameListActivity.class);
        		            //startActivityForResult(i, 7);
        					break;
        				case R.id.icon_overflow:
        					StatsActivity.this.openOptionsMenu();
        			}
        		}
        	});
	}
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	menu.findItem(R.id.menu_new).setEnabled(false);
    	menu.findItem(R.id.menu_save).setEnabled(false);
    	menu.findItem(R.id.menu_stats).setEnabled(false);
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
         	case R.id.menu_settings:
	        	startActivity(new Intent(this, SettingsActivity.class));
	            break;
	        case R.id.menu_help:
	        	//MainActivity.openHelpDialog();
	            break;
	        default:
	    	     return super.onOptionsItemSelected(item);
    	 }
    	 return true;
    }
}