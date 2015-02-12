/**
 * Created with love by corbett.
 * User: corbett
 * Date: 4/30/14
 * Time: 1:44 PM
 */
package cworks.treefs.client.builder.ls;

import cworks.treefs.client.builder.framework.MethodMeta;
import cworks.treefs.client.builder.framework.TransitionType;

import java.util.List;

public interface Glob<_ReturnType> {
    @MethodMeta(type = TransitionType.Recursive)
    Glob<_ReturnType> glob(String pattern);

    @MethodMeta(type = TransitionType.Terminal)
    _ReturnType fetch();

    @MethodMeta(type = TransitionType.Terminal)
    List<_ReturnType> fetchList();

}
