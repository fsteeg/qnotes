package com.quui.notes.editor.ui.mpe;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.progress.IProgressService;

import com.quui.notes.editor.data.Note;

/**
 * @author Fabian Steeg (fsteeg)
 */
public final class EditorForm {

    private ScrolledForm form;
    private FormToolkit toolkit;
    private Editor ed;

    /**
     * @param ed The editor to create this form in
     * @return The form
     */
    public static EditorForm in(final Editor ed) {
        return new EditorForm(ed);
    }

    private EditorForm(final Editor ed) {
        this.ed = ed;
        toolkit = new FormToolkit(ed.getTheContainer().getDisplay());
        form = toolkit.createScrolledForm(ed.getTheContainer());
        TableWrapLayout layout = new TableWrapLayout();
        form.getBody().setLayout(layout);
        form.setText("Loading Notes...");
        Composite c = toolkit.createComposite(form.getBody());
        toolkit.paintBordersFor(c);
        c.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
        c.setLayout(new GridLayout());
        EditorHeader.createHeader(ed, c, new Runnable() {
            public void run() {
                updateFormPage(true);
            }
        });
        Button button = toolkit.createButton(form.getBody(), "Add", SWT.FLAT);
        button.addSelectionListener(new SelectionListener() {
            public void widgetSelected(final SelectionEvent e) {
                ed.query = "";
                ed.allNotes.add(0, Note.getInstance("", "New Note",
                        new java.sql.Date(System.currentTimeMillis())
                                .toString(), "", "false"));
                ed.updateEditorFromList();
                buildEditPage();
            }

            public void widgetDefaultSelected(final SelectionEvent e) {}
        });
        buildEditPage();
    }

    private void buildEditPage() {
        ed.allNotes = ed.updateListFromEditor();
        updateFormPage(true);
        form.reflow(true);
    }

    /**
     * @param refetch @see {@link #doFormUpdate(boolean)}
     */
    protected void updateFormPage(final boolean refetch) {
        IWorkbench wb = PlatformUI.getWorkbench();
        IProgressService ps = wb.getProgressService();
        try {
            ps.busyCursorWhile(new IRunnableWithProgress() {
                public void run(final IProgressMonitor pm) {
                    Display.getDefault().asyncExec(new Runnable() {
                        public void run() {
                            doFormUpdate(refetch);
                        }
                    });
                }
            });
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * @param refetch If true, the list is refiltered
     */
    protected void doFormUpdate(final boolean refetch) {
        long start = System.currentTimeMillis();
        System.out.println("Updating Form, refetch: " + refetch);
        if (refetch || ed.filteredNotes == null) {
            ed.filteredNotes = EditorHeader.filtered(ed.allNotes, ed.query, ed
                    .selectedComboValue());
        }
        long end = System.currentTimeMillis() - start;
        start = System.currentTimeMillis();
        System.out.println("Filtering notes took: " + end);
        Control[] children = form.getBody().getChildren();
        for (Control c : children) {
            if (c instanceof Section) {
                c.dispose();
            }
        }
        end = System.currentTimeMillis() - start;
        start = System.currentTimeMillis();
        System.out.println("Disposing children took: " + end);
        form
                .setText(ed.filteredNotes.size() + " notes for "
                        + ed.queryString());
        insertNotes();
        end = System.currentTimeMillis() - start;
        start = System.currentTimeMillis();
        System.out.println("Creating children took: " + end);
        form.reflow(true);
        end = System.currentTimeMillis() - start;
        start = System.currentTimeMillis();
        System.out.println("Reflowing took: " + end);

    }

    private void insertNotes() {
        /* TODO this needs some simplification */
        for (final Note n : ed.filteredNotes.subList(0, Math.min(
                ed.filteredNotes.size(), Integer.parseInt(ed.limit.getText())))) {
            Section section = new Section(form.getBody(), Section.TITLE_BAR
                    | Section.TWISTIE | Section.EXPANDED);
            section.setText(n.toString());
            toolkit.paintBordersFor(section);
            Composite c1 = toolkit.createComposite(section);
            c1.setLayout(new TableWrapLayout());
            Composite c2 = toolkit.createComposite(c1);
            c2.setLayout(new GridLayout(7, false));
            c2.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
            toolkit.createLabel(c2, "Title:");
            final Text title = toolkit.createText(c2, n.getTitle());
            title.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            final Button pub = toolkit.createButton(c2, "public", SWT.CHECK);
            pub.setSelection(n.isPublic());
            final Text date = toolkit.createText(c2, n.getDate());
            toolkit.createLabel(c2, "Tags:");
            final Text tag = toolkit.createText(c2, n.getTag());
            Button delete = toolkit.createButton(c2, "Delete", SWT.FLAT);
            SelectionListener selectionListener = new SelectionListener() {
                public void widgetSelected(SelectionEvent e) {
                    ed.allNotes.remove(n);
                    Composite parent = ((Button) e.getSource()).getParent();
                    Composite parent2 = parent;
                    while (!(parent2 instanceof Section)) {
                        parent2 = parent2.getParent();
                    }
                    parent2.dispose();
                    ed.updateEditorFromList();
                    buildEditPage();
                }
                public void widgetDefaultSelected(final SelectionEvent e) {}
            };
            delete.addSelectionListener(selectionListener);
            final Text formText = toolkit.createText(c1, n.getText(), SWT.WRAP);
            final KeyListener keyListener = new KeyListener() {
                public void keyReleased(final KeyEvent e) {
                    if (!n.getTag().equals(tag.getText())
                            || !n.getText().equals(formText.getText())
                            || !n.getTitle().equals(title.getText())
                            || !n.getDate().equals(date.getText())
                            || !n.isPublic() == pub.getSelection()) {
                        n.setTag(tag.getText());
                        n.setText(formText.getText());
                        n.setTitle(title.getText());
                        n.setDate(date.getText());
                        n.setPublic(pub.getSelection());
                        if (!ed.editor.isDirty()) {
                            IDocument document = ed.editor
                                    .getDocumentProvider().getDocument(
                                            ed.editor.getEditorInput());
                            String string = document.get();
                            document.set(string + "");
                        }
                    }
                }
                public void keyPressed(final KeyEvent e) {}
            };
            title.addKeyListener(keyListener);
            date.addKeyListener(keyListener);
            tag.addKeyListener(keyListener);
            formText.addKeyListener(keyListener);
            pub.addSelectionListener(new SelectionListener() {
                public void widgetSelected(SelectionEvent e) {
                    keyListener.keyReleased(null);
                }
                public void widgetDefaultSelected(SelectionEvent e) {}
            });
            toolkit.paintBordersFor(c1);
            toolkit.paintBordersFor(c2);
            section.setClient(c1);
            TableWrapData td = new TableWrapData(TableWrapData.FILL_GRAB);
            td.colspan = 1;
            td.grabHorizontal = true;
            td.grabVertical = true;
            section.setLayoutData(td);
        }
    }

    /**
     * @return The scrolled form
     */
    public ScrolledForm getForm() {
        return form;
    }
}
