    package $PACKAGE_NAME$;

import org.eclipse.uide.core.ILanguageService;
import org.eclipse.uide.editor.ISourceFormatter;
import org.eclipse.uide.parser.IParseController;

import $AST_PACKAGE$.*;


public class $FORMATTER_CLASS_NAME$ implements ILanguageService, ISourceFormatter {
    private int fIndentSize= 4;
    private String fIndentString;

    public void formatterStarts(String initialIndentation) {
        // Should pick up preferences here
        fIndentSize= 4;
        StringBuffer buff= new StringBuffer(fIndentSize);
        for(int i=0; i < fIndentSize; i++)
            buff.append(' ');
        fIndentString= buff.toString();
    }

    public String format(IParseController parseController, String content, boolean isLineStart, String indentation, int[] positions) {
        final StringBuffer buff= new StringBuffer();
        $AST_NODE$ root= ($AST_NODE$) parseController.getCurrentAst();

        // SMS 9 Aug 2006
        // The original call to root.accept(..) assumes that the AST-related
        // classes (including AbstractVisitor) are generated within the parser
        // class, which (I don't think) is what happens by default now in the
        // usual case (instead they're in the AST package)
        //root.accept(new $CLASS_NAME_PREFIX$Parser.AbstractVisitor() {
        root.accept(new AbstractVisitor() {
            private int prodCount;
            private int prodIndent;
            public void unimplementedVisitor(String s) {
                System.out.println("Unhandled node type: " + s);
            }
            // START_HERE
            // Put in some visit methods with node types
            // appropriate to your AST
            ///*
            public boolean visit(assignmentStmt n) {
                buff.append(fIndentString);
                return true;
            }
            public boolean visit(declaration0 n) {
                buff.append(fIndentString);
                buff.append(n.getint());
                buff.append(' ');
                buff.append(n.getIDENTIFIER());
                return true;
            }
            public boolean visit(declaration1 n) {
                buff.append(fIndentString);
                buff.append(n.getshort());
                buff.append(' ');
                buff.append(n.getIDENTIFIER());
                return true;
            }
            //*/
        });

    return buff.toString();
    }

    public void formatterStops() {
    // TODO Auto-generated method stub
    }
}
