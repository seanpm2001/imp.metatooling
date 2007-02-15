package $PACKAGE_NAME$;

$IPROJECT_IMPORT$
import org.eclipse.uide.core.ProjectNatureBase;
import org.eclipse.uide.runtime.IPluginLog;
$SMAPI_IMPORT$
import $LANG_NAME$.$CLASS_NAME_PREFIX$Plugin;

public class $CLASS_NAME_PREFIX$Nature extends ProjectNatureBase {
    public static final String k_natureID = $CLASS_NAME_PREFIX$Plugin.kPluginID + ".safari.nature";

    public String getNatureID() {
        return k_natureID;
    }

    public String getBuilderID() {
        return $BUILDER_CLASS_NAME$.BUILDER_ID;
    }
$SMAP_SUPPORT$
    protected void refreshPrefs() {
        // TODO implement preferences and hook in here
    }

    public IPluginLog getLog() {
        return $CLASS_NAME_PREFIX$Plugin.getInstance();
    }

    protected String getDownstreamBuilderID() {
        // TODO Auto-generated method stub
        return null;
    }
}