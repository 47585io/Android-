package com.who.Edit.Base.Share.Share3;
import java.util.*;

public class CollectionSpiltor
{
	public static<T> void addSome(Collection<T> coll, T... some)
	{
		for(int i=0;i<some.length;++i){
			coll.add(some[i]);
		}
	}
	
	public static<T> void fill(Collection<T> coll, T o, int n)
	{
		while(n-->0){
			coll.add(o);
		}
	}
	
}
