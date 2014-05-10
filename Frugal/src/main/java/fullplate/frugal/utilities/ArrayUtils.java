package fullplate.frugal.utilities;

import java.util.HashSet;
import java.util.Set;

public class ArrayUtils {
    public static String[] concatStringArrays(String[] A, String[] B) {
        int aLen = A.length;
        int bLen = B.length;
        String[] C = new String[aLen+bLen];
        System.arraycopy(A, 0, C, 0, aLen);
        System.arraycopy(B, 0, C, aLen, bLen);
        return C;
    }

    public static String[] filterUniqueStrings(String[] A) {
        Set<String> unique = new HashSet<>();

        for (int i = 0; i < A.length; i++) {
            unique.add(A[i]);
        }

        return unique.toArray(new String[unique.size()]);
    }

}
