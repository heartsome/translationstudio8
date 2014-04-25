/*
 *  File: JaretTableActionFactory.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.util.action;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.action.Action;

import de.jaret.util.ui.table.JaretTable;

/**
 * Utility ActionFactory for the jaret table, producing actions for some common tasks to accomodate on a jaret table.
 * 
 * @author Peter Kliem
 * @version $Id: JaretTableActionFactory.java,v 1.1 2012-05-07 01:34:39 jason Exp $
 */
public class JaretTableActionFactory {
    /**
     * Constant denoting the configure columns action.
     */
    public static final String ACTION_CONFIGURECOLUMNS = "jarettable.configurecolumns";
    public static final String ACTION_OPTROWHEIGHT = "jarettable.optimizerowheigth";
    public static final String ACTION_OPTALLROWHEIGHTS = "jarettable.optimizeallrowheights";

    protected Map<String, Action> _actionMap;

    public Action createStdAction(JaretTable table, String name) {
        if (_actionMap == null) {
            _actionMap = new HashMap<String, Action>();
        }
        Action result = _actionMap.get(name);
        if (result != null) {
            return result;
        }
        if (name.equals(ACTION_CONFIGURECOLUMNS)) {
            result = new ConfigureColumnsAction(table);
        } else if (name.equals(ACTION_OPTROWHEIGHT)) {
            result = new OptimizeRowHeightAction(table);
        } else if (name.equals(ACTION_OPTALLROWHEIGHTS)) {
            result = new OptimizeAllRowHeightsAction(table);
        } else if (name.equals("s")) {
            result = null;
        }

        if (result != null) {
            _actionMap.put(name, result);
        }
        return result;
    }

}
