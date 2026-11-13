package com.gathin.tvhome;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The on-TV theme library: theme folders under {@code themes/} in Config.dir, each holding a
 * config.json and optionally wallpaper.jpg / a wallpapers/ collection — the same layout
 * tools/theme.sh pushes to the overlay's own spot. Launcher settings' Theme row applies one by
 * copying its files over the overlay and bumping theme_revision, the same way a push does, so a
 * pick clears the on-TV choices the theme would have cleared. Everything runs on the app's own
 * files: no permissions, no provider.
 */
final class ThemeLibrary {

    /** A theme with no overlay at all: the built-in config.xml look, like theme.sh --reset. */
    static final String DEFAULT = "";

    private ThemeLibrary() {}

    /** Theme folders with a config.json, sorted by folder name. */
    static List<File> installed(File dir) {
        File[] folders = dir == null ? null : new File(dir, ThemeFiles.THEMES).listFiles();
        List<File> themes = new ArrayList<>();
        if (folders == null) return themes;
        Arrays.sort(folders);
        for (File f : folders) {
            if (f.isDirectory() && new File(f, Config.FILE_NAME).isFile()) themes.add(f);
        }
        return themes;
    }

    /**
     * The folder name of the library theme whose config.json is the active overlay, DEFAULT when
     * no overlay exists, or null when the overlay matches no installed theme (a theme.sh push of a
     * theme that is not in the library, or hand-edited files).
     */
    static String current(File dir) {
        File overlay = dir == null ? null : new File(dir, Config.FILE_NAME);
        if (overlay == null || !overlay.isFile()) return DEFAULT;
        byte[] active = read(overlay);
        if (active == null) return null;
        for (File theme : installed(dir)) {
            if (Arrays.equals(active, read(new File(theme, Config.FILE_NAME)))) return theme.getName();
        }
        return null;
    }

    /**
     * Copies the theme's files over the active overlay and writes a fresh theme_revision; a null
     * theme resets to the built-in look. False when dir is missing or a copy failed.
     */
    static boolean apply(File dir, File theme) {
        if (dir == null) return false;
        clear(dir);
        if (theme != null) {
            try {
                copy(new File(theme, Config.FILE_NAME), new File(dir, Config.FILE_NAME));
                File wallpaper = new File(theme, Config.WALLPAPER_NAME);
                if (wallpaper.isFile()) copy(wallpaper, new File(dir, Config.WALLPAPER_NAME));
                File[] images = new File(theme, ThemeFiles.WALLPAPERS).listFiles(ThemeLibrary::themeFile);
                if (images != null && images.length > 0) {
                    File target = new File(dir, ThemeFiles.WALLPAPERS);
                    if (!target.isDirectory() && !target.mkdirs()) return false;
                    for (File image : images) copy(image, new File(target, image.getName()));
                }
            } catch (IOException e) {
                return false;
            }
        }
        bumpRevision(dir);
        return true;
    }

    private static boolean themeFile(File dir, String name) {
        return ThemeFiles.collectionImage(name) || WallpaperCaptions.FILE_NAME.equals(name);
    }

    /** The overlay's files gone: config.json, wallpaper.jpg, and the collection's own files. */
    private static void clear(File dir) {
        new File(dir, Config.FILE_NAME).delete();
        new File(dir, Config.WALLPAPER_NAME).delete();
        File[] images = new File(dir, ThemeFiles.WALLPAPERS).listFiles(ThemeLibrary::themeFile);
        if (images != null) for (File image : images) image.delete();
    }

    /** A new marker so Config's theme signature changes, exactly like a theme.sh push does. */
    private static void bumpRevision(File dir) {
        try {
            Files.write(new File(dir, "theme_revision").toPath(),
                    Long.toHexString(System.nanoTime()).getBytes("US-ASCII"));
        } catch (IOException ignored) {
            // A missing marker just keeps the previous signature; the theme still applies.
        }
    }

    private static byte[] read(File file) {
        try {
            return file.isFile() && file.length() <= 64 * 1024 ? Files.readAllBytes(file.toPath()) : null;
        } catch (IOException e) {
            return null;
        }
    }

    private static void copy(File from, File to) throws IOException {
        Files.copy(from.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
}
