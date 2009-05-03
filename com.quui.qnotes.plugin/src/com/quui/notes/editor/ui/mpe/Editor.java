package com.quui.notes.editor.ui.mpe;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.MultiPageEditorPart;

import com.quui.notes.editor.Util;
import com.quui.notes.editor.data.Note;
import com.quui.notes.editor.data.NotesReader;
import com.quui.notes.editor.data.NotesWriter;
import com.quui.notes.editor.data.NotesFilter.SearchOption;

/**
 * An editor for notes.
 * <ul>
 * <li>page 0 contains a nested text editor
 * <li>page 1 contains a HTML preview of the notes
 * <li>page 2 contains a form for editing the notes
 * </ul>
 * @author Fabian Steeg (fsteeg)
 */
public final class Editor extends MultiPageEditorPart {
    private static final String FORM = "Edit";
    private static final String HTML = "View";
    /** The text editor used in page 0. */
    TextEditor editor;
    /** The edit form. */
    private EditorForm form;
    /** All notes for the current file. */
    List<Note> allNotes;
    /** The current view on all notes. */
    List<Note> filteredNotes;
    /** The filter query. */
    String query = "";
    /** The note field to search in. */
    int searchOption = 0;
    /** The limit of notes to show in the form. */
    Text limit;
    private EditorResourceChangeListener resourceChangeListener;
    private EditorHtmlPage htmlPage;

    /**
     * Creates a multi-page editor.
     */
    public Editor() {
        super();
        resourceChangeListener = new EditorResourceChangeListener(this);
        ResourcesPlugin.getWorkspace().addResourceChangeListener(
                resourceChangeListener);
    }

    /**
     * {@inheritDoc}
     * @see org.eclipse.ui.part.MultiPageEditorPart#createPages()
     */
    protected void createPages() {
        createTextPage();
        createHtmlPage();
        createFormPage();
        setActivePage(1);
    }

    void createTextPage() {
        try {
            editor = new TextEditor();
            int index = addPage(editor, getEditorInput());
            setPageText(index, editor.getTitle());
            setPartName(editor.getPartName());
        } catch (PartInitException e) {
            ErrorDialog.openError(getSite().getShell(),
                    "Error creating nested text editor", null, e.getStatus());
        }
    }

    void createFormPage() {
        form = EditorForm.in(this);
        int index = addPage(form.getForm());
        setPageText(index, FORM);
    }

    void createHtmlPage() {
        htmlPage = EditorHtmlPage.in(this);
        int index = addPage(htmlPage.getPage());
        setPageText(index, HTML);
    }

    Composite getTheContainer() {
        return super.getContainer();
    }

    public String queryString() {
        return "'" + (query.trim().equals("") ? "anything" : query) + "'"
                + " in " + selectedComboValue();
    }

    /**
     * {@inheritDoc}
     * @see org.eclipse.ui.part.MultiPageEditorPart#dispose()
     */
    public void dispose() {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(
                resourceChangeListener);
        super.dispose();
    }

    /**
     * {@inheritDoc}
     * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void doSave(final IProgressMonitor monitor) {
        updateEditorFromList();
        getEditor(0).doSave(monitor);
        monitor.beginTask("Reflowing form...", IProgressMonitor.UNKNOWN);
        form.getForm().reflow(true);
        monitor.done();
    }

    /**
     * {@inheritDoc}
     * @see org.eclipse.ui.part.EditorPart#doSaveAs()
     */
    public void doSaveAs() {
        IEditorPart editor = getEditor(0);
        editor.doSaveAs();
        setPageText(0, editor.getTitle());
        setInput(editor.getEditorInput());
    }

    /**
     * Method declared on IEditorPart.
     */
    public void gotoMarker(final IMarker marker) {
        setActivePage(0);
        IDE.gotoMarker(getEditor(0), marker);
    }

