package com.gjk.utils.media2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;
import android.os.Environment;

import com.gjk.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

    public static File createTimestampedImageFile(Context ctx) throws IOException {
        // Create an image file name
        final String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        final String imageFileName = String.format("%s_%s.jpg", ctx.getResources().getString(R.string.app_name),
                timeStamp);
        return createImageFile(ctx, imageFileName);
    }

    public static File createTempImageFile(Context ctx) throws IOException {
        // Create an image file name
        return createImageFile(ctx, "temp");
    }

    public static void removeTempImageFile(Context ctx) throws IOException {
        // Create an image file name
        final File photoFile = getFile(ctx, "temp");
        if (photoFile != null) {
            photoFile.delete();
        }
    }

    private static File createImageFile(Context ctx, String imageFileName) throws IOException {
        final File photoFile = getFile(ctx, imageFileName);
        photoFile.createNewFile();
        return photoFile;
    }

    public static File getFile(Context ctx, String imageFileName) throws IOException {
        final File storageDir = new File(Environment.getExternalStorageDirectory(), ctx.getResources().getString(
                R.string.app_name));
        storageDir.mkdirs();
        return new File(storageDir, imageFileName);
    }
}
