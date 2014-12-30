package fastHashtable;

/*
 * A hashtable for int keys and long values. This class has enough of the methods of Java's Hashtable
 * to allow a performance comparison.
 */
public class FastHashtable {
	

	/*
	 * The size of the bucket array. The size will grow
	 * if there are "too many" collisons.
	 */
	private int _BUCKET_COUNT=2048;
	/*
	 * The number of key/value pairs in the hash table.
	 */
	private int _size;
	
	private int[] _buckets;
	private long[][] _values;
	


	private void _init() {
		this._buckets = new int[this._BUCKET_COUNT];
		this._values = new long[this._BUCKET_COUNT][32];
	}


	public FastHashtable() {
		this._init();
	}

	public FastHashtable(int p_max_entries) {
		this._BUCKET_COUNT = (int)java.lang.Math.ceil(p_max_entries/(float)32);
		this._init();
	}


	public int size() {
		return this._size;
	}

	public void put(int p_key, long p_value) {
		int i = p_key % this._BUCKET_COUNT;
		int k = (p_key-i)/this._BUCKET_COUNT;

		if((this._buckets[i]&(1<<k))==0) {
			this._size++;
		}
		this._buckets[i] |= (1 << k);
		this._values[i][k] = p_value;
	}

	public boolean containsKey(int p_key) {
		int i = p_key % this._BUCKET_COUNT;
		int k = (p_key-i)/this._BUCKET_COUNT;
		if((this._buckets[i]&((1<<k)))!=0) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean get(int p_key,long[] p_result) {
		int i = p_key % this._BUCKET_COUNT;
		int k = (p_key-i)/this._BUCKET_COUNT;
		if((this._buckets[i]&((1<<k)))!=0) {
			p_result[0] = 1;
			p_result[1] = this._values[i][k];
			return true;
		} else {
			return false;
		}
	}
	
	
	public String toString() {
		int[] bitmasks = new int[32];
		for(int i=0;i<32;++i) {
			bitmasks[i] = 1<<i;
		}
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		sb.append(')');
		return sb.toString();
	}


}
