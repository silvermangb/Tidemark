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
	private int[]    _buckets;
	private long[][] _table;
	private int       maxCollisions=0;
	private int       growthFactor=3;
	private int       binarySearchTarget=4;

	/*
	 * The number of entries pairs in the hash table.
	 */
	public int size() {
		return this._size;
	}

	
	public long getMemoryUsage() {
		
		long total = (long)Integer.SIZE*this._bucketCount;
		for(int i=0;i<this._bucketCount;++i) {
			if (this._table[i]!=null) {
				total += (long)Long.SIZE * this._table[i].length;
			}
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
		
		int hash = hashFunction(l,this._bucketCount);

		long[] bucket = this._table[hash];

		if (bucket == null) {
			return false;
		}

		int collisions = 0;
		for (int i = 0; i < this._buckets[hash]; ++i) {
			
			if (bucket[i] == l) {
				maxCollisions = Math.max(maxCollisions, collisions);
				this.lookupCollisions+=collisions;
				return true;
			};
			++collisions;
		};
		return false;

	}
	
	public void finalizeSet() {
		
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
		int maxCollsionsGoal = binarySearchWC/this.binarySearchTarget;
		int hashValue;
		int goal = 4;
		maxCollisions = maxCollsionsGoal+1;
		int[] h = null;
		int M = HashUtil.nextPrime(2*this._size+1);
		while(maxCollisions>goal && maxCollisions>1 && this._size>0 && (M/this._size)<5) {
			int collisions = 0;
			h = new int[M];
			for(int j=0;j<this.data.size();++j) {
				long[] l = this.data.get(j);
				for(int k=0;k<l.length;++k) {
					hashValue = this.hashFunction(l[k], M);
					++h[hashValue];
					collisions = Math.max(collisions, h[hashValue]);
				}
			}
			M = HashUtil.nextPrime(3*M/2);
			maxCollisions = collisions;
			
		}
		M = Math.min(M,h==null?1:h.length);
		if(h==null) {
			h = new int[M];
			int collisions = 0;
			h = new int[M];
			for(int j=0;j<this.data.size();++j) {
				long[] l = this.data.get(j);
				for(int k=0;k<l.length;++k) {
					hashValue = this.hashFunction(l[k], M);
					++h[hashValue];
					collisions = Math.max(collisions, h[hashValue]);
				}
			}
			maxCollisions = collisions;
		}
		System.out.println("maxCollisions="+maxCollisions);
				
		//...
		//...insert the data
		//...
		
		this._bucketCount = M;
		this._buckets = new int[this._bucketCount];
		this._table = new long[this._bucketCount][];

		boolean present = false;
		for(int j=0;j<this.data.size();++j) {
			long[] larray = this.data.get(j);
			for(long l : larray) {
				hashValue = hashFunction(l,this._bucketCount);
				if(h[hashValue]>0) {
					this._table[hashValue] = new long[h[hashValue]];
					h[hashValue] = 0;
				}
				long[] bucket = this._table[hashValue];
				present = false;
				for(int i=0;i<this._buckets[hashValue];++i) {
					if(this._table[hashValue][i]==l) {
						present = true;
						break;
					}
				}
				if (!present) {
					try {
						bucket[this._buckets[hashValue]++] = l;
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
		this.data.clear();
		this.data = null;
		
		maxCollisions = 0;
	
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
		System.out.println("get lookup statistics: "+lookups+" "+lookupCollisions+" "+maxCollisions);
		return (double)lookupCollisions/lookups;
	}
	
	public long getMaxCollisions() {
		return maxCollisions;
	}
	
	public int setGrowthFactor(int gf) {
		int tmp = growthFactor;
		growthFactor = gf;
		return tmp;
	}
	
	public int setBinarySearchTarget(int bst) {
		int tmp = this.binarySearchTarget;
		this.binarySearchTarget = bst;
		return tmp;
	}
}
