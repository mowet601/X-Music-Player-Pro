package com.riseapps.xmusic.view;

import android.Manifest;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.riseapps.xmusic.R;
import com.riseapps.xmusic.executor.AlbumArtChecker;
import com.riseapps.xmusic.executor.GenerateNotification;
import com.riseapps.xmusic.executor.SharedPreferenceSingelton;
import com.riseapps.xmusic.model.MusicService;
import com.riseapps.xmusic.model.Song;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements SongsFragment.OnFragmentInteractionListener, ArtistFragment.OnFragmentInteractionListener, PlaylistFragment.OnFragmentInteractionListener {

    private static final String DEBUG_TAG ="abc" ;
    private ArrayList<Song> songList;
    private MusicService musicService;
    private Intent playIntent;
    public boolean musicPlaying;
    private SongsFragment currentFragment;
    private SeekBar seekBar;
    private RecyclerView recyclerView;
    private CardView miniPlayer;
    private ConstraintLayout constraintLayout;


    TextView artist,title_mini,artist_mini,currentPosition,totalDuration,title;
    ImageButton play_pause,play_pause_mini,prev,next,hide,repeat;
    ImageView album_art,album_art_mini;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private TabLayout tabLayout;
    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(3);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            tab.setCustomView(mSectionsPagerAdapter.getTabView(i));
        }
        songList = new ArrayList<Song>();

        init();

        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int current = musicService.getCurrentIndex();
                int previous = current - 1;
                // If current was 0, then play the last song in the list
                if (previous < 0)
                    previous = songList.size() - 1;
                musicService.setSong(previous);
                musicService.togglePlay();
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int current = musicService.getCurrentIndex();

                int next = current + 1;
                // If current was the last song, then play the first song in the list
                if (next == songList.size())
                    next = 0;
                musicService.setSong(next);
                musicService.togglePlay();
            }
        });

        miniPlayer= (CardView) findViewById(R.id.mini_player);
        constraintLayout= (ConstraintLayout) findViewById(R.id.player);
        miniPlayer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                constraintLayout.setVisibility(View.VISIBLE);
                tabLayout.setVisibility(View.GONE);
                mViewPager.setVisibility(View.GONE);
                miniPlayer.setVisibility(View.GONE);
                return false;
            }
        });
        hide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                miniPlayer.setVisibility(View.VISIBLE);
                mViewPager.setVisibility(View.VISIBLE);
                tabLayout.setVisibility(View.VISIBLE);
                constraintLayout.setVisibility(View.GONE);
            }
        });
    }

    private View.OnClickListener togglePlayBtn = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            musicService.togglePlay();
        }
    };

    private void init() {
        title= (TextView) findViewById(R.id.name);
        title_mini= (TextView) findViewById(R.id.name_mini);
        album_art = (ImageView) findViewById(R.id.album_art);
        album_art_mini = (ImageView) findViewById(R.id.album_art_mini);
        artist = (TextView) findViewById(R.id.artist);
        artist_mini = (TextView) findViewById(R.id.artist_mini);
        play_pause= (ImageButton) findViewById(R.id.play_pause);
        play_pause_mini=(ImageButton) findViewById(R.id.play_pause_mini);
        next= (ImageButton) findViewById(R.id.next);
        prev= (ImageButton) findViewById(R.id.prev);
        hide= (ImageButton) findViewById(R.id.hide);
        repeat= (ImageButton) findViewById(R.id.repeat);
        currentPosition= (TextView) findViewById(R.id.currentPosition);
        totalDuration= (TextView) findViewById(R.id.totalDuration);
        seekBar= (SeekBar) findViewById(R.id.seekBar);
        play_pause.setOnClickListener(togglePlayBtn);
        play_pause_mini.setOnClickListener(togglePlayBtn);
    }

    private ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(final ComponentName name, IBinder service) {

            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            musicService.setSongs(songList);
            musicService.setUIControls(seekBar, currentPosition, totalDuration);

            musicService.setOnSongChangedListener(new MusicService.OnSongChangedListener() {
                @Override
                public void onSongChanged(Song song) {
                   if (!song.getImagepath().equalsIgnoreCase("no_image")) {
                       album_art.setImageURI(Uri.parse(song.getImagepath()));
                       album_art_mini.setImageURI(Uri.parse(song.getImagepath()));
                   }
                   else {
                       album_art.setImageResource(R.drawable.empty);
                       album_art_mini.setImageResource(R.drawable.empty);
                   }

                   title.setText(song.getName());
                   title_mini.setText(song.getName());

                   artist.setText(song.getArtist());
                   artist_mini.setText(song.getArtist());

                    long time=song.getDuration();
                    totalDuration.setText(String.format(Locale.getDefault(),"%d:%02d",
                            TimeUnit.MILLISECONDS.toMinutes(time),
                            TimeUnit.MILLISECONDS.toSeconds(time) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time)) ));
                }

                @Override
                public void onPlayerStatusChanged(int status) {
                    switch(status) {
                        case MusicService.PLAYING:
                            play_pause.setImageResource(R.drawable.ic_pause);
                            play_pause_mini.setImageResource(R.drawable.ic_pause);
                            musicPlaying=true;
                            break;
                        case MusicService.PAUSED:
                            play_pause.setImageResource(R.drawable.ic_play);
                            play_pause_mini.setImageResource(R.drawable.ic_play);
                            musicPlaying=false;
                            break;
                    }
                }
            });

            musicService.setSong(0);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {


        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        if (playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            startService(playIntent);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            Toast.makeText(MainActivity.this, "Bind", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        unbindService(musicConnection);
        stopService(playIntent);
        super.onDestroy();
    }

    @Override
    protected void onResume() {

        if(constraintLayout.getVisibility()==View.VISIBLE){
            miniPlayer.setVisibility(View.VISIBLE);
            mViewPager.setVisibility(View.VISIBLE);
            tabLayout.setVisibility(View.VISIBLE);
            //recyclerView.setVisibility(View.VISIBLE);
            constraintLayout.setVisibility(View.GONE);
        }
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if (musicPlaying)
       moveTaskToBack(true);
        else {
           super.onBackPressed();
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        Toast.makeText(this, "helo", Toast.LENGTH_SHORT).show();
    }

    public ArrayList<Song> getSongs() {
        return songList;
    }

    public void setSongs(ArrayList<Song> songList) {
        this.songList = songList;

    }

    public void setRecyclerView(RecyclerView recyclerView){
        this.recyclerView=recyclerView;
    }

    public MusicService getMusicService() {
        return musicService;
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        String tabTitles[] = new String[] {"Playlist","Albums", "Artist" , "All Songs","Artist","Artist"};
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return PlaylistFragment.newInstance();
                case 1:
                    return PlaylistFragment.newInstance();
                case 2:
                    return ArtistFragment.newInstance();
                case 3:
                    return SongsFragment.newInstance();
                case 4:
                    return ArtistFragment.newInstance();
                case 5:
                    return ArtistFragment.newInstance();
            }
            return PlaylistFragment.newInstance();
        }

        @Override
        public int getCount() {
            return 6;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }
        public View getTabView(int position) {
            View tab = LayoutInflater.from(MainActivity.this).inflate(R.layout.custom_tab, null);
            TextView tv = (TextView) tab.findViewById(R.id.custom_text);
            tv.setText(tabTitles[position]);
            return tab;
        }
    }
}