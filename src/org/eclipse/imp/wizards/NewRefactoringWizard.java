	package org.eclipse.imp.wizards;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.pde.core.plugin.IPluginModel;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.imp.runtime.RuntimePlugin;
import org.eclipse.imp.utils.ExtensionPointUtils;

public class NewRefactoringWizard extends GeneratedComponentWizard {
    protected static final String thisWizardName= "New Refactoring";

    protected static final String thisWizardDescription= "Wizard for creating a new refactoring";

    private List<GeneratedComponentAttribute> refactoringAttributes;

    private WizardPageField fPrefixField;

    private WizardPageField fNameField;

    private String fRefactoringName;

    private String fRefactoringPrefix;

    public WizardPageField fToolTipField;

    public WizardPageField fDescriptionField;

    private String fToolTip;

    private String fDescription;

    public NewRefactoringWizard() {
	super();
    }

    public void addPages() {
	refactoringAttributes= setupAttributes();
	addPages(new GeneratedComponentWizardPage[] { new NewRefactoringPage(this) });
    }

    private List<GeneratedComponentAttribute> setupAttributes() {
	return Collections.emptyList();
    }

    class NewRefactoringPage extends GeneratedComponentWizardPage {
	public NewRefactoringPage(GeneratedComponentWizard owner) {
	    super(owner, RuntimePlugin.IMP_RUNTIME, "refactoring", false, refactoringAttributes.toArray(new GeneratedComponentAttribute[refactoringAttributes
		    .size()]), thisWizardName, thisWizardDescription);
	}

	protected void createFirstControls(Composite parent) {
	    super.createFirstControls(parent);
	    fPrefixField= new WizardPageField(null, "refactoringPrefix", "Refactoring Name Prefix:", "InsertCrap", 0, true,
	    				      "Prefix used for the various refactoring implementation classes");
	    createLabelText(parent, fPrefixField);
	    fNameField= new WizardPageField(null, "refactoringName", "Refactoring Name:", "Insert Crap", 0, true,
		    			    "Human-readable name of the refactoring");
	    createLabelText(parent, fNameField);
	    fToolTipField= new WizardPageField(null, "toolTip", "Refactoring tooltip:", "something suitably pithy", 0, true,
	    				       "Tool-tip to appear when hovering cursor over menu item in user interface");
	    createLabelText(parent, fToolTipField);
	    fDescriptionField= new WizardPageField(null, "description", "Refactoring Description:", "something suitably verbose", 0, true,
	    					   "Prefix used for the various refactoring implementation classes");
	    createLabelText(parent, fDescriptionField);
	    fFields.add(fPrefixField);
	    fFields.add(fNameField);
	    fFields.add(fToolTipField);
	    fFields.add(fDescriptionField);
	}
    }

    protected List getPluginDependencies() {
	return Arrays.asList(new String[] { "org.eclipse.core.runtime", "org.eclipse.core.resources", "org.eclipse.imp.runtime", "org.eclipse.jdt.core",
		"org.eclipse.jdt.ui", "org.eclipse.ui.ide", "org.eclipse.ltk.core.refactoring", "org.eclipse.ltk.ui.refactoring" });
    }

    protected static class InsertLocation {
	private static final int AT_BEGINNING= 0;

	private static final int BEFORE= 1;

	private static final int AFTER= 2;

	private static final int AT_END= 3;

	private final String fAnchor;

	private final int fRelation;

	protected InsertLocation(String s, int relation) {
	    fAnchor= s;
	    fRelation= relation;
	}

	public static InsertLocation after(String s) {
	    return new InsertLocation(s, AFTER);
	}

	public int getLocationIn(String contents) {
	    if (fRelation == AT_BEGINNING)
		return 0;
	    if (fRelation == AT_END)
		return contents.length();
	    
	    int where= contents.indexOf(fAnchor);

	    if (where >= 0) {
		if (fRelation == AFTER)
		    return where + fAnchor.length();
		else
		    return where;
	    }
	    return -1;
	}

	public static InsertLocation atEnd() {
	    return new InsertLocation(null, AT_END);
	}

	public static InsertLocation atBeginning() {
	    return new InsertLocation(null, AT_BEGINNING);
	}
    }

