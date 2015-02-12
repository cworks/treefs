package cworks.treefs.awssp.tree;

public interface S3Visitor<T> {

    public S3Visitor<T> visitTree(S3Tree<T> tree);

    public void visitData(S3Tree<T> parent, T data);
}
