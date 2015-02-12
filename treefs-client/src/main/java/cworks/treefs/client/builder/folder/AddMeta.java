package cworks.treefs.client.builder.folder;

import cworks.treefs.client.builder.framework.MethodMeta;
import cworks.treefs.client.builder.framework.TransitionType;

public interface AddMeta<_ReturnType> {

    @MethodMeta(type = TransitionType.Recursive)
    AddMeta<_ReturnType> addMeta(String k, Object v);

    @MethodMeta(type = TransitionType.Terminal)
    _ReturnType create();
}
