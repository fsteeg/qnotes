package com.quui.notes.editor.ui.mpe;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.quui.notes.editor.Util;
import com.quui.notes.editor.data.HtmlExport;

/**
 * @author Fabian Steeg (fsteeg)
 */
public final class EditorHtmlPage {
    private Editor e;
    private Browser browser;

    /**
     * @param e The containing editor
     */
    public EditorHtmlPage(final Editor e) {
        this.e = e;
    }

    /**
     * @param ed The containing editor
     * @return Returns a html page in the given editor
     */
    public static EditorHtmlPage in(final Editor ed) {
        return new EditorHtmlPage(ed);
    }

    /**
     * @return The control to be added in the notebook editor
     */
    public Control getPage() {
        final Composite c = new Composite(e.getTheContainer(), SWT.NONE);
        c.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        c.setLayout(new GridLayout());
        EditorHeader.createHeader(e, c, new Runnable() {
            public void run() {
                updateHtmlPage(true);
            }
        });
        browser = new Browser(c, SWT.NONE);
        browser.setLayoutData(new GridData(GridData.FILL_BOTH));
        return c;
    }

    /**
     * @param refetch if true, the selected notes are filtered for the current
     *            query
     */
    void updateHtmlPage(final boolean refetch) {
        System.out.println("Updating HTML, refetch: " + refetch);
        if (refetch || e.filteredNotes == null) {
            e.filteredNotes = EditorHeader.filtered(e.allNotes, e.query, e
                    .selectedComboValue());
        }
        IFile f = e.getEditorFile();
        String name = f.getLocation().segment(
                f.getLocation().segmentCount() - 1);
        String html = f.getParent().getLocation().makeAbsolute()
                + File.separator + name + ".html";
        HtmlExport.getInstance(e.filteredNotes, html).export(true,
                e.queryString());
        try {
            f.getParent().refreshLocal(IResource.DEPTH_INFINITE, null);
        } catch (CoreException x) {
            x.printStackTrace();
        }
        browser.setText(Util.read(html));
    }
}
