/**
 * Created with love by corbett.
 * User: corbett
 * Date: 4/30/14
 * Time: 1:40 PM
 */
package cworks.treefs.client.builder.ls;

import cworks.treefs.client.builder.framework.MethodMeta;
import cworks.treefs.client.builder.framework.TransitionType;

import java.util.List;

public interface Glob_Recursive_FilesOnly<_ReturnType> {

    @MethodMeta(type = TransitionType.Recursive)
    Glob_Recursive_FilesOnly<_ReturnType> glob(String pattern);

    @MethodMeta(type = TransitionType.Lateral)
    Glob_FilesOnly<_ReturnType> recursive();

    @MethodMeta(type = TransitionType.Lateral)
    Glob_Recursive<_ReturnType> filesOnly();

    @MethodMeta(type = TransitionType.Terminal)
    _ReturnType fetch();

    @MethodMeta(type = TransitionType.Terminal)
    List<_ReturnType> fetchList();
}
