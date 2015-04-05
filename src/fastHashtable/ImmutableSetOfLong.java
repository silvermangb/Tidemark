package fastHashtable;

import java.lang.Math;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

	private boolean isFinalized = false;
	

	private List<long[]> data = new ArrayList<>();
	
	/*
	 * The number of key/value pairs in the hash table.
	 */
	private int _size;
	
	private int      _bucketCount;
	private int      _bucketLength;
	private int[]    _buckets;
	private long[][] _table;
	private int       maxCollisions=0;

	/*
	 * The number of entries pairs in the hash table.
	 */
	public int size() {
		return this._size;
	}

	
	public long getMemoryUsage() {
		
		int n = this._bucketCount;
		int total = Integer.SIZE*n;
		for(int i=0;i<this._bucketCount;++i) {
			n = this._table[i].length;
			total += Long.SIZE*n;
		}
		return total;
		
	}

	public void add(long[] larray) {
		if(this.isFinalized) {
			throw new IllegalStateException("set is finalized");
		}
		this.data.add(larray);
	}
	

	
	/*
	 */
	public boolean contains(long l) {

		if(!isFinalized) {
			throw new IllegalStateException("the object has not been finalized");
		}
		
		++lookups;

		int hash = hashFunction(l,this._table.length);

		if (hash >= this._table.length) {
			return false;
		}

		long[] bucket = this._table[hash];

		if (bucket == null) {
			return false;
		}

		
		int collisions = 0;
		for (int i = 0; i < this._buckets[hash]; ++i) {
			if (bucket[i] == l) {
				lookupCollisions += collisions;
				maxCollisions = Math.max(maxCollisions, collisions);
				return true;
			};
			++collisions;

		}
        ;
		return false;

	}
	
	public void finalize() {
		
		this.isFinalized = true;
		
		for(long[] l : this.data) {
			this._size += l.length;
		}
		
		double dlog2 = Math.log(this._size)/Math.log(2);
		int ilog2 = (int)dlog2;
		// binary search worst case
		int binarySearchWC = ilog2;
		if(ilog2<dlog2) {
			++binarySearchWC;
		}
		int maxCollsionsGoal = binarySearchWC/4;
		int hashValue;
		int M=0;
		for(int i=0;i<3;++i) {
			maxCollisions = 0;
			M = (1<<(i+1))*this._size + 1;
			for(int j=0;j<this.data.size();++j) {
				long[] l = this.data.get(j);
				int[]  h = new int[M];
				for(int k=0;k<l.length;++k) {
					hashValue = this.hashFunction(l[k], M);
					++h[hashValue];
					maxCollisions = Math.max(maxCollisions, h[hashValue]);
				}
			}
			if(maxCollisions<=maxCollsionsGoal) {
				break;
			}
		}
		
		//...
		//...insert the data
		//...
		
		this._bucketCount = M;
		this._buckets = new int[this._bucketCount];
		this._bucketLength = maxCollisions;
		this._table = new long[M][];
		for(int i=0;i<M;++i) {
			this._table[i] = new long[maxCollisions];
		}
		for(int j=0;j<this.data.size();++j) {
			long[] larray = this.data.get(j);
			for(long l : larray) {
				hashValue = hashFunction(l,M);
				long[] bucket = this._table[hashValue];
				bucket[this._buckets[hashValue]++] = l;
			}
		}
		
		maxCollisions = 0;
		//...
		//...optimize the memory usage
		//...
		for(int j=0;j<this._bucketCount;++j) {
			this._table[j] = Arrays.copyOf(this._table[j], this._buckets[j]);
		}
		
	
	}


	private int hashFunction(long l, int N) {
		
        int a = 3 * (((int) l) ^ (int) (l >>> 32));
        int b = a & POS_BITS;
        int d = b % N;
        
        return d;
		
	}
	
	private long lookups = 0;
	private long lookupCollisions = 0;
	
	public double getLookupStatistics() {
		System.out.println("get lookup statistics: "+lookups+" "+lookupCollisions);
		return (double)lookupCollisions/lookups;
	}
	
	public long getMaxCollisions() {
		return maxCollisions;
	}
}
