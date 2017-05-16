package es.lombrinus.projects.mods.videos;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/**
 * Activity contenedora de los fragment para reproducir vídeo.
 * Estará a pantalla completa y se le puede pasar un color para el fondo del fragment.
 *
 * @author anthorlop
 */
public class EasyVideoActivity extends AppCompatActivity implements EasyVideoFragment.EasyVideoFragmentListener
{
    protected final static String VIDEO_FRAGMENT_TAG = "video_fragment_tag";
    public final static String ARG_URL_VIDEO = "url_video";
    public final static String ARG_URL_ADS = "url_ads";
    public final static String ARG_BACKGROUND_COLOR = "arg_background_color";
    public final static String ARG_FORCE_MEDIAPLAYER = "url_force_mp";

    //---------------------
    //RESULT
    //---------------------
    public final static int URL_VIDEO_NULL = EasyVideoFragment.URL_VIDEO_NULL;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        //Configuración del color de background de la activity
        if (getIntent() != null && !TextUtils.isEmpty(getIntent().getStringExtra(ARG_BACKGROUND_COLOR)))
        {
            try
            {
                findViewById(R.id.activity_video_lay).setBackgroundColor(Color.parseColor(getIntent().getStringExtra(ARG_BACKGROUND_COLOR)));
            } catch (Exception e)
            {
                e.printStackTrace();
//                v.setBackgroundColor(Color.parseColor(DEFAUL_BACKGROUND_COLOR));
            }
        }

        init();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            toggleStatatusBar(true);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void toggleStatatusBar(boolean hasToShowStatusBar)
    {
        if (hasToShowStatusBar)
        {
            Window window = getWindow();
            View decorView = window.getDecorView();

            window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);


            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null)
                actionBar.hide();
        } else
        {
            Window window = getWindow();
            View decorView = window.getDecorView();

            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

            int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
            decorView.setSystemUiVisibility(uiOptions);

            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null)
                actionBar.show();
        }
    }

    protected void init()
    {
        EasyVideoFragment ueVideoFragment = (EasyVideoFragment) getSupportFragmentManager().findFragmentByTag(VIDEO_FRAGMENT_TAG);

        //Búsqueda del fragment
        Intent i = getIntent();
        if (i != null && ueVideoFragment == null)
        {
            ueVideoFragment = EasyVideoFragment.newInstance(i.getStringExtra(ARG_URL_VIDEO), i.getStringExtra(ARG_URL_ADS), true, i.getBooleanExtra(ARG_FORCE_MEDIAPLAYER, false), i.getStringExtra(ARG_BACKGROUND_COLOR));
            ueVideoFragment.setUEVideoFragmentListener(this);
            getSupportFragmentManager().beginTransaction().replace(R.id.video_fragment, ueVideoFragment, VIDEO_FRAGMENT_TAG).commitAllowingStateLoss();
        }
    }

    //Listener , sobreescribir en la app si se desea algún comportamiento como trankear eventos

    @Override
    public void onAdsStart(int duration)
    {

    }

    @Override
    public void onAdsPause()
    {

    }

    @Override
    public void onAdsCompleted(boolean isErrorFinish)
    {

    }

    @Override
    public void onAdsVideoClick()
    {

    }

    @Override
    public void onAdsError(int codeError, int extra)
    {

    }

    @Override
    public void onVideoPlay(int duration)
    {

    }

    @Override
    public void onVideoPause()
    {

    }

    @Override
    public void onVideoCompleted(boolean isErrorFinish)
    {

    }

    @Override
    public void onVideoClick()
    {

    }

    @Override
    public void onVideoError(int codeError, int extra)
    {

    }

    @Override
    public View getVideoViewError()
    {
        return null;
    }
}
