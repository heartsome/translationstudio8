package net.sourceforge.nattable.style;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

public interface CellStyleAttributes {
    
    public static final ConfigAttribute<Color> BACKGROUND_COLOR = new ConfigAttribute<Color>();
    
    public static final ConfigAttribute<Color> FOREGROUND_COLOR = new ConfigAttribute<Color>();
    
    public static final ConfigAttribute<HorizontalAlignmentEnum> HORIZONTAL_ALIGNMENT = new ConfigAttribute<HorizontalAlignmentEnum>();
    
    public static final ConfigAttribute<VerticalAlignmentEnum> VERTICAL_ALIGNMENT = new ConfigAttribute<VerticalAlignmentEnum>();
    
    public static final ConfigAttribute<Font> FONT = new ConfigAttribute<Font>();
    
    public static final ConfigAttribute<Image> IMAGE = new ConfigAttribute<Image>();
    
    public static final ConfigAttribute<BorderStyle> BORDER_STYLE = new ConfigAttribute<BorderStyle>();

}
