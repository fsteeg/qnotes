package com.quui.notes.editor.ui;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.quui.notes.editor.Activator;
import com.quui.notes.editor.data.Note;
import com.quui.notes.editor.data.NotesWriter;
import com.quui.notes.editor.ui.mpe.Editor;

/**
 * @author Fabian Steeg (fsteeg)
 */
public final class SaveXmlAction extends Action {
    /**
     * Creates the action with default title, tooltip and icon.
     */
    public SaveXmlAction() {
        this.setText("Save Notebook...");
        this.setToolTipText("Save as Notebook XML file...");
        this.setImageDescriptor(Activator.getImageDescriptor("icons/xml.gif"));
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
        FileDialog dialog = new FileDialog(window.getShell(), SWT.MULTI);
        dialog.setFilterExtensions(new String[] { "*.*" });
        String result = dialog.open();
        NotesWriter.getInstance(selection).write(result);
    }
}
