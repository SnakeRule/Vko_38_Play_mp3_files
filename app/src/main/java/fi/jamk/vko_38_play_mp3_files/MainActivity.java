package fi.jamk.vko_38_play_mp3_files;

import android.Manifest;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // list view
    private ListView listview;

    // path to mp3-files
    private String mediaPath = System.getenv("SECONDARY_STORAGE");

    // List of Strings to hold mp3-filenames
    private List<String> songs = new ArrayList<String>();

    // Mediaplayer for playing music
    private MediaPlayer mediaPlayer = new MediaPlayer();

    // Mediacontroller
    private MediaController mediaController;

    // use AsyncTask to load filenames
    private LoadSongsTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        listview = (ListView) findViewById(R.id.listView);
        mediaPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).toString();
        int o = new File(mediaPath).listFiles().length;

        // item listener
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    mediaPlayer.reset();

                    mediaPlayer.setDataSource(songs.get(position));
                    mediaPlayer.prepare();
                    mediaPlayer.start();

                } catch(IOException e){
                    Toast.makeText(getBaseContext(), "Cannot start the audio", Toast.LENGTH_SHORT).show();
                }
            }
        });

        task = new LoadSongsTask();
        task.execute();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mediaPlayer.isPlaying()) mediaPlayer.reset();
    }

    private class LoadSongsTask extends AsyncTask<Void, String, Void> {
        private List <String> loadedSongs = new ArrayList<String>();

        protected void onPreExecute() {
            Toast.makeText(getApplicationContext(),"Loading, please wait", Toast.LENGTH_LONG).show();
        }

        protected Void doInBackground(Void... url){
                updateSongListRecursive(new File(mediaPath));
            return null;
        }

        public void updateSongListRecursive(File path) {
            if(path.isDirectory()) {
                String lel = path.getPath();

                for (int i = 0; i < path.listFiles().length; i++) {
                    File file = path.listFiles()[i];
                    updateSongListRecursive(file);
                }
            } else {
                String name = path.getAbsolutePath();
                publishProgress(name);
                if (name.endsWith(".mp3")) {
                    loadedSongs.add(name);
                }
            }
        }

        protected void onPostExecute(Void args) {
            ArrayAdapter<String> songList = new
                    ArrayAdapter<String>(MainActivity.this,
                    android.R.layout.simple_list_item_1, loadedSongs);
            listview.setAdapter(songList);
            songs = loadedSongs;

            Toast.makeText(getApplicationContext(),
                    "Songs="+songs.size(),
                    Toast.LENGTH_LONG).show();
        }
    }
}
