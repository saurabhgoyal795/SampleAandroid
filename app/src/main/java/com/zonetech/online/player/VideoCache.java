package com.zonetech.online.player;

import android.content.Context;

import com.google.android.exoplayer2.database.ExoDatabaseProvider;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;

import java.io.File;

public class VideoCache {
    private static SimpleCache sDownloadCache;
    public static SimpleCache getInstance(Context context) {
        if (sDownloadCache == null){
            ExoDatabaseProvider databaseProvider = new ExoDatabaseProvider(context);
            File downloadDirectory = new File(context.getCacheDir(), "media");
            sDownloadCache = new SimpleCache(downloadDirectory,
                    new LeastRecentlyUsedCacheEvictor(100*1024*1024),
                    databaseProvider);
        }
        return sDownloadCache;
    }
}
