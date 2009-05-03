package com.quui.notes.editor.data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;

import com.quui.notes.editor.DotDrawer;

/**
 * HTML export for note instances.
 * @author Fabian Steeg (fsteeg)
 */
public final class HtmlExport {

    final String fileName;
    final List<Note> notes;
    private File imageFolder;

    private HtmlExport(List<Note> notes, String fileName) {
        this.notes = notes;
        this.fileName = fileName;
        File wxml = new File(fileName);
        imageFolder = new File(wxml.getParent(), wxml.getName().split("\\.")[0]);
        boolean mkdir = imageFolder.mkdir();
        if (!mkdir && !imageFolder.exists()) {
            throw new IllegalStateException("Could not create image folder.");
        }
    }

    public static HtmlExport getInstance(List<Note> notes, String fileName) {
        return new HtmlExport(notes, fileName);
    }

    public void export(boolean absolute, String query) {
        /* TODO switch to JET */
        StringBuilder html = new StringBuilder(
                String
                        .format(
                                "<head>"
                                        + "<meta http-equiv='Content-Type' content='text/html; charset=UTF-8'/>"
                                        + "<link rel='stylesheet' href='./style.css' type='text/css' />"
                                        + "</head>"
                                        + "<div style='font-weight:bold;''>%s notes for %s</div><div style='text-align:left; font-size:8pt; font-style:italic; padding-bottom: 2px; margin-bottom: 12px;margin-top: 4px;'>%s</div>",
                                notes.size(), query, tags(notes)));
        for (Note n : notes) {
            String folder = absolute
                    ? imageFolder.getAbsolutePath() : imageFolder.getName();
            String htmlAppend = n.toHtml(folder.endsWith(File.separator)
                    ? folder : folder + File.separator);
            html.append(htmlAppend);
            writeDot(n);
        }
        html
                .append("<div style='border: 1px solid; font-size:small; padding: 2px; margin: 2px;'>"
                        + "<b>The Eclipse Notebook | by Fabian Steeg | This file was generated on "
                        + new java.sql.Date(System.currentTimeMillis())
                        + " by '"
                        + System.getProperty("user.name")
                        + "'</b></div>");
        try {
            BufferedWriter fileWriter = new BufferedWriter(new FileWriter(
                    new File(fileName)));
            fileWriter.write(html.toString());
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static String tags(List<Note> notes) {
        Set<String> set = new HashSet<String>();
        for (Note note : notes) {
            set.addAll(Arrays.asList(note.getTag().split(" ")));
        }
        return set.toString();
    }

    private void writeDot(Note n) {
        String dotContent = n.dotContent();
        if (dotContent != null) {
            String location = imageFolder.getAbsolutePath() + File.separator
                    + dotContent.hashCode();
            /*
             * We use the hash code of the content as the name, so if the
             * content changes, the filename changes:
             */
            if (!new File(location + ".dot").exists()) {
                saveDot(location, dotContent);
                new DotDrawer("", "", location + ".dot", location + ".png",
                        null).renderImage("png");
            }
        }
    }

    void saveDot(String title, String dotText) {
        try {
            String loc = title + ".dot";
            BufferedWriter w = new BufferedWriter(new FileWriter(loc));
            w.write(StringEscapeUtils.unescapeHtml(dotText));
            w.close();
            System.out.println("Wrote: " + dotText.length() + " chars to: "
                    + new File(loc).getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
