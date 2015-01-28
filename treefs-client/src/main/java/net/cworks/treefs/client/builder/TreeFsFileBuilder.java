package net.cworks.treefs.client.builder;

import net.cworks.treefs.domain.TreeFsFile;

import java.io.File;

public class TreeFsFileBuilder {
    public interface Start extends In<Void> { }

    public interface In<_ReturnType> {
        AddMeta_Overwrite_SaveAs_Size_Sha1_Key in(String path);
    }

    // addMeta	overwrite	saveAs	size	sha1	key
    public interface AddMeta_Overwrite_SaveAs_Size_Sha1_Key<_ReturnType> {
        // transition=recursive
        AddMeta_Overwrite_SaveAs_Size_Sha1_Key<_ReturnType> addMeta(String k, Object v);
        // transition=lateral
        AddMeta_SaveAs_Size_Sha1_Key<_ReturnType> overwrite(boolean option);
        // transition=lateral
        AddMeta_Overwrite_Size_Sha1_Key<_ReturnType> saveAs(String name);
        // transition=lateral
        AddMeta_Overwrite_SaveAs_Sha1_Key<_ReturnType> size(long n);
        // transition=lateral
        AddMeta_Overwrite_SaveAs_Size_Key<_ReturnType> sha1(String hash);
        // transition=lateral
        AddMeta_Overwrite_SaveAs_Size_Sha1<_ReturnType> key(String key);
        // transition=terminal
        TreeFsFile create();
    }

    public interface AddMeta_Overwrite_SaveAs_Sha1_Key<_ReturnType> {
        // transition=recursive
        AddMeta_Overwrite_SaveAs_Sha1_Key<_ReturnType> addMeta(String k, Object v);
        // transition=lateral
        AddMeta_SaveAs_Sha1_Key<_ReturnType> overwrite(boolean option);
        // transition=lateral
        AddMeta_Overwrite_Sha1_Key<_ReturnType> saveAs(String name);
        // transition=lateral
        AddMeta_Overwrite_SaveAs_Key<_ReturnType> sha1(String hash);
        // transition=lateral
        AddMeta_Overwrite_SaveAs_Sha1<_ReturnType> key(String key);
        // transition=terminal
        TreeFsFile create();
    }

    // addMeta	overwrite	saveAs	size	sha1
    public interface AddMeta_Overwrite_SaveAs_Size_Sha1<_ReturnType> {
        // transition=recursive
        AddMeta_Overwrite_SaveAs_Size_Sha1<_ReturnType> addMeta(String k, Object v);
        // transition=lateral
        AddMeta_SaveAs_Size_Sha1<_ReturnType> overwrite(boolean option);
        // transition=lateral
        AddMeta_Overwrite_Size_Sha1<_ReturnType> saveAs(String name);
        // transition=lateral
        AddMeta_Overwrite_SaveAs_Sha1<_ReturnType> size(long n);
        // transition=lateral
        AddMeta_Overwrite_SaveAs_Size<_ReturnType> sha1(String hash);
        // transition=terminal
        TreeFsFile create();
    }

    public interface AddMeta_Overwrite_SaveAs_Size_Key<_ReturnType> {
        // transition=recursive
        AddMeta_Overwrite_SaveAs_Size_Key<_ReturnType> addMeta(String k, Object v);
        // transition=lateral
        AddMeta_SaveAs_Size_Key<_ReturnType> overwrite(boolean option);
        // transition=lateral
        AddMeta_Overwrite_Size_Key<_ReturnType> saveAs(String name);
        // transition=lateral
        AddMeta_Overwrite_SaveAs_Key<_ReturnType> size(long n);
        // transition=lateral
        AddMeta_Overwrite_SaveAs_Size<_ReturnType> key(String key);
        // transition=terminal
        TreeFsFile create();
    }

    public interface AddMeta_Overwrite_Size_Sha1_Key<_ReturnType> {
        // transition=recursive
        AddMeta_Overwrite_Size_Sha1_Key<_ReturnType> addMeta(String k, Object v);
        // transition=lateral
        AddMeta_Size_Sha1_Key<_ReturnType> overwrite(boolean option);
        // transition=lateral
        AddMeta_Overwrite_Sha1_Key<_ReturnType> size(long n);
        // transition=lateral
        AddMeta_Overwrite_Size_Key<_ReturnType> sha1(String hash);
        // transition=lateral
        AddMeta_Overwrite_Size_Sha1<_ReturnType> key(String key);
        // transition=terminal
        TreeFsFile create();
    }

