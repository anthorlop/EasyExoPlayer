package es.project.framework.video_test;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import es.lombrinus.projects.mods.videos.EasyVideoFragment;


public class VideoActivity extends AppCompatActivity
{
    private static String VIDEO_FRAGMENT_TAG = "video_fragment_tag";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_example);

        Intent i = getIntent();

        EasyVideoFragment eVideoFragment = (EasyVideoFragment) getSupportFragmentManager().findFragmentByTag(VIDEO_FRAGMENT_TAG);

        if (i != null && eVideoFragment == null)
        {
            eVideoFragment = EasyVideoFragment.newInstance(i.getStringExtra(EasyVideoFragment.ARG_VIDEO_URL), i.getStringExtra(EasyVideoFragment.ARG_ADS_URL), true, false, i.getStringExtra(EasyVideoFragment.ARG_BACKGROUND_COLOR));
            getSupportFragmentManager().beginTransaction().replace(es.lombrinus.projects.mods.videos.R.id.video_fragment, eVideoFragment, VIDEO_FRAGMENT_TAG).commitAllowingStateLoss();
        }
    }
}
