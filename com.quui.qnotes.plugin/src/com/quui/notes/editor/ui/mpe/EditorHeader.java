package com.quui.notes.editor.ui.mpe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.quui.notes.editor.data.NotesFilter;
import com.quui.notes.editor.data.Note;
import com.quui.notes.editor.data.NotesFilter.SearchOption;

/**
 * @author Fabian Steeg (fsteeg)
 */
public final class EditorHeader {
    private EditorHeader() {/* Util class */}

    static Composite createHeader(final Editor ed, Composite parent,
            final Runnable strategy) {
        final Composite c = new Composite(parent, SWT.NONE);
        c.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        c.setLayout(new GridLayout(1, false));
        final Composite header = new Composite(c, SWT.NONE);
        header.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        header.setLayout(new GridLayout(6, false));
        Label label = new Label(header, SWT.NONE);
        label.setText("Filter:");
        final Text queryTextField = new Text(header, SWT.SEARCH);
        queryTextField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        queryTextField.setText(ed.query);
        Label label2 = new Label(header, SWT.NONE);
        label2.setText("in:");
        final Combo searchOptionCombo = new Combo(header, SWT.NONE);
        for (SearchOption o : SearchOption.values()) {
            searchOptionCombo.add(o.toString());
        }
        searchOptionCombo.select(ed.searchOption);
        SelectionListener selectionListener = new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                update();
            }
            public void widgetDefaultSelected(SelectionEvent e) {
                update();
            }
            private void update() {
                ed.searchOption = searchOptionCombo.getSelectionIndex();
                ed.query = queryTextField.getText();
                strategy.run();
            }
        };
        queryTextField.addSelectionListener(selectionListener);
        searchOptionCombo.addSelectionListener(selectionListener);
        ed.limit = new Text(header, SWT.SEARCH);
        ed.limit.setText("20");
        ed.limit.addSelectionListener(selectionListener);
        return header;
    }

    /**
     * @return Returns a view on the currently filtered notes
     */
    static List<Note> filtered(List<Note> all, String q, String s) {
        List<Note> view = new ArrayList<Note>();
        for (Note note : all) {
            if (NotesFilter.matches(note, q, SearchOption.valueOf(s))) {
                view.add(note);
            }
        }
        Collections.sort(view);
        return view;
    }

}
