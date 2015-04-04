package fastHashtable;

import java.lang.Math;
import java.util.Arrays;

/**
 * Objects of this class hold a fixed set of values and are optimized to provide one set operation,
 * "is a member". Values can be added iteratively to the set. 
 * After all data has been add the object is "finalized", at which point, the internal hash table is rehashed and
 * optimized for fast lookup under some memory constraint. After the object is finalized, attempting to add
 * more data causes an exception.
 * 
 */
public class ImmutableSetOfLong {
    
	public final static int POS_BITS = 0x7fffffff;

	/*
	 * The size of the bucket array. Each int in the array is a bit array. The
	 * bucket index and the bit number maps to a value in _values. If the bit is
	 * 1 there is an entry in _values, otherwise, there is none.
	 */
	private final int _DEFAULT_BUCKET_COUNT = 2048+1;
	private int _MAX_BUCKET_LENGTH = 6;
	private int _finalizedBucketCount;
	
	private boolean isFinalized = false;
	
	/*
	 * The number of key/value pairs in the hash table.
	 */
	private int _size;

	private int[]    _bucketSize;
	private long[][] _table;

	private void _init(int bucketSize) {
		this._bucketSize = new int[bucketSize];
		this._table = new long[this._bucketSize.length][];
	}

	private void _rehash() {


		ImmutableSetOfLong isol = new ImmutableSetOfLong(false);
		isol._MAX_BUCKET_LENGTH = 2*this._MAX_BUCKET_LENGTH;
		final int bucketCount = 2*this._table.length+1;
		isol._table = new long[bucketCount][];
		for(int i=0;i<bucketCount;++i) {
			isol._table[i] = new long[isol._MAX_BUCKET_LENGTH];
		}
		isol._bucketSize = new int[bucketCount];
		
		for(int i=0;i<this._table.length;++i) {
			if(this._bucketSize[i]>0) {
				isol.add(this._table[i],this._bucketSize[i]);
			}
		}
	

		this._MAX_BUCKET_LENGTH = isol._MAX_BUCKET_LENGTH;
		this._table 		= isol._table;
		this._bucketSize    = isol._bucketSize;
		this._size			= isol._size;

		
	}
	
	/*
	 * default constructor.
	 */
	public ImmutableSetOfLong() {
		this._init(_DEFAULT_BUCKET_COUNT);
	}

	/**
	 * 
	 * @param numberOfItems
	 */
	public ImmutableSetOfLong(int numberOfItems) {
		int bucketCount = 
			(int) java.lang.Math.floor((double)numberOfItems / _MAX_BUCKET_LENGTH) + 1;
		if(bucketCount*_MAX_BUCKET_LENGTH<numberOfItems) {
			++bucketCount;
		}
		this._init(bucketCount);
	}
	
	/**
	 * Construct a nil object which will have its parameters set optimally for
	 * the number of values it will contain before the data is added.
	 * 
	 * @param deffer
	 */
	private ImmutableSetOfLong(boolean deffer) {}

	/*
	 * The number of entries pairs in the hash table.
	 */
	public int size() {
		return this._size;
	}

	
	public long getMemoryUsage() {
		
		int n = this._bucketSize.length;
		int total = Integer.SIZE*n;
		for(int i=0;i<this._bucketSize.length;++i) {
			n = this._table[i].length;
			total += Long.SIZE*n;
		}
		return total;
		
	}

	public void add(long[] larray) {
		for(long l : larray) {
			add(l);
		}
	}
	
	public void add(long[] larray,final int size) {
		for(int i=0;i<size;++i) {
			add(larray[i]);
		}
	}
	
