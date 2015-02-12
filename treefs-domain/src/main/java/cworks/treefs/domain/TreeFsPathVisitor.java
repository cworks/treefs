package cworks.treefs.domain;

/**
 * visitor used in the walk of the tree structure
 */
public interface TreeFsPathVisitor {
    public void visit(TreeFsPath path);
}
