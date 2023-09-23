package com.editor.text2.builder.words;

import java.util.*;

public class CodeWords implements Words
{

	//所有单词使用Map存储，以使index可以为任意的值
	private Map<Integer,Collection<Character>> mchars;
	private Map<Integer,Collection<CharSequence>> mdates;
	private Map<Integer,Map<CharSequence,CharSequence>> mmaps;
	//支持保存Span单词，但可能有一些异常

	public CodeWords(){
		init();
	}
	public void init()
	{
		//即使未使用，也先装入空的集合，以使get不为null
		mchars = new HashMap<>();
		mdates = new HashMap<>();
		mmaps = new HashMap<>();
		
		mchars.put(chars_fuhao,new HashSet<>());
		mchars.put(chars_spilt,new HashSet<>());
		for(int i=words_key;i<=words_attr;++i){
			mdates.put(i,new HashSet<>());
		}
		mmaps.put(maps_zhu,new HashMap<>());
	}

	@Override
	public Collection<Character> getACollectionChars(int index){
		return mchars.get(index);
	}
	@Override
	public Collection<CharSequence> getACollectionWords(int index){
		return mdates.get(index);
	}
	@Override
	public Map<CharSequence, CharSequence> getAMapWords(int index){
		return mmaps.get(index);
	}

	@Override
	public void setACollectionChars(int index, Collection<Character> words){
		mchars.put(index,words);
	}
	@Override
	public void setACollectionWords(int index, Collection<CharSequence> words){
		mdates.put(index,words);
	}
	@Override
	public void setAMapWords(int index, Map<CharSequence, CharSequence> words){
		mmaps.put(index,words);
	}

	@Override
	public void clear(){
		init();
	}
	@Override
	public int size(){
		return mdates.size()+mmaps.size()+mchars.size();
	}
	@Override
	public boolean contrans(int index){
		return mchars.containsKey(index) || mdates.containsKey(index) || mmaps.containsKey(index);
	}
	
	private static Map EmptyMap()
	{
		return Collections.synchronizedMap(new HashMap<>());
	}
	private static Set EmptySet()
	{
		return Collections.synchronizedSet(new HashSet<>());
	}
	private static List EmptyList()
	{
		return Collections.synchronizedList(new ArrayList<>());
	}
	private static Set copySet(Collection coll)
	{
		return Collections.synchronizedSet(new HashSet(coll));
	}
	private static Map copyMap(Map coll)
	{
		return Collections.synchronizedMap(new HashMap(coll));
	}
	
}
	
