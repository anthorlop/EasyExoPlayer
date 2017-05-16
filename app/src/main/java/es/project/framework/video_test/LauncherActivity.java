package es.project.framework.video_test;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import es.lombrinus.projects.mods.videos.EasyVideoActivity;
import es.lombrinus.projects.mods.videos.dailymotion.DailymotionVideoActivity;
import es.lombrinus.projects.mods.videos.dailymotion.components.DailymotionConfiguration;

public class LauncherActivity extends Activity
{
    private static String urlVideo = "http://www.html5videoplayer.net/videos/toystory.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);


        Button play_video = (Button) findViewById(R.id.play_video);
        Button playDailymotion = (Button) findViewById(R.id.play_dailymotion);

        final EditText editText = (EditText) findViewById(R.id.etVideoIdDailymotion);

        play_video.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

                Intent i = new Intent(getApplicationContext(), EasyVideoActivity.class);
                i.putExtra(EasyVideoActivity.ARG_URL_VIDEO, urlVideo);
//               i.putExtra(EasyVideoActivity.ARG_FORCE_MEDIAPLAYER, true); // Force MediaPlayer instead of ExoPlayer although android version support it
                startActivity(i);
            }
        });

        playDailymotion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String idVideo = editText.getText().toString();
                DailymotionConfiguration config = new DailymotionConfiguration(true);

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

                Intent intent = new Intent(LauncherActivity.this, DailymotionVideoActivity.class);
                intent.putExtra(DailymotionVideoActivity.ID_VIDEO_KEY, idVideo);
                intent.putExtra(DailymotionVideoActivity.CONFIGURATION_KEY, config);
                startActivity(intent);
            }
        });
    }


}
