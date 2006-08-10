package org.eclipse.uide.wizards;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2005  All Rights Reserved
 */

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.uide.WizardPlugin;
import org.eclipse.uide.core.ErrorHandler;
import org.eclipse.uide.utils.StreamUtils;
import org.osgi.framework.Bundle;

/**
 * This wizard creates a new file resource in the provided container. 
 * The wizard creates one file with the extension "g". 
 */
public abstract class GeneratedComponentWizard extends Wizard implements INewWizard {
    private static final String START_HERE= "// START_HERE";

    protected int currentPage;

    protected GeneratedComponentWizardPage pages[];

    protected int NPAGES;

    public GeneratedComponentWizard() {
	super();
	setNeedsProgressMonitor(true);
    }

    public int getPageCount() {
	return NPAGES;
    }

    protected void addPages(GeneratedComponentWizardPage[] pages) {
	this.pages= pages;
	NPAGES= pages.length;
	for(int n= 0; n < pages.length; n++) {
	    addPage(pages[n]);
	}
	List/*<String>*/extenRequires= getPluginDependencies();
	for(Iterator/*<String>*/iter= extenRequires.iterator(); iter.hasNext();) {
	    String requiredPlugin= (String) iter.next();
	    for(int n= 0; n < pages.length; n++) {
		List/*<String>*/pageRequires= pages[n].getRequires();
		pageRequires.add(requiredPlugin);
	    }
	}
    }

    public IWizardPage getPreviousPage(IWizardPage page) {
	if (currentPage == 0)
	    return null;
	return pages[currentPage];
    }

    public IWizardPage getNextPage(IWizardPage page) {
	if (currentPage == pages.length - 1)
	    return null;
	return pages[++currentPage];
    }

    public boolean canFinish() {
	return super.canFinish();// pages[currentPage].isPageComplete() && (currentPage >= pages.length - 1);
    }

    /**
     * @return the list of plugin dependencies for this language service.
     */
    protected abstract List getPluginDependencies();

    /**
     * Generate any necessary code for this extension from template files in the
     * templates directory.<br>
     * Implementations can use <code>getTemplateFile(String)</code> to access the
     * necessary template files.<br>
     * Implementations must be careful not to access the fields of the wizard page,
     * as this code will probably be called from a thread other than the UI thread.
     * I.e., don't write something like:<br>
     * <code>pages[0].languageText.getText()</code><br>
     * Instead, in the wizard class, override <code>collectCodeParams()</code>,
     * which gets called earlier from the UI thread, and save any necessary data
     * in fields in the wizard class.
     * @param monitor
     * @throws CoreException
     */
    protected abstract void generateCodeStubs(IProgressMonitor mon) throws CoreException;

    /**
     * Implementers of generateCodeStubs() should override this to collect any
     * necessary information from the fields in the various wizard pages needed
     * to generate code.
     */
    protected void collectCodeParms() {}

    /**
     * This method is called when 'Finish' button is pressed in the wizard.
     * We will create an operation and run it using wizard as execution context.
     */
    public boolean performFinish() {
	collectCodeParms(); // Do this in the UI thread while the wizard fields are still accessible
	IRunnableWithProgress op= new IRunnableWithProgress() {
	    public void run(IProgressMonitor monitor) throws InvocationTargetException {
		IWorkspaceRunnable wsop= new IWorkspaceRunnable() {
		    public void run(IProgressMonitor monitor) throws CoreException {
			try {
			    for(int n= 0; n < pages.length; n++) {
					GeneratedComponentWizardPage page= pages[n];
	
					// SMS 26 Jul 2006:
					// Presumably not needed if we're not generating an extension
					/*
					// BUG Make sure the extension ID is correctly set
					if (!page.hasBeenSkipped() && page.fSchema != null)
					    //ExtensionPointEnabler.enable(page, monitor);
						CodeGeneratorEnabler.enable(page, monitor);
					*/
			    }
			    generateCodeStubs(monitor);
			} catch (Exception e) {
			    ErrorHandler.reportError("Could not add extension points", e);
			} finally {
			    monitor.done();
			}
		    }
		};
		try {
		    ResourcesPlugin.getWorkspace().run(wsop, monitor);
		} catch (Exception e) {
		    ErrorHandler.reportError("Could not add extension points", e);
		}
	    }
	};
	try {
	    getContainer().run(true, false, op);
	} catch (InvocationTargetException e) {
	    Throwable realException= e.getTargetException();
	    ErrorHandler.reportError("Error", realException);
	    return false;
	} catch (InterruptedException e) {
	    return false;
	}
	return true;
    }

