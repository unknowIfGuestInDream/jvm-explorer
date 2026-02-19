package com.tlcsdm.jvmexplorer.preferences;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Locale;
import java.util.prefs.Preferences;

public class AppPreferences {

	private static final String LANGUAGE_KEY = "language";
	private static final String THEME_KEY = "theme";
	private static final String DEFAULT_THEME = "Primer Light";
	private static final Preferences prefs = Preferences.userNodeForPackage(AppPreferences.class);

	private static final AppPreferences INSTANCE = new AppPreferences();

	private final StringProperty language = new SimpleStringProperty();
	private final StringProperty theme = new SimpleStringProperty();

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

	private static String getDefaultLanguage() {
		String sysLang = Locale.getDefault().getLanguage();
		return switch (sysLang) {
			case "zh", "ja" -> sysLang;
			default -> "en";
		};
	}

	public static AppPreferences getInstance() {
		return INSTANCE;
	}

	public String getLanguage() {
		return language.get();
	}

	public void setLanguage(String language) {
		this.language.set(language);
	}

	public StringProperty languageProperty() {
		return language;
	}

	public Locale getLocale() {
		String lang = getLanguage();
		return switch (lang) {
			case "zh" -> Locale.forLanguageTag("zh");
			case "ja" -> Locale.forLanguageTag("ja");
			default -> Locale.forLanguageTag("en");
		};
	}

	public String getTheme() {
		return theme.get();
	}

	public void setTheme(String theme) {
		this.theme.set(theme);
	}

	public StringProperty themeProperty() {
		return theme;
	}

}
