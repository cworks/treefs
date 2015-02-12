package cworks.treefs.common;

import static cworks.treefs.common.ObjectUtils.isEmpty;
import static cworks.treefs.common.ObjectUtils.isNull;
import static cworks.treefs.common.ObjectUtils.isNullOrEmpty;

public final class StringUtils {

    /**
     * Remove a substring if and only if its at the end of source
     * @param source
     * @param remove
     * @return
     */
    public static String removeEnd(String source, String remove) {
        if (isEmpty(source) || isEmpty(remove)) {
            return source;
        }
        if (source.endsWith(remove)) {
            return source.substring(0, source.length() - remove.length());
        }
        return source;
    }

    public static String diff(final String a, final String b) {
        if(isNullOrEmpty(a)) {
            return b;
        }
        if(isNullOrEmpty(b)) {
            return a;
        }
        final int at = diffIndex(a, b);
        if(at == -1) {
            return "";
        }

        return b.substring(at);
    }

    public static int diffIndex(final String a, final String b) {
        if(a == b) {
            return -1;
        }
        if(isNullOrEmpty(a) || isNullOrEmpty(b)) {
            return 0;
        }
        int i;
        for(i = 0; i < a.length() && i < b.length(); ++i) {
            if(a.charAt(i) != b.charAt(i)) {
                break;
            }
        }
        if(i < b.length() || i < a.length()) {
            return i;
        }

        return -1;
    }

    /**
     * Convert the input path to a path of the unix variety
     * @param path
     * @return
     */
    public static String unixPath(String path) {
        String convert = path.replace("\\", "/");
        return convert;
    }

    /**
     * Join the stringified version of each Object with the given separator
     * @param array
     * @param separator
     * @return
     */
    public static String join(Object[] array, String separator) {

        if(isNull(array)) {
            return null;
        }
        if(isNullOrEmpty(separator)) {
            separator = "";
        }

        StringBuilder buffer = new StringBuilder(64);

        for (int i = 0; i < array.length; i++) {
            if(i > 0) {
                buffer.append(separator);
            }
            if(!isNull(array[i])) {
                buffer.append(array[i]);
            }
        }
        return buffer.toString();
    }

}
