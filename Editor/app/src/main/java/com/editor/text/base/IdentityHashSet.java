package com.editor.text.base;

import java.io.*;
import java.util.*;


public class IdentityHashSet<E> extends AbstractSet<E> 
{
	
	transient IdentityHashMap<E, IdentityHashSet<E>> backingMap;
	
	public IdentityHashSet() {
		this(new IdentityHashMap<E, IdentityHashSet<E>>());
	}
	public IdentityHashSet(int capacity) {
		this(new IdentityHashMap<E, IdentityHashSet<E>>(capacity));
	}
	public IdentityHashSet(Collection<? extends E> collection) {
		this(new IdentityHashMap<E, IdentityHashSet<E>>(collection.size() < 6 ? 11 : collection
										.size() * 2));
		for (E e : collection) {
			add(e);
		}
	}
	IdentityHashSet(IdentityHashMap<E, IdentityHashSet<E>> backingMap) {
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
