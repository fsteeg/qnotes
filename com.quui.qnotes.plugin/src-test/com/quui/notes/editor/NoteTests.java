package com.quui.notes.editor;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.quui.notes.editor.data.Note;

public class NoteTests {

    @Test
    public void regExTest() {
        String text = " <note public='false' title='Zusammenhaenge' date='2008-09-24' tag='quui'>dot:\ngraph z{\nnode[shape=box]\ntm[label=&quot;Text Mining&quot;]\ndm[label=&quot;Dokumentenmanagement&quot;]\narch[label=&quot;Archivierung&quot;]\nprob[label=&quot;Problem in kl. und mitt. Untern.&quot;]\ntm--prob\ndm--prob\narch--prob\n\n}\n:dot</note>";
        Note instance = Note.getInstance(text, "title", null, null, null);
        System.out.println(instance);
        String dotted = instance.dotProcess(".");
        System.out.println("After: " + dotted);
        assertTrue(!dotted.contains("dot:"));
    }
}
