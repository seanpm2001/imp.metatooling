/**
 * 
 */
package org.eclipse.uide.wizards;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.uide.runtime.RuntimePlugin;

public class NewHyperlinkDetector extends CodeServiceWizard {
    public void addPages() {
        addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, RuntimePlugin.UIDE_RUNTIME, "hyperLink"), });
    }

    protected List getPluginDependencies() {
        return Arrays.asList(new String[] { "org.eclipse.core.runtime", "org.eclipse.core.resources",
    	    "org.eclipse.uide.runtime" });
    }

    public void generateCodeStubs(IProgressMonitor mon) throws CoreException {
        ExtensionPointWizardPage page= (ExtensionPointWizardPage) pages[0];
        IProject project= page.getProject();
        Map subs= getStandardSubstitutions();

        // SMS 14 Jul 2006:  This bit is from the original
        // (should check how parameter is used)
//      { "$PKG_NAME$", fPackageName },
        subs.put("$PARSER_PKG$", fParserPackage);

        
        // SMS 14 Jul 2006
        WizardPageField field = pages[0].getField("class");
        String qualifiedClassName = field.fValue;
        String className = qualifiedClassName.substring(qualifiedClassName.lastIndexOf('.')+1) + ".java";
        subs.remove("$PACKAGE_NAME$");
        String packageName = qualifiedClassName.substring(0, qualifiedClassName.lastIndexOf('.'));
        subs.put("$PACKAGE_NAME$", packageName);
        String packageFolder = packageName.replace('.', File.separatorChar);
        
        IFile detectorSrc= createFileFromTemplate(className, "hyperlink_detector.tmpl", packageFolder, subs, project, mon);
        //IFile resolverSrc = createFileFromTemplate(fClassName + "ReferenceResolver.java", "reference_resolver.tmpl", fPackageFolder, subs, project, mon);
        IFile resolverSrc = createFileFromTemplate(fClassName + "ReferenceResolver.java", "reference_resolver.tmpl", packageFolder, subs, project, mon);
        
        editFile(mon, detectorSrc);
        editFile(mon, resolverSrc);
    }
}