    public interface AddMeta_SaveAs_Size_Sha1_Key<_ReturnType> {
        // transition=recursive
        AddMeta_SaveAs_Size_Sha1_Key<_ReturnType> addMeta(String k, Object v);
        // transition=lateral
        AddMeta_Size_Sha1_Key<_ReturnType> saveAs(String name);
        // transition=lateral
        AddMeta_SaveAs_Sha1_Key<_ReturnType> size(long n);
        // transition=lateral
        AddMeta_SaveAs_Size_Key<_ReturnType> sha1(String hash);
        // transition=lateral
        AddMeta_SaveAs_Size_Sha1<_ReturnType> key(String key);
        // transition=terminal
        TreeFsFile create();
    }

    public interface AddMeta_Size_Sha1_Key<_ReturnType> {
        // transition=recursive
        AddMeta_Size_Sha1_Key<_ReturnType> addMeta(String k, Object v);
        // transition=lateral
        AddMeta_Sha1_Key<_ReturnType> size(long n);
        // transition=lateral
        AddMeta_Size_Key<_ReturnType> sha1(String hash);
        // transition=lateral
        AddMeta_Size_Sha1<_ReturnType> key(String key);
        // transition=terminal
        TreeFsFile create();
    }

    public interface AddMeta_SaveAs_Sha1_Key<_ReturnType> {
        // transition=recursive
        AddMeta_SaveAs_Sha1_Key<_ReturnType> addMeta(String k, Object v);
        // transition=lateral
        AddMeta_Sha1_Key<_ReturnType> saveAs(String name);
        // transition=lateral
        AddMeta_SaveAs_Key<_ReturnType> sha1(String hash);
        // transition=lateral
        AddMeta_SaveAs_Sha1<_ReturnType> key(String key);
        // transition=terminal
        TreeFsFile create();
    }

    public interface AddMeta_SaveAs_Size_Key<_ReturnType> {
        // transition=recursive
        AddMeta_SaveAs_Size_Key<_ReturnType> addMeta(String k, Object v);
        // transition=lateral
        AddMeta_Size_Key<_ReturnType> saveAs(String name);
        // transition=lateral
        AddMeta_SaveAs_Key<_ReturnType> size(long n);
        // transition=lateral
        AddMeta_SaveAs_Size<_ReturnType> key(String key);
        // transition=terminal
        TreeFsFile create();
    }

    public interface AddMeta_SaveAs_Size_Sha1<_ReturnType> {
        // transition=recursive
        AddMeta_SaveAs_Size_Sha1<_ReturnType> addMeta(String k, Object v);
        // transition=lateral
        AddMeta_Size_Sha1<_ReturnType> saveAs(String name);
        // transition=lateral
        AddMeta_SaveAs_Sha1<_ReturnType> size(long n);
        // transition=lateral
        AddMeta_SaveAs_Size<_ReturnType> sha1(String hash);
        // transition=terminal
        TreeFsFile create();
    }

    public interface AddMeta_Overwrite_Sha1_Key<_ReturnType> {
        // transition=recursive
        AddMeta_Overwrite_Sha1_Key<_ReturnType> addMeta(String k, Object v);
        // transition=lateral
        AddMeta_Sha1_Key<_ReturnType> overwrite(boolean option);
        // transition=lateral
        AddMeta_Overwrite_Key<_ReturnType> sha1(String hash);
        // transition=lateral
        AddMeta_Overwrite_Sha1<_ReturnType> key(String key);
        // transition=terminal
        TreeFsFile create();;
    }

    public interface AddMeta_Overwrite_Size_Key<_ReturnType> {
        // transition=recursive
        AddMeta_Overwrite_Size_Key<_ReturnType> addMeta(String k, Object v);
        // transition=lateral
        AddMeta_Size_Key<_ReturnType> overwrite(boolean option);
        // transition=lateral
        AddMeta_Overwrite_Key<_ReturnType> size(long n);
        // transition=lateral
        AddMeta_Overwrite_Size<_ReturnType> key(String key);
        // transition=terminal
        TreeFsFile create();
    }

