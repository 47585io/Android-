package com.editor.text2.base.share4;
import com.editor.text.base.*;

public abstract class RecylePool<T>
{
	private T[] mObjects;
	private int mObjectCount;
	private int maxCount;

	public RecylePool(){
		mObjects = newInstance(GrowingArrayUtils.growSize(10));
		maxCount = mObjects.length;
	}

	synchronized public void setMaxCount(int count){
		maxCount = count;
	}
	synchronized public void recyle(T object)
	{
		if(mObjectCount+1 > maxCount){
			return;
		}
		if(mObjectCount+1 > mObjects.length){
			mObjects = newInstance(GrowingArrayUtils.growSize(mObjectCount+1));
		}
		mObjects[mObjectCount++] = object;
	}
	synchronized public T obtain()
	{
		T object = mObjects[mObjectCount-1];
		mObjects[mObjectCount-1] = null;
		mObjectCount--;
		return object;
	}

	abstract protected T[] newInstance(int size)

}
