package net.sourceforge.nattable.persistence;

import static net.sourceforge.nattable.persistence.IPersistable.DOT;
import static net.sourceforge.nattable.style.CellStyleAttributes.BACKGROUND_COLOR;
import static net.sourceforge.nattable.style.CellStyleAttributes.BORDER_STYLE;
import static net.sourceforge.nattable.style.CellStyleAttributes.FONT;
import static net.sourceforge.nattable.style.CellStyleAttributes.FOREGROUND_COLOR;
import static net.sourceforge.nattable.style.CellStyleAttributes.HORIZONTAL_ALIGNMENT;
import static net.sourceforge.nattable.style.CellStyleAttributes.VERTICAL_ALIGNMENT;

import java.util.Properties;

import net.sourceforge.nattable.style.BorderStyle;
import net.sourceforge.nattable.style.CellStyleAttributes;
import net.sourceforge.nattable.style.HorizontalAlignmentEnum;
import net.sourceforge.nattable.style.Style;
import net.sourceforge.nattable.style.VerticalAlignmentEnum;
import net.sourceforge.nattable.util.GUIHelper;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

/**
 * Saves and loads the following components of a style to a properties object.
 * 	- Foreground color
 * 	- Background color
 * 	- Horizontal alignment
 * 	- Vertical alignment
 * 	- Font
 * 	- Border style
 */
public class StylePersistor {

	// Style prefix constants
	public static final String STYLE_PERSISTENCE_PREFIX = "style";
	public static final String BLUE_COLOR_PREFIX = "blue";
	public static final String GREEN_COLOR_PREFIX = "green";
	public static final String RED_COLOR_PREFIX = "red";
	public static final String V_ALIGNMENT_PREFIX = "verticalAlignment";
	public static final String H_ALIGNMENT_PREFIX = "horizontalAlignment";
	public static final String BG_COLOR_PREFIX = "bg";
	public static final String FG_COLOR_PREFIX = "fg";
	public static final String FONT_PREFIX = "font";
	public static final String BORDER_PREFIX = "border";

	// Save

	public static void saveStyle(String prefix, Properties properties, Style style) {
		prefix = prefix + DOT + STYLE_PERSISTENCE_PREFIX;

		saveColor(prefix + DOT + BG_COLOR_PREFIX, properties, style.getAttributeValue(BACKGROUND_COLOR));
		saveColor(prefix + DOT + FG_COLOR_PREFIX, properties, style.getAttributeValue(FOREGROUND_COLOR));

		saveHAlign(prefix, properties, style.getAttributeValue(HORIZONTAL_ALIGNMENT));
		saveVAlign(prefix, properties, style.getAttributeValue(VERTICAL_ALIGNMENT));

		saveFont(prefix, properties, style.getAttributeValue(FONT));

		saveBorder(prefix, properties, style.getAttributeValue(BORDER_STYLE));
	}

	protected static void saveVAlign(String prefix, Properties properties, VerticalAlignmentEnum vAlign) {
		if (vAlign == null) {
			return;
		}
		properties.setProperty(prefix + DOT + V_ALIGNMENT_PREFIX, vAlign.name());
	}

	protected static void saveHAlign(String prefix, Properties properties, HorizontalAlignmentEnum hAlign) {
		if (hAlign == null) {
			return;
		}
		properties.setProperty(prefix + DOT + H_ALIGNMENT_PREFIX, hAlign.name());
	}

	protected static void saveBorder(String prefix, Properties properties, BorderStyle borderStyle) {
		if (borderStyle == null) {
			return;
		}
		properties.setProperty(prefix + DOT + BORDER_PREFIX, String.valueOf(borderStyle.toString()));
	}

	protected static void saveFont(String prefix, Properties properties, Font font) {
		if (font == null) {
			return;
		}
		properties.setProperty(prefix + DOT + FONT_PREFIX, String.valueOf(font.getFontData()[0].toString()));
	}

	protected static void saveColor(String prefix, Properties properties, Color color) {
		if (color == null) {
			return;
		}
		ColorPersistor.saveColor(prefix, properties, color);
	}

	// Load

	public static Style loadStyle(String prefix, Properties properties) {
		Style style = new Style();
		prefix = prefix + DOT + STYLE_PERSISTENCE_PREFIX;

		// BG Color
		String bgColorPrefix = prefix + DOT + BG_COLOR_PREFIX;
		Color bgColor = loadColor(bgColorPrefix, properties);
		if (bgColor != null) {
			style.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, bgColor);
		}

		// FG Color
		String fgColorPrefix = prefix + DOT + FG_COLOR_PREFIX;
		Color fgColor = loadColor(fgColorPrefix, properties);
		if (fgColor != null) {
			style.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, fgColor);
		}

		// Alignment
		String hAlignPrefix = prefix + DOT + H_ALIGNMENT_PREFIX;
		HorizontalAlignmentEnum hAlign = loadHAlignment(hAlignPrefix, properties);
		if (hAlign != null) {
			style.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, hAlign);
		}

		String vAlignPrefix = prefix + DOT + V_ALIGNMENT_PREFIX;
		VerticalAlignmentEnum vAlign = loadVAlignment(vAlignPrefix, properties);
		if (vAlign != null) {
			style.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, vAlign);
		}

		// Font
		String fontPrefix = prefix + DOT + FONT_PREFIX;
		Font font = loadFont(fontPrefix, properties);
		if (font != null) {
			style.setAttributeValue(CellStyleAttributes.FONT, font);
		}

		// Border Style
		String borderPrefix = prefix + DOT + BORDER_PREFIX;
		BorderStyle borderStyle = loadBorderStyle(borderPrefix, properties);
		if (borderStyle != null) {
			style.setAttributeValue(CellStyleAttributes.BORDER_STYLE, borderStyle);
		}

		return style;
	}

	private static BorderStyle loadBorderStyle(String borderPrefix, Properties properties) {
		String borderStyle = properties.getProperty(borderPrefix);
		if (borderStyle != null) {
			return new BorderStyle(borderStyle);
		}
		return null;
	}

	private static Font loadFont(String fontPrefix, Properties properties) {
		String fontdata = properties.getProperty(fontPrefix);
		if (fontdata != null) {
			return GUIHelper.getFont(new FontData(fontdata));
		}
		return null;
	}

	private static HorizontalAlignmentEnum loadHAlignment(String hAlignPrefix, Properties properties) {
		String enumName = properties.getProperty(hAlignPrefix);
		if (enumName != null) {
			return HorizontalAlignmentEnum.valueOf(enumName);
		}
		return null;
	}

	private static VerticalAlignmentEnum loadVAlignment(String vAlignPrefix, Properties properties) {
		String enumName = properties.getProperty(vAlignPrefix);
		if (enumName != null) {
			return VerticalAlignmentEnum.valueOf(enumName);
		}
		return null;
	}

	protected static Color loadColor(String prefix, Properties properties) {
		return ColorPersistor.loadColor(prefix, properties);
	}
}
