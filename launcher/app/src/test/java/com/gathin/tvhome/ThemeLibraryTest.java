package com.gathin.tvhome;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class ThemeLibraryTest {
    @Rule public final TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void installedListsThemeFoldersWithAConfigByName() throws Exception {
        File dir = folder.newFolder("files");
        File themes = new File(dir, "themes");
        File zed = new File(themes, "zed");
        File aurora = new File(themes, "aurora");
        File incomplete = new File(themes, "incomplete");
        assertTrue(zed.mkdirs());
        assertTrue(aurora.mkdirs());
        assertTrue(incomplete.mkdirs());
        write(new File(zed, "config.json"), "{}");
        write(new File(aurora, "config.json"), "{}");

        List<File> installed = ThemeLibrary.installed(dir);
        assertEquals(2, installed.size());
        assertEquals("aurora", installed.get(0).getName());
        assertEquals("zed", installed.get(1).getName());
    }

    @Test
    public void applyCopiesTheThemeAndClearsThePreviousOverlay() throws Exception {
        File dir = folder.newFolder("files");
        File theme = new File(dir, "themes/aurora");
        File collection = new File(theme, "wallpapers");
        assertTrue(collection.mkdirs());
        write(new File(theme, "config.json"), "{\"columns\":6}");
        write(new File(theme, "wallpaper.jpg"), "wallpaper");
        write(new File(collection, "dusk.jpg"), "dusk");
        write(new File(collection, "captions.txt"), "dusk.jpg\tDusk");
        write(new File(dir, "config.json"), "{\"columns\":4}");
        write(new File(dir, "wallpaper.jpg"), "old");
        File stale = new File(dir, "wallpapers/stale.jpg");
        assertTrue(stale.getParentFile().mkdirs());
        write(stale, "stale");

        assertTrue(ThemeLibrary.apply(dir, theme));

        assertEquals("{\"columns\":6}", read(new File(dir, "config.json")));
        assertEquals("wallpaper", read(new File(dir, "wallpaper.jpg")));
        assertEquals("dusk", read(new File(dir, "wallpapers/dusk.jpg")));
        assertEquals("dusk.jpg\tDusk", read(new File(dir, "wallpapers/captions.txt")));
        assertFalse(stale.exists());
        assertEquals("aurora", ThemeLibrary.current(dir));
    }

    @Test
    public void applyNullReturnsToDefaultAndCurrentMarksCustomOverlays() throws Exception {
        File dir = folder.newFolder("files");
        File theme = new File(dir, "themes/aurora");
        assertTrue(theme.mkdirs());
        write(new File(theme, "config.json"), "{}");
        write(new File(dir, "config.json"), "{}");
        assertEquals("aurora", ThemeLibrary.current(dir));

        write(new File(dir, "config.json"), "{\"columns\":6}");
        assertNull(ThemeLibrary.current(dir));

        assertTrue(ThemeLibrary.apply(dir, null));
        assertFalse(new File(dir, "config.json").exists());
        assertEquals(ThemeLibrary.DEFAULT, ThemeLibrary.current(dir));
    }

    private static void write(File file, String text) throws Exception {
        Files.write(file.toPath(), text.getBytes(StandardCharsets.UTF_8));
    }

    private static String read(File file) throws Exception {
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    }
}
