package cworks.treefs.awssp.tree;

import java.util.LinkedHashSet;
import java.util.Set;

public class S3Tree<T> implements S3Visitable<T> {

    // NB: LinkedHashSet preserves insertion order
    private final Set<S3Tree> children = new LinkedHashSet<S3Tree>();
    private final T data;

    public S3Tree(T data) {
        this.data = data;
    }

    public void accept(S3Visitor<T> visitor) {
        visitor.visitData(this, data);
        for (S3Tree child : children) {
            S3Visitor<T> childVisitor = visitor.visitTree(child);
            child.accept(childVisitor);
        }
    }

    public S3Tree child(T data) {
        for (S3Tree child: children ) {
            if (child.data.equals(data)) {
                return child;
            }
        }

        return child(new S3Tree(data));
    }

    S3Tree child(S3Tree<T> child) {
        children.add(child);
        return child;
    }

    public String toString() {
        return this.data.toString();
    }
}
