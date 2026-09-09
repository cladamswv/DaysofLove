package com.carl.lovewithmary;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

public final class WidgetThemes {
    public static final String DEFAULT_THEME = "Romantic Rose";
    public static final int DEFAULT_BG = Color.rgb(212, 20, 90);

    public static final String[] NAMES = new String[]{
            "Romantic Rose",
            "Blush Sunset",
            "Ruby Velvet",
            "Midnight Romance",
            "Lavender Kiss",
            "Champagne Glow",
            "Soft Ivory",
            "Ocean Date"
    };

    private WidgetThemes() {}

    public static int backgroundFor(String name) {
        if ("Blush Sunset".equals(name)) return Color.rgb(240, 98, 146);
        if ("Ruby Velvet".equals(name)) return Color.rgb(143, 20, 61);
        if ("Midnight Romance".equals(name)) return Color.rgb(58, 23, 60);
        if ("Lavender Kiss".equals(name)) return Color.rgb(126, 87, 194);
        if ("Champagne Glow".equals(name)) return Color.rgb(214, 166, 92);
        if ("Soft Ivory".equals(name)) return Color.rgb(255, 244, 247);
        if ("Ocean Date".equals(name)) return Color.rgb(63, 108, 180);
        return DEFAULT_BG;
    }

    public static int textFor(String name) {
        if ("Soft Ivory".equals(name)) return Color.rgb(111, 23, 57);
        if ("Champagne Glow".equals(name)) return Color.rgb(67, 36, 25);
        return Color.WHITE;
    }

    public static int indexOf(String name) {
        for (int i = 0; i < NAMES.length; i++) if (NAMES[i].equals(name)) return i;
        return 0;
    }

    public static GradientDrawable previewDrawable(int baseColor, int strengthPercent, float density) {
        int alpha = Math.max(0, Math.min(100, strengthPercent)) * 255 / 100;
        int light = withAlpha(blend(baseColor, Color.WHITE, 0.14f), alpha);
        int dark = withAlpha(blend(baseColor, Color.BLACK, 0.10f), alpha);
        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{light, baseWithAlpha(baseColor, alpha), dark}
        );
        drawable.setCornerRadius(24f * density);
        return drawable;
    }

    public static int blend(int colorA, int colorB, float amountB) {
        amountB = Math.max(0f, Math.min(1f, amountB));
        float amountA = 1f - amountB;
        int r = Math.round(Color.red(colorA) * amountA + Color.red(colorB) * amountB);
        int g = Math.round(Color.green(colorA) * amountA + Color.green(colorB) * amountB);
        int b = Math.round(Color.blue(colorA) * amountA + Color.blue(colorB) * amountB);
        return Color.rgb(r, g, b);
    }

    public static int withAlpha(int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    public static int baseWithAlpha(int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }
}
