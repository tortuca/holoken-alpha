package net.mathdoku.holoken;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class SaveGameListAdapter extends BaseAdapter {
	
	public ArrayList<String> mGameFiles;
	private LayoutInflater inflater;
	private SaveGameListActivity mContext;
	//private Typeface mFace;

	public SaveGameListAdapter(SaveGameListActivity context) {
		this.inflater = LayoutInflater.from(context);
		this.mContext = context;
		this.mGameFiles = new ArrayList<String>();
		this.refreshFiles();

	}
	
	public class SortSavedGames implements Comparator<String> {
		long save1 = 0;
		long save2 = 0;
		public int compare(String object1, String object2) {
			try {
				save1 = new SaveGame(SaveGameListActivity.SAVEGAME_DIR + "/" + object1).ReadDate();
				save2 = new SaveGame(SaveGameListActivity.SAVEGAME_DIR + "/" + object2).ReadDate();
			}
			catch (Exception e) {
				//
			}
			return (int) ((save2 - save1)/1000);
		}
		
	}
	
	public void refreshFiles() {
		this.mGameFiles.clear();
		File dir = new File(SaveGameListActivity.SAVEGAME_DIR);
		String[] allFiles = dir.list();
		for (String entryName : allFiles)
			if (entryName.startsWith("savegame_"))
				this.mGameFiles.add(entryName);
		
		Collections.sort((List<String>)this.mGameFiles, new SortSavedGames());
		
	}

	public int getCount() {
		return this.mGameFiles.size() + 1;
	}

	public Object getItem(int arg0) {
		if (arg0 == 0)
			return "";
		return this.mGameFiles.get(arg0-1);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		if (position == 0) {
			convertView = inflater.inflate(R.layout.activity_savegame, null);
			
			final Button saveButton = (Button)convertView.findViewById(R.id.savebutton);
			saveButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					saveButton.setEnabled(false);
					mContext.currentSaveGame();
				}
			});
			if (mContext.mCurrentSaved)
				saveButton.setEnabled(false);
			return convertView;
		}
		
		convertView = inflater.inflate(R.layout.object_savegame, null);
		
		GridView grid = (GridView)convertView.findViewById(R.id.saveGridView);
		TextView gametitle = (TextView)convertView.findViewById(R.id.saveGameTitle);
		TextView datetime = (TextView)convertView.findViewById(R.id.saveDateTime);

		final String saveFile = SaveGameListActivity.SAVEGAME_DIR + this.mGameFiles.get(position-1);
		
		grid.mContext = this.mContext;
		grid.mActive = false;
	    grid.mDupedigits = PreferenceManager.getDefaultSharedPreferences(
	    		convertView.getContext()).getBoolean("duplicates", true);
	    grid.mBadMaths = PreferenceManager.getDefaultSharedPreferences(
	    		convertView.getContext()).getBoolean("badmaths", true);

		SaveGame saver = new SaveGame(saveFile);
		try {
			saver.Restore(grid);
		}
		catch (Exception e) {
			// Error, delete the file.
			new File(saveFile).delete();
			return convertView;
		}
		
		long millis = grid.mPlayTime;
        int seconds = (int) (millis / 1000);
        int minutes = seconds / 60 % 60;
        int hours   = seconds / 3600;
        seconds     = seconds % 60;
		gametitle.setText(String.format("%dx%d - %02d:%02d:%02d", grid.mGridSize, 
				grid.mGridSize, hours, minutes, seconds));
		
		Calendar gameDateTime = Calendar.getInstance();
		gameDateTime.setTimeInMillis(grid.mDate);
		datetime.setText("" + DateFormat.getDateTimeInstance(
				DateFormat.MEDIUM, DateFormat.SHORT).format(grid.mDate));

		grid.setBackgroundColor(0xFFFFFFFF);
		
		for (GridCell cell : grid.mCells)
			cell.mSelected = false;
		
		ImageButton loadButton = (ImageButton)convertView.findViewById(R.id.button_play);
		loadButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mContext.loadSaveGame(saveFile);
			}
		});
		
		ImageButton deleteButton = (ImageButton)convertView.findViewById(R.id.button_delete);
		deleteButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mContext.deleteGameDialog(saveFile);
			}
		});
		
		return convertView;
	}

}