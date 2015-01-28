/**
 * Created with love by corbett.
 * User: corbett
 * Date: 4/30/14
 * Time: 1:35 PM
 */
package net.cworks.treefs.client.builder.ls;

import net.cworks.treefs.client.builder.framework.MethodMeta;
import net.cworks.treefs.client.builder.framework.TransitionType;

import java.util.List;

public interface Glob_Depth_Recursive_FoldersOnly_FilesOnly<_ReturnType> {

    @MethodMeta(type = TransitionType.Recursive)
    Glob_Depth_Recursive_FoldersOnly_FilesOnly<_ReturnType> glob(String pattern);

    @MethodMeta(type = TransitionType.Lateral)
    Glob_Recursive_FoldersOnly_FilesOnly<_ReturnType> depth(int d);

    @MethodMeta(type = TransitionType.Lateral)
    Glob_Depth_FoldersOnly_FilesOnly<_ReturnType> recursive();

    @MethodMeta(type = TransitionType.Lateral)
    Glob_Depth_Recursive_FilesOnly<_ReturnType> foldersOnly();

    @MethodMeta(type = TransitionType.Lateral)
    Glob_Depth_Recursive_FoldersOnly<_ReturnType> filesOnly();

    @MethodMeta(type = TransitionType.Terminal)
    _ReturnType fetch();

    @MethodMeta(type = TransitionType.Terminal)
    List<_ReturnType> fetchList();
}
