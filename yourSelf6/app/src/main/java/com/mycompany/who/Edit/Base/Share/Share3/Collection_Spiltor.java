package com.mycompany.who.Edit.Base.Share.Share3;
import java.util.*;

public class Collection_Spiltor
{
	
	public static void addAll(Collection t,Collection t2){
		if(t!=null && t2!=null)
			t.addAll(t2);
	}
	
	public static void addAll(Map p,Map p2){
		if(p!=null && p2!=null)
			p.putAll(p2);
	}
	
	public static List EmptyList(){
		return Collections.synchronizedList(new ArrayList());
	}
	public static Map EmptyMap(){
		return Collections.synchronizedMap(new HashMap());
	}
	public static Set EmptySet(){
		return Collections.synchronizedSet(new HashSet());
	}
	
	public static Object vualeToKey(Object v,Map map)
	{
		for(Object k: map.keySet())
		{
			Object value = map.get(k);
			if(value.equals(v)){
				return k;
			}
		}
		return null;
	}
	
}