    public interface AddMeta_Overwrite_Size_Sha1<_ReturnType> {
        // transition=recursive
        AddMeta_Overwrite_Size_Sha1<_ReturnType> addMeta(String k, Object v);
        // transition=lateral
        AddMeta_Size_Sha1<_ReturnType> overwrite(boolean option);
        // transition=lateral
        AddMeta_Overwrite_Sha1<_ReturnType> size(long n);
        // transition=lateral
        AddMeta_Overwrite_Size<_ReturnType> sha1(String hash);
        // transition=terminal
        TreeFsFile create();
    }

    public interface AddMeta_Overwrite_SaveAs_Key<_ReturnType> {
        // transition=recursive
        AddMeta_Overwrite_SaveAs_Key<_ReturnType> addMeta(String k, Object v);
        // transition=lateral
        AddMeta_SaveAs_Key<_ReturnType> overwrite(boolean option);
        // transition=lateral
        AddMeta_Overwrite_Key<_ReturnType> saveAs(String name);
        // transition=lateral
        AddMeta_Overwrite_SaveAs<_ReturnType> key(String key);
        // transition=terminal
        TreeFsFile create();
    }

    public interface AddMeta_Overwrite_SaveAs_Sha1<_ReturnType> {
        // transition=recursive
        AddMeta_Overwrite_SaveAs_Sha1<_ReturnType> addMeta(String k, Object v);
        // transition=lateral
        AddMeta_SaveAs_Sha1<_ReturnType> overwrite(boolean option);
        // transition=lateral
        AddMeta_Overwrite_Sha1<_ReturnType> saveAs(String name);
        // transition=lateral
        AddMeta_Overwrite_SaveAs<_ReturnType> sha1(String hash);
        // transition=terminal
        TreeFsFile create();
    }

    public interface AddMeta_Overwrite_SaveAs_Size<_ReturnType> {
        // transition=recursive
        AddMeta_Overwrite_SaveAs_Size<_ReturnType> addMeta(String k, Object v);
        // transition=lateral
        AddMeta_SaveAs_Size<_ReturnType> overwrite(boolean option);
        // transition=lateral
        AddMeta_Overwrite_Size<_ReturnType> saveAs(String name);
        // transition=lateral
        AddMeta_Overwrite_SaveAs<_ReturnType> size(long n);
        // transition=terminal
        TreeFsFile create();
    }

    public interface AddMeta_Sha1_Key<_ReturnType> {
        // transition=recursive
        AddMeta_Sha1_Key<_ReturnType> addMeta(String k, Object v);
        // transition=lateral
        AddMeta_Key<_ReturnType> sha1(String hash);
        // transition=lateral
        AddMeta_Sha1<_ReturnType> key(String key);
        // transition=terminal
        TreeFsFile create();
    }

    public interface AddMeta_Size_Key<_ReturnType> {
        // transition=recursive
        AddMeta_Size_Key<_ReturnType> addMeta(String k, Object v);
        // transition=lateral
        AddMeta_Key<_ReturnType> size(long n);
        // transition=lateral
        AddMeta_Size<_ReturnType> key(String key);
        // transition=terminal
        TreeFsFile create();
    }

    public interface AddMeta_Size_Sha1<_ReturnType> {
        // transition=recursive
        AddMeta_Size_Sha1<_ReturnType> addMeta(String k, Object v);
        // transition=lateral
        AddMeta_Sha1<_ReturnType> size(long n);
        // transition=lateral
        AddMeta_Size<_ReturnType> sha1(String hash);
        // transition=terminal
        TreeFsFile create();
    }

    public interface AddMeta_SaveAs_Key<_ReturnType> {
        // transition=recursive
        AddMeta_SaveAs_Key<_ReturnType> addMeta(String k, Object v);
        // transition=lateral
        AddMeta_Key<_ReturnType> saveAs(String name);
        // transition=lateral
        AddMeta_SaveAs<_ReturnType> key(String key);
        // transition=terminal
        TreeFsFile create();
    }

    public interface AddMeta_SaveAs_Sha1<_ReturnType> {
        // transition=recursive
        AddMeta_SaveAs_Sha1<_ReturnType> addMeta(String k, Object v);
        // transition=lateral
        AddMeta_Sha1<_ReturnType> saveAs(String name);
        // transition=lateral
        AddMeta_SaveAs<_ReturnType> sha1(String hash);
        // transition=terminal
        TreeFsFile create();
    }

