/*
 * This mini test harness has a framework for adding new unit tests. The main function automatically
 * runs each test that is loaded into the tests list.
 */

package FHTP;

import fastHashtable.FastHashtable;

import java.util.ArrayList;

public class FHTest {

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
			FastHashtable ft = new FastHashtable();
			ft.put(0, 1);
			return ft.containsKey(0);
		}
	}

	/*
	 * Test growing the table.
	 */
	public class TestGrow extends TestAbstract {
		public boolean run() {
			FastHashtable ft = new FastHashtable();
			//...
			//...ft.maxSize()>ft.maxKey(), so
			//...the table must grow.
			//...
			int k = ft.maxSize();
			ft.put(k, k);
			ft.put(k+1, k+1);
			assert ft.containsKey(k);
			assert ft.containsKey(k+1);
			long[] r = new long[2];
			ft.get(k, r);
			assert r[0]==1;
			assert r[1]==k;
			//...
			//...test that the table grew by a
			//...factor of 2.
			//...
			assert ft.maxSize()==2*k;
			//...
			//...the max key is one less
			//...then the max size.
			//..
			assert ft.maxKey()==2*k-1;
			return true;
		}
	}

	/*
	 * Test put & containsKey
	 */
	public class TestInvalidKey extends TestAbstract {
		public boolean run() {
			FastHashtable ft = new FastHashtable();
			try {
				ft.put(-1, -1);
				assert false;
			} catch (java.lang.ArrayIndexOutOfBoundsException e) {

			}
			boolean b = ft.containsKey(ft.maxKey() + 1);
			assert !b;
			b = ft.containsKey(-1);
			assert !b;
			ft.put(ft.maxKey(), 1);
			assert ft.containsKey(ft.maxKey());

			long[] r = new long[2];

			assert !ft.get(-1, r);
			assert r[0] == 0;

			assert !ft.get(ft.maxKey() + 1, r);
			assert r[0] == 0;

			return true;
		}
	}

	public class TestToString extends TestAbstract {
		/*
		 * construct a string representation in parallel with populating the
		 * hash table.
		 */
		public boolean run() {

			FastHashtable ft = new FastHashtable();

			StringBuilder sb = new StringBuilder();

			sb.append('(');
			for (int i = 0; i < 16; i += 2) {
				ft.put(i, i);
				sb.append('(');
				sb.append(i);
				sb.append(',');
				sb.append(i);
				sb.append(')');
			}
			sb.append(')');

			String a = sb.toString();
			String b = ft.toString();

			assert a.equals(b);
			return true;
		}
	}

	/*
	 * Test for collision. Collisions occur given the initial capacity and the
	 * number of key/value pairs.
	 */
	public class TestCollision extends TestAbstract {
		public boolean run() {
			// ...
			// ...the FastHashtable will always have a
			// ...minimum capacity of FastHashtable._bit_vector_size.
			// ...by passing 0 as the max size the FastHashtable
			// ...will have one bucket and each put after
			// ...the first will cause a collision
			// ...
			FastHashtable ft = new FastHashtable(0);
			final int N = ft.maxKey();
			for (int i = 0; i < N; ++i) {
				ft.put(i, i);
			}
			long[] r = new long[2];
			for (int i = 0; i < N; ++i) {
				boolean j = ft.get(i, r);
				assert j;
				assert r[0] == 1;
				assert (long) i == r[1];
			}

			return true;
		}
	}

	/*
	 * Test where the key does not exist in the container.
	 */
	public class TestForMissingKey extends TestAbstract {
		public boolean run() {
			FastHashtable ft = new FastHashtable();
			boolean r;
			/*
			 * the container is empty,
			 */
			r = !ft.containsKey(0);
			assert r;
			/*
			 * the container has entries but for key equals 1.
			 */
			ft.put(0, 1);
			r = !ft.containsKey(1);
			assert r;
			/*
			 * try to remove a key that does not exist.
			 */
			assert r;

			ft = new FastHashtable(1 << 16);
			for (int i = 0; i < (1 << 16); i += 2) {
				ft.put(i, i);
			}
			long[] res = new long[2];
			for (int i = 1; i < (1 << 16); i += 2) {
				boolean b = ft.get(i, res);
				assert !b;
				assert res[0] == 0;
				assert res[1] == 0;

			}
			for (int i = 0; i < (1 << 16); i += 2) {
				boolean b = ft.get(i, res);
				assert b;
				assert res[0] == 1;
				assert res[1] == i;

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
		tests.add(new TestToString());

		return tests;

	}

	public static void main(String[] args) {

		/*
		 * iterate over all of the unit tests and count the number of failures,
		 * then, report the result.
		 */
		int failure_count = 0;
		FHTest test = new FHTest();
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
