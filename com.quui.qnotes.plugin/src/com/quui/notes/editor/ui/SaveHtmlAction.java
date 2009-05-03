package com.quui.notes.editor.ui;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.quui.notes.editor.Activator;
import com.quui.notes.editor.data.HtmlExport;
import com.quui.notes.editor.data.Note;
import com.quui.notes.editor.ui.mpe.Editor;

/**
 * @author Fabian Steeg (fsteeg)
 */
public final class SaveHtmlAction extends Action {
    private String last = null;

    /**
     * Creates the actiuon with a particular default name, tooltip and icon.
     */
    public SaveHtmlAction() {
        this.setText("Save HTML...");
        this.setToolTipText("Save as HTML file...");
        this.setImageDescriptor(Activator.getImageDescriptor("icons/www.gif"));
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run() {
        IWorkbenchWindow window = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow();
        Editor editor = Editor.in(window);
        List<Note> selection = editor.getFilteredNotes();
        String query = editor.queryString();
        String location = askForFile(window);
        HtmlExport.getInstance(selection, location).export(false, query);
    }

    private String askForFile(final IWorkbenchWindow window) {
        FileDialog dialog = new FileDialog(window.getShell(), SWT.MULTI);
        dialog.setFilterExtensions(new String[] { "*.html" });
        if (last != null) {
            dialog.setFilterPath(last);
        }
        String result = dialog.open();
        last = result;
        return result;
    }
}
