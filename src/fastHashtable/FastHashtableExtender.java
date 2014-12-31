package fastHashtable;

import java.util.Arrays;

public class FastHashtableExtender {

	private final int _ftSize = 1<<16;
	private FastHashtable[] _array;

	public int size() {
		int size = 0;
		for(int i=0;i<_array.length;++i) {
			if(this._array[i]!=null) {
				size += this._array[i].size();
			}
		}
		return size;
	}
	public int maxSize() {
		return this._array.length*this._ftSize;
	}
	
	public int maxKey() {
		return this.maxSize()-1;
	}
	
	public void put(int p_key,long p_value) {

		int i = p_key % this._ftSize;
		int j = p_key/this._ftSize;

		if(j>=this._array.length) {
			FastHashtable[] t = Arrays.copyOf(this._array, j+1);
			this._array = t;			
		}
		if(this._array[j]==null) {
			this._array[j] = new FastHashtable(this._ftSize);
		}
		
		this._array[j].put(i, p_value);
	}
	
	public boolean containsKey(int p_key) {

		int i = p_key % this._ftSize;
		int j = p_key/this._ftSize;
		if(j>=this._array.length) {
			return false;
		} else {
			if(this._array[j]==null) {
				return false;
			} else {
				return this._array[j].containsKey(i);
			}
		}
	}
	
	
	public boolean get(int p_key,long[] r) {

		int i = p_key % this._ftSize;
		int j = p_key/this._ftSize;
		if(j>=this._array.length) {
			r[0] = 0;
			r[1] = 0;
			return false;
		} else {
			if(this._array[j]==null) {
				r[0] = 0;
				r[1] = 0;
				return false;
			} else {
				if(this._array[j].get(i, r)) {
					r[1] = j*this._ftSize + i;
					return true;
				} else {
					return false;
				}
			}
		}
	}
	
	public FastHashtableExtender() {

		this._array = new FastHashtable[0];

	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		for(int i=0;i<this._array.length;++i) {
			if(this._array[i]!=null) {
				sb.append(this._array[i].toString());
			}
		}
		
		return sb.toString();
	}

}
