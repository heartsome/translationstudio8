/*
 *  File: DummyTableNode.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.examples.table;

import de.jaret.util.ui.table.model.AbstractTableNode;

/**
 * Dummy table node class for test and demonstarting purposes.
 * 
 * @author Peter Kliem
 * @version $Id: DummyTableNode.java,v 1.1 2012-05-07 01:34:39 jason Exp $
 */
public class DummyTableNode extends AbstractTableNode {
    protected String _id;

    private String t1;
    private String t2;
    private String t3;
    private boolean b1;

    public DummyTableNode(String id, String t1, String t2, String t3) {
        _id = id;
        this.t1 = t1;
        this.t2 = t2;
        this.t3 = t3;
    }

    public String getId() {
        return _id;
    }

    /**
     * @return Returns the t1.
     */
    public String getT1() {
        return t1;
    }

    /**
     * @param t1 The t1 to set.
     */
    public void setT1(String t1) {
        this.t1 = t1;
        firePropertyChange("T1", null, t1);
    }

    /**
     * @return Returns the t2.
     */
    public String getT2() {
        return t2;
    }

    /**
     * @param t2 The t2 to set.
     */
    public void setT2(String t2) {
        this.t2 = t2;
        firePropertyChange("T2", null, t2);
    }

    /**
     * @return Returns the t3.
     */
    public String getT3() {
        return t3;
    }

    /**
     * @param t3 The t3 to set.
     */
    public void setT3(String t3) {
        this.t3 = t3;
        firePropertyChange("T3", null, t3);
    }

    /**
     * @return the b1
     */
    public boolean getB1() {
        return b1;
    }

    /**
     * @param b1 the b1 to set
     */
    public void setB1(boolean b1) {
        this.b1 = b1;
        firePropertyChange("B3", null, b1);
    }

}
