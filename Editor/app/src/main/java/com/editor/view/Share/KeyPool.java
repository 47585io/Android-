package com.editor.view.Share;
import java.util.*;
import com.editor.text.base.*;

public class KeyPool
{
	private int mKeySize;
	private int[] mKeyPool;
	private int mKeyBinderSize;
	private int[][] mKeyBinders;
	private KeyBinderListener[] mKeyBinderListeners;
	
	public KeyPool(){
		mKeyPool=EmptyArray.INT;
		mKeyBinders=EmptyArray.emptyArray(int[].class);
		mKeyBinderListeners=EmptyArray.emptyArray(KeyBinderListener.class);
	}

	public void putkey(int keycode)
	{
		GrowingArrayUtils.append(mKeyPool,mKeySize++,keycode);
		int i = contians();
		if(i!=-1){
		    popkeys(mKeyBinders[i].length);
			mKeyBinderListeners[i].onKeyBindTrigger();
		}
	}
	public void popkeys(int len){
		mKeySize-=len;
	}

	public int contians()
	{
		for(int i=0;i<mKeyBinders.length;++i)
		{
			int[] arr= mKeyBinders[i];
			if(has(arr)){
				return i;
			}
		}
		return -1;
	}
	private boolean has(int[] arr)
	{
		for(int i=0;i<mKeySize&&i<arr.length;++i)
		{
			if(mKeyPool[mKeySize-i] != arr[arr.length-i]){
		        break;
			}
			else if(arr.length-i-1 == 0){
				return true;
			}
	    }
		return false;
	}

	public void putKeyBinder(int... keys, KeyBinderListener li)
	{
		GrowingArrayUtils.append(mKeyBinders,mKeyBinderSize,keys);
		GrowingArrayUtils.append(mKeyBinderListeners,mKeyBinderSize,li);
		mKeyBinderSize++;
	}
	public void removeKeyBinder(KeyBinderListener li)
	{
		int i = ArrayUtils.indexOf(mKeyBinderListeners,li,0);
		if(i!=-1){
			GrowingArrayUtils.remove(mKeyBinders,mKeyBinderSize,i);
			GrowingArrayUtils.remove(mKeyBinderListeners,mKeyBinderSize,i);
			mKeyBinderSize--;
		}
	}
	
	public static interface KeyBinderListener
	{
		public void onKeyBindTrigger()
	}

}