    @Override
    protected void collectCodeParms() {
        super.collectCodeParms();
	fRefactoringName= fNameField.getText();
	fRefactoringPrefix= fPrefixField.getText();
	fToolTip= fToolTipField.getText();
	fDescription= fDescriptionField.getText();
    }

    protected void generateCodeStubs(IProgressMonitor mon) throws CoreException {
	GeneratedComponentWizardPage page= (GeneratedComponentWizardPage) pages[0];
	IProject project= page.getProject();
	Map<String,String> subs= getStandardSubstitutions();
	String packageName= "safari." + page.sLanguage + ".refactoring";// qualifiedClassName.substring(0, qualifiedClassName.lastIndexOf('.'));
	String packageFolder= packageName.replace('.', File.separatorChar);

	subs.put("$LANG_NAME$", page.sLanguage);
	subs.put("$REFACTORING_NAME$", fRefactoringName);
	subs.put("$REFACTORING_PREFIX$", fRefactoringPrefix);
	subs.put("$PACKAGE_NAME$", packageName);
	createFileFromTemplate(fRefactoringPrefix + "InputPage.java", "refactoringInputPage.java", packageFolder, subs, project, mon);
	createFileFromTemplate(fRefactoringPrefix + "Wizard.java", "refactoringWizard.java", packageFolder, subs, project, mon);
	createFileFromTemplate(fRefactoringPrefix + "RefactoringAction.java", "refactoringAction.java", packageFolder, subs, project, mon);

	String contributorClassName= "RefactoringContributor";
	String contributorQualClassName= packageName + "." + contributorClassName;

	if (ExtensionPointUtils.findExtensionByName("refactoringContributor", pages[0].getPluginModel()) == null) {
	    try {
		ExtensionPointEnabler.addExtension(
			// SMS 24 Jul 2007:  added project parameter reflecting change
			// in ExtensionPointEnabler (evidently)
			pages[0].getProject(),
			(IPluginModel) pages[0].getPluginModel().getAdapter(IPluginModel.class),
			RuntimePlugin.IMP_RUNTIME,
			"refactoringContributions",
			new String[][] { { "refactoringContributor:language", pages[0].sLanguage },
				         { "refactoringContributor:class", contributorQualClassName } },
		    getPluginDependencies()
		);
		createFileFromTemplate(contributorClassName + ".java", "refactoringContributor.java", packageFolder, subs, project, mon);
		createFileFromTemplate("RefactoringMessages.java", "refactoringMessages.java", packageFolder, subs, project, mon);
		createFile("RefactoringMessages.properties", packageFolder, project, mon);
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
	insertCode("new " + fRefactoringPrefix + "RefactoringAction(editor),", InsertLocation.after("return new IAction[] {"), contributorClassName + ".java", packageFolder, project, mon);
	insertCode(fRefactoringPrefix + ".label=" + fRefactoringName + "\n" +
		fRefactoringPrefix + ".tooltip=" + fToolTip + "\n" +
		fRefactoringPrefix + ".description=" + fDescription + "\n",
		InsertLocation.atEnd(),
		"RefactoringMessages.properties",
		packageFolder, project, mon);

	IFile refactoringSrc= createFileFromTemplate(fRefactoringPrefix + "Refactoring.java", "refactoring.java", packageFolder, subs, project, mon);

	editFile(mon, refactoringSrc);
    }

    private void insertCode(String newCode, InsertLocation location, String intoClass, String folder, IProject project, IProgressMonitor monitor) {
	try {
	    IFile srcFile= getFile(intoClass, folder, project);

	    if (!srcFile.exists())
		return;

	    String currentContents= new String(getFileContents(srcFile));
	    int textPos= location.getLocationIn(currentContents);
	    StringBuffer buff= new StringBuffer();

	    buff.append(currentContents, 0, textPos);
	    buff.append(newCode);
	    buff.append(currentContents, textPos, currentContents.length());
	    srcFile.setContents(new StringBufferInputStream(buff.toString()), true, true, monitor);
	} catch (CoreException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    private byte[] getFileContents(IFile srcFile) throws CoreException, IOException {
	InputStream fis= srcFile.getContents();
	DataInputStream is= new DataInputStream(fis);
	byte bytes[]= new byte[fis.available()];
	is.readFully(bytes);
	is.close();
	fis.close();
	return bytes;
    }

    @Override
    protected Map<String,String> getStandardSubstitutions() {
	return new HashMap<String,String>();
    }
}
