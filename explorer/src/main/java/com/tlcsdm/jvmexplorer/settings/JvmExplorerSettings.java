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

/**
 * Provides the jvm explorer settings implementation used by the com.tlcsdm.jvmexplorer.settings package.
 */
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

	/**
	 * Loads JVM Explorer settings from the specified file.
	 */
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

	/**
	 * Compares all properties with another JvmExplorerSettings instance.
	 */
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

	/**
	 * Returns all observable properties in this settings instance.
	 */
	private List<Property<?>> properties() {
		return List.of(x, y, width, height, maximized, firstDividerPosition, secondDividerPosition, showClassLoader);
	}

	/**
	 * Handles the configure auto saving workflow.
	 */
	public void configureAutoSaving(File settingsFile) {
		properties().forEach(property -> property.addListener((obs, old, newv) -> save(settingsFile)));
	}

	/**
	 * Handles the save workflow.
	 */
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


	/**
	 * Returns the show class loader value.
	 */
	public SimpleBooleanProperty getShowClassLoader() {
		return this.showClassLoader;
	}

	/**
	 * Returns the first divider position value.
	 */
	public SimpleDoubleProperty getFirstDividerPosition() {
		return this.firstDividerPosition;
	}

	/**
	 * Returns the second divider position value.
	 */
	public SimpleDoubleProperty getSecondDividerPosition() {
		return this.secondDividerPosition;
	}

	/**
	 * Returns the width value.
	 */
	public SimpleDoubleProperty getWidth() {
		return this.width;
	}

	/**
	 * Returns the height value.
	 */
	public SimpleDoubleProperty getHeight() {
		return this.height;
	}

	/**
	 * Returns the maximized value.
	 */
	public SimpleBooleanProperty getMaximized() {
		return this.maximized;
	}

	/**
	 * Returns the x value.
	 */
	public SimpleDoubleProperty getX() {
		return this.x;
	}

	/**
	 * Returns the y value.
	 */
	public SimpleDoubleProperty getY() {
		return this.y;
	}

	/**
	 * Returns a readable description of this instance.
	 */
	@Override
	public String toString() {
		return "JvmExplorerSettings()";
	}

}
