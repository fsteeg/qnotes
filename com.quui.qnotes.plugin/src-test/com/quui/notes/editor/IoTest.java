package com.quui.notes.editor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;

import com.quui.notes.editor.data.Note;
import com.quui.notes.editor.data.NotesReader;
import com.quui.notes.editor.data.NotesWriter;

public class IoTest {

    @Test
    public void test() {
        List<Note> notesInput = NotesReader.getInstance("new_notebook.xml")
                .read();
        System.out.println("Read: " + notesInput.size() + " Notes");
        for (Note note : notesInput) {
            assertNotNull("Null date!", note.getDate());
            assertNotNull("Null publik!", note.isPublic());
            assertNotNull("Null tag!", note.getTag());
            assertNotNull("Null text!", note.getText());
            assertNotNull("Null title!", note.getTitle());
        }
        String output = "output/notes-output";
        NotesWriter.getInstance(notesInput).write(output);
        List<Note> notesOutput = NotesReader.getInstance(output + ".nxml")
                .read();
        System.out.println("Wrote: " + notesOutput.size() + " Notes");
        assertEquals("Wrong number", notesInput.size(), notesOutput.size());
        for (int i = 0; i < notesInput.size(); i++) {
            assertEquals("Wrong date;", notesInput.get(i).getDate(), notesOutput
                    .get(i).getDate());
            assertEquals("Wrong publik;", notesInput.get(i).isPublic(), notesOutput
                    .get(i).isPublic());
            assertEquals("Wrong tag;", notesInput.get(i).getTag(), notesOutput
                    .get(i).getTag());
            assertEquals("Wrong text;", notesInput.get(i).getText(), notesOutput
                    .get(i).getText());
            assertEquals("Wrong title;", notesInput.get(i).getTitle(), notesOutput
                    .get(i).getTitle());

        }
    }
}
