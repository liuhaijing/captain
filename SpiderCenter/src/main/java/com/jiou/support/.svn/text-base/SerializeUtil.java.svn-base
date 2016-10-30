package com.jiou.support;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author zhe.li
 */
public class SerializeUtil {
	public static <T extends Serializable> void serialize(Collection<T> coll, File file, boolean append) {

		List<T> list = new ArrayList<T>(coll);
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(file, append));
			oos.writeObject(list);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (oos != null) {
					oos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	@SuppressWarnings("unchecked")
	public static <T extends Serializable> Collection<T> antiSerialize(File file) {
		if (file == null || !file.exists()) {
			throw new RuntimeException("File does not exist");
		}

		ObjectInputStream ois = null;
		try {

			ois = new ObjectInputStream(new FileInputStream(file));
			return (Collection<T>) ois.readObject();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (ois != null) {
					ois.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return Collections.emptyList();

	}
}
