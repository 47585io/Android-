package com.who.Edit.EditBuilder.WordsVistor;
import java.util.*;

public class prefixCharSequenceMap implements Collection<CharSequence>
{
	
	private Map<CharSequence,Set<CharSequence>> mCharPrefixMap;
	private Set<CharSequence> mCharSet;
	private StringBuilder builder;
	
	public prefixCharSequenceMap()
	{
		mCharPrefixMap = new HashMap<>();
		mCharSet = new HashSet<>();
		builder = new StringBuilder();
	}
	
	@Override
	public int size(){
		return mCharSet.size();
	}
	@Override
	public boolean isEmpty(){
		return mCharSet.isEmpty();
	}
	@Override
	public boolean contains(Object p1){
		return mCharSet.contains(p1);
	}
	@Override
	public Iterator<CharSequence> iterator(){
		return mCharSet.iterator();
	}
	@Override
	public Object[] toArray(){
		return mCharSet.toArray();
	}
	@Override
	public <T extends Object> T[] toArray(T[] p1){
		return mCharSet.toArray(p1);
	}

	@Override
	synchronized public boolean add(CharSequence p1)
	{
		int len = p1.length();
		for(int i=0;i<len;++i)
		{
			builder.append(p1.charAt(i));
			CharSequence prefix = builder.toString();
			Set<CharSequence> set = mCharPrefixMap.get(prefix);
			if(set==null){
				set = new HashSet<>();
				mCharPrefixMap.put(prefix,set);
			}
			set.add(p1);
		}
		mCharSet.add(p1);
		builder.delete(0,len);
		return true;
	}
	@Override
	synchronized public boolean remove(Object o)
	{
		CharSequence p1 = (CharSequence) o;
		int len = p1.length();
		for(int i=0;i<len;++i)
		{
			builder.append(p1.charAt(i));
			CharSequence prefix = builder.toString();
			Set<CharSequence> set = mCharPrefixMap.get(prefix);
			if(set!=null){
				set.remove(prefix);
			}
		}
		mCharSet.remove(p1);
		builder.delete(0,len);
		return true;
	}

	public Set<CharSequence> getCharSetFromPrefix(CharSequence prefix){
		return mCharPrefixMap.get(prefix);
	}
	@Override
	public boolean containsAll(Collection<?> p1){
		return mCharSet.containsAll(p1);
	}
	@Override
	public boolean addAll(Collection<? extends CharSequence> p1)
	{
		for(CharSequence o:p1){
			add(o);
		}
		return true;
	}
	@Override
	public boolean removeAll(Collection<?> p1)
	{
		for(Object o:p1){
			remove(o);
		}
		return true;
	}
	@Override
	synchronized public boolean retainAll(Collection<?> p1)
	{
		for(Set<CharSequence> set:mCharPrefixMap.values()){
			set.retainAll(p1);
		}
		mCharSet.retainAll(p1);
		return true;
	}
	@Override
	synchronized public void clear()
	{
		mCharPrefixMap.clear();
		mCharSet.clear();
	}
	
}
