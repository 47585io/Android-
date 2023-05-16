package com.mycompany.who.SuperVisor.CodeMoudle.Share;
import java.util.*;
import java.security.*;

public class KeyPool
{
	
	List<Integer> pool;
	Map<Integer,int[]> values;
	
	public KeyPool(){
		pool=new ArrayList<>();
		values=new HashMap<>();
	}
	
	public int putkey(int keycode)
	{
		pool.add(keycode);
		int value= contrans();
		if(value!=-1){
		    popkeys(values.get(value).length);
		}
		return value;
	}
	
	public void popkeys(int len)
	{
		for(;len>0;--len){
			pool.remove(pool.size());
		}
	}
	
	public int contrans(){
		for(int value:values.keySet())
		{
			int[] arr= values.get(value);
			if(has(arr)){
				return value;
			}
		}
		return -1;
	}

	private boolean has(int[] arr)
	{
		int i;
		for(i=0;i<pool.size()&&i<arr.length;++i)
		{
			if(pool.get(pool.size()-i)!=arr[arr.length-i]){
		        break;
			}
			else if(arr.length-i-1==0){
				return true;
			}
	    }
		return false;
	}
	
	public void push_KeyValue(int value,int ... key){
		values.put(value,key);
	}
	public void del_KeyValue(int value){
		values.remove(value);
	}

}
