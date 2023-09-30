package com.editor.text2.builder.words;
import java.util.*;

public class WordCountSet<E> extends AbstractSet<E>
{

	transient HashMap<E, Int> backingMap;

	public WordCountSet() {
		this(new HashMap<E, Int>());
	}
	public WordCountSet(int capacity) {
		this(new HashMap<E, Int>(capacity));
	}
	public WordCountSet(Collection<? extends E> collection) {
		this(new HashMap<E, Int>(collection.size() < 6 ? 11 : collection
															.size() * 2));
		for (E e : collection) {
			add(e);
		}
	}
	WordCountSet(HashMap<E, Int> backingMap) {
		this.backingMap = backingMap;
	}

	@Override
	public boolean add(E object)
	{
		Int count = backingMap.get(object);
		if(count!=null){
			count.vuale++;
			return false;
		}
		return backingMap.put(object, new Int(1)) == null;
	}
	
	@Override
	public boolean remove(Object object)
	{
		Int count = backingMap.get(object);
		if(count!=null && --count.vuale!=0){
			return false;
		}
		return backingMap.remove(object) != null;
	}
	
	@Override
	public void clear() {
		backingMap.clear();
	}

	@Override
	public boolean contains(Object object) {
		return backingMap.containsKey(object);
	}

	@Override
	public boolean isEmpty() {
		return backingMap.isEmpty();
	}

	@Override
	public Iterator<E> iterator() {
		return backingMap.keySet().iterator();
	}

	@Override
	public int size() {
		return backingMap.size();
	}
	
	
	private static class Int
	{
		Int(int v){
			vuale = v;
		}
		
		public int vuale;
	}
}
