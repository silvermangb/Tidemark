package fastHashtable;

/*
 * A hashtable for int keys and long values. This class has enough of the methods of Java's Hashtable
 * to allow a performance comparison.
 * 
 * Requirements:
 * 
 * 1. Negative ints are not supported.
 * 2. The maximum key value is _bit_vector_size*_BUCKET_COUNT-1.
 * 
 * Some methods of this class throw a runtime exception if those requirements are not met.
 * 
 * These limitation support fast hashtable methods for where these requirements are acceptable.
 * 
 * Note: if long was used for the array each bucket would be a bit array of size 64.
 */
public class FastHashtable {

	/*
	 * The size of the bucket array. Each int in the array is a bit array. The
	 * bucket index and the bit number maps to a value in _values. If the bit is
	 * 1 there is an entry in _values, otherwise, there is none.
	 */
	private int _BUCKET_COUNT = 2048;
	
	/*
	 * The number of key/value pairs in the hash table.
	 */
	private int _size;

	private int[] _buckets;
	private long[][] _values;

	private final int _bit_vector_size = Integer.SIZE;

	private void _init() {
		this._buckets = new int[this._BUCKET_COUNT];
		this._values = new long[this._BUCKET_COUNT][_bit_vector_size];
	}

	private void _rehash() {
		FastHashtable ft = new FastHashtable(2*(this.maxSize()));
		int[] bitmasks = new int[_bit_vector_size];
		for (int i = 0; i < 32; ++i) {
			bitmasks[i] = 1 << i;
		}
		long[] r = new long[2];
		for (int i = 0; i < this._bit_vector_size; ++i) {
			for (int j = 0; j < this._BUCKET_COUNT; ++j) {
				if ((this._buckets[j] & (1 << i)) != 0) {
					int key = i * this._buckets.length + j;
					this.get(key, r);
					ft.put(key, r[1]);
				}
			}
		}
		
		this._BUCKET_COUNT	= ft._BUCKET_COUNT;
		this._buckets		= ft._buckets;
		this._values		= ft._values;
		this._size			= ft._size;
		
		ft._buckets = null;
		ft._values  = null;
		
	}
	/*
	 * default constructor.
	 */
	public FastHashtable() {
		this._init();
	}

	/*
	 * specify the capacity of the object.
	 * 
	 * the capacity will always be a multiple of _bit_vector_size. _BUCKET_COUNT
	 * will always round up to a multiple of _bit_vector_size.
	 */
	public FastHashtable(int p_max_entries) {
		this._BUCKET_COUNT = 
			(int) java.lang.Math.floor(p_max_entries / (float) _bit_vector_size);
		if((this._BUCKET_COUNT*this._bit_vector_size)<p_max_entries) {
			this._BUCKET_COUNT+=1;
		}
		this._init();
	}

	/*
	 * The number of key/value pairs in the hash table.
	 */
	public int size() {
		return this._size;
	}

	/*
	 * key k must satisfy 0<=k<=maxKey() to be a valid key.
	 */
	public int maxKey() {
		return this._BUCKET_COUNT * _bit_vector_size - 1;
	}

	public int maxSize() {
		return this._BUCKET_COUNT * _bit_vector_size;
	}
	
	/*
	 * this method will throw for p_key<0 or p_key>this.maxKey()
	 */
	public void put(int p_key, long p_value) {
		if(p_key>this.maxKey()) {
			this._rehash();
		}
		int i = p_key % this._BUCKET_COUNT;
		int k = (p_key - i) / this._BUCKET_COUNT;

		if ((this._buckets[i] & (1 << k)) == 0) {
			this._size++;
		}
		this._buckets[i] |= (1 << k);
		this._values[i][k] = p_value;
	}

	/*
	 * this method will not throw with an invalid key. it's always ok to ask.
	 */
	public boolean containsKey(int p_key) {
		if (p_key < 0) {
			return false;
		}
		int i = p_key % this._BUCKET_COUNT;
		int k = (p_key - i) / this._BUCKET_COUNT;
		if ((this._buckets[i] & ((1 << k))) != 0) {
			return true;
		} else {
			return false;
		}
	}

	/*
	 * this method will not throw with an invalid key. it's always ok to ask.
	 */
	public boolean get(int p_key, long[] p_result) {
		if (p_key < 0) {
			return false;
		}
		int i = p_key % this._BUCKET_COUNT;
		int k = (p_key - i) / this._BUCKET_COUNT;
		if ((this._buckets[i] & ((1 << k))) != 0) {
			p_result[0] = 1;
			p_result[1] = this._values[i][k];
			return true;
		} else {
			return false;
		}
	}

	/*
	 * this method could be faster if inline code were used instead of the
	 * FastHashtable.get method.
	 */
	public String toString() {
		int[] bitmasks = new int[_bit_vector_size];
		for (int i = 0; i < 32; ++i) {
			bitmasks[i] = 1 << i;
		}
		long[] r = new long[2];
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		for (int i = 0; i < this._bit_vector_size; ++i) {
			for (int j = 0; j < this._BUCKET_COUNT; ++j) {
				if ((this._buckets[j] & (1 << i)) != 0) {
					int key = i * this._buckets.length + j;
					this.get(key, r);
					sb.append('(');
					sb.append(key);
					sb.append(',');
					sb.append(r[1]);
					sb.append(')');
				}
			}
		}
		sb.append(')');
		return sb.toString();
	}

}
