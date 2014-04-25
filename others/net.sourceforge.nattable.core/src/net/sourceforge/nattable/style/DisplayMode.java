package net.sourceforge.nattable.style;

/**
 * The various modes the table can be under.
 * <ol>
 *    <li>During normal display a cell is in NORMAL mode.</li>
 *    <li>If the contents of the cell are being edited, its in EDIT mode.</li>
 *    <li>If a cell has been selected, its in SELECT mode.</li>
 * </ol>
 * <br/>
 * These modes are used to bind different settings to different modes.<br/>
 * For example, a different style can be registered for a cell
 * when it is in SELECT mode.
 *
 */
public interface DisplayMode {

	public static final String NORMAL = "NORMAL";
	public static final String SELECT = "SELECT";
	public static final String EDIT = "EDIT";
	
}
