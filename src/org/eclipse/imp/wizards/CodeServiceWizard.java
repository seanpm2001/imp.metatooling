/**
 * 
 */
package org.eclipse.uide.wizards;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.pde.core.plugin.IExtensions;
import org.eclipse.pde.core.plugin.IPluginAttribute;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginModel;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.IPluginObject;
import org.eclipse.pde.internal.core.bundle.WorkspaceBundleModel;
import org.eclipse.pde.internal.core.plugin.PluginElement;
	
/**
 * An ExtensionPointWizard that also generates source code from one or more template files.
 * @author rfuhrer@watson.ibm.com
 */
public abstract class CodeServiceWizard extends ExtensionPointWizard {
	protected String fProjectName;
    protected String fLanguageName;
    protected String fPackageName;
    protected String fPackageFolder;
    protected String fParserPackage;
    protected String fClassName;

    protected void collectCodeParms() {
    	fProjectName = pages[0].fProjectText.getText();
        fLanguageName= pages[0].fLanguageText.getText();
        fPackageName= pages[0].fPackageName;
        fPackageName= Character.toLowerCase(fPackageName.charAt(0)) + fPackageName.substring(1);
        fPackageFolder= fPackageName.replace('.', File.separatorChar);
        
        String[] subPkgs= fPackageName.split("\\.");
        StringBuffer buff= new StringBuffer();

        for(int i= 0; i < subPkgs.length-1; i++) {
            if (i > 0) buff.append('.');
            buff.append(subPkgs[i]);
        }
        buff.append(".parser");
        fParserPackage= buff.toString();
        fClassName= Character.toUpperCase(fLanguageName.charAt(0)) + fLanguageName.substring(1);
    }

    

    /**
     * Get the name of the package in which a plugin class is defined
     * for this project, or a default value if there is no such package
     * or if the project is null.  If no default name is provided, then
     * the name of the language is used for a default.
     * 
     * The intention here is to return a the name of the plugin package,
     * if the package exists, or a name that could be used as the name
     * of the plugin package, if the package does not exist.  So this
     * method should not return null and should not be used as a test
     * of whether a given project contains a plugin package or class.
     * 
     * 
     * 
     * SMS 23 Mar 2007
     * 
     * @param project		The project for which the plugin package name is sought;
     * 						may be null
     * @param defaultName	A name to return if the given package lacks a plugin class;
     * 						may be null
     * @return				The name of the package that contains the project's plugin
     * 						class, if there is one, or a name that could be used for the
     * 						plugin package, if there is none.
     */
    public String getPluginPackageName(IProject project, String defaultName)
    {
    	String result = defaultName;
    	if (result == null)
    		result = fLanguageName;
       	if (project != null) {
            String activator = null;
            IPluginModel pm = ExtensionPointEnabler.getPluginModelForProject(project);
            if (pm != null) {
            	WorkspaceBundleModel wbm = new WorkspaceBundleModel(project.getFile("META-INF/MANIFEST.MF")); //$NON-NLS-1$
            	activator = wbm.getBundle().getHeader("Bundle-Activator");
            }

            if (activator != null) {
            	result = activator.substring(0, activator.lastIndexOf("."));
            }
    	}
       	return result;
    }
    
