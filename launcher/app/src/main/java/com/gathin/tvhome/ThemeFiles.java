package com.gathin.tvhome;

import java.util.List;
import java.util.regex.Pattern;

/** The files ThemeProvider may write or delete under Config.dir, by the URI's path segments. */
final class ThemeFiles {
    static final String WALLPAPERS = "wallpapers";
    /** Folder of installable theme folders the on-TV picker lists (ThemeLibrary); sits beside the active overlay. */
    static final String THEMES = "themes";
    /** The names tools/theme.sh accepts for a collection image, and the types WallpaperRotation reads. */
    private static final Pattern IMAGE = Pattern.compile("[A-Za-z0-9_.-]+\\.(jpg|jpeg|png|webp)",
            Pattern.CASE_INSENSITIVE);
    /** A theme folder's name: "aurora", never "."/".." or a nested path. */
    private static final Pattern THEME_NAME = Pattern.compile("[A-Za-z0-9_-][A-Za-z0-9._-]*");

    private ThemeFiles() {}

    /** The path relative to the theme directory, e.g. "config.json" or "wallpapers/dusk.jpg", or null if refused. */
    static String path(List<String> segments) {
        if (segments == null) return null;
        if (segments.size() == 1) {
            String name = segments.get(0);
            return Config.FILE_NAME.equals(name) || "wallpaper.jpg".equals(name) || "theme_revision".equals(name)
                    ? name : null;
        }
        if (segments.size() == 2 && WALLPAPERS.equals(segments.get(0))
                && (collectionImage(segments.get(1)) || WallpaperCaptions.FILE_NAME.equals(segments.get(1)))) {
            return WALLPAPERS + "/" + segments.get(1);
        }
        // The on-TV library: themes/<name>/config.json, themes/<name>/wallpaper.jpg,
        // themes/<name>/wallpapers/<image or captions.txt>.
        if (segments.size() == 3 && THEMES.equals(segments.get(0)) && themeName(segments.get(1))) {
            String leaf = segments.get(2);
            if (Config.FILE_NAME.equals(leaf) || "wallpaper.jpg".equals(leaf))
                return THEMES + "/" + segments.get(1) + "/" + leaf;
        }
        if (segments.size() == 4 && THEMES.equals(segments.get(0)) && themeName(segments.get(1))
                && WALLPAPERS.equals(segments.get(2))
                && (collectionImage(segments.get(3)) || WallpaperCaptions.FILE_NAME.equals(segments.get(3)))) {
            return THEMES + "/" + segments.get(1) + "/" + WALLPAPERS + "/" + segments.get(3);
        }
        return null;
    }

    static boolean themeName(String name) {
        return name != null && THEME_NAME.matcher(name).matches();
    }

    /** True for "wallpapers" alone: a delete of it clears the collection's images. */
    static boolean isCollection(List<String> segments) {
        return segments != null && segments.size() == 1 && WALLPAPERS.equals(segments.get(0));
    }

    static boolean collectionImage(String name) {
        return name != null && !name.startsWith(".") && IMAGE.matcher(name).matches();
    }
}
