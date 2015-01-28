/**
 * Created with love by corbett.
 * User: corbett
 * Date: 4/30/14
 * Time: 1:43 PM
 */
package net.cworks.treefs.client.builder.ls;

import net.cworks.treefs.client.builder.framework.MethodMeta;
import net.cworks.treefs.client.builder.framework.TransitionType;

import java.util.List;

public interface Glob_FromFs_Depth<_ReturnType> {

    @MethodMeta(type = TransitionType.Recursive)
    Glob_FromFs_Depth<_ReturnType> glob(String pattern);

    @MethodMeta(type = TransitionType.Lateral)
    Glob_Depth<_ReturnType> fromFs(String fs);

    @MethodMeta(type = TransitionType.Lateral)
    Glob_FromFs<_ReturnType> depth(int d);

    @MethodMeta(type = TransitionType.Terminal)
    _ReturnType fetch();

    @MethodMeta(type = TransitionType.Terminal)
    List<_ReturnType> fetchList();
}
