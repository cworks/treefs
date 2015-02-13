package cworks.treefs.security;

import org.junit.Assert;
import org.junit.Test;

public class TreeFsSecurityTest {

    @Test
    public void testDefaultAuthorization() {

        // This is one way to get config file into system...via the treefs.clients property
        System.setProperty("treefs.clients", "src/test/resources/treefsclients.json");
        Assert.assertFalse(TreeFsSecurity.isAuthorized("awsuser"));
        Assert.assertTrue(TreeFsSecurity.isAuthorized("sysuser"));
        Assert.assertTrue(TreeFsSecurity.isAuthorized("treefsuser"));
        Assert.assertTrue(TreeFsSecurity.isAuthorized("corbofett"));

    }
}
