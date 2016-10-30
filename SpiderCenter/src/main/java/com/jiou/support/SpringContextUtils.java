package com.jiou.support;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.stereotype.Component;

/**
 * @author zhe.li
 */
@Component
@Scope("singleton")
public class SpringContextUtils implements ApplicationContextAware {
	private static ApplicationContext context;

	/**
	 * Load spring configuration from classpath or filesystem.
	 * 
	 * @param configLocations
	 */
	public static void load(String... configLocations) {
		try {
			context = new ClassPathXmlApplicationContext(configLocations);
		} catch (Exception e) {
			context = new FileSystemXmlApplicationContext(configLocations);
		}
	}

	/**
	 * Get bean by specific type
	 * 
	 * @param requiredType
	 * @return
	 */
	public static <T> T getBean(Class<T> requiredType) {
		return context.getBean(requiredType);
	}

	/**
	 * Get bean by name and specific type
	 * 
	 * @param name
	 *            the name of the bean
	 * @param requiredType
	 * @return
	 */
	public static <T> T getBean(String name, Class<T> requiredType) {
		return context.getBean(name, requiredType);
	}

	/**
	 * Get all beans by specific type
	 * 
	 * @param requiredType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] getBeans(Class<T> requiredType) {
		Collection<T> collection = context.getBeansOfType(requiredType).values();

		if (collection.size() > 0) {
			T[] arr = (T[]) Array.newInstance(requiredType, collection.size());

			int x = 0;
			Iterator<T> it = collection.iterator();
			while (it.hasNext()) {
				arr[x++] = it.next();
			}

			return arr;
		}

		return null;
	}

	/**
	 * Get ApplicationContext
	 * 
	 * @return
	 */
	public static ApplicationContext getContext() {
		return context;
	}

	/**
	 * Set ApplicationContext
	 */
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		context = applicationContext;
	}

}