	/*
	 * entries are added, but, *never* removed.
	 * 
	 * negative values are not allowed.
	 * 
	 */
	public void add(long l) {
		
		if(isFinalized) {
			throw new IllegalStateException("no data can be added after the object is finalized");
		}
		
		if(l<0) {
			return;
		}

		int hash = hashFunction(l);
		
		long[] bucket = this._table[hash];
		if(bucket==null) {
			bucket = new long[_MAX_BUCKET_LENGTH];
			bucket[0] = l;
			this._bucketSize[hash] = 1;
			this._table[hash] = bucket;
			++this._size;
		} else {
			if (this._bucketSize[hash] < _MAX_BUCKET_LENGTH) {
				if (this._table[hash] == null) {
					this._table[hash] = new long[_MAX_BUCKET_LENGTH];
				}
				bucket = _table[hash];
				bucket[this._bucketSize[hash]] = l;
				++this._bucketSize[hash];
				++this._size;
				return;
			}
			this._rehash();
			this.add(l);
		}

	}


	
	/*
	 */
	public boolean contains(long l) {

		if(!isFinalized) {
			throw new IllegalStateException("the object has not been finalized");
		}
		
		++lookups;

		int hash = hashFunction(l);

		if (hash >= _finalizedBucketCount) {
			return false;
		}

		long[] bucket = this._table[hash];

		if (bucket == null) {
			return false;
		}

		for (int i = 0; i < this._bucketSize[hash]; ++i) {
			if (bucket[i] == l) {
				return true;
			}
			++lookupCollisions;
		}

		return false;

	}
	
	public void dump(String banner) {
		System.out.println("dump: "+banner);
		if (false) {
			if (!isFinalized) {
				throw new IllegalStateException("not finalized");
			}
		}
		assert this._table.length==this._bucketSize.length : "table and buckeSize not of same length" ;
		for (int i = 0; i < this._table.length; ++i) {
			if (this._table[i] != null) {
				System.out.print("row " + i + ": ");
				for (int j = 0; j < this._table[i].length; ++j) {
					System.out.print(this._table[i][j] + ", ");
				}
				System.out.println();
			}
		}
		System.out.flush();
	}
	
	private void getValues(ImmutableSetOfLong s,long[] a) {
		int index = 0;
		for(int i=0;i<s._table.length;++i) {
			if(s._table[i]!=null) {
				for(int j=0;j<s._bucketSize[i];++j) {
					a[index++] = s._table[i][j];
				}
			}
		}
		Arrays.sort(a);
	}
	
	public void finalize() {
		
		this.isFinalized = true;
		
		ImmutableSetOfLong optimized = new ImmutableSetOfLong(true);
		
		/**
		 * Binary search on a sorted array of the values in this set
		 * would provide optimal memory usage and provide a worst case
		 * lookup on the order of log2(this._size). To improve lookup 
		 */
		double dlog2 = (int)( Math.log(this._size)/Math.log(2));
		int    ilog2 = (int)dlog2;
		if(dlog2>ilog2) {
			++ilog2;
		}
		optimized._MAX_BUCKET_LENGTH = Math.max(ilog2, 10);
		_finalizedBucketCount = Math.max((this._size+1)/optimized._MAX_BUCKET_LENGTH,optimized._MAX_BUCKET_LENGTH);
		optimized._table = new long[_finalizedBucketCount][];
		for(int i=0;i<_finalizedBucketCount;++i) {
			optimized._table[i] = new long[optimized._MAX_BUCKET_LENGTH];
		}
		optimized._bucketSize = new int[_finalizedBucketCount];
		for(int i=0;i<this._table.length;++i) {
			if(this._table[i]!=null) {
					optimized.add(this._table[i],this._bucketSize[i]);
			}
		}
		
		for(int i=0;i<optimized._table.length;++i) {
			if (optimized._table[i]!=null) {
				long[] trimmedBucket = Arrays.copyOf(optimized._table[i],
						optimized._bucketSize[i]);
				optimized._table[i] = trimmedBucket;
			}
		}
		
		this._bucketSize = optimized._bucketSize;
		this._table = optimized._table;
		this._finalizedBucketCount = this._table.length;
	}


	private int hashFunction(long l) {
		
        int c = this._table.length;
        int e = this._bucketSize.length;
        int a = 3 * (((int) l) ^ (int) (l >>> 32));
        int b = a & POS_BITS;
        int d = b % c;
        
        return d;
		
	}
	
	private long lookups = 0;
	private long lookupCollisions = 0;
	
	public double getLookupStatistics() {
		return (double)lookupCollisions/lookups;
	}
	
	public long getLookupCollisonCount() {
		return this.lookupCollisions;
	}
}
