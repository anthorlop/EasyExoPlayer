package es.lombrinus.projects.mods.videos.dailymotion.components;

/**
 * Created by antonio.hormigo on 11/10/16.
 */

public interface DMEventListener {

    void onStart();
    void onPlay();
    void onPause();
    void onProgress(int progress);
    void onBuffered(double progress);
    void onRebuffering(boolean rebuffering);
    void onError(String error);
    void onFullscreenChange(boolean fullscreen);
    void onFinished();

}
