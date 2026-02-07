package com.tlcsdm.jvmexplorer.settings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.tlcsdm.jvmexplorer.JvmExplorer;
import com.google.gson.Gson;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.hildan.fxgson.FxGson;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.List;
import java.util.Objects;

public class JvmExplorerSettings {

	private static final Logger log = LoggerFactory.getLogger(JvmExplorerSettings.class);


	public static final File DEFAULT_SETTINGS_FILE = new File(JvmExplorer.APP_DIR, "settings.json");

	private static final Gson GSON = FxGson.coreBuilder()
	                                       .serializeSpecialFloatingPointValues()
	                                       .setPrettyPrinting()
	                                       .create();

	private final SimpleBooleanProperty showClassLoader = new SimpleBooleanProperty(false);

	private final SimpleDoubleProperty firstDividerPosition = new SimpleDoubleProperty(0.23);
	private final SimpleDoubleProperty secondDividerPosition = new SimpleDoubleProperty(0.5);

	private final SimpleDoubleProperty width = new SimpleDoubleProperty(Double.NaN);
	private final SimpleDoubleProperty height = new SimpleDoubleProperty(Double.NaN);

	private final SimpleBooleanProperty maximized = new SimpleBooleanProperty(false);

	private final SimpleDoubleProperty x = new SimpleDoubleProperty(Double.NaN);
	private final SimpleDoubleProperty y = new SimpleDoubleProperty(Double.NaN);

	public static JvmExplorerSettings load(File settingsFile) {
		try {
			final String settingsFileContent = Files.readString(settingsFile.toPath());
			final JvmExplorerSettings settings = GSON.fromJson(settingsFileContent, JvmExplorerSettings.class);
			if (settings == null) {
				return new JvmExplorerSettings();
			}
			return settings;
		}
		catch (FileNotFoundException | NoSuchFileException e) {
			return new JvmExplorerSettings();
		}
		catch (Exception e) {
			log.warn("Failed to load initial settings", e);
			return new JvmExplorerSettings();
		}
	}

	public boolean propertiesEquals(JvmExplorerSettings other) {
		final List<Property<?>> ourProperties = properties();
		final List<Property<?>> otherProperties = other.properties();
		if (ourProperties.size() != otherProperties.size()) {
			return false;
		}
		for (int i = 0; i < ourProperties.size(); i++) {
			final Object ourProperty = ourProperties.get(i).getValue();
			final Object otherProperty = otherProperties.get(i).getValue();
			if (!Objects.equals(ourProperty, otherProperty)) {
				return false;
			}
		}
		return true;
	}

	private List<Property<?>> properties() {
		return List.of(x, y, width, height, maximized, firstDividerPosition, secondDividerPosition, showClassLoader);
	}

	public void configureAutoSaving(File settingsFile) {
		properties().forEach(property -> property.addListener((obs, old, newv) -> save(settingsFile)));
	}

	public void save(File settingsFile) {
		try {
			final File parent = settingsFile.getParentFile();
			if (parent != null) {
				parent.mkdirs();
			}
			final String settingsFileContent = GSON.toJson(this);
			Files.writeString(settingsFile.toPath(), settingsFileContent);
		}
		catch (Exception e) {
			log.warn("Failed to save settings", e);
		}
	}


	public JvmExplorerSettings(static final Gson GSON, SimpleBooleanProperty showClassLoader, SimpleDoubleProperty firstDividerPosition, SimpleDoubleProperty secondDividerPosition, SimpleDoubleProperty width, SimpleDoubleProperty height, SimpleBooleanProperty maximized, SimpleDoubleProperty x, SimpleDoubleProperty y) {
		this.GSON = GSON;
		this.showClassLoader = showClassLoader;
		this.firstDividerPosition = firstDividerPosition;
		this.secondDividerPosition = secondDividerPosition;
		this.width = width;
		this.height = height;
		this.maximized = maximized;
		this.x = x;
		this.y = y;
	}


	public JvmExplorerSettings() {
	}

	public static final Gson getGSON() {
		return this.GSON;
	}

	public SimpleBooleanProperty getShowClassLoader() {
		return this.showClassLoader;
	}

	public SimpleDoubleProperty getFirstDividerPosition() {
		return this.firstDividerPosition;
	}

	public SimpleDoubleProperty getSecondDividerPosition() {
		return this.secondDividerPosition;
	}

	public SimpleDoubleProperty getWidth() {
		return this.width;
	}

	public SimpleDoubleProperty getHeight() {
		return this.height;
	}

	public SimpleBooleanProperty getMaximized() {
		return this.maximized;
	}

	public SimpleDoubleProperty getX() {
		return this.x;
	}

	public SimpleDoubleProperty getY() {
		return this.y;
	}


	@Override
	public String toString() {
		return "JvmExplorerSettings(" + "GSON=" + GSON + ", showClassLoader=" + showClassLoader + ", firstDividerPosition=" + firstDividerPosition + ", secondDividerPosition=" + secondDividerPosition + ", width=" + width + ", height=" + height + ", maximized=" + maximized + ", x=" + x + ", y=" + y" + ")";
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		JvmExplorerSettings other = (JvmExplorerSettings) o;
		return java.util.Objects.equals(this.GSON, other.GSON) && java.util.Objects.equals(this.showClassLoader, other.showClassLoader) && java.util.Objects.equals(this.firstDividerPosition, other.firstDividerPosition) && java.util.Objects.equals(this.secondDividerPosition, other.secondDividerPosition) && java.util.Objects.equals(this.width, other.width) && java.util.Objects.equals(this.height, other.height) && java.util.Objects.equals(this.maximized, other.maximized) && java.util.Objects.equals(this.x, other.x) && java.util.Objects.equals(this.y, other.y);
	}


	@Override
	public int hashCode() {
		return java.util.Objects.hash(GSON, showClassLoader, firstDividerPosition, secondDividerPosition, width, height, maximized, x, y);
	}

}
