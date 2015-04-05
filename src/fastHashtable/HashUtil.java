package fastHashtable;



/**
 * Consolidate commonly used hash routines and constants
 *
 * @author pvancleave
 */
public class HashUtil {

    public final static int POS_BITS = 0x7fffffff;
    public final static int EMPTY = -1;
    public final static byte AVAILABLE = 0;
    public final static byte USED = 1;

    public static int nextPrime (int n) {
        if ((n & 1) == 0) {
            ++n;
        }
        while ( ! isPrime(n)) {
            n += 2;
        }
        return n;
    }

    public static boolean isPrime(int n) {
        if (n <= 1) {
            return false;
        }
        if (n == 2) {
            return true;
        }
        if ((n & 1) == 0 ) { // even #s aren't prime
            return false;
        }
        final int limit = 1 + (int)Math.sqrt(n);
        for (int i = 3; i <= limit; i += 2) {
            if (n % i == 0) {
                return false;
            }
        }
        return true;
    }
}

