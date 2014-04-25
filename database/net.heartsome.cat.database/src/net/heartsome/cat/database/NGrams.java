package net.heartsome.cat.database;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

public class NGrams {

	private static int NGRAMSIZE = 3;
	public static String SEPARATORS = " \r\n\f\t\u2028\u2029,.;\"\':<>?!()[]{}=+-/*\u00AB\u00BB\u201C\u201D\u201E\uFF00\u2003"; //$NON-NLS-1$

	public static int[] getNGrams(String src, boolean quality) {
		src = src.toLowerCase();
		Vector<String> words = buildWordList(src);
		Hashtable<String, String> table = new Hashtable<String, String>();

		if (quality) {
			Iterator<String> it = words.iterator();
			while (it.hasNext()) {
				String word = it.next();
				char[] array = word.toCharArray();
				int length = word.length();
				int ngrams = length / NGRAMSIZE;
				if ( ngrams * NGRAMSIZE < length ) {
					ngrams++;
				}
				for (int i = 0; i < ngrams; i++) {
					String gram = "";
					for (int j = 0; j < NGRAMSIZE; j++) {
						if ( i*NGRAMSIZE + j < length) {
							char c = array[i*NGRAMSIZE + j];
							gram = gram + c;
						}
					}
					table.put("" + gram.hashCode(), "");
				}
			}
		} else {
			int length = words.size();
			for (int i=0 ; i<length ; i++) {
				table.put("" + words.get(i).hashCode(), "");
			}
		}
		Enumeration<String> keys = table.keys();
		int[] result = new int[table.size()];
		int idx = 0;
		while (keys.hasMoreElements()) {
			result[idx++] = Integer.parseInt(keys.nextElement());
		}
		return result;
	}

    private static Vector<String> buildWordList(String src) {
    	Vector<String> result = new Vector<String>();
        StringTokenizer tokenizer = new StringTokenizer(src,SEPARATORS);
        while (tokenizer.hasMoreElements()) {
            result.add(tokenizer.nextToken());
        }
        return result;
    }

}