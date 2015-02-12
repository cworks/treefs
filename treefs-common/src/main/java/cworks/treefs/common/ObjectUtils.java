package cworks.treefs.common;

import java.util.Collection;

public class ObjectUtils {

    public static boolean isEmpty(String thing) {
        return "".equals(thing.trim()) ? true : false;
    }

    public static boolean isNull(Object thing) {
        return thing == null ? true : false;
    }

    public static boolean isNullOrEmpty(Object thing) {
        if(isNull(thing)) {
            return true;
        }
        if(thing instanceof Collection) {
            Collection collection = (Collection)thing;
            return collection.isEmpty();
        }
        if(thing instanceof String) {
            return isEmpty((String)thing);
        }

        return false;
    }
}
