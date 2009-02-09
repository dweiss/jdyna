package com.jdyna.pathfinder;

import java.util.Map;
import java.util.PriorityQueue;

import com.google.common.collect.Maps;

/**
 * A special version of PriorityQueue which enables you to retrieve a specific element.
 * 
 * @author Bartosz Wesołowski
 */
final class RetrievablePriorityQueue<T> {
	private final PriorityQueue<T> queue = new PriorityQueue<T>();
	private final Map<T, T> map = Maps.newHashMap();
	
	/** Adds an element to the queue. */
	public void add(T element) {
		queue.add(element);
		map.put(element, element);
	}
	
	/** Returns the smallest element and removes it from the queue. */
	public T poll() {
		final T result = queue.poll();
		map.remove(result);
		return result;
	}
	
	/** Returns a reference to element equal to the given element. */
	public T get(final T element) {
		return map.get(element);
	}
	
	/** Returns <code>true</code> if the queue contains the element and <code>false</code> otherwise. */
	public boolean contains(final T element) {
		return map.containsKey(element);
	}
	
	/** @return <code>true</code> if the queue is empty and <code>false</code> otherwise. */
	public boolean isEmpty() {
		return queue.isEmpty();
	}

	@Override
	public String toString() {
		return queue.toString();
	}
	
}
