package net.mathdoku.holoken;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SaveGameListActivity extends ListActivity {
	public static final String SAVEGAME_DIR = "/data/data/net.mathdoku.holoken/";
	public static final String SAVEGAME_AUTO = SAVEGAME_DIR + "autosave";
	public static final String SAVEGAME_PREFIX_ = SAVEGAME_DIR + "savegame_";
	
	private SaveGameListAdapter mAdapter;
	public boolean mCurrentSaved;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.mAdapter = new SaveGameListAdapter(this);
		setListAdapter(this.mAdapter);
	}
	
	public void deleteGameDialog(final String filename) {
		new AlertDialog.Builder(SaveGameListActivity.this)
        .setTitle(R.string.dialog_delete_title)
        .setMessage(R.string.dialog_delete_msg)
        .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
					dialog.cancel();
                }
        })
        .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
            		SaveGameListActivity.this.deleteSaveGame(filename);
                }
        })
        .show();
	}
	
	public void deleteSaveGame(final String filename) {
		new File(filename).delete();
        mAdapter.refreshFiles();
        mAdapter.notifyDataSetChanged();		
	}
	
	public void loadSaveGame(String filename) {
        Intent i = new Intent().putExtra("filename", filename);
        setResult(Activity.RESULT_OK, i);
        finish();
	}
	
	public void currentSaveGame() {
		this.mCurrentSaved = true;
		int fileIndex;
		
		for (fileIndex = 0 ; ; fileIndex++)
			if (! new File(SAVEGAME_PREFIX_ + fileIndex).exists())
				break;
		String filename = SAVEGAME_PREFIX_ + fileIndex;
		try {
			this.copy(new File(SAVEGAME_AUTO), new File(filename));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.mAdapter.refreshFiles();
		this.mAdapter.notifyDataSetChanged();
	}
	
		
    void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);
    
        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }
    


}
