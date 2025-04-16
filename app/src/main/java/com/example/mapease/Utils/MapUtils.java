package com.example.mapease.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.mapease.R;

public class MapUtils {

    /**
     * Vẽ một marker có nền tròn đỏ và icon lá cờ cố định tại vị trí chỉ định.
     *
     * @param context  context
     * @param map      GoogleMap object
     * @param position LatLng vị trí
     * @param title    tiêu đề marker
     * @return Marker đã được thêm vào bản đồ
     */
    public static Marker addCustomMarkerSimple(Context context, GoogleMap map, LatLng position, String title) {
        int size = 50;
        int iconSize = (int) (size * 0.5);

        // Vẽ nền tròn đỏ
        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        Paint bgPaint = new Paint();
        bgPaint.setAntiAlias(true);
        bgPaint.setColor(Color.parseColor("#FF0000"));
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, bgPaint);

        // Load icon (hỗ trợ mọi Drawable: Vector/Bitmap)
        Drawable drawable = ContextCompat.getDrawable(context, R.drawable.ic_warning);
        if (drawable == null) return null;

        Bitmap flagBitmap = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888);
        Canvas canvasIcon = new Canvas(flagBitmap);
        drawable.setBounds(0, 0, iconSize, iconSize);
        drawable.draw(canvasIcon);

        int left = (size - iconSize) / 2;
        int top = (size - iconSize) / 2;
        canvas.drawBitmap(flagBitmap, left, top, null);

        return map.addMarker(new MarkerOptions()
                .position(position)
                .title(title)
                .icon(BitmapDescriptorFactory.fromBitmap(output)));
    }
}
