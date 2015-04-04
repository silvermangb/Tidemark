/*
 * This mini test harness has a framework for adding new unit tests. The main function automatically
 * runs each test that is loaded into the tests list.
 */

package FHTP;


import fastHashtable.Hashtable;


import java.util.ArrayList;

public class ImmutableSetOfLongTest {

	/*
	 * Base class for unit tests.
	 */
	private abstract class TestAbstract {
		public abstract boolean run();
	}

	/*
	 * Test put & containsKey
	 */
	public class TestPut extends TestAbstract {
		public boolean run() {
			Hashtable ht = new Hashtable();
			long l = (long)Integer.MAX_VALUE + 1;
			ht.put(l);
			return ht.containsKey(l);
		}
	}

	/*
	 * Test growing the table.
	 */
	public class TestGrow extends TestAbstract {
		public boolean run() {
			Hashtable ht = new Hashtable();
			
			long k = ht.maxSize();
			ht.put(k);
			ht.put(k+1);
			assert ht.containsKey(k);
			assert ht.containsKey(k+1);


			return true;
		}
	}

	/*
	 * Test put & containsKey
	 */
	public class TestInvalidKey extends TestAbstract {
		public boolean run() {
			Hashtable ht = new Hashtable();

			ht.put(-1);
			assert !ht.containsKey(-1);

			return true;
		}
	}


	/*
	 * Test for collision. Collisions occur given the initial capacity and the
	 * number of key/value pairs.
	 */
	public class TestCollision extends TestAbstract {
		public boolean run() {

			final int N = 1<<20;
			
			Hashtable ht = new Hashtable(4*N);
			
			java.util.Random rand = new java.util.Random();

			long[] includedValues = new long[N];
			long l;
			for(int i=0;i<N;++i) {
				l = rand.nextLong();
				l &= Long.MAX_VALUE;
				assert l>=0;
				includedValues[i] = l;
				ht.put(l);
				
			}

			long n = ht.getMemoryUsage();
			long d = Long.SIZE*N;
			System.out.println(this.getClass().getName()+": "+n+" "+d+" "+((double)n/d));
			for (int i = 0; i < N; ++i) {

				
				assert ht.containsKey(includedValues[i]);
				assert !ht.containsKey(rand.nextLong()&Long.MAX_VALUE);
			}
			
			System.out.println("lookupStatistics: "+ht.getLookupStatistics());

			return true;
		}
	}

	/*
	 * Test where the key does not exist in the container.
	 */
	public class TestForMissingKey extends TestAbstract {
		public boolean run() {
			Hashtable ht = new Hashtable();
			boolean r;
			/*
			 * the container is empty,
			 */
			r = !ht.containsKey(0);
			assert r;
			/*
			 * the container has entries but for key equals 1.
			 */
			ht.put(0);
			r = !ht.containsKey(1);
			assert r;



			ht = new Hashtable(1 << 16);
			for (long i = 0; i < (1 << 16); i += 2) {
				ht.put(i);
			}
			for (long i = 1; i < (1 << 16); i += 2) {
				assert !ht.containsKey(i);
			}
			for (long i = 0; i < (1 << 16); i += 2) {
				assert ht.containsKey(i);
			}

			return true;
		}
	}

	/*
	 * Returns the list of unit tests to run. Add new tests here.
	 */
	public ArrayList<TestAbstract> getTestsList() {

		ArrayList<TestAbstract> tests = new ArrayList<TestAbstract>();

		tests.add(new TestGrow());
		tests.add(new TestPut());
		tests.add(new TestCollision());
		tests.add(new TestForMissingKey());
		tests.add(new TestInvalidKey());
//		tests.add(new TestToString());

		return tests;

	}

	public static void main(String[] args) {

		/*
		 * iterate over all of the unit tests and count the number of failures,
		 * then, report the result.
		 */
		int failure_count = 0;
		ImmutableSetOfLongTest test = new ImmutableSetOfLongTest();
		ArrayList<TestAbstract> tests = test.getTestsList();

		TestAbstract ti = null;

		for (int i = 0; i < tests.size(); ++i) {
			try {
				ti = tests.get(i);
				boolean r = ti.run();
				assert r;
			} catch (AssertionError e) {
				++failure_count;
				System.out.println("failure: " + ti.getClass());

			}
		}
		System.out.println("failure count: " + failure_count + " of "
				+ tests.size() + " unit tests");

	}

}
