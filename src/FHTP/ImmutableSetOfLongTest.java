/*
 * This mini test harness has a framework for adding new unit tests. The main function automatically
 * runs each test that is loaded into the tests list.
 */

package FHTP;


import fastHashtable.ImmutableSetOfLong;


import java.util.ArrayList;

public class ImmutableSetOfLongTest {

	/*
	 * Base class for unit tests.
	 */
	private abstract class TestAbstract {
		public abstract boolean run();
	}

	/*
	 * Test add & contains
	 */
	public class TestPut extends TestAbstract {
		public boolean run() {
			ImmutableSetOfLong isol = new ImmutableSetOfLong();
			long l = (long)Integer.MAX_VALUE + 1;
			isol.add(l);
			isol.finalize();
			return isol.contains(l);
		}
	}

	/*
	 * Test growing the table.
	 */
	public class TestGrow extends TestAbstract {
		public boolean run() {
			ImmutableSetOfLong isol = new ImmutableSetOfLong();
			
			long k = (long)Integer.MAX_VALUE+1;
			isol.add(k);
			isol.add(k+1);
			
			isol.finalize();
			
			assert isol.contains(k);
			assert isol.contains(k+1);


			return true;
		}
	}

	/*
	 * Test add & contains
	 */
	public class TestInvalidKey extends TestAbstract {
		public boolean run() {
			ImmutableSetOfLong isol = new ImmutableSetOfLong();

			isol.add(-1);
			isol.finalize();
			assert !isol.contains(-1);

			return true;
		}
	}


	/*
	 * Test for collision. Collisions occur given the initial capacity and the
	 * number of key/value pairs.
	 */
	public class TestCollision extends TestAbstract {
		public boolean run() {

			double delta;
			long now;
			long then;
			final int N = (1<<22)+(int)((System.currentTimeMillis()%1024)-512);
			
			ImmutableSetOfLong isol = new ImmutableSetOfLong();
			
			java.util.Random rand = new java.util.Random(System.currentTimeMillis());

			long[] includedValues = new long[N];
			long l;
			for(int i=1;i<N;++i) {
				l = rand.nextLong();
				l &= Long.MAX_VALUE;
				assert l>=0;
				includedValues[i] = l;				
			}
			
			then = System.currentTimeMillis();
			isol.add(includedValues);
			now = System.currentTimeMillis();
			delta = (now-then)/10000.0;
			System.out.println(delta);
			

			isol.finalize();
			long n = isol.getMemoryUsage();
			long d = Long.SIZE*N;
			System.out.println(this.getClass().getName()+": "+n+" "+d+" "+((double)n/d));
			then = System.currentTimeMillis();
			for (int i = 1; i < N; ++i) {

				
				assert isol.contains(includedValues[i]);
				assert !isol.contains(rand.nextLong()&Long.MAX_VALUE);
			}
			now = System.currentTimeMillis();
			delta = (now-then)/1000.0;
			System.out.println(delta);

			System.out.println("lookupStatistics: "+isol.getLookupStatistics());

			return true;
		}
	}

	/*
	 * Test where the key does not exist in the container.
	 */
	public class TestForMissingKey extends TestAbstract {
		public boolean run() {
			
			ImmutableSetOfLong isol = new ImmutableSetOfLong();

			isol = new ImmutableSetOfLong(1 << 16);
			for (long i = 0; i < (1 << 16); i += 2) {
				isol.add(i);
			}
			isol.finalize();
			for (long i = 1; i < (1 << 16); i += 2) {
				assert !isol.contains(i);
			}
			for (long i = 0; i < (1 << 16); i += 2) {
				assert isol.contains(i);
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
