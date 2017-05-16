package es.lombrinus.projects.mods.videos.dailymotion.components;

import android.content.pm.ActivityInfo;
import android.os.Parcel;
import android.os.Parcelable;

import es.lombrinus.projects.mods.videos.R;

/**
 * Parcelable class to configure dailymotion player
 *
 * Created by antonio.hormigo on 13/10/16.
 */
public class DailymotionConfiguration implements Parcelable {


    private boolean autoPlay = true;
    private boolean allowFullscreen = true;
    private boolean onlyFullscreen = false;
    private int playIconResource = 0;
    private int pauseIconResource = 0;
    private int backIconResource = 0;
    private int fullscreenIconResource = 0;
    private int closeFullscreenIconResource = 0;

    private int fullscreenOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR;

    private int buttonsColor = 0;
    private int seekBarColor = R.color.dailymotion_color;
    private int currentTimeTextColor = R.color.dailymotion_color;
    private String currentTimeShortFormat = "%02d:%02d";
    private String currentTimeLongFormat = "%d:%02d:%02d";

    public DailymotionConfiguration() {
        currentTimeTextColor = R.color.dailymotion_color;
        seekBarColor = R.color.dailymotion_color;
        buttonsColor = 0;
    }

    /**
     * Simple constructor
     *
     * @param autoPlay autoplay
     */
    public DailymotionConfiguration(boolean autoPlay) {
        this.autoPlay = autoPlay;
    }

    /**
     * Colors constructor
     *
     * @param seekBarColor seekBarColor
     * @param buttonsColor buttonsColor
     * @param currentTimeTextColor currentTimeTextColor
     */
    public DailymotionConfiguration(int seekBarColor, int buttonsColor, int currentTimeTextColor) {
        this.seekBarColor = seekBarColor;
        this.buttonsColor = buttonsColor;
        this.currentTimeTextColor = currentTimeTextColor;
    }

    public DailymotionConfiguration(boolean autoPlay, boolean allowFullscreen, boolean onlyFullscreen, int fullscreenOrientation) {
        this.autoPlay = autoPlay;
        this.allowFullscreen = allowFullscreen;
        this.onlyFullscreen = onlyFullscreen;
        this.fullscreenOrientation = fullscreenOrientation;
    }

    public DailymotionConfiguration(int playIconResource, int pauseIconResource, int backIconResource, int fullscreenIconResource, int closeFullscreenIconResource) {
        this.playIconResource = playIconResource;
        this.pauseIconResource = pauseIconResource;
        this.backIconResource = backIconResource;
        this.fullscreenIconResource = fullscreenIconResource;
        this.closeFullscreenIconResource = closeFullscreenIconResource;
    }

    public DailymotionConfiguration(boolean autoPlay, boolean allowFullscreen, boolean onlyFullscreen, int playIconResource, int pauseIconResource,
                                    int backIconResource, int fullscreenIconResource, int closeFullscreenIconResource) {
        this.playIconResource = playIconResource;
        this.pauseIconResource = pauseIconResource;
        this.backIconResource = backIconResource;
        this.fullscreenIconResource = fullscreenIconResource;
        this.closeFullscreenIconResource = closeFullscreenIconResource;
    }

    public DailymotionConfiguration(boolean autoPlay, boolean allowFullscreen, boolean onlyFullscreen, int playIconResource, int pauseIconResource, int backIconResource,
                                    int fullscreenIconResource, int closeFullscreenIconResource, int buttonsColor, int seekBarColor, int currentTimeTextColor,
                                    String currentTimeShortFormat, String currentTimeLongFormat) {
        this.autoPlay = autoPlay;
        this.allowFullscreen = allowFullscreen;
        this.onlyFullscreen = onlyFullscreen;
        this.playIconResource = playIconResource;
        this.pauseIconResource = pauseIconResource;
        this.backIconResource = backIconResource;
        this.fullscreenIconResource = fullscreenIconResource;
        this.closeFullscreenIconResource = closeFullscreenIconResource;
        this.buttonsColor = buttonsColor;
        this.seekBarColor = seekBarColor;
        this.currentTimeTextColor = currentTimeTextColor;
        this.currentTimeShortFormat = currentTimeShortFormat;
        this.currentTimeLongFormat = currentTimeLongFormat;
    }

    protected DailymotionConfiguration(Parcel in) {
        autoPlay = in.readByte() != 0;
        allowFullscreen = in.readByte() != 0;
        onlyFullscreen = in.readByte() != 0;
        playIconResource = in.readInt();
        pauseIconResource = in.readInt();
        backIconResource = in.readInt();
        fullscreenIconResource = in.readInt();
        closeFullscreenIconResource = in.readInt();
        fullscreenOrientation = in.readInt();
        buttonsColor = in.readInt();
        seekBarColor = in.readInt();
        currentTimeTextColor = in.readInt();
        currentTimeShortFormat = in.readString();
        currentTimeLongFormat = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (autoPlay ? 1 : 0));
        dest.writeByte((byte) (allowFullscreen ? 1 : 0));
        dest.writeByte((byte) (onlyFullscreen ? 1 : 0));
        dest.writeInt(playIconResource);
        dest.writeInt(pauseIconResource);
        dest.writeInt(backIconResource);
        dest.writeInt(fullscreenIconResource);
        dest.writeInt(closeFullscreenIconResource);
        dest.writeInt(fullscreenOrientation);
        dest.writeInt(buttonsColor);
        dest.writeInt(seekBarColor);
        dest.writeInt(currentTimeTextColor);
        dest.writeString(currentTimeShortFormat);
        dest.writeString(currentTimeLongFormat);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DailymotionConfiguration> CREATOR = new Creator<DailymotionConfiguration>() {
        @Override
        public DailymotionConfiguration createFromParcel(Parcel in) {
            return new DailymotionConfiguration(in);
        }

        @Override
        public DailymotionConfiguration[] newArray(int size) {
            return new DailymotionConfiguration[size];
        }
    };