    public void setPage(int page) {
	currentPage= page;
    }

    public void init(IWorkbench workbench, IStructuredSelection selection) {}

    /**
     * Opens the given file in the appropriate editor for editing.<br>
     * If the file contains a comment "// START_HERE", the cursor will
     * be positioned just after that.
     * @param monitor
     * @param file
     */
    protected void editFile(IProgressMonitor monitor, final IFile file) {
	monitor.setTaskName("Opening file for editing...");
	getShell().getDisplay().asyncExec(new Runnable() {
	    public void run() {
		IWorkbenchPage page= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		try {
		    IEditorPart editorPart= IDE.openEditor(page, file, true);
		    AbstractTextEditor editor= (AbstractTextEditor) editorPart;
		    IFileEditorInput fileInput= (IFileEditorInput) editorPart.getEditorInput();
		    String contents= StreamUtils.readStreamContents(file.getContents(), file.getCharset());
		    int cursor= contents.indexOf(START_HERE);

		    if (cursor >= 0) {
			TextSelection textSel= new TextSelection(editor.getDocumentProvider().getDocument(fileInput), cursor, START_HERE.length());
			editor.getEditorSite().getSelectionProvider().setSelection(textSel);
		    }
		} catch (PartInitException e) {
		} catch (CoreException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	    }
	});
	monitor.worked(1);
    }

    protected IFile createFileFromTemplate(String fileName, String templateName, String folder, Map replacements,
	    IProject project, IProgressMonitor monitor) throws CoreException {
	monitor.setTaskName("Creating " + fileName);

	final IFile file= project.getFile(new Path("src/" + folder + "/" + fileName));
	String templateContents= new String(getTemplateFile(templateName));
	String contents= performSubstitutions(templateContents, replacements);

	if (file.exists()) {
	    file.setContents(new ByteArrayInputStream(contents.getBytes()), true, true, monitor);
	} else {
            createSubFolders("src/" + folder, project, monitor);
	    file.create(new ByteArrayInputStream(contents.getBytes()), true, monitor);
	}
//	monitor.worked(1);
	return file;
    }
    
    
    protected void createSubFolders(String folder, IProject project, IProgressMonitor monitor) throws CoreException {
        String[] subFolderNames= folder.split("[\\" + File.separator + "\\/]");
        String subFolderStr= "";

        for(int i= 0; i < subFolderNames.length; i++) {
            String childPath= subFolderStr + "/" + subFolderNames[i];
            Path subFolderPath= new Path(childPath);
            IFolder subFolder= project.getFolder(subFolderPath);

            if (!subFolder.exists())
                subFolder.create(true, true, monitor);
            subFolderStr= childPath;
        }
    }

    public static void replace(StringBuffer sb, String target, String substitute) {
	for(int index= sb.indexOf(target); index != -1; index= sb.indexOf(target))
	    sb.replace(index, index + target.length(), substitute);
    }

    protected String performSubstitutions(String contents, Map replacements) {
	StringBuffer buffer= new StringBuffer(contents);

	for(Iterator iter= replacements.keySet().iterator(); iter.hasNext();) {
	    String key= (String) iter.next();
	    String value= (String) replacements.get(key);

	    if (value != null)
		replace(buffer, key, value);
	}
	return buffer.toString();
    }

    protected String getTemplateBundleID() {
	return WizardPlugin.kPluginID;
    }

    protected byte[] getTemplateFile(String fileName) {
	try {
	    Bundle bundle= Platform.getBundle(getTemplateBundleID());
	    URL templateURL= Platform.find(bundle, new Path("/templates/" + fileName));
            if (templateURL == null) {
                ErrorHandler.reportError("Unable to find template file: " + fileName, true);
                return new byte[0];
            }
            URL url= Platform.asLocalURL(templateURL);
	    String path= url.getPath();
	    FileInputStream fis= new FileInputStream(path);
	    DataInputStream is= new DataInputStream(fis);
	    byte bytes[]= new byte[fis.available()];

	    is.readFully(bytes);
	    is.close();
	    fis.close();
	    return bytes;
	} catch (Exception e) {
	    e.printStackTrace();
	    return ("// missing template file: " + fileName).getBytes();
	}
    }

    protected abstract Map getStandardSubstitutions();
}