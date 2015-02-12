/**
 * Created with love by corbett.
 * User: corbett
 * Date: 4/30/14
 * Time: 1:38 PM
 */
package cworks.treefs.client.builder.ls;

import cworks.treefs.client.builder.framework.TransitionType;
import cworks.treefs.client.builder.framework.MethodMeta;

import java.util.List;

public interface Glob_Depth_Recursive_FoldersOnly<_ReturnType> {

    @MethodMeta(type = TransitionType.Recursive)
    Glob_Depth_Recursive_FoldersOnly<_ReturnType> glob(String pattern);

    @MethodMeta(type = TransitionType.Lateral)
    Glob_Recursive_FoldersOnly<_ReturnType> depth(int d);

    @MethodMeta(type = TransitionType.Lateral)
    Glob_Depth_FoldersOnly<_ReturnType> recursive();

    @MethodMeta(type = TransitionType.Lateral)
    Glob_Depth_Recursive<_ReturnType> foldersOnly();

    @MethodMeta(type = TransitionType.Terminal)
    _ReturnType fetch();

    @MethodMeta(type = TransitionType.Terminal)
    List<_ReturnType> fetchList();
}
