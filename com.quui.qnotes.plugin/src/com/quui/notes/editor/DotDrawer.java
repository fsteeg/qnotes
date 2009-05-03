package com.quui.notes.editor;

import java.io.File;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * Class for drawing dot graphs, using an given dot installation, which is
 * called using Runtime.exec().
 * @author Fabian Steeg (fsteeg)
 */
public final class DotDrawer {

    private static final String DOT_CALL = "dot";
    private String outputFormat = "-T";
    private static final String VAR = "-o";
    private String resultPng;
    private String dotFile;
    private String dotAppPath;
    private String outputFolder;
    private String[] commands;
    private static final String CAPTION_DOT_SELECT_SHORT = "Where is dot?";
    private static final String CAPTION_DOT_SELECT_LONG = "Specify the directory containing the Graphviz dot binary.";
    private String inputFolder = null;

    /**
     * @param inFolder The folder containing the input dot file
     * @param outFolder The folder to store the result in
     * @param inputDotFile The dot file to render
     * @param outputImageFile The desired output image file name
     * @param dotLocation The path to the dot executable. Pass null to be asked
     *            via UI.
     */
    public DotDrawer(final String inFolder, final String outFolder,
            final String inputDotFile, final String outputImageFile,
            final String dotLocation) {
        outputFolder = outFolder;// + File.separator;
        inputFolder = inFolder;// + File.separator;
        dotFile = inputDotFile;
        resultPng = outputImageFile;
        dotAppPath = dotLocation;
        if (dotAppPath == null) {
            // get the saved location for dot
            dotAppPath = Activator.getDefault().getPreferenceStore().getString(
                    "dotpath");
            if (dotAppPath.equals("")) {
                // path to dot has not been given, aks for it
                askForDot();
            }
        }
        commands = new String[] { dotAppPath + DOT_CALL, outputFormat, VAR,
                outputFolder + resultPng, inputFolder + dotFile };
    }

    private void askForDot() {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        DirectoryDialog dialog = new DirectoryDialog(shell);
        dialog.setMessage(CAPTION_DOT_SELECT_LONG);
        dialog.setText(CAPTION_DOT_SELECT_SHORT);
        try {
            String open = dialog.open();
            if (open != null) {
                File folder = new File(open);
                String[] files = folder.list();
                boolean ok = false;
                for (int i = 0; i < files.length; i++) {
                    if (files[i].equals("dot") || files[i].equals("dot.exe")) {
                        ok = true;
                    }
                }
                if (!ok) {
                    MessageDialog
                            .openError(shell, "Not found", "Dot not found");
                } else {
                    String string = open + File.separator;
                    Activator.getDefault().getPreferenceStore().setValue(
                            "dotpath", string);
                    System.out.println("PATH: " + string);
                    System.out.println("SEP: " + File.separator);
                    dotAppPath = Activator.getDefault().getPreferenceStore()
                            .getString("dotpath");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Calls dot to render the image from the generated dot-file.
     * @param format The file format to export to
     * @return The process exit value (see {@link Process#exitValue()})
     */
    public int renderImage(final String format) {
        if (this.outputFormat.length() <= 2) {
            this.outputFormat = this.outputFormat + format;
        }
        commands[1] = outputFormat;
        System.out.println("Will use command:");
        for (String command : commands) {
            System.out.println("command: " + command);
        }
        Runtime runtime = Runtime.getRuntime();
        Process p = null;
        try {
            p = runtime.exec(commands);
            p.waitFor();
        } catch (Exception x) {
            x.printStackTrace();
        }
        System.out.println("Exit status: " + p.exitValue());
        return p.exitValue();
    }
}