    /**
     * Get the name of the plugin class for this project, or a default
     * name if there is no plugin class or if the given project is null.
     * If no default name is provided, then a name based on the name of
     * the language is used for a default.
     * 
     * The intention here is to return a the name of the plugin class,
     * if it exists, or a name that could be used as the name of the
     * plugin class, if it does not exist.  So this method should not
     * return null and should not be used as a test of whether a given
     * project contains a plugin class.
     * 
     * SMS 27 Mar 2007
     * 
     * @param project		The project for which the plugin class name is sought;
     * 						may be null
     * @param defaultName	A name to return if the given package lacks a plugin class;
     * 						may be null
     * @return				The name of the project's plugin class, if there is one,
     * 						or a name that could be used for the plugin class, if there
     * 						is none.
     */
    public String getPluginClassName(IProject project, String defaultName)
    {
    	String result = defaultName;
    	if (result == null)
    		result = fClassName + "Plugin";
       	if (project != null) {
            String activator = null;
            IPluginModel pm = ExtensionPointEnabler.getPluginModelForProject(project);
            if (pm != null) {
            	WorkspaceBundleModel wbm = new WorkspaceBundleModel(project.getFile("META-INF/MANIFEST.MF")); //$NON-NLS-1$
            	activator = wbm.getBundle().getHeader("Bundle-Activator");
            }

            if (activator != null) {
            	result = activator.substring(activator.lastIndexOf(".")+1);
            }
    	}
       	return result;
    }
    
    /**
     * Get the plugin id defined for this project, or a default value if
     * there is no plugin id or if the given project is null.   If no default
     * id is provided, then an id based on the name of the project is used
     * for a default.
     * 
     * The intention here is to return a plugin id, if it exists, or a
     * value that could be used as the id of the plugin, if it does not
     * exist.  So this method should not return null and should not be
     * used as a test of whether a given project has a plugin id.
     * 
     * SMS 27 Mar 2007
     * 
     * @param project		The project for which the plugin id name is sought;
     * 						may be null
     * @param defaultID		A value to return if the given package lacks a plugin id;
     * 						may be null
     * @return				The plugin id of the project, if there is one, or a value
     * 						that could be used as the plugin id, if there is none.
     */
    public String getPluginID(IProject project, String defaultID)
    {
    	String result = defaultID;
    	if (result == null)
    		getPluginPackageName(project, null);
       	if (project != null) {
            result = ExtensionPointEnabler.getPluginIDForProject(project);
    	}
       	return result;
    }
    
    
    // SMS 23 Mar 2007
    // This version takes an IProject and provides mappings
    // related to the project's plugin aspect
    public Map<String,String> getStandardSubstitutions(IProject project) {
    	Map<String, String> result = getStandardSubstitutions();
    	result.remove("$PLUGIN_PACKAGE$");
        result.put("$PLUGIN_PACKAGE$", getPluginPackageName(project, null));
        // SMS 27 Mar 2007
    	result.remove("$PLUGIN_CLASS$");
        result.put("$PLUGIN_CLASS$", getPluginClassName(project, null));
        result.remove("$PLUGIN_ID$");
        result.put("$PLUGIN_ID$", getPluginID(project, null));
        return result;
    }
    
    
    