    /**
     * {@inheritDoc}
     * @see org.eclipse.ui.part.MultiPageEditorPart#init(org.eclipse.ui.IEditorSite,
     *      org.eclipse.ui.IEditorInput)
     */
    public void init(final IEditorSite site, final IEditorInput editorInput)
            throws PartInitException {
        if (!(editorInput instanceof IFileEditorInput)) {
            throw new PartInitException(
                    "Invalid Input: Must be IFileEditorInput");
        }
        super.init(site, editorInput);
    }

    /**
     * {@inheritDoc}
     * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
     */
    public boolean isSaveAsAllowed() {
        return true;
    }

    /**
     * {@inheritDoc}
     * @see org.eclipse.ui.part.MultiPageEditorPart#pageChange(int)
     */
    protected void pageChange(final int newPageIndex) {
        System.err.println("Page change");
        super.pageChange(newPageIndex);
        if (newPageIndex == 0) {
            updateEditorFromList();
        } else if (newPageIndex == 1) {
            htmlPage.updateHtmlPage(true);
        } else if (newPageIndex == 2) {
            form.updateFormPage(true);
        }
    }

    void updateEditorFromList() {
        // TODO here and in the other place, use direct String
        // processing!
        java.io.File file;
        try {
            long start = System.currentTimeMillis();
            file = File.createTempFile("note", null);
            NotesWriter.getInstance(allNotes).write(file.getAbsolutePath());
            long end = System.currentTimeMillis() - start;
            start = System.currentTimeMillis();
            System.out.println("Writing to temp file took: " + end);
            final String content = Util.read(file.getAbsolutePath() + ".nxml");
            System.out.println("Read TEXT: " + content);
            end = System.currentTimeMillis() - start;
            start = System.currentTimeMillis();
            System.out.println("Reading from temp file took: " + end);
            final IDocument document = editor.getDocumentProvider()
                    .getDocument(editor.getEditorInput());
            end = System.currentTimeMillis() - start;
            start = System.currentTimeMillis();
            System.out.println("Getting the editor document took: " + end);
            if (!document.get().equals(content)) {
                document.set(content);
            }
            end = System.currentTimeMillis() - start;
            System.out.println("Setting editor content took: " + end);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    List<Note> updateListFromEditor() {
        System.out.println("Update for query: " + query);
        long start = System.currentTimeMillis();
        String editorText = editor.getDocumentProvider().getDocument(
                editor.getEditorInput()).get();
        long end = System.currentTimeMillis() - start;
        start = System.currentTimeMillis();
        System.out.println("Getting editor input took: " + end);
        try {
            java.io.File file = File.createTempFile("notes", null);
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            end = System.currentTimeMillis() - start;
            start = System.currentTimeMillis();
            System.out.println("Writing to temp file took: " + end);
            writer.write(editorText);
            System.out.println("Wrote TEXT: " + editorText);
            writer.close();
            List<Note> result = NotesReader.getInstance(file.getAbsolutePath())
                    .read(null, null);
            end = System.currentTimeMillis() - start;
            System.out.println("Reading from temp file took: " + end);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    String selectedComboValue() {
        return new ArrayList<SearchOption>(Arrays.asList(SearchOption.values()))
                .get(searchOption).toString();
    }

    IFile getEditorFile() {
        IEditorInput editorInput = editor.getEditorInput();
        IFile f = ((IFileEditorInput) editorInput).getFile();
        return f;
    }

    /**
     * @param window The window that should contain a notebook
     * @return Returns a notebook editor if that was in the window
     */
    public static Editor in(final IWorkbenchWindow window) {
        IEditorPart activeEditor = window.getActivePage().getActiveEditor();
        if (!(activeEditor instanceof Editor)) {
            throw new IllegalStateException(
                    "We are in an action for the notebook editor, "
                            + "but the active editor is no notebook;");
        }
        Editor editor = (Editor) activeEditor;
        return editor;
    }

    public List<Note> getFilteredNotes() {
        return filteredNotes;
    }
}
