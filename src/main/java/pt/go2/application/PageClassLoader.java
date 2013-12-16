package pt.go2.application;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pt.go2.annotations.Injected;
import pt.go2.annotations.Page;
import pt.go2.annotations.Singleton;

public class PageClassLoader {

	private static final Logger LOG = LogManager
			.getLogger(PageClassLoader.class);

	public static void load(final List<String> paths,
			final List<Class<?>> classes) {

		for (final String path : paths) {
			load(path, classes);
		}
	}

	public static void load(final String path, final List<Class<?>> classes) {

		final List<Class<?>> toLoad = ClassEnumerator
				.getClassesForPackage(path);

		for (final Class<?> pageClass : toLoad) {

			if (pageClass.getAnnotation(Page.class) != null) {

				LOG.info("Loadding: " + pageClass.getName());
				classes.add(pageClass);

			} else {
				LOG.warn("Skipping: " + pageClass.getName());
			}
		}
	}

	public static void injectDependencies(final List<Class<?>> pages,
			final List<Object> pageObjs) {

		// keep track of singletons already instantiated

		final Map<Class<?>, Object> singletons = new HashMap<>();

		for (final Class<?> page : pages) {

			// instantiate page

			final Object pageObj;
			try {
				pageObj = page.newInstance();
				pageObjs.add(pageObj);
			} catch (InstantiationException | IllegalAccessException e) {
				LOG.error("Error instantiating page object: " + page);
				continue;
			}

			// instantiate fields on page

			final Field[] fields = page.getDeclaredFields();

			for (final Field f : fields) {

				final Injected inj = f.getAnnotation(Injected.class);
				if (inj == null) {
					continue;
				}

				final Class<?> type = f.getType();

				Object o;
				if (type.getAnnotation(Singleton.class) != null) {

					o = singletons.get(type);
					if (o == null) {
						try {
							o = type.newInstance();
						} catch (InstantiationException
								| IllegalAccessException e) {
							LOG.error("Error instantiating object: " + type);
							continue;
						}
						singletons.put(type, o);
					}
				} else {
					try {
						o = type.newInstance();
					} catch (InstantiationException | IllegalAccessException e) {
						LOG.error("Error instantiating object: " + type);
						continue;
					}
				}

				try {
					f.setAccessible(true);
					f.set(pageObj, o);

				} catch (IllegalArgumentException | IllegalAccessException e) {
					LOG.error("Error seting field on object: " + f);
				}
			}
		}
	}

	private PageClassLoader() {
	}
}
