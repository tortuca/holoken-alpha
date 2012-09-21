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
	private SaveGameList mContext;
	//private Typeface mFace;

	public SaveGameListAdapter(SaveGameList context) {
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
				save1 = new SaveGame(SaveGameList.SAVEGAME_DIR + "/" + object1).ReadDate();
				save2 = new SaveGame(SaveGameList.SAVEGAME_DIR + "/" + object2).ReadDate();
			}
			catch (Exception e) {
				//
			}
			return (int) ((save2 - save1)/1000);
		}
		
	}
	
	public void refreshFiles() {
		this.mGameFiles.clear();
		File dir = new File(SaveGameList.SAVEGAME_DIR);
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
					mContext.createSaveGame();
				}
			});
			if (mContext.mCurrentSaved)
				saveButton.setEnabled(false);
			return convertView;
		}
		
		convertView = inflater.inflate(R.layout.object_savegame, null);
		
		GridView grid = (GridView)convertView.findViewById(R.id.saveGridView);
		TextView label = (TextView)convertView.findViewById(R.id.saveGameTime);

		final String saveFile = SaveGameList.SAVEGAME_DIR + this.mGameFiles.get(position-1);
		
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
		Calendar gameTime = Calendar.getInstance();
		gameTime.setTimeInMillis(grid.mDate);
		label.setText("" + DateFormat.getDateTimeInstance(
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