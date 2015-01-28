/**
 * Created with love by corbett.
 * User: corbett
 * Date: 4/30/14
 * Time: 1:42 PM
 */
package net.cworks.treefs.client.builder.ls;

import net.cworks.treefs.client.builder.framework.MethodMeta;
import net.cworks.treefs.client.builder.framework.TransitionType;

import java.util.List;

public interface Glob_FromFs_FoldersOnly<_ReturnType> {

    @MethodMeta(type = TransitionType.Recursive)
    Glob_FromFs_FoldersOnly<_ReturnType> glob(String pattern);

    @MethodMeta(type = TransitionType.Lateral)
    Glob_FoldersOnly<_ReturnType> fromFs(String fs);

    @MethodMeta(type = TransitionType.Lateral)
    Glob_FromFs<_ReturnType> foldersOnly();

    @MethodMeta(type = TransitionType.Terminal)
    _ReturnType fetch();

    @MethodMeta(type = TransitionType.Terminal)
    List<_ReturnType> fetchList();
}
