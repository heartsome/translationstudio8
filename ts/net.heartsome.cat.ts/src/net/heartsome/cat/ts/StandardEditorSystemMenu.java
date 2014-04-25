package net.heartsome.cat.ts;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.presentations.SystemMenuClose;
import org.eclipse.ui.internal.presentations.SystemMenuCloseAll;
import org.eclipse.ui.internal.presentations.SystemMenuCloseOthers;
import org.eclipse.ui.internal.presentations.SystemMenuMaximize;
import org.eclipse.ui.internal.presentations.SystemMenuMinimize;
import org.eclipse.ui.internal.presentations.SystemMenuMove;
import org.eclipse.ui.internal.presentations.SystemMenuRestore;
import org.eclipse.ui.internal.presentations.UpdatingActionContributionItem;
import org.eclipse.ui.internal.presentations.util.ISystemMenu;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IStackPresentationSite;

/**
 * 定制编辑器右键菜单
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
@SuppressWarnings("restriction")
public class StandardEditorSystemMenu implements ISystemMenu {

    /* package */ MenuManager menuManager = new MenuManager();
    private SystemMenuRestore restore;
    private SystemMenuMove move; 
    private SystemMenuMinimize minimize;
    private SystemMenuMaximize maximize;
    private SystemMenuClose close;
    
    private SystemMenuCloseOthers closeOthers;
    private SystemMenuCloseAll closeAll;
    
    /**
     * Create the standard view menu
     * 
     * @param site the site to associate the view with
     */
    public StandardEditorSystemMenu(IStackPresentationSite site) {
        restore = new SystemMenuRestore(site);
        move = new SystemMenuMove(site, getMoveMenuText(), false);
        minimize = new SystemMenuMinimize(site);
        maximize = new SystemMenuMaximize(site);
        close = new SystemMenuClose(site);
        closeOthers = new SystemMenuCloseOthers(site);
        closeAll = new SystemMenuCloseAll(site);
        
        { // Initialize system menu
            menuManager.add(new GroupMarker("misc")); //$NON-NLS-1$
            menuManager.add(new GroupMarker("restore")); //$NON-NLS-1$
            menuManager.add(new UpdatingActionContributionItem(restore));

            menuManager.add(move);
            menuManager.add(new GroupMarker("size")); //$NON-NLS-1$
            menuManager.add(new GroupMarker("state")); //$NON-NLS-1$
            menuManager.add(new UpdatingActionContributionItem(minimize));

            menuManager.add(new UpdatingActionContributionItem(maximize));
            menuManager.add(new Separator("close")); //$NON-NLS-1$
            menuManager.add(close);
            menuManager.add(closeOthers);
            menuManager.add(closeAll);

            site.addSystemActions(menuManager);
        } // End of system menu initialization

    }

    String getMoveMenuText() {
    	return WorkbenchMessages.EditorPane_moveEditor;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.ISystemMenu#show(org.eclipse.swt.graphics.Point, org.eclipse.ui.presentations.IPresentablePart)
     */
    public void show(Control parent, Point displayCoordinates, IPresentablePart currentSelection) {
        restore.update();
        move.setTarget(currentSelection);
        move.update();
        minimize.update();
        maximize.update();
        close.setTarget(currentSelection);
        closeOthers.setTarget(currentSelection);
        closeAll.update();
        
        Menu aMenu = menuManager.createContextMenu(parent);
        menuManager.update(true);
        aMenu.setLocation(displayCoordinates.x, displayCoordinates.y);
        aMenu.setVisible(true);
    }
    
    /**
     * Dispose resources associated with this menu
     */
    public void dispose() {
        menuManager.dispose();
        menuManager.removeAll();
    }

}
