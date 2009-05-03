package com.quui.notes.editor.ui.wizards;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Scanner;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import com.quui.notes.editor.Activator;

/**
 * This is a notebook new wizard. Its role is to create a new file resource in
 * the provided container. If the container resource (a folder or a project) is
 * selected in the workspace when the wizard is opened, it will accept it as the
 * target container. The wizard creates one file with the extension "nxml". If a
 * notebook editor (also available as a template) is registered for the same
 * extension, it will be able to open it.
 * @author Fabian Steeg (fsteeg)
 */
public final class NotesNewWizard extends Wizard implements INewWizard {
    private NotesNewWizardPage page;
    private ISelection selection;

    /**
     * Constructor for NotesNewWizard.
     */
    public NotesNewWizard() {
        super();
        setNeedsProgressMonitor(true);
    }

    /**
     * {@inheritDoc}
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    public void addPages() {
        page = new NotesNewWizardPage(selection);
        addPage(page);
    }

    /**
     * {@inheritDoc}
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    public boolean performFinish() {
        final String containerName = page.getContainerName();
        final String fileName = page.getFileName();
        IRunnableWithProgress op = new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor)
                    throws InvocationTargetException {
                try {
                    doFinish(containerName, fileName, monitor);
                } catch (CoreException e) {
                    throw new InvocationTargetException(e);
                } finally {
                    monitor.done();
                }
            }
        };
        try {
            getContainer().run(true, false, op);
        } catch (InterruptedException e) {
            return false;
        } catch (InvocationTargetException e) {
            Throwable realException = e.getTargetException();
            MessageDialog.openError(getShell(), "Error", realException
                    .getMessage());
            return false;
        }
        return true;
    }

    /**
     * The worker method. It will find the container, create the file if missing
     * or just replace its contents, and open the editor on the newly created
     * file.
     */
    private void doFinish(String containerName, String fileName,
            IProgressMonitor monitor) throws CoreException {
        monitor.beginTask("Creating " + fileName, 2);
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IResource resource = root.findMember(new Path(containerName));
        if (!resource.exists() || !(resource instanceof IContainer)) {
            throwCoreException("Container \"" + containerName
                    + "\" does not exist.");
        }
        IContainer container = (IContainer) resource;
        final IFile file = container.getFile(new Path(fileName));
        try {
            InputStream stream = openContentStream();
            if (file.exists()) {
                file.setContents(stream, true, true, monitor);
            } else {
                file.create(stream, true, monitor);
            }
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        monitor.worked(1);
        monitor.setTaskName("Opening file for editing...");
        getShell().getDisplay().asyncExec(new Runnable() {
            public void run() {
                IWorkbenchPage page = PlatformUI.getWorkbench()
                        .getActiveWorkbenchWindow().getActivePage();
                try {
                    IDE.openEditor(page, file, true);
                } catch (PartInitException e) {
                    e.printStackTrace();
                }
            }
        });
        monitor.worked(1);
    }

    /**
     * We will initialize file contents with a sample text.
     */
    private InputStream openContentStream() {
        String contents = read("new_notebook.xml"); //$NON-NLS-1$
        return new ByteArrayInputStream(contents.getBytes());
    }

    private String read(final String loc) {
        try {
            URL url = FileLocator.find(Activator.getDefault().getBundle(),
                    new Path(loc), Collections.EMPTY_MAP);
            URL resolved = FileLocator.resolve(url);
            Scanner s = new Scanner(new File(resolved.toURI()));
            StringBuilder builder = new StringBuilder();
            while (s.hasNextLine()) {
                builder.append(s.nextLine()).append(
                        System.getProperty("line.separator")); //$NON-NLS-1$
            }
            return builder.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void throwCoreException(String message) throws CoreException {
        IStatus status = new Status(IStatus.ERROR, "com.quui.notes.editor",
                IStatus.OK, message, null);
        throw new CoreException(status);
    }

    /**
     * {@inheritDoc}
     * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
     *      org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.selection = selection;
    }
}
