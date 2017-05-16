package es.lombrinus.projects.mods.videos.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import es.lombrinus.projects.mods.videos.R;

/**
 * RelativeLayout para la reprodución de Ads
 *
 * @author anthorlop
 */
public class EasyPreRollVideoView extends RelativeLayout
{
    //-----------------
    //VIDEOVIEWS
    //-----------------
    private EasyVideoView ueVideoView;

    public EasyPreRollVideoView(Context context)
    {
        super(context);
        ueVideoView = new EasyVideoView(context);
        ueVideoView.setForceMediaPlayer(true);
        init(context);
    }

    public EasyPreRollVideoView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        ueVideoView = new EasyVideoView(context, attrs);
        ueVideoView.setForceMediaPlayer(true);
        init(context);
    }

    public EasyPreRollVideoView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        ueVideoView = new EasyVideoView(context, attrs, defStyleAttr);
        ueVideoView.setForceMediaPlayer(true);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public EasyPreRollVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
        ueVideoView = new EasyVideoView(context, attrs, defStyleAttr, defStyleRes);
        ueVideoView.setForceMediaPlayer(true);
        init(context);
    }

    protected void init(Context context)
    {
        View v = inflate(context, R.layout.easyprerollvideoview, this);

        //los videos de Ads se contempla que siempre se reproduzcan nada más abrir
        ueVideoView.setAutoplay(true);

        //Se desactiva el control y canSeekForward
        ueVideoView.setShow_controls(false);
        ueVideoView.setCanSeekForward(false);


        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        ueVideoView.setLayoutParams(params);

        ((RelativeLayout) v).addView(ueVideoView);
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        ueVideoView.setEnabled(enabled);
        super.setEnabled(enabled);
    }

    public EasyVideoView getUeVideoView()
    {
        return ueVideoView;
    }

    public void destroy()
    {
        ueVideoView.destroy();
    }
//
//    @Override
//    public boolean onTouchEvent(MotionEvent event)
//    {
//        //Si se ha seleccionado que no tenga autoplay, se configura para que puedas ver el controler para darle a play
//        if(!eVideoView.isPlaying())
//        {
//            eVideoView.setShow_controls(true);
//
//        }
//        else
//            eVideoView.setShow_controls(false);
//        return super.onTouchEvent(event);
//    }
}
