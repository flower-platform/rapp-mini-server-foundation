package com.flowerplatform.rapp_mini_server.persistent_properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Properties;
import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flowerplatform.rapp_mini_server.StringConversionUtil;

/**
 * @author Cristian Spiescu
 */
public abstract class PersistentPropertiesCapable {

	// static logic
	private static Logger logger = LoggerFactory.getLogger(PersistentPropertiesCapable.class);
	
	protected static String propertiesFilePath;
	protected static Properties properties;
	
	public static void setPropertiesFilePath(String propertiesFilePath) {
		File file = new File(propertiesFilePath);
		logger.debug("Setting propertiesFilePath: '{}'", file.getAbsolutePath());
		// we store the absolute path, in case the current dir will change
		PersistentPropertiesCapable.propertiesFilePath = file.getAbsolutePath();
	}

	protected static Properties getProperties() {
		if (properties == null) {
			if (propertiesFilePath == null) {
				throw new IllegalStateException("propertiesFilePath not set!");
			}
			properties = new Properties();
		}
		return properties;
	}
	
	// instance logic (i.e. non static)
	
	protected PersistentPropertiesCapable parent;
	protected String referencingPropertyFromParent;
	
	public PersistentPropertiesCapable getParent() {
		return parent;
	}

	public String getReferencingPropertyFromParent() {
		return referencingPropertyFromParent;
	}

	public PersistentPropertiesCapable(PersistentPropertiesCapable parent, String referencingPropertyFromParent) {
		super();
		this.parent = parent;
		this.referencingPropertyFromParent = referencingPropertyFromParent;
	}

	protected String getPersistenceKeyForInstance() {
		if (parent == null) {
			return referencingPropertyFromParent == null ? "" : referencingPropertyFromParent;
		} else {
			return parent.getPersistenceKeyForInstance() + "." + referencingPropertyFromParent;
		}
	}
	
	protected void iteratePersistentProperties(BiConsumer<String, Field> callback) {
		String key = getPersistenceKeyForInstance();
		Class<?> current = this.getClass();
		while (current != null && !(current.equals(PersistentPropertiesCapable.class))) {
			logger.trace("Looking for persistent properties in: {}", current);
			for (Field f : current.getDeclaredFields()) {
				if (f.isAnnotationPresent(PersistentProperty.class)) {
					f.setAccessible(true);
					try {
						String k = key + "-" + f.getName();
						callback.accept(k, f);
					} catch (IllegalArgumentException e) {
						throw new RuntimeException(e);
					}
				}
			}
			current = current.getSuperclass();
		}		
	}
	
	public void storePersistentProperties() {
		File file = new File(propertiesFilePath);
		if (logger.isDebugEnabled()) {
			logger.debug("For: {}, storing to: {}", getPersistenceKeyForInstance(), file.getAbsolutePath());
		}

		iteratePersistentProperties((key, field) -> {
			try {
				String v = StringConversionUtil.toString(field.get(this));
				logger.trace("Putting property: '{}' value: '{}'", key, v);
				getProperties().put(key, v);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}			
		});
		
		try (FileOutputStream fos = new FileOutputStream(file)) {
			getProperties().store(fos, null);	
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> T loadPersistentProperties() {
		File file = new File(propertiesFilePath);
		if (logger.isDebugEnabled()) {
			logger.debug("For: {}, loading from: {}", getPersistenceKeyForInstance(), file.getAbsolutePath());
		}
		try (FileInputStream fos = new FileInputStream(file)) {
			getProperties().clear();
			getProperties().load(fos);
		} catch (FileNotFoundException e) {
			logger.debug("File not found");
			return (T) this;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		iteratePersistentProperties((key, field) -> {
			String v = getProperties().getProperty(key);
			if (v != null) {
				Object castedValue = StringConversionUtil.fromString(field.getType(), v);
				logger.trace("Setting property: {} to: {}", key, castedValue);
				try {
					field.set(this, castedValue);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}
		});
		
		return (T) this;
	}
	
}
