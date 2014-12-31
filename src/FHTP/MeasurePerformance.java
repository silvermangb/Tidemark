/*
 * Measure runtime of FastHashtable versus java.util.Hashtable.
 */
package FHTP;

import java.util.Date;
import java.util.Hashtable;

import fastHashtable.FastHashtable;

public class MeasurePerformance {

	/*
	 * for a FastHashtable and a Hashtable, repeatedly create a large table.
	 * insertion speed is the only metric reported.
	 */
	public static void main(String[] args) {

		/*
		 * for powers of 2 in an interesting range, populate the tables win 2^p
		 * entries.
		 * 
		 * do it K times for each exponent and average the elapsed times to
		 * smooth out time measurements.
		 */
		double ft_total_time = 0;
		double ht_total_time = 0;
		for (int p = 10; p < 23; p++) {

			final int N = (1 << p);

			for (int K = 0; K < 3; K++) {
				final int INITIAL_CAPACITY = N / 32;
				Date start_time = null;
				Date stop_time = null;
				// ...
				// ...
				FastHashtable ft = new FastHashtable(N);
				/*
				 * for each type of hash table, put N times. Use wall clock time
				 * to measure performance.
				 */
				/*
				 * first run the FastHashtable.
				 */
				start_time = new Date();
				for (int i = 0; i < N; ++i) {
					ft.put(i, i);
				}
				stop_time = new Date();
				ft_total_time += (stop_time.getTime() - start_time.getTime());
				ft = null;
				/*
				 * now run the java.util.Hashtable.
				 */
				Hashtable<Integer, Long> ht = new Hashtable<Integer, Long>(
						INITIAL_CAPACITY, 0.75f);
				start_time = new Date();
				for (int i = 0; i < N; ++i) {
					Long v = new Long(i);
					ht.put(i, v);
				}
				stop_time = new Date();
				ht_total_time += (stop_time.getTime() - start_time.getTime());
			}
			/*
			 * compare the elapsed times.
			 */
			double ratio = ft_total_time / ht_total_time;
			boolean high = ratio > 1;
			System.out.println("runtime comparison: ft: " + "p=" + p + ", N="
					+ N + ", " + ft_total_time + "ms, ht: " + ht_total_time
					+ "ms, ft/ht: " + ratio + ", high=" + high);

		}

	}

}