    public Map<String, String> getStandardSubstitutions() {
        Map<String,String> result = new HashMap();
        
        // SMS 17 May 2006
        // Need to get a name for the AST package and AST node type for use in
        // the NewFoldingUpdater wizard
        // Note:  The method used assumes that these are the default values
        // (if that assumption is wrong, then the generated folding service won't
        // compile, but if we don't provide any values then it won't compile in
        // any case--specifically because substitutions for these parameters will
        // not have been made)
        result = getASTInformation();
        
        // continuing with original:
        result.put("$LANG_NAME$", fLanguageName);
        result.put("$CLASS_NAME_PREFIX$", fClassName);
        result.put("$PACKAGE_NAME$", fPackageName);
        // SMS 22 Mar 2007
        result.put("$PROJECT_NAME$", fProjectName);
        // SMS 23 Mar 2007
        // Not the greatest solution, but if we don't have the
        // project then we may as well assume that $PLUGIN_PACKAGE$
        // has a default value
        result.put("$PLUGIN_PACKAGE$", getPluginPackageName(null, null));
        // SMS 27 Mar 2007:  ditto
        result.put("$PLUGIN_CLASS$", getPluginClassName(null, null));
        result.put("$PLUGIN_ID$", getPluginID(null, null));

        return result;
    }
    
    
    /**
     * Return a Map containing the the names of the AST package and class
     * bound to "well-known" symbols, "$AST_PACKAGE$" and "$AST_CLASS$", 
     * respectively.
     * 
     * WARNING:  The names returned are currently the DEFAULT names (which
     * should be the most commonly occurring but which may not be appropriate
     * in general).
     * 
     * The actual values for the AST package and class are generated in the
     * NewParser wizard but are not (yet) stored anywhere for reference by
     * other wizards.  There is at least one other wizard, the NewFoldingUpdater
     * wizard, which does need these names to complete a template.  In order
     * to make available some reasonable values for these names, this method
     * recomputes the names using the same assumptions as are used for the
     * default case in the NewParser wizard.
     * 
     * TODO:  Provide a means for (more) persistently maintaining the names
     * of hte AST package and class in such a way that they can become part
     * of the "standard substitutions."  (ALTERNATIVELY:  the class could just
     * be obtained in wizards where needed, in which case it need not be part
     * of the standard substitutions.)
     * 
     * @return	A Map that contains two valuse:  the name of the package that
     * 			contains the AST class, and the name of the AST class.
     * 			These values keyed, respectively, by the symbols "$AST_PACKAGE$"
     * 			and "$AST_NODE$".
     * 
     * Updates:  Stan Sutton, 9 Aug 2006
     * 			Changed return from $AST_CLASS$ to $AST_NODE$ since the latter
     * 			is the symbol more commonly used (and the one on which I will
     * 			try to standardize)
     * 
     * @author	Stan Sutton
     * @since	17 May 2006
     */
    protected Map<String,String> getASTInformation()
    {
    	Map<String,String> result = new HashMap();
    	
        // Get the extension that represents the parser
        IPluginModelBase pluginModel = pages[0].getPluginModel();
        IExtensions extensionsThing = pluginModel.getExtensions();
        IPluginExtension[] extensions = extensionsThing.getExtensions();
        IPluginExtension parserExtension = null;
        for (int i = 0; i < extensions.length; i++) {
        	if(extensions[i].getPoint().equals("org.eclipse.uide.runtime.parser")) {
        		parserExtension = extensions[i];
        		break;
        	}
        }

        // Get the plugin element that represents the class of the parser
        PluginElement parserPluginElement = null;
        if (parserExtension != null) {
        	IPluginObject[] children = parserExtension.getChildren();
        	for (int i = 0; i < children.length; i++) {
        		if(children[i].getName().equals("parser")) {
        			parserPluginElement = (PluginElement) children[i];
        				break;
        		}
        	}
        }
        if (parserPluginElement == null) return result;

        /*
        IPluginAttribute astPackageAttribute = parserPluginElement.getAttribute("class");
        String astPackageName = astPackageAttribute.getValue();
        astPackageName = astPackageName.substring(0, astPackageName.lastIndexOf('.'));
        astPackageName += ".Ast";
        String astClassName = "ASTNode";
        */
        
        // Get the names of the parser package, AST package, and AST (node) class name
        
        IPluginAttribute parserClassAttribute = parserPluginElement.getAttribute("class");
        String parserPackageName = parserClassAttribute.getValue();
        parserPackageName = parserPackageName.substring(0, parserPackageName.lastIndexOf('.'));
        // ASSUME that the AST package name is the parser package name extended 
        // with ".Ast" (this is the default when auto-generated)
        String astPackageName = parserPackageName + ".Ast";
        // Just assume this is true
        // TBD:  check whether this exists (or put the info somewhere from
        // where it can be retrieved here)
        String astClassName = "ASTNode";
        
        // Save these values in the substitutions map
        result.put("$PROJ_NAME$", fProjectName);
        result.put("$PARSER_PACKAGE$", parserPackageName);
        result.put("$AST_PACKAGE$", astPackageName);
        result.put("$AST_NODE$", astClassName);
        
        return result;
    }
    
}
