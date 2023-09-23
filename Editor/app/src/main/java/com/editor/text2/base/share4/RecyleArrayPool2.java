package com.editor.text2.base.share4;
import com.editor.text.base.*;

public abstract class RecyleArrayPool2<T>
{
	private T[] mArray;
	private int[] mArraySize;
	private int count;
	private int maxCount;

	synchronized public void setMaxCount(int count){
		maxCount = count;
	}
	synchronized public void recyle(T array, int size)
	{
		if(count+1 > maxCount){
			return;
		}
		if(count+1 > mArray.length){
			newInstance(GrowingArrayUtils.growSize(count+1));
		}
		if(size < mArraySize[count])
		{
			int index = findIndexBySize(size);
			System.arraycopy(mArray,index,mArray,index+1,count-index);
			System.arraycopy(mArraySize,index,mArraySize,index+1,count-index);
			mArray[index] = array;
			mArraySize[index] = size;
		}
		else{
			mArray[count] = array;
			mArraySize[count] = size;
		}
		count++;
	}
	synchronized public T obtain(int size)
	{
		if(mArraySize[count-1]<size){
			return newArray(GrowingArrayUtils.growSize(size));
		}
		int index = findIndexBySize(size);
		T array = mArray[index];
		System.arraycopy(mArray,index+1,mArray,index,count-index-1);
		System.arraycopy(mArraySize,index+1,mArraySize,index,count-index-1);
		mArray[--count] = null;
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
			if (size == mArraySize[middle]) 
				break;   
			else if (size < mArraySize[middle])
				high = middle - 1;   
			else 
				low = middle + 1;
		}  
		return middle;
	}

	abstract protected T[] newInstance(int size)

	abstract protected T newArray(int size)
}
