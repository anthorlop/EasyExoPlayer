# EasyExoPlayer
Easy way to play videos using ExoPlayer 2.4.0 (Includes a player for dailymotion videos)

## EasyExoPlayer dependency
```gradle

repositories {
    maven { url 'https://github.com/anthorlop/mvn-android/raw/master/' }
}

// EasyExoPlayer gradle dependencies
compile 'es.lombrinus.projects.mods:EasyExoPlayer:1.0'
```

## Playing video
```java

        Intent i = new Intent(getApplicationContext(), EasyVideoActivity.class);
        i.putExtra(EasyVideoActivity.ARG_URL_VIDEO, urlVideo);
//      i.putExtra(EasyVideoActivity.ARG_FORCE_MEDIAPLAYER, true); // Force MediaPlayer instead of ExoPlayer although android version support it
        startActivity(i);

```
<img src="https://github.com/anthorlop/EasyExoPlayer/blob/master/eep_a.jpeg" width="350"/> <img src="https://github.com/anthorlop/EasyExoPlayer/blob/master/eep_b.jpeg" width="350"/>

## Customization

You can change colors adding to colors.xml:

#### e_controller_bg
Controller background color
#### e_controller_items
Controller icons and texts color
#### e_controller_buffered
Seekbar buffer color
#### e_controller_bar
Seekbar main color

You also can change controller view, creating your own layout "easy_controller_layout.xml" which will replace the current one. You must use same view IDs.
```xml

<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:orientation="horizontal"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:minHeight="56dp"
    android:id="@+id/easy_controllers"
    android:background="@color/e_controller_bg">


    <LinearLayout
        android:id="@+id/buttons"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:layout_gravity="center_vertical">

        <ImageView
            android:id="@+id/eControllerPlay"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:padding="10dp"
            android:src="@drawable/ue_controller_play"
            android:tint="@color/e_controller_items"
            android:layout_alignParentLeft="true"
            android:layout_alignParentStart="true"/>

        <ImageView
            android:id="@+id/eControllerPause"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:padding="10dp"
            android:src="@drawable/ue_controller_pause"
            android:tint="@color/e_controller_items"
            android:visibility="gone"
            android:layout_alignTop="@+id/button"
            android:layout_toRightOf="@+id/button"/>
    </LinearLayout>

    <RelativeLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_centerVertical="true"
        android:layout_alignParentLeft="true"
        android:layout_alignParentStart="true"
        android:layout_below="@+id/textView"
        android:layout_gravity="center_vertical">

        <RelativeLayout
            android:id="@+id/seekBarLay"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_alignParentRight="true"
            android:layout_centerVertical="true"
            android:layout_alignParentLeft="true"
            android:layout_alignParentStart="true"
            android:layout_gravity="center_vertical">

            <TextView
                android:id="@+id/duration"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_alignParentRight="true"
                android:layout_centerVertical="true"
                android:text="-"
                android:textSize="16dp"
                android:textColor="@color/e_controller_items"
                android:layout_marginRight="16dp"/>

            <TextView
                android:id="@+id/separator"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_toLeftOf="@id/duration"
                android:layout_centerVertical="true"
                android:text="/"
                android:textSize="16dp"
                android:textColor="@color/e_controller_items"/>

            <TextView
                android:id="@+id/actualPosition"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_centerVertical="true"
                android:layout_marginLeft="8dp"
                android:text="-"
                android:textSize="16dp"
                android:textColor="@color/e_controller_items"
                android:layout_toLeftOf="@id/separator"/>

            <com.google.android.exoplayer2.ui.DefaultTimeBar
                android:id="@+id/seekBar"
                android:layout_width="match_parent"
                android:layout_height="26dp"
                app:played_color="@color/e_controller_bar"
                app:buffered_color="@color/e_controller_buffered"
                android:layout_toLeftOf="@id/actualPosition"
                app:ad_marker_color="@color/e_controller_bar"/>
        </RelativeLayout>

    </RelativeLayout>

</LinearLayout>

```

If you add previous/next/rewind/fastForward buttons you can use:

```xml
com.google.android.exoplayer2.ui.R.id.exo_prev
com.google.android.exoplayer2.ui.R.id.exo_next
com.google.android.exoplayer2.ui.R.id.exo_rew
com.google.android.exoplayer2.ui.R.id.exo_ffwd
```

This way the action of the buttons will be implemented automatically.
