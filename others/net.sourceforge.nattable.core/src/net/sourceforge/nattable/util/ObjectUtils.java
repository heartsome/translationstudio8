package net.sourceforge.nattable.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

public class ObjectUtils {

	/**
	 * Transfers the iterator to an unmodifiable collection.
	 * @return Contents of the Iterator<Cell> as a Collection.
	 */
	public static <T>Collection<T> asCollection(Iterator<T> iterator) {
		Collection<T> collection = new ArrayList<T>();
		return addToCollection(iterator, collection);
	}

	public static <T> List<T> asList(Collection<T> collection) {
		return new ArrayList<T>(collection);
	}

	public static int[] asIntArray(Collection<Integer> collection) {
		int[] copy = new int[collection.size()];

		int index = 0;
		for (Integer value : collection) {
			copy[index] = value.intValue();
			index++;
		}

		return copy;
	}

	/**
	 * Returns an unmodifiable ordered collection.
	 * @param <T>
	 * @param iterator
	 * @return
	 */
	public static <T>Collection<T> asOrderedCollection(Iterator<T> iterator, Comparator<T> comparator) {
		Collection<T> collection = new TreeSet<T>(comparator);
		return addToCollection(iterator, collection);
	}

	private static <T> Collection<T> addToCollection(Iterator<T> iterator, Collection<T> collection) {
		while (iterator.hasNext()) {
			T object = iterator.next();
			collection.add(object);
		}
		return Collections.unmodifiableCollection(collection);
	}

	public static <T>String toString(Collection<T> collection){
		if (collection == null) {
			return "NULL";
		}
		String out = "[ ";
		int count = 1;
		for (T object : collection) {
			if(object == null) continue;
			out = out + object.toString();
			if(collection.size() != count){
				out = out + ";\n";
			}
			count++;
		}
		out = out + " ]";
		return out;
	}

	public static <T>String toString(T[] array){
		return toString(Arrays.asList(array));
	}

	/**
	 * @return TRUE is collection is null or contains no elements
	 */
	public static <T> boolean isEmpty(Collection<T> collection) {
		return collection == null || collection.size() == 0;
	}

	/**
	 * @return TRUE if string == null || string.length() == 0
	 */
	public static <T> boolean isEmpty(String string) {
		return string == null || string.length() == 0;
	}

	/**
	 * @return TRUE if string != null && string.length() > 0
	 */
	public static <T> boolean isNotEmpty(String string) {
		return string != null && string.length() > 0;
	}

	/**
	 * @see ObjectUtils#isEmpty(Collection)
	 */
	public static <T> boolean isNotEmpty(Collection<T> collection) {
		return !isEmpty(collection);
	}

	/**
	 * @return TRUE if object reference is null
	 */
	public static boolean isNull(Object object) {
		return object == null;
	}

	/**
	 * @return TRUE if object reference is NOT null
	 */
	public static boolean isNotNull(Object object) {
		return object != null;
	}

	private static final Random RANDOM = new Random();

	/**
	 * @return a random Date
	 */
	public static Date getRandomDate() {
		return new Date(RANDOM.nextLong());
	}

	/**
	 * @return 4 digit random Integer number
	 */
	public static int getRandomNumber() {
		return RANDOM.nextInt(10000);
	}

	/**
	 * @return random Integer number between 0 and parameter max
	 */
	public static int getRandomNumber(int max) {
		return RANDOM.nextInt(max);
	}

	public static <T> T getLastElement(List<T> list) {
		return list.get(list.size() - 1);
	}

	public static <T> T getFirstElement(List<T> list) {
		return list.get(0);
	}
}
