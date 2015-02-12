package cworks.treefs.awssp.tree;

interface S3Visitable<T> {

    public void accept(S3Visitor<T> visitor);
}
