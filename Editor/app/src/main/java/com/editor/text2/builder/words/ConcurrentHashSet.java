package com.editor.text2.builder.words;
import java.util.*;
import java.util.concurrent.*;

public class ConcurrentHashSet<E> extends AbstractSet<E> 
{

	transient ConcurrentHashMap<E, ConcurrentHashSet<E>> backingMap;

	public ConcurrentHashSet() {
		this(new ConcurrentHashMap<E, ConcurrentHashSet<E>>());
	}
	public ConcurrentHashSet(int capacity) {
		this(new ConcurrentHashMap<E, ConcurrentHashSet<E>>(capacity));
	}
	public ConcurrentHashSet(Collection<? extends E> collection) {
		this(new ConcurrentHashMap<E, ConcurrentHashSet<E>>(collection.size() < 6 ? 11 : collection
														.size() * 2));
		for (E e : collection) {
			add(e);
		}
	}
	ConcurrentHashSet(ConcurrentHashMap<E, ConcurrentHashSet<E>> backingMap) {
		this.backingMap = backingMap;
	}

	@Override
	public boolean add(E object) {
		return backingMap.put(object, this) == null;
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
	public boolean remove(Object object) {
		return backingMap.remove(object) != null;
	}

	@Override
	public int size() {
		return backingMap.size();
	}

}
