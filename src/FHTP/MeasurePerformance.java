/*
 * Measure runtime of FastHashtable versus java.util.Hashtable.
 */
package FHTP;

import java.util.Date;
import java.util.Hashtable;

import fastHashtable.FastHashtable;

public class MeasurePerformance {

	/*
	 * for a FastHashtable and a Hashtable, repeatedly create a large object, access its
	 * entries, and, remove them.
	 */
	public static void main(String[] args) {


		long s=0;
		for (int p = 10; p < 21; p++) {
			final int N = 5 * (1 << p);
			final int INITIAL_CAPACITY = N / 32;
			final int K = 8;
			Date start_time = null;
			Date stop_time = null;
			double ft_total_time = 0;
			double ht_total_time = 0;
			//...
			//...initialize both FastHashtable & Hashtable
			//...
			FastHashtable ft = new FastHashtable(N);
			/*
			 * for each type of hash table, put, get, remove many pairs, N, K times. Use wall clock
			 * time to measure performance.
			 */
			/*
			 * first run the FastHashtable.
			 */
			start_time = new Date();
			for (int j = 0; j < K; ++j) {
				for (int i = 0; i < N; ++i) {
					ft.put(i, i);
				}
				long[] r = new long[2];
				for(int i=0;i<N;++i) {
					ft.get(i,r);
					s += r[1];
				}
			}
			stop_time = new Date();
			ft_total_time += (stop_time.getTime() - start_time.getTime());
			ft = null;
			/*
			 * now run the java.util.Hashtable.
			 */
			Hashtable<Integer, Long> ht = new Hashtable<Integer, Long>(INITIAL_CAPACITY, 0.75f);
			start_time = new Date();
			for (int j = 0; j < K; ++j) {
				for (int i = 0; i < N; ++i) {
					Long v = new Long(i);
					ht.put(i, v);
				}
				for(int i=0;i<N;++i) {
					s += ht.get(i);
				}
			}
			stop_time = new Date();
			/*
			 * compare the elapsed times.
			 */
			ht_total_time += (stop_time.getTime() - start_time.getTime());
			System.out.println("runtime comparison: ft: " + "p="+p+", N="+N+", "+ft_total_time
					+ "ms, ht: " + ht_total_time + "ms, ft/ht: "
					+ (ft_total_time / ht_total_time));
		}
		System.out.println(s);


	}

}
