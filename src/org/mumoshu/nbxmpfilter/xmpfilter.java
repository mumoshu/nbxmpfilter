/*
 * xmpfilter for NetBeans (org.mumoshu.xmpfilter)
 *
 * Copyright 2009 mumoshu <http://d.hatena.ne.jp/mumoshu>.
 * All rights reserved.
 *
 * requires xmpfilter installed, and the PATH is set to the installed location.
 */
package org.mumoshu.nbxmpfilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileObject;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

public final class xmpfilter extends CookieAction {

    protected void performAction(Node[] activatedNodes) {
        EditorCookie editorCookie = activatedNodes[0].getLookup().lookup(EditorCookie.class);
        DataObject data = activatedNodes[0].getLookup().lookup(DataObject.class);
        FileObject file = data.files().toArray(new FileObject[0])[0];

//        デバッグ用の諸々
//        for (Node n : activatedNodes) {
//            System.out.println(n); // => org.netbeans.modules.gsf.GsfDataNode@1668534[Name=boot, displayName=boot.rb]
//        }
//        System.out.println("editorCookie: " + editorCookie); // => editorCookieorg.netbeans.modules.gsf.GsfDataObject$GenericEditorSupport@beafe7
//        System.out.println("data: " + data); // => data: org.netbeans.modules.gsf.GsfDataObject@84c1f9[C:\Users\ykuoka\Documents\NetBeansProjects\Depot\config\boot.rb]
//        System.out.println("file: " + file); // => file: C:\Users\ykuoka\Documents\NetBeansProjects\Depot\config\boot.rb

        String rbfile = file.toString();
        try {
            editorCookie.saveDocument();

            /* Process p = Runtime.getRuntime().exec("ruby -S xmpfilter -a " + file);
             * だとPATHを見てくれないらしいので、
             * ProcessBuilderでxmpfilterを起動。
             * 参考: http://www.02.246.ne.jp/~torutk/javahow2/exec.htm
             */
            ProcessBuilder pb = new ProcessBuilder("xmpfilter.bat", file.toString());
            pb.directory(new File(new File(rbfile).getParent()));

            Process p = pb.start();

            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String text = "";
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                text += line + "\n";
            }

            StyledDocument doc = editorCookie.getDocument();
            doc.remove(0, doc.getLength());
            doc.insertString(0, text, null);

            int ret = p.waitFor();
            System.out.println("xmpfilter exited with value : " + ret);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (BadLocationException ble) {
            ble.printStackTrace();
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    protected int mode() {
        return CookieAction.MODE_EXACTLY_ONE;
    }

    public String getName() {
        return NbBundle.getMessage(xmpfilter.class, "CTL_xmpfilter");
    }

    protected Class[] cookieClasses() {
        return new Class[]{EditorCookie.class};
    }

    @Override
    protected void initialize() {
        super.initialize();
        // see org.openide.util.actions.SystemAction.iconResource() Javadoc for more details
        putValue("noIconInMenu", Boolean.TRUE);
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}

