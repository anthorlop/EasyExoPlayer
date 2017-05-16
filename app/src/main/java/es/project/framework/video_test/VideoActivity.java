package es.project.framework.video_test;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import es.lombrinus.projects.mods.videos.EasyVideoFragment;


public class VideoActivity extends AppCompatActivity
{
    private static String VIDEO_FRAGMENT_TAG = "video_fragment_tag";
    public static String ARG_URL_VIDEO = "url_video";
    public static String ARG_URL_ADS = "url_ads";
    public static String ARG_BACKGROUND_COLOR = "arg_background_color";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_example);

        Intent i = getIntent();

        EasyVideoFragment eVideoFragment = (EasyVideoFragment) getSupportFragmentManager().findFragmentByTag(VIDEO_FRAGMENT_TAG);

        if (i != null && eVideoFragment == null)
        {
            eVideoFragment = EasyVideoFragment.newInstance(i.getStringExtra(ARG_URL_VIDEO), i.getStringExtra(ARG_URL_ADS), true, false, i.getStringExtra(ARG_BACKGROUND_COLOR));
            getSupportFragmentManager().beginTransaction().replace(es.lombrinus.projects.mods.videos.R.id.video_fragment, eVideoFragment, VIDEO_FRAGMENT_TAG).commitAllowingStateLoss();
        }
    }
}
