package com.riseapps.xmusic.executor.RecycleViewAdapters;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.provider.MediaStore;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.riseapps.xmusic.R;
import com.riseapps.xmusic.component.CustomAnimation;
import com.riseapps.xmusic.component.SharedPreferenceSingelton;
import com.riseapps.xmusic.executor.Interfaces.AdapterToActivityListener;
import com.riseapps.xmusic.executor.Interfaces.MainListPlayingListener;
import com.riseapps.xmusic.executor.MyApplication;
import com.riseapps.xmusic.executor.PlaySongExec;
import com.riseapps.xmusic.model.Pojo.Album;
import com.riseapps.xmusic.model.Pojo.Song;

import java.util.ArrayList;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter {

    public ArrayList<Song> songsList;
    private Context c;
    public int count = 0;
    private MainListPlayingListener mainListPlayingListener;
    private AdapterToActivityListener adapterToActivityListener;
    private int colorSelected = Color.LTGRAY;
    private int colorNormal = Color.WHITE;
    private Cursor dataCursor;
    private int textLimit = 27;

    public SongAdapter(Context context, RecyclerView recyclerView, Cursor cursor) {
        dataCursor = cursor;
        adapterToActivityListener = (AdapterToActivityListener) context;
        SharedPreferenceSingelton sharedPreferenceSingelton = new SharedPreferenceSingelton();
        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        c = context;
        if (sharedPreferenceSingelton.getSavedInt(c,"Theme")==1) {
            colorSelected = Color.rgb(58, 58, 71);
            colorNormal = Color.rgb(22, 22, 25);
        }else if (sharedPreferenceSingelton.getSavedInt(c,"Theme")==2) {
            colorSelected = Color.rgb(163, 87, 66);
            colorNormal = Color.rgb(110, 60, 27);
        }
        else if (sharedPreferenceSingelton.getSavedInt(c,"Theme")==3) {
            colorSelected = Color.rgb(96, 77, 89);
            colorNormal = Color.rgb(73, 59, 68);
        }
        else if (sharedPreferenceSingelton.getSavedInt(c,"Theme")==4) {
            colorSelected = Color.rgb(242, 238, 220);
            colorNormal = Color.rgb(255, 253, 242);
        }
        else if (sharedPreferenceSingelton.getSavedInt(c,"Theme")==5) {
            colorSelected = Color.rgb(81, 109, 137);
            colorNormal = Color.rgb(52, 73, 94);
        }
        else if (sharedPreferenceSingelton.getSavedInt(c,"Theme")==6) {
            colorSelected = Color.rgb(58, 58, 71);
            colorNormal = Color.rgb(0, 119, 192);
        }
        else if (sharedPreferenceSingelton.getSavedInt(c,"Theme")==7) {
            colorSelected = Color.rgb(242, 207, 169);
            colorNormal = Color.rgb(255, 233, 209);
        }

    }

    public SongAdapter getInstance() {
        return SongAdapter.this;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_list_row, parent, false);
        return new SongViewHolder(view, c);
    }

    public Cursor swapCursor(Cursor cursor) {
        if (dataCursor == cursor) {
            return null;
        }
        Cursor oldCursor = dataCursor;
        this.dataCursor = cursor;
        if (cursor != null) {
            this.notifyDataSetChanged();
        }
        int short_time = new SharedPreferenceSingelton().getSavedInt(c, "Short_music_time");
        if (cursor != null && cursor.moveToFirst()) {
            songsList = new ArrayList<>();

            do {
                long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DURATION));
                if (duration > (short_time * 1000)) {
                    long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                    String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                    String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    String imagepath = "content://media/external/audio/media/" + id + "/albumart";
                    if (title.length() > textLimit)
                        title = title.substring(0, textLimit) + "...";
                    if (artist == null)
                        artist = "Unknown";
                    if (artist.length() > textLimit)
                        artist = artist.substring(0, textLimit) + "...";
                    songsList.add(new Song(id, duration, title, artist, imagepath, false));
                }
            }
            while (cursor.moveToNext());
        }

        return oldCursor;
    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        Song song = songsList.get(position);
        String name = song.getName();
        String artist = song.getArtist();
        String imagepath = song.getImagepath();

        ((SongViewHolder) holder).name.setText(name);

        ((SongViewHolder) holder).artist.setText(artist);

        Glide.with(c).load(imagepath)
                .dontAnimate()
                .centerCrop()
                .placeholder(R.drawable.dummy)
                .into(((SongViewHolder) holder).iv);
        if (new MyApplication(c).getWritableDatabase().isFavourite(song.getID())) {
            song.setFavourite(true);
            ((SongViewHolder) holder).like.setImageResource(R.drawable.ic_liked_toolbar);
        } else {
            song.setFavourite(false);
            ((SongViewHolder) holder).like.setImageResource(R.drawable.ic_like);
        }

        ((SongViewHolder) holder).songListCard.setCardBackgroundColor(song.isSelected() ? colorSelected : colorNormal);
        ((SongViewHolder) holder).song = song;


    }


    public void setMainListPlayingListener(MainListPlayingListener mainListPlayingListener) {
        this.mainListPlayingListener = mainListPlayingListener;
    }

    public void delete(int position) {
        songsList.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return (dataCursor == null) ? 0 : songsList.size();
    }

    class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        ImageView iv;
        TextView name, artist;
        ImageButton like;
        CardView songListCard;
        private Context ctx;
        Song song;

        SongViewHolder(View v, Context context) {
            super(v);
            this.ctx = context;
            iv = (ImageView) v.findViewById(R.id.album_art);
            name = (TextView) v.findViewById(R.id.name);
            artist = (TextView) v.findViewById(R.id.artist_mini);
            like = (ImageButton) v.findViewById(R.id.like);

            songListCard = (CardView) v.findViewById(R.id.song_list_card);
            like.setOnClickListener(this);
            songListCard.setOnClickListener(this);
            songListCard.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == like.getId()) {
                like.startAnimation(new CustomAnimation().likeAnimation(ctx));
                if (song.getFavourite()) {
                    new MyApplication(ctx).getWritableDatabase().deleteFavourite(song.getID());
                    like.setImageResource(R.drawable.ic_like);
                    song.setFavourite(false);
                } else {
                    new MyApplication(ctx).getWritableDatabase().insertFavourite(song.getID());
                    like.setImageResource(R.drawable.ic_liked_toolbar);
                    song.setFavourite(true);
                }
            } else if (v.getId() == songListCard.getId()) {
                if (count > 0 && count <= songsList.size()) {
                    if (song.isSelected()) {
                        song.setSelected(false);
                        count--;
                        adapterToActivityListener.onTrackLongPress(count, song.getID(), false);
                    } else {
                        song.setSelected(true);
                        count++;
                        adapterToActivityListener.onTrackLongPress(count, song.getID(), true);
                    }
                    songListCard.setCardBackgroundColor(song.isSelected() ? colorSelected : colorNormal);
                } else {
                    mainListPlayingListener.onPlayingFromTrackList();
                    new PlaySongExec(ctx, getAdapterPosition()).startPlaying();
                    new SharedPreferenceSingelton().saveAs(ctx, "Shuffle", false);
                }
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (count == 0) {
                adapterToActivityListener.onFirstTrackLongPress();
            }

            if (song.isSelected()) {
                song.setSelected(false);
                count--;
                adapterToActivityListener.onTrackLongPress(count, song.getID(), false);
            } else {
                song.setSelected(true);
                count++;
                adapterToActivityListener.onTrackLongPress(count, song.getID(), true);
            }
            songListCard.setCardBackgroundColor(song.isSelected() ? colorSelected : colorNormal);
            return true;
        }

    }
}