    /**
     * @return Gets the value of currentTimeLongFormat and returns currentTimeLongFormat
     */
    public String getCurrentTimeLongFormat() {
        return currentTimeLongFormat;
    }

    /**
     * @return Gets the value of autoPlay and returns autoPlay
     */
    public boolean isAutoPlay() {
        return autoPlay;
    }

    /**
     * @return Gets the value of allowFullscreen and returns allowFullscreen
     */
    public boolean isAllowFullscreen() {
        return allowFullscreen;
    }

    /**
     * @return Gets the value of onlyFullscreen and returns onlyFullscreen
     */
    public boolean isOnlyFullscreen() {
        return onlyFullscreen;
    }

    /**
     * @return Gets the value of playIconResource and returns playIconResource
     */
    public int getPlayIconResource() {
        return playIconResource;
    }

    /**
     * @return Gets the value of pauseIconResource and returns pauseIconResource
     */
    public int getPauseIconResource() {
        return pauseIconResource;
    }

    /**
     * @return Gets the value of backIconResource and returns backIconResource
     */
    public int getBackIconResource() {
        return backIconResource;
    }

    /**
     * @return Gets the value of fullscreenIconResource and returns fullscreenIconResource
     */
    public int getFullscreenIconResource() {
        return fullscreenIconResource;
    }

    /**
     * @return Gets the value of closeFullscreenIconResource and returns closeFullscreenIconResource
     */
    public int getCloseFullscreenIconResource() {
        return closeFullscreenIconResource;
    }

    /**
     * @return Gets the value of buttonsColor and returns buttonsColor
     */
    public int getButtonsColor() {
        return buttonsColor;
    }

    /**
     * @return Gets the value of seekBarColor and returns seekBarColor
     */
    public int getSeekBarColor() {
        return seekBarColor;
    }

    /**
     * @return Gets the value of currentTimeTextColor and returns currentTimeTextColor
     */
    public int getCurrentTimeTextColor() {
        return currentTimeTextColor;
    }

    /**
     * @return Gets the value of currentTimeShortFormat and returns currentTimeShortFormat
     */
    public String getCurrentTimeShortFormat() {
        return currentTimeShortFormat;
    }

    /**
     * @return Gets the value of fullscreenOrientation and returns fullscreenOrientation
     */
    public int getFullscreenOrientation() {
        return fullscreenOrientation;
    }

    /**
     * Set a value from autoPlay variable
     */
    public void setAutoPlay(boolean autoPlay) {
        this.autoPlay = autoPlay;
    }

    /**
     * Set a value from allowFullscreen variable
     */
    public void setAllowFullscreen(boolean allowFullscreen) {
        this.allowFullscreen = allowFullscreen;
    }

    /**
     * Set a value from onlyFullscreen variable
     */
    public void setOnlyFullscreen(boolean onlyFullscreen) {
        this.onlyFullscreen = onlyFullscreen;
    }

    /**
     * Set a value from playIconResource variable
     */
    public void setPlayIconResource(int playIconResource) {
        this.playIconResource = playIconResource;
    }

    /**
     * Set a value from pauseIconResource variable
     */
    public void setPauseIconResource(int pauseIconResource) {
        this.pauseIconResource = pauseIconResource;
    }

    /**
     * Set a value from backIconResource variable
     */
    public void setBackIconResource(int backIconResource) {
        this.backIconResource = backIconResource;
    }

    /**
     * Set a value from fullscreenIconResource variable
     */
    public void setFullscreenIconResource(int fullscreenIconResource) {
        this.fullscreenIconResource = fullscreenIconResource;
    }

    /**
     * Set a value from closeFullscreenIconResource variable
     */
    public void setCloseFullscreenIconResource(int closeFullscreenIconResource) {
        this.closeFullscreenIconResource = closeFullscreenIconResource;
    }

    /**
     * Set a value from fullscreenOrientation variable
     */
    public void setFullscreenOrientation(int fullscreenOrientation) {
        this.fullscreenOrientation = fullscreenOrientation;
    }

    /**
     * Set a value from buttonsColor variable
     */
    public void setButtonsColor(int buttonsColor) {
        this.buttonsColor = buttonsColor;
    }

    /**
     * Set a value from seekBarColor variable
     */
    public void setSeekBarColor(int seekBarColor) {
        this.seekBarColor = seekBarColor;
    }

    /**
     * Set a value from currentTimeTextColor variable
     */
    public void setCurrentTimeTextColor(int currentTimeTextColor) {
        this.currentTimeTextColor = currentTimeTextColor;
    }

    /**
     * Set a value from currentTimeShortFormat variable
     */
    public void setCurrentTimeShortFormat(String currentTimeShortFormat) {
        this.currentTimeShortFormat = currentTimeShortFormat;
    }

    /**
     * Set a value from currentTimeLongFormat variable
     */
    public void setCurrentTimeLongFormat(String currentTimeLongFormat) {
        this.currentTimeLongFormat = currentTimeLongFormat;
    }
}
