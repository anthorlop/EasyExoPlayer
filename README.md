# EasyExoPlayer
Easy way to play videos using ExoPlayer 2.4.0 (Includes a player for dailymotion videos)

## EasyExoPlayer dependency
```gradle

repositories {
    maven { url 'https://github.com/anthorlop/mvn-android/raw/master/' }
}

// GoogleSearchImageParser gradle dependencies
compile 'es.lombrinus.projects.mods:EasyExoPlayer:1.0'
```

## Playing video
```java

        Intent i = new Intent(getApplicationContext(), EasyVideoActivity.class);
        i.putExtra(EasyVideoActivity.ARG_URL_VIDEO, urlVideo);
//      i.putExtra(EasyVideoActivity.ARG_FORCE_MEDIAPLAYER, true); // Force MediaPlayer instead of ExoPlayer although android version support it
        startActivity(i);

```

## Customization

You can change colors adding to colors.xml:

#### e_controller_bg
Controller background color
#### e_controller_items
Controller icons color
#### e_controller_buffered
Seekbar buffer color
#### e_controller_bar
Seekbar main color
