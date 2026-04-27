package com.tlcsdm.jvmexplorer.preferences;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Locale;
import java.util.prefs.Preferences;

/**
 * Provides the app preferences implementation used by the com.tlcsdm.jvmexplorer.preferences package.
 */
public class AppPreferences {

	private static final String LANGUAGE_KEY = "language";
	private static final String THEME_KEY = "theme";
	private static final String DEFAULT_THEME = "Primer Light";
	private static final Preferences prefs = Preferences.userNodeForPackage(AppPreferences.class);

	private static final AppPreferences INSTANCE = new AppPreferences();

	private final StringProperty language = new SimpleStringProperty();
	private final StringProperty theme = new SimpleStringProperty();

	/**
	 * Creates a new AppPreferences instance.
	 */
	private AppPreferences() {
		language.set(prefs.get(LANGUAGE_KEY, getDefaultLanguage()));
		theme.set(prefs.get(THEME_KEY, DEFAULT_THEME));

		language.addListener((obs, oldVal, newVal) -> {
			if (newVal != null) {
				prefs.put(LANGUAGE_KEY, newVal);
			}
		});

		theme.addListener((obs, oldVal, newVal) -> {
			if (newVal != null) {
				prefs.put(THEME_KEY, newVal);
			}
		});
	}

	/**
	 * Returns the default language value.
	 */
	private static String getDefaultLanguage() {
		String sysLang = Locale.getDefault().getLanguage();
		return switch (sysLang) {
			case "zh", "ja" -> sysLang;
			default -> "en";
		};
	}

	/**
	 * Returns the instance value.
	 */
	public static AppPreferences getInstance() {
		return INSTANCE;
	}

	/**
	 * Returns the language value.
	 */
	public String getLanguage() {
		return language.get();
	}

	/**
	 * Updates the language value.
	 */
	public void setLanguage(String language) {
		this.language.set(language);
	}

	/**
	 * Performs the language property operation.
	 */
	public StringProperty languageProperty() {
		return language;
	}

	/**
	 * Returns the locale value.
	 */
	public Locale getLocale() {
		String lang = getLanguage();
		return switch (lang) {
			case "zh" -> Locale.forLanguageTag("zh");
			case "ja" -> Locale.forLanguageTag("ja");
			default -> Locale.forLanguageTag("en");
		};
	}

	/**
	 * Returns the theme value.
	 */
	public String getTheme() {
		return theme.get();
	}

	/**
	 * Updates the theme value.
	 */
	public void setTheme(String theme) {
		this.theme.set(theme);
	}

	/**
	 * Performs the theme property operation.
	 */
	public StringProperty themeProperty() {
		return theme;
	}

}
