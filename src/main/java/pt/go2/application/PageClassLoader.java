package pt.go2.application;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pt.go2.annotations.Page;

public class PageClassLoader {

	private static final Logger LOG = LogManager
			.getLogger(PageClassLoader.class);

	public static void load(final List<String> paths, final List<Class<?>> classes) {

		for (final String path : paths) {
			load(path, classes);
		}
	}

	public static void load(final String path, final List<Class<?>> classes) {

		final ArrayList<Class<?>> toLoad = ClassEnumerator
				.getClassesForPackage(path);

		for (final Class<?> pageClass : toLoad) {

			if (pageClass.getAnnotation(Page.class) != null) {

				LOG.info("Loadding: " + pageClass.getName());
				classes.add(pageClass);
				
			} else {
				LOG.info("Skipping: " + pageClass.getName());
			}
		}
	}

	private PageClassLoader() {
	}
}
