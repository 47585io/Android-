package com.editor.text2.builder.words;

import java.util.*;
import java.util.concurrent.*;


public class CodeWords implements Words
{

	private List<Collection<Character>> mChars;
	private List<Collection<CharSequence>> mWords;
	private List<Map<CharSequence,CharSequence>> mMaps;
	
	public CodeWords(){
		init();
	}
	private void init(){
		mChars = EmptyList();
		mWords = EmptyList();
		mMaps = EmptyList();
	}
	@Override
	public void clear(){
		mChars.clear();
		mWords.clear();
		mMaps.clear();
	}
	
	@Override
	public Collection<Character> getACollectionChars(int index){
		return mChars.get(index);
	}
	@Override
	public Collection<CharSequence> getACollectionWords(int index){
		return mWords.get(index);
	}
	@Override
	public Map<CharSequence, CharSequence> getAMapWords(int index){
		return mMaps.get(index);
	}

	@Override
	public void setACollectionChars(int index, Collection<Character> chars)
	{
		if(mChars.size()>index)
		{
			Collection<Character> coll = mChars.get(index);
			if(coll==null){
				mChars.set(index,newSet(chars));
			}else{
				mChars.set(index,copySet(coll,chars));
			}
		}
		else{
			mChars.add(index,newSet(chars));
		}
	}
	@Override
	public void setACollectionChars(int index, char[] chars)
	{
		if(mChars.size()>index)
		{
			Collection<Character> coll = mChars.get(index);
			if(coll==null){
				mChars.set(index,newSet(chars));
			}else{
				mChars.set(index,copySet(coll,chars));
			}
		}
		else{
			mChars.add(index,newSet(chars));
		}
	}
	@Override
	public void setACollectionWords(int index, Collection<CharSequence> words)
	{
		if(mWords.size()>index)
		{
			Collection<CharSequence> coll = mWords.get(index);
			if(coll==null){
				mWords.set(index,newSet(words));
			}else{
				mWords.set(index,copySet(coll,words));
			}
		}
		else{
			mWords.add(index,newSet(words));
		}
	}
	@Override
	public void setACollectionWords(int index, CharSequence[] words)
	{
		if(mWords.size()>index)
		{
			Collection<CharSequence> coll = mWords.get(index);
			if(coll==null){
				mWords.set(index,newSet(words));
			}else{
				mWords.set(index,copySet(coll,words));
			}
		}
		else{
			mWords.add(index,newSet(words));
		}
	}
	@Override
	public void setAMapWords(int index, Map<CharSequence, CharSequence> words)
	{
		if(mMaps.size()>index)
		{
			Map<CharSequence,CharSequence> map = mMaps.get(index);
			if(map==null){
				mMaps.set(index,newMap(words));
			}else{
				mMaps.set(index,copyMap(map,words));
			}
		}
		else{
			mMaps.add(index,newMap(words));
		}
	}
	@Override
	public void setAMapWords(int index, CharSequence[] keys, CharSequence[] vaules)
	{
		if(mMaps.size()>index)
		{
			Map<CharSequence,CharSequence> map = mMaps.get(index);
			if(map==null){
				mMaps.set(index,newMap(keys,vaules));
			}else{
				mMaps.set(index,copyMap(map,keys,vaules));
			}
		}
		else{
			mMaps.add(index,newMap(keys,vaules));
		}
	}
	
	
	public static Map EmptyMap(){
		return new ConcurrentHashMap();
	}
	public static Set EmptySet(){
		return new ConcurrentHashSet();
	}
	public static List EmptyList(){
		return new CopyOnWriteArrayList();
	}
	
	public static<T> Set<T> newSet(Collection<T> dst){
		return dst==null ? null : new ConcurrentHashSet<T>(dst);
	}
	public static<K,V> Map<K,V> newMap(Map<K,V> dst){
		return dst==null ? null : new ConcurrentHashMap<K,V>(dst);
	}
	public static<T> Set<T> newSet(T[] dst)
	{
		if(dst==null){
			return null;
		}
		Set<T> set = new ConcurrentHashSet<>();
		for(int i=0;i<dst.length;++i){
			set.add(dst[i]);
		}
		return set;
	}
	public static<K,V> Map<K,V> newMap(K[] keys, V[] values)
	{
		if(keys==null || values==null){
			return null;
		}
		Map<K,V> map = new ConcurrentHashMap<>();
		for(int i=0;i<keys.length;++i){
			map.put(keys[i],values[i]);
		}
		return map;
	}
	public static Set<Character> newSet(char[] dst)
	{
		if(dst==null){
			return null;
		}
		Set<Character> set = new ConcurrentHashSet<>();
		for(int i=0;i<dst.length;++i){
			set.add(dst[i]);
		}
		return set;
	}
	
	public static<T> Collection<T> copySet(Collection<T> self, Collection<T> dst)
	{
		if(dst==null){
			return null;
		}
		self.clear();
		self.addAll(dst);
		return self;
	}
	public static<K,V> Map<K,V> copyMap(Map<K,V> self, Map<K,V> dst)
	{
		if(dst==null){
			return null;
		}
		self.clear();
		self.putAll(dst);
		return self;
	}
	public static<T> Collection<T> copySet(Collection<T> self, T[] dst)
	{
		if(dst==null){
			return null;
		}
		self.clear();
		for(int i=0;i<dst.length;++i){
			self.add(dst[i]);
		}
		return self;
	}
	public static<K,V> Map<K,V> copyMap(Map<K,V> self, K[] keys, V[] vaules)
	{
		if(keys==null || vaules==null){
			return null;
		}
		self.clear();
		for(int i=0;i<keys.length;++i){
			self.put(keys[i],vaules[i]);
		}
		return self;
	}
	public static Collection<Character> copySet(Collection<Character> self, char[] dst)
	{
		if(dst==null){
			return null;
		}
		self.clear();
		for(int i=0;i<dst.length;++i){
			self.add(dst[i]);
		}
		return self;
	}
	
}
