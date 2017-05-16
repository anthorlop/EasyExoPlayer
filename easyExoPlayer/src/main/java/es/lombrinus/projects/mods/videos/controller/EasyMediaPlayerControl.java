package es.lombrinus.projects.mods.videos.controller;

import android.widget.MediaController;

import com.google.android.exoplayer2.ExoPlayer;

/**
 * Created by antonio.hormigo on 11/5/17.
 */

public interface EasyMediaPlayerControl extends MediaController.MediaPlayerControl {

    ExoPlayer getExoPlayer();

}
