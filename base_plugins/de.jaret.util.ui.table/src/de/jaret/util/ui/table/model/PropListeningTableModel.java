/*
 *  File: PropListeningTableModel.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import de.jaret.util.misc.PropertyObservable;

/**
 * Extension of the DefaultJaretTableModel registering itself as a property change listener on each row.
 * 
 * @author Peter Kliem
 * @version $Id: PropListeningTableModel.java,v 1.1 2012-05-07 01:34:37 jason Exp $
 */
public class PropListeningTableModel extends DefaultJaretTableModel implements PropertyChangeListener {

    /**
     * {@inheritDoc}
     */
    public void addRow(IRow row) {
        super.addRow(row);
        if (row instanceof PropertyObservable) {
            ((PropertyObservable) row).addPropertyChangeListener(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void remRow(IRow row) {
        super.remRow(row);
        if (row instanceof PropertyObservable) {
            ((PropertyObservable) row).removePropertyChangeListener(this);
        }

    }

    /**
     * {@inheritDoc} Evry change will trigger row changed.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() instanceof IRow) {
            fireRowChanged((IRow) evt.getSource());
        }

    }

}
