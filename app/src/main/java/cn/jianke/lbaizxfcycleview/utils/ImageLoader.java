package cn.jianke.lbaizxfcycleview.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

public class ImageLoader {

    public static void load(Context context, ImageView imageView, String url, Drawable defaultImage){
        load(context, imageView, url, defaultImage, null, false);
    }

    public static void load(Context context, ImageView imageView, String url, Drawable defaultImage,
                     Drawable errorImage , boolean isCropCircle){
        DrawableTypeRequest request = Glide.with(context).load(url);
        request.centerCrop();
        if (isCropCircle)
            request.bitmapTransform(new CropCircleTransformation(context));
                request.placeholder(defaultImage)
                .crossFade()
                .priority(Priority.NORMAL)
                .fallback(null)
                .error(errorImage)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(imageView);
    }
}
