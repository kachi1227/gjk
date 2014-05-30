package com.gjk.utils.media2;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;

public class ImageUtil {

    public static Bitmap getCroppedBitmap(Bitmap bitmap) {
        final int d = Math.min(bitmap.getWidth(), bitmap.getHeight());
        final Bitmap outputBitmap = Bitmap.createBitmap(d, d, Bitmap.Config.ARGB_8888);

        final Path path = new Path();
        path.addCircle(
                (float) (d / 2)
                , (float) (d / 2)
                , (float) (d / 2)
                , Path.Direction.CCW);

        final Canvas canvas = new Canvas(outputBitmap);
        canvas.clipPath(path);
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, null);
        return outputBitmap;
    }
}
