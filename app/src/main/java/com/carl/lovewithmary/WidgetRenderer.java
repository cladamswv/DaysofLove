package com.carl.lovewithmary;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;

public final class WidgetRenderer {
    private WidgetRenderer() {}

    public static Bitmap renderBackground(Context context) {
        int width = 900;
        int height = 300;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        int base = AppState.widgetBackgroundColor(context);
        int strength = AppState.widgetBackgroundStrength(context);
        int alpha = strength * 255 / 100;

        int light = WidgetThemes.withAlpha(WidgetThemes.blend(base, Color.WHITE, 0.16f), alpha);
        int dark = WidgetThemes.withAlpha(WidgetThemes.blend(base, Color.BLACK, 0.12f), alpha);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setShader(new LinearGradient(0, 0, width, height, light, dark, Shader.TileMode.CLAMP));
        float radius = 68f;
        canvas.drawRoundRect(0, 0, width, height, radius, radius, paint);

        // Quiet romantic highlights. These are intentionally subtle behind the text.
        Paint glow = new Paint(Paint.ANTI_ALIAS_FLAG);
        glow.setColor(Color.argb(Math.min(alpha, 32), 255, 255, 255));
        canvas.drawCircle(width * 0.15f, height * 0.05f, 120f, glow);
        canvas.drawCircle(width * 0.90f, height * 0.80f, 150f, glow);

        Paint heartPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        heartPaint.setColor(Color.argb(Math.min(alpha, 30), 255, 255, 255));
        drawHeart(canvas, width - 105f, 56f, 42f, heartPaint);
        drawHeart(canvas, 88f, height - 52f, 24f, heartPaint);

        return bitmap;
    }

    private static void drawHeart(Canvas canvas, float cx, float cy, float size, Paint paint) {
        Path p = new Path();
        p.moveTo(cx, cy + size * 0.72f);
        p.cubicTo(cx - size * 1.15f, cy + size * 0.08f,
                cx - size * 0.62f, cy - size * 0.75f,
                cx, cy - size * 0.20f);
        p.cubicTo(cx + size * 0.62f, cy - size * 0.75f,
                cx + size * 1.15f, cy + size * 0.08f,
                cx, cy + size * 0.72f);
        canvas.drawPath(p, paint);
    }
}
