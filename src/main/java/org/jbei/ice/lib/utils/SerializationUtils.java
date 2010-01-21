package org.jbei.ice.lib.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.sun.org.apache.xml.internal.security.utils.Base64;
import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;

public class SerializationUtils {
	public static class SerializationUtilsException extends Exception {
		private static final long serialVersionUID = -6597529889622775652L;

		public SerializationUtilsException() {
			super();
		}

		public SerializationUtilsException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	public static Serializable deserializeFromString(String serializedObject) throws SerializationUtilsException {
		try {
			byte[] data = Base64.decode(serializedObject);

			ObjectInputStream objectInputStream;
			objectInputStream = new ObjectInputStream(new ByteArrayInputStream(data));
			Serializable result = (Serializable) objectInputStream.readObject();
			objectInputStream.close();

			return result;
		} catch (IOException e) {
			throw new SerializationUtilsException("Deserialization failed! IOException", e);
		} catch (Base64DecodingException e) {
			throw new SerializationUtilsException("Deserialization failed! Base64DecodingException", e);
		} catch (ClassNotFoundException e) {
			throw new SerializationUtilsException("Deserialization failed! ClassNotFoundException", e);
		}
	}

	public static String serializeToString(Serializable object) throws SerializationUtilsException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ObjectOutputStream objectOutputStream;
		try {
			objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
			objectOutputStream.writeObject(object);
			objectOutputStream.close();
		} catch (IOException e) {
			throw new SerializationUtilsException("Serialization failed! IOException", e);
		}

		return new String(Base64.encode(byteArrayOutputStream.toByteArray()));
	}
}