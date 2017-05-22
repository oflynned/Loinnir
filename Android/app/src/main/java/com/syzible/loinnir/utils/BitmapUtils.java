package com.syzible.loinnir.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;

/**
 * Created by ed on 17/05/2017.
 */

public class BitmapUtils {
    private static final int BITMAP_SIZE = 640;

    public static Bitmap getCroppedCircle(Bitmap bitmap) {
        float ratio = Math.min(
                (float) BITMAP_SIZE / bitmap.getWidth(),
                (float) BITMAP_SIZE / bitmap.getHeight());
        int width = Math.round(ratio * bitmap.getWidth());
        int height = Math.round(ratio * bitmap.getHeight());

        final Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        final Bitmap outputBitmap = Bitmap.createBitmap(scaledBitmap.getWidth(),
                scaledBitmap.getHeight(), Bitmap.Config.ARGB_8888);

        final Path path = new Path();
        path.addCircle((float) (width / 2), (float) (height / 2),
                (float) Math.min(width, (height / 2)), Path.Direction.CCW);

        final Canvas canvas = new Canvas(outputBitmap);
        canvas.clipPath(path);
        canvas.drawBitmap(scaledBitmap, 0, 0, null);
        return outputBitmap;
    }
}
