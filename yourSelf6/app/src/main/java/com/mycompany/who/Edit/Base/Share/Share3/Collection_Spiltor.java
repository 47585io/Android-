package com.mycompany.who.Edit.Base.Share.Share3;
import java.util.*;

public class Collection_Spiltor
{
	
	public static void addAll(Collection target,Collection src){
		if(target!=null && src!=null)
			target.addAll(src);
	}
	
	public static void addAll(Map target,Map src){
		if(target!=null && src!=null)
			target.putAll(src);
	}
	
	public static List copyList(Collection src){
		List target = EmptyList();
		addAll(target,src);
		return target;
	}
	public static Map copyMap(Map src){
		Map target = EmptyMap();
		addAll(target,src);
		return target;
	}
	public static Set copySet(Collection src){
		Set target = EmptySet();
		addAll(target,src);
		return target;
	}
	public static Collection copyCollection(Collection t){
		if(t!=null)
		    return Collections.synchronizedCollection(t);
		return null;
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