    public interface AddMeta_SaveAs_Size<_ReturnType> {
        // transition=recursive
        AddMeta_SaveAs_Size<_ReturnType> addMeta(String k, Object v);
        // transition=lateral
        AddMeta_Size<_ReturnType> saveAs(String name);
        // transition=lateral
        AddMeta_SaveAs<_ReturnType> size(long n);
        // transition=terminal
        TreeFsFile create();
    }

    public interface AddMeta_Overwrite_Key<_ReturnType> {
        // transition=recursive
        AddMeta_Overwrite_Key<_ReturnType> addMeta(String k, Object v);
        // transition=lateral
        AddMeta_Key<_ReturnType> overwrite(boolean option);
        // transition=lateral
        AddMeta_Overwrite<_ReturnType> key(String key);
        // transition=terminal
        TreeFsFile create();
    }

    public interface AddMeta_Overwrite_Sha1<_ReturnType> {
        // transition=recursive
        AddMeta_Overwrite_Sha1<_ReturnType> addMeta(String k, Object v);
        // transition=lateral
        AddMeta_Sha1<_ReturnType> overwrite(boolean option);
        // transition=lateral
        AddMeta_Overwrite<_ReturnType> sha1(String hash);
        // transition=terminal
        TreeFsFile create();
    }

    public interface AddMeta_Overwrite_Size<_ReturnType> {
        // transition=recursive
        AddMeta_Overwrite_Size<_ReturnType> addMeta(String k, Object v);
        // transition=lateral
        AddMeta_Size<_ReturnType> overwrite(boolean option);
        // transition=lateral
        AddMeta_Overwrite<_ReturnType> size(long n);
        // transition=terminal
        TreeFsFile create();
    }

    public interface AddMeta_Overwrite_SaveAs<_ReturnType> {
        // transition=recursive
        AddMeta_Overwrite_SaveAs<_ReturnType> addMeta(String k, Object v);
        // transition=lateral
        AddMeta_SaveAs<_ReturnType> overwrite(boolean option);
        // transition=lateral
        AddMeta_Overwrite<_ReturnType> saveAs(String name);
        // transition=terminal
        TreeFsFile create();
    }

    public interface AddMeta_Key<_ReturnType> {
        // transition=recursive
        AddMeta_Key<_ReturnType> addMeta(String k, Object v);
        // transition=lateral
        AddMeta<_ReturnType> key(String key);
        // transition=terminal
        TreeFsFile create();
    }

    public interface AddMeta_Sha1<_ReturnType> {
        // transition=recursive
        AddMeta_Sha1<_ReturnType> addMeta(String k, Object v);
        // transition=lateral
        AddMeta<_ReturnType> sha1(String hash);
        // transition=terminal
        TreeFsFile create();
    }

    public interface AddMeta_Size<_ReturnType> {
        // transition=recursive
        AddMeta_Size<_ReturnType> addMeta(String k, Object v);
        // transition=lateral
        AddMeta<_ReturnType> size(long n);
        // transition=terminal
        TreeFsFile create();
    }

    public interface AddMeta_SaveAs<_ReturnType> {
        // transition=recursive
        AddMeta_SaveAs<_ReturnType> addMeta(String k, Object v);
        // transition=lateral
        AddMeta<_ReturnType> saveAs(String name);
        // transition=terminal
        TreeFsFile create();
    }

    public interface AddMeta_Overwrite<_ReturnType> {
        // transition=recursive
        AddMeta_Overwrite<_ReturnType> addMeta(String k, Object v);
        // transition=lateral
        AddMeta<_ReturnType> overwrite(boolean option);
        // transition=terminal
        TreeFsFile create();
    }

    public interface AddMeta<_ReturnType> {
        // transition=recursive
        AddMeta<_ReturnType> addMeta(String k, Object v);
        // transition=terminal
        TreeFsFile create();
    }


    public static Start newFile(File file) {
        if (file == null) {
            throw new IllegalArgumentException("file cannot be null.");
        }

        return new Start() {

            @Override
            public AddMeta_Overwrite_SaveAs_Size_Sha1_Key in(String path) {
                return null;
            }
        };
    }
}
