package com.quui.notes.editor.data;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.mediawiki.core.MediaWikiLanguage;

/**
 * Representation of a single note in a notebook.
 * @author Fabian Steeg (fsteeg)
 */
public final class Note implements Comparable<Note> {

    static final String STYLE = "style='font-size:16pt; border: 1px solid; padding: 2px; margin: 2px; background-color: #f4f4f4;'";
    private static final Object STYLE_METADATA = "style='text-align:left; border: 1px solid; font-size:8pt; font-style:italic; padding: 2px; margin: 2px;'";
    private String publik;
    private String tag;
    private String date;
    private String title;
    private String text;

    String regex = "dot:(.+?):dot";
    Pattern compile = Pattern
            .compile(regex, Pattern.DOTALL | Pattern.MULTILINE);

    private Note(String text, String title, String date, String tag,
            String publik) {
        this.text = text;
        this.title = title;
        this.date = date;
        this.tag = tag;
        this.publik = publik;
    }

    public static Note getInstance(String text, String title, String date,
            String tag, String publik) {
        return new Note(text, title, date, tag, publik);
    }

    /**
     * {@inheritDoc}
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        return (obj instanceof Note) && ((Note) obj).compareTo(this) == 0;
    }

    /**
     * {@inheritDoc}
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return text.hashCode();
    }

    /**
     * {@inheritDoc}
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("%s, %s, %s, %s, length %s", title, date, tag,
                publik, text.length());
    }

    public String toHtml(String folder) {
        String styleTop = STYLE;
        String styleText = "";
        return String.format("<div " + styleTop + ">"
                + "%s</div><p %s>%s<div %s>Noted on %s as %s, %s</div></p>",
                title, styleText, dotProcess(folder), STYLE_METADATA, date,
                tag, (publik.equals("true") ? "public" : "private"));
    }

    public String dotProcess(String folder) {
        String content = text;
        String wikitext = "[wikitext]";
        if (!content.contains("<") || content.contains(wikitext)) {
            content = content.replace(wikitext, "");
            content = convertToHtml(content);
        }
        Matcher matcher = compile.matcher(content);
        folder = folder.replaceAll("\\\\", "/");
        String dotContent = dotContent();
        String r = matcher
                .replaceFirst("<img src='"
                        + folder
                        + File.separator
                        + (dotContent == null ? "".hashCode() : dotContent
                                .hashCode())
                        + ".png' alt='Image not found. Dot files in need to be rendered."
                        + "'" + "/>");
        return r;
    }

    private String convertToHtml(String r) {
        MarkupParser p = new MarkupParser(new MediaWikiLanguage());
        String html = p.parseToHtml(r);
        return html;
    }

    String dotContent() {
        Matcher matcher = compile.matcher(text);
        if (matcher.find()) {
            String dotText = matcher.group(1);
            return dotText;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Note arg0) {
        return arg0.date.compareTo(this.date);
    }

    public boolean isPublic() {
        return this.publik.equals("true");
    }
    public String getTag() {
        return tag;
    }
    public String getDate() {
        return date;
    }
    public String getTitle() {
        return title;
    }
    public String getText() {
        return text;
    }

    /* TODO can we get rid of these setters (see EditorForm#insertNotes)? */

    public void setPublic(boolean publik) {
        this.publik = publik ? "true" : "false";
    }
    public void setTag(String tag) {
        this.tag = tag;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setText(String text) {
        this.text = text;
    }
}
