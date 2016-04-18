package cn.common.config;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.engine.executor.FifoPriorityThreadPoolExecutor;
import com.bumptech.glide.module.GlideModule;

import android.content.ComponentCallbacks2;
import android.content.Context;
import android.os.Environment;

/**
 * 描述：Glide图片加载器配置
 *
 * @author jakechen
 * @since 2016/4/18 18:06
 */
public class GlideConfig  implements GlideModule {
    private static final int MEMORY_CACHE_SIZE = 60 * 1024 * 1024;
    private static final int DISK_CACHE_SIZE = 250 * 1024 * 1024;
    private static final int BITMAP_POOL_SIZE = 100;

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        builder.setBitmapPool(new LruBitmapPool(BITMAP_POOL_SIZE));
        builder.setMemoryCache(new LruResourceCache(MEMORY_CACHE_SIZE));
        builder.setDecodeFormat(DecodeFormat.PREFER_RGB_565);
        builder.setDiskCache(new DiskLruCacheFactory(Environment.getExternalStorageDirectory().getAbsolutePath(), "jake/cache/.image", DISK_CACHE_SIZE));
//        builder.setDiskCache(new DiskLruCacheFactory( Environment.getExternalStorageDirectory().getAbsolutePath(),"jake/cache/.image",DISK_CACHE_SIZE));
        builder.setDiskCacheService(new FifoPriorityThreadPoolExecutor(10));
        builder.setResizeService(new FifoPriorityThreadPoolExecutor(3));
    }

    @Override
    public void registerComponents(Context context, Glide glide) {
        glide.trimMemory(ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW);
    }
}