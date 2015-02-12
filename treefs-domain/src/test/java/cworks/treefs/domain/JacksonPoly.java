package cworks.treefs.domain;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import cworks.json.Json;
import org.junit.Assert;
import org.junit.Test;

/**
 * Just playing with basic Jackson polymorphic serialization
 */
public class JacksonPoly {

    @Test
    public void serialize() {

        String treefsfile = "{\"type\":\"fil\",\"name\":\"aFileName\"}";
        TreeFsObj obj = Json.asObject(treefsfile, TreeFsObj.class);
        System.out.println(Json.asString(obj));
        if(!(obj instanceof TreeFsFil)) {
            Assert.fail("obj should be an instance of TreeFsFil!");
        }

        String treefsfolder = "{\"type\":\"fol\",\"name\":\"aFolderName\"}";
        obj = Json.asObject(treefsfolder, TreeFsObj.class);
        System.out.println(Json.asString(obj));
        if(!(obj instanceof TreeFsFol)) {
            Assert.fail("obj should be an instance of TreeFsFol!");
        }

        String treefspath = "{\"type\":\"pth\",\"name\":\"aPathName\"}";
        obj = Json.asObject(treefspath, TreeFsObj.class);
        System.out.println(Json.asString(obj));
        if(!(obj instanceof TreeFsPth)) {
            Assert.fail("obj should be an instance of TreeFsPth!");
        }

    }

}

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value=TreeFsPth.class, name="pth"),
    @JsonSubTypes.Type(value=TreeFsFil.class, name="fil"),
    @JsonSubTypes.Type(value=TreeFsFol.class, name="fol")
})
class TreeFsObj {
    public TreeFsObj() { }
}

class TreeFsPth extends TreeFsObj {
    public TreeFsPth() { }

}

class TreeFsFil extends TreeFsPth {
    public TreeFsFil() { }
}

class TreeFsFol extends TreeFsPth {
    public TreeFsFol() { }

}
