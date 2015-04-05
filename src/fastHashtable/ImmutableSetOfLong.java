package fastHashtable;

import java.lang.Math;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Objects of this class hold a fixed set of values and are optimized to provide one set operation,
 * "is a member". Values can be added iteratively to the set. 
 * After all data has been add the object is "finalized", at which point, the internal hash table is created and
 * optimized for fast lookup under some memory constraint. After the object is finalized, attempting to add
 * more data causes an exception.
 * 
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
	private int       maxActualCollisions=0;
	
	/**
	 * the number of buckets in the hash table will not be more
	 * than maxTableSizeMultiple*(number of entries in the set).
	 */
	private int       maxTableSizeMultiple=5;
	
	/**
	 * the maximum number of collisions in the hash table will not be
	 * more than maxCollisionsGoal uder the maxTableSizeMultiple constraint.
	 */
	private int       maxCollisionsGoal=4;
	
	/**
	 * the hash table size will grow by this factor over each
	 * iteration in finalizeSet.
	 */
	private double 	  tableSizeGrowthFactor=1.5;
	
	/**
	 * if the set were implemented as a sorted array which used
	 * binary search to find entries, binarySearchWC would be the
	 * worst case performance for lookup.
	 */
	private int       binarySearchWC=0;

	/*
	 * The number of entries pairs in the hash table.
	 */
	public int size() {
		return this._size;
	}

	
	/**
	 * approximately, how many bytes are used by this object.
	 * 
	 * @return
	 */
	public long getMemoryUsage() {
		
		long total = (long)Integer.SIZE*this._bucketCount;
		for(int i=0;i<this._bucketCount;++i) {
			if (this._table[i]!=null) {
				total += (long)Long.SIZE * this._table[i].length;
			}
		}
		return total;
		
	}

    /**
     * add an array of long to the object.
     * 
     * no data can be added after the object is "finalized". (See finalizeSet.)
     * @param larray
     */
	public void add(long[] larray) {
		if(this.isFinalized) {
			throw new IllegalStateException("set is finalized");
		}
		this.data.add(larray);
	}
	


	/**
	 * search for value l in the set.
	 * 
	 * search is not allowed before the object is "finalized". (See finalizeSet.)
	 * 
	 * Also, statistics for the maximum actual number of collisions and the average
	 * number of collisions per search are generated.
	 * 
	 * @param l
	 * @return
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
				maxActualCollisions = Math.max(maxActualCollisions, collisions);
				this.lookupCollisions+=collisions;
				return true;
			};
			++collisions;
		};
		return false;

	}
	
	/**
	 * Create a hash table in which the maximum length of any bucket is less than or equal to maxCollision goals,
	 * subject to the constraint on size, that the number of buckets is less than or equal to maxTableSizeMultiple*this._size.
	 * 
	 */
	public void finalizeSet() {
		
		this.isFinalized = true;
		
		/**
		 * how many elements are in this set.
		 */
		for(long[] l : this.data) {
			this._size += l.length;
		}
		
		/**
		 * what would the worst case performance for binary search on an ordered
		 * list of these elements be?
		 */
		double dlog2 = Math.log(this._size)/Math.log(2);
		int ilog2 = (int)dlog2;
		this.binarySearchWC = ilog2;
		if(this.binarySearchWC<dlog2) {
			++this.binarySearchWC;
		}

		/**
		 * for successive hash table sizes, compute the collisions per bucket.
		 * stop when the maxCollisionGoal is met or exceeded, or the maxTableSizeMultiple is
		 * is exceeded.
		 * 
		 * for each hash table size, create a histogram of collisions per bucket.
		 * 
		 * make each candidate table size a prime number.
		 * 
		 */
		int maxCollisions = maxCollisionsGoal+1;
		
		/**
		 * compute the maximum hash table candidate size.
		 */
		int maxSize = this._size;
		while(maxSize<=this.maxTableSizeMultiple*this._size){
			maxSize = HashUtil.nextPrime((int)(this.tableSizeGrowthFactor*maxSize)+1);
		}
		
		/**
		 * try successive table sizes until the collision goal is met or the
		 * maximum table size is exceeded.
		 */
		int[] h = new int[maxSize];
		int hashValue;
		int M = this._size;
		do{
			M = HashUtil.nextPrime((int)(this.tableSizeGrowthFactor*M)+1);
			int collisions = 0;
			for(int j=0;j<this.data.size();++j) {
				long[] l = this.data.get(j);
				Arrays.fill(h, 0, M, 0);
				for(long v : l) {
					hashValue = this.hashFunction(v, M);
					++h[hashValue];
					collisions = Math.max(collisions, h[hashValue]-1);
				}
			}
			maxCollisions = collisions;
			
		} while(maxCollisions>maxCollisionsGoal && maxCollisions>0 && M<maxSize);

				
		/**
		 * now, M is the optimal table size, so create and populate the table.
		 */
		
		this._bucketCount = M;
		this._buckets = new int[this._bucketCount];
		this._table = new long[this._bucketCount][];

		for(int j=0;j<this.data.size();++j) {
			long[] larray = this.data.get(j);
			for(long l : larray) {
				hashValue = hashFunction(l,this._bucketCount);
				/**
				 * the number of values that hash to this bucket
				 * has already been computed and stored in histogra
				 * h.
				 */
				if(h[hashValue]>0) {
					this._table[hashValue] = new long[h[hashValue]];
					h[hashValue] = 0;
				}
				long[] bucket = this._table[hashValue];
				for(int i=0;i<this._buckets[hashValue];++i) {
					if(this._table[hashValue][i]==l) {
						break;
					}
				}
				bucket[this._buckets[hashValue]++] = l;
			}
		}
		
		this.data.clear();
		this.data = null;
		
	
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
		System.out.println("get lookup statistics: "+lookups+" "+lookupCollisions+" "+maxActualCollisions);
		return (double)lookupCollisions/lookups;
	}
	
	public long getMaxCollisions() {
		return maxActualCollisions;
	}
	
	public int setMaxTableSizeMultiple(int n) {
		int tmp = this.maxTableSizeMultiple;
		this.maxTableSizeMultiple = n;
		return tmp;
	}
	
	public int getBinarySearchWC() {
		return this.binarySearchWC;
	}
}
