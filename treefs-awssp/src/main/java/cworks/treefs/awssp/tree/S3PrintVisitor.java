package cworks.treefs.awssp.tree;

public class S3PrintVisitor implements S3Visitor<String> {

    private final int indent;

    public S3PrintVisitor(int indent) {
        this.indent = indent;
    }

    @Override
    public S3Visitor<String> visitTree(S3Tree<String> tree) {
        return new S3PrintVisitor(indent + 2);
    }

    @Override
    public void visitData(S3Tree<String> parent, String data) {
        for (int i = 0; i < indent; i++) {
            System.out.print(" ");
        }

        System.out.println(data);
    }
}
