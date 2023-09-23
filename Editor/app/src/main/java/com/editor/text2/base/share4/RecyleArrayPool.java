package com.editor.text2.base.share4;
import com.editor.text.base.*;

public abstract class RecyleArrayPool<T>
{
	private T[][] mArray;
	private int count;
	private int maxCount;
	
	public RecyleArrayPool(){
		mArray = EmptyArray.emptyArray(T[].class);
		maxCount = 10;
	}
	
	synchronized public void setMaxCount(int count){
		maxCount = count;
	}
	synchronized public void recyle(T[] array)
	{
		if(count+1 > maxCount){
			return;
		}
		if(count+1 > mArray.length){
			mArray = newInstance(GrowingArrayUtils.growSize(count+1));
		}
		if(array.length < mArray[count].length)
		{
			int index = findIndexBySize(array.length);
			System.arraycopy(mArray,index,mArray,index+1,count-index);
			mArray[index] = array;
		}
		else{
			mArray[count] = array;
		}
		count++;
	}
	synchronized public T[] obtain(int size)
	{
		if(mArray[count-1].length<size){
			return newArray(GrowingArrayUtils.growSize(size));
		}
		int index = findIndexBySize(size);
		T[] array = mArray[index];
		System.arraycopy(mArray,index+1,mArray,index,count-index-1);
		count--;
		return array;
	}
	
	private int findIndexBySize(int size)
	{
		int low = 0;   
		int high = count - 1;   
		int middle = 0;
		while (low <= high)
		{   
			middle = (low + high) / 2;   
			if (size == mArray[middle].length) 
				break;   
			else if (size < mArray[middle].length)
				high = middle - 1;   
			else 
				low = middle + 1;
		}  
		return middle;
	}
	
	abstract protected T[][] newInstance(int size)
	
	abstract protected T[] newArray(int size)
}
