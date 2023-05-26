package com.mycompany.who.Edit.Base.Share.Share3;
import java.util.*;

public class Collection_Spiltor
{
	
	//将src添加到target中
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
	
	//拷贝一个新的集合
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
	
	//安全的空集合
	public static List EmptyList(){
		return Collections.synchronizedList(new ArrayList());
	}
	public static Map EmptyMap(){
		return Collections.synchronizedMap(new HashMap());
	}
	public static Set EmptySet(){
		return Collections.synchronizedSet(new HashSet());
	}
	
	//通过value得到key
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
	
	//合并相同元素
	public static<T> void And_Same(Collection<T> d1,Collection<T> d2,Collection<T> end)
	{
		if(d1==null&&d2==null){
			return;
		}
		for(T o: d1)
	    {
			if(d2.contains(o))
				end.add(o);
		}
	}
	//删除相同的元素
	public static<T> void delSame(Collection<T> dst,Collection<T> src)
	{
		for(Object o: dst.toArray())
		{
			if(src.contains(o))
				dst.remove(o);
		}
	}
	//删除数字
	public static void delNumber(Collection<CharSequence> dst)
	{
		for(Object o: dst.toArray())
		{
			if(String_Splitor. IsNumber(((CharSequence)o))){
				dst.remove(o);
			}
		}
	}
	//查找集合中所有出现了str的元素
	public static List<CharSequence> indexsOf(CharSequence str,Collection<CharSequence> keyword,int start,Idea i) 
	{	
		if(str.length()==0 || keyword==null||keyword.size()==0)
			return null;
	    List<CharSequence> words = new ArrayList<>();
		for(CharSequence word:keyword)
	    {
			if(i.can(word,str,start)){
				words.add(word);
			}
		}
		return words;
	}
	
}
