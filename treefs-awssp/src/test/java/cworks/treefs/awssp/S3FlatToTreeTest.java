package cworks.treefs.awssp;

import cworks.treefs.awssp.tree.S3PrintVisitor;
import cworks.treefs.awssp.tree.S3Tree;
import org.junit.Test;

import java.util.Arrays;

public class S3FlatToTreeTest {

    private static final String[] subPaths = new String[] {
        "unittest",
        "unittest/n1_1",
        "unittest/n1_1/n2_1",
        "unittest/n1_1/n2_1/corn.txt",
        "unittest/n1_1/n2_1/eagles_eggs.txt",
        "unittest/n1_1/n2_1/n3_1",
        "unittest/n1_1/n2_1/n3_1/hooray_nacho.xlsx",
        "unittest/n1_1/n2_1/n3_1/ouch.docx",
        "unittest/n1_1/n2_1/n3_2",
        "unittest/n1_1/n2_1/n3_2/n4_1",
        "unittest/n1_1/n2_1/n3_2/n4_1/corn.txt",
        "unittest/n1_1/n2_1/n3_2/n4_1/encarnacion.txt",
        "unittest/n1_1/n2_1/n3_2/n4_1/ramses.txt",
        "unittest/n1_1/n2_1/n3_2/n4_2",
        "unittest/n1_1/n2_1/n3_2/n4_2/eagles_eggs.txt",
        "unittest/n1_1/n2_1/n3_2/nacho_hero.jpg",
        "unittest/n1_1/n2_1/nacho_libre.zip",
        "unittest/n1_1/n2_2",
        "unittest/n1_1/n2_2/n3_1",
        "unittest/n1_1/n2_2/n3_1/nacho_fight.pptx",
        "unittest/n1_1/n2_2/n3_2",
        "unittest/n1_1/n2_2/n3_2/n4_1",
        "unittest/n1_1/n2_2/n3_2/n4_1/hooray_nacho.xlsx",
        "unittest/n1_1/n2_2/n3_2/n4_1/nacho_libre.pdf",
        "unittest/n1_1/n2_2/n3_2/n4_1/ouch.docx",
        "unittest/n1_1/n2_2/n3_2/n4_2",
        "unittest/n1_1/n2_2/n3_2/n4_2/corn.txt",
        "unittest/n1_1/n2_2/n3_2/n4_2/ouch.docx",
        "unittest/n1_1/n2_2/n3_2/n4_2/religious_man.txt",
        "unittest/n1_1/n2_2/n3_2/nacho_eggs.png",
        "unittest/n1_1/n2_2/n3_2/religious_man.txt",
        "unittest/n1_1/n2_2/n3_3",
        "unittest/n1_1/n2_2/n3_3/n4_1",
        "unittest/n1_1/n2_2/n3_3/n4_1/nacho_eggs.png",
        "unittest/n1_1/n2_2/n3_3/n4_1/nacho_libre.pdf",
        "unittest/n1_1/n2_2/n3_3/n4_1/ouch.docx",
        "unittest/n1_1/n2_2/n3_3/n4_1/religious_man.txt",
        "unittest/n1_1/n2_2/n3_3/nacho_eggs.png",
        "unittest/n1_1/n2_2/n3_3/nacho_libre.pdf",
        "unittest/n1_1/n2_2/nacho_fight.pptx",
        "unittest/n1_1/nacho_eggs.png",
        "unittest/n1_1/nacho_fight.pptx",
        "unittest/n1_1/nacho_libre.pdf",
        "unittest/n1_2",
        "unittest/n1_2/n2_1",
        "unittest/n1_2/n2_2",
        "unittest/n1_3",
        "unittest/nacho_libre.pdf",
    };

    /**
     * AWS S3 lists subpaths in a flattened format and StorageProvider interface expects only
     * one SFolder be returned from certain operations such as openFolder.  However SFolder
     * items can contain other SPath(s) if client calls openFolder and specifies a depth.  So
     * a SPath graph must be built instead of a flattened structure like S3 or the subPaths variable
     * below.
     */
    @Test
    public void flatToTree() {

        S3Tree<String> tree  = new S3Tree<String>("/");
        S3Tree<String> current = tree;
        for(String path : Arrays.asList(subPaths)) {
            S3Tree<String> root = current;

            for (String item : path.split("/")) {
                current = current.child(item);
            }

            current = root;
        }

        tree.accept(new S3PrintVisitor(2));
    }

}



