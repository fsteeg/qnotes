package com.quui.notes.editor.data;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javanet.staxutils.IndentingXMLStreamWriter;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Write XML notebook file using STAX.
 * @author Fabian Steeg (fsteeg)
 */
public final class NotesWriter {
    /***/
    static final String XML = ".nxml";
    /***/

    /***/
    private List<Note> notes;

    /**
     * @param notes The notes to serilaize
     * @return The writer intances
     */
    public static NotesWriter getInstance(List<Note> notes) {
        return new NotesWriter(notes);
    }

    /**
     * @param notes The notes to serialize
     */
    private NotesWriter(List<Note> notes) {
        this.notes = new ArrayList<Note>(Collections
                .unmodifiableCollection(notes));
    }

    /**
     * Writes the annotations produced in this experiment to XML. Uses STAX for
     * efficient writing, as the annotations will become rather large for any
     * realistic scenario, and the format is quite simple.
     * @param outputLocation The location to write the annotations to
     */
    public void write(final String outputLocation) {
        if (notes == null) {
            throw new IllegalStateException("No notes!");
        }
        try {
            BufferedOutputStream outx = new BufferedOutputStream(
                    new FileOutputStream(outputLocation + XML));
            XMLOutputFactory factory = XMLOutputFactory.newInstance();
            XMLStreamWriter writer = factory.createXMLStreamWriter(outx);
            // TODO do this optional, prefs ("pretty-print")
            writer = new IndentingXMLStreamWriter(writer);
            /* ---------------------- */
            writer.writeStartDocument("UTF-8", "1.0");
            writer.writeStartElement("notes");
            for (Note note : notes) {
                writer.writeStartElement("note");
                writer.writeAttribute("public", note.isPublic()
                        ? "true" : "false");
                writer.writeAttribute("title", note.getTitle());
                writer.writeAttribute("date", note.getDate());
                writer.writeAttribute("tag", note.getTag());
                String unescapeHtml = note.getText();
                writer.writeCharacters(unescapeHtml);
                writer.writeEndElement();
            }
            writer.writeEndElement();
            writer.writeEndDocument();
            /* ---------------------- */
            writer.flush();
            writer.close();
            outx.flush();
            outx.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (FactoryConfigurationError e) {
            e.printStackTrace();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
