package com.quui.notes.editor.ui.mpe;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

/**
 * @author Fabian Steeg (fsteeg)
 */
public final class EditorResourceChangeListener implements
        IResourceChangeListener {

    protected IEditorPart editor;

    /**
     * @param editor The editor to close when a corresponding resource is closed
     */
    public EditorResourceChangeListener(final IEditorPart editor) {
        this.editor = editor;
    }

    /**
     * Closes all project files on project close. {@inheritDoc}
     * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
     */
    public void resourceChanged(final IResourceChangeEvent event) {
        if (event.getType() == IResourceChangeEvent.PRE_CLOSE) {
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    IWorkbenchPage[] pages = PlatformUI.getWorkbench()
                            .getActiveWorkbenchWindow().getPages();
                    for (int i = 0; i < pages.length; i++) {
                        if (((FileEditorInput) editor.getEditorInput())
                                .getFile().getProject().equals(
                                        event.getResource())) {
                            IEditorPart editorPart = pages[i].findEditor(editor
                                    .getEditorInput());
                            pages[i].closeEditor(editorPart, true);
                        }
                    }
                }
            });
        }
    }

}
