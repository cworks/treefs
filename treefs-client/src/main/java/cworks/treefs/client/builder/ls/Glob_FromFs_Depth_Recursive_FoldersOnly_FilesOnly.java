package cworks.treefs.client.builder.ls;

import cworks.treefs.client.builder.framework.MethodMeta;
import cworks.treefs.client.builder.framework.TransitionType;

import java.util.List;

public interface Glob_FromFs_Depth_Recursive_FoldersOnly_FilesOnly<_ReturnType> {

    @MethodMeta(type = TransitionType.Recursive)
    Glob_FromFs_Depth_Recursive_FoldersOnly_FilesOnly<_ReturnType> glob(String pattern);

    @MethodMeta(type = TransitionType.Lateral)
    Glob_Depth_Recursive_FoldersOnly_FilesOnly<_ReturnType> fromFs(String fs);

    @MethodMeta(type = TransitionType.Lateral)
    Glob_FromFs_Recursive_FoldersOnly_FilesOnly<_ReturnType> depth(int d);

    @MethodMeta(type = TransitionType.Lateral)
    Glob_FromFs_Depth_FoldersOnly_FilesOnly<_ReturnType> recursive();

    @MethodMeta(type = TransitionType.Lateral)
    Glob_FromFs_Depth_Recursive_FilesOnly<_ReturnType> foldersOnly();

    @MethodMeta(type = TransitionType.Lateral)
    Glob_FromFs_Depth_Recursive_FoldersOnly<_ReturnType> filesOnly();

    @MethodMeta(type = TransitionType.Terminal)
    _ReturnType fetch();

    @MethodMeta(type = TransitionType.Terminal)
    List<_ReturnType> fetchList();
}