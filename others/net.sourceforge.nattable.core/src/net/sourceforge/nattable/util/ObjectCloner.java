package net.sourceforge.nattable.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Public domain code from Dave Miller, JavaWorld.com
 */
public class ObjectCloner {

	/**
	 * @return a deep copy of an object
	 */
	public static Object deepCopy(Object oldObj) {
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(bos);

			// serialize and pass the object
			oos.writeObject(oldObj);
			oos.flush();
			ByteArrayInputStream bin = new ByteArrayInputStream(bos.toByteArray());
			ois = new ObjectInputStream(bin);

			// return the new object
			return ois.readObject();
		} catch (Exception e) {
			e.printStackTrace(System.err);
			return null;
		} finally {
			try {
				oos.close();
				ois.close();
			} catch (IOException e) {
				e.printStackTrace(System.err);
			}
		}
	}

}
