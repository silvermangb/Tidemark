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
	public class TestInvalidKey extends TestAbstract {
		public boolean run() {
			ImmutableSetOfLong isol = new ImmutableSetOfLong();

			isol.finalizeSet();
			assert !isol.contains(-1);
			
			isol = new ImmutableSetOfLong();
			isol.add(new long[]{0});
			isol.finalizeSet();
			
			assert !isol.contains(1);
			return true;
		}
	}


	/*
	 * Test for collision. Collisions occur given the initial capacity and the
	 * number of key/value pairs.
	 * 
	 * Re-factored.
	 */
	public class TestCollision extends TestAbstract {
		public boolean _run(ImmutableSetOfLong isol,long[] includedValues,long[] excludedValues) {

			final int N = includedValues.length;
			
			double delta;
			long now;
			long then;			
		
			then = System.currentTimeMillis();
			isol.add(includedValues);
			now = System.currentTimeMillis();
			delta = (now-then)/10000.0;
			System.out.println(this.getClass().getName()+":time to add data to set:\t"+delta);System.out.flush();
			
			then = now;//System.currentTimeMillis();
			isol.finalizeSet();
			now = System.currentTimeMillis();
			delta = (now-then)/10000.0;
			System.out.println(this.getClass().getName()+":time to finalize:\t"+delta);System.out.flush();
			long n = isol.getMemoryUsage();
			long d = Long.SIZE*N;
			System.out.println(this.getClass().getName()+":memory usage:\t"+n+" "+d+" "+((double)n/d));System.out.flush();
			
			
			for (int j = 0; j < 8; j++) {
				then = now;//System.currentTimeMillis();
				for (int i = 0; i < N; ++i) {
					assert isol.contains(includedValues[i]) : "missing";
					assert !isol.contains(excludedValues[i]) : "invalid";
				}
				now = System.currentTimeMillis();
				delta = (now - then) / 1000.0;
				System.out.println(this.getClass().getName()
						+ ":time to test lookups:\t" + delta);
				System.out.flush();
			}
			System.out.println(this.getClass().getName()+":lookupStatistics:\t"+isol.getLookupStatistics());System.out.flush();
			System.out.println(this.getClass().getName()+":maxCollisions:\t"+isol.getMaxCollisions());System.out.flush();

			return true;
		}
		public boolean run() {

			long testStartTime = System.currentTimeMillis();
			double delta;
			long now;
			long then;
			final int N = (1<<22)+(int)((System.currentTimeMillis()%1024)-512);
			
			ImmutableSetOfLong isol = new ImmutableSetOfLong();
			
			java.util.Random rand = new java.util.Random(System.currentTimeMillis());
			then = System.currentTimeMillis();
			long[] includedValues = new long[N];
			long[] excludedValues = new long[N];
			long l;
			for(int i=0;i<N;++i) {
				l = rand.nextLong();
				l &= Long.MAX_VALUE;
				includedValues[i] = l;		
				l = rand.nextLong();
				l &= Long.MAX_VALUE;
				excludedValues[i] = l;
			}
			includedValues[0] = 0;
			includedValues[1] = 0;
//			excludedValues[0] = 0;
			now = System.currentTimeMillis();
			delta = (now-then)/10000.0;
			System.out.println(this.getClass().getName()+":time to generate data:\t"+delta);System.out.flush();
		
			this._run(isol, includedValues, excludedValues);
			
			long testStopTime = System.currentTimeMillis();
			System.out.println(this.getClass().getName()+":total test time: "+((testStopTime-testStartTime)/1000.0));System.out.flush();
			return true;
		}
	}

	/*
	 * Test where the key does not exist in the container.
	 */
	public class TestForMissingKey extends TestAbstract {
		public boolean run() {
			
			final int N = 16;
			ImmutableSetOfLong isol = new ImmutableSetOfLong();

			isol = new ImmutableSetOfLong();
			long[] l = new long[N];
			long   v = 0;
			for (int i = 0; i < N; ++i) {
				l[i] = v;
				v += 2;
			}
			isol.add(l);
			isol.finalizeSet();
			for (long i = 1; i < N; i += 2) {
				assert !isol.contains(i);
			}
			for (long i = 0; i < N; i += 2) {
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
