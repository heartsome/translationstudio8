/*
 *  File: PropCol.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.model;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Column implementation for the jaret table using reflection (getPropName, setPropName) to retrieve the column value.
 * Does not support listening for property changes on the underlying POJO (But will fire a valueChanged if the model is
 * changed by setValue). Supports property paths and an optional accessor (IPropColAccessor).
 * 
 * @todo error handling is NOT implemented correct
 * 
 * @author Peter Kliem
 * @version $Id: PropCol.java,v 1.1 2012-05-07 01:34:37 jason Exp $
 */
public class PropCol extends AbstractColumn implements IColumn {
    /** id string. */
    protected String _id;

    /** header label string. */
    protected String _headerLabel;

    /** name of the mapped property (or path separeted by dots). */
    protected String _prop;

    /** array of property names parsed from the property. */
    protected String[] _propPath;

    /** true if the col should be able to sort the table. */
    protected boolean _supportSorting = true;

    /** true if the column is editable. */
    protected boolean _editable = true;

    /** content class if specified directly. */
    protected Class<?> _contentClass;

    /** optional accessor. */
    protected IPropColAccessor _accessor = null;

    /**
     * Construct the column.
     * 
     * @param id id of the column
     * @param label header label
     * @param prop property name (usually starting with a capital letter or path)
     * @param contentClass class of the objects held by the column
     * @param accessor optinal accessor to be invoked on the property to retrive/set the value
     */
    public PropCol(String id, String label, String prop, Class<?> contentClass, IPropColAccessor accessor) {
        super();
        _headerLabel = label;
        _id = id;
        _prop = prop;
        initPropPath();
        _contentClass = contentClass;
        _accessor = accessor;
    }

    /**
     * Construct the column.
     * 
     * @param id id of the column
     * @param label header label
     * @param prop property name (usually starting with a capital letter or path)
     * @param contentClass class of the objects held by the column
     */
    public PropCol(String id, String label, String prop, Class<?> contentClass) {
        this(id, label, prop, contentClass, null);
    }

    /**
     * Construct the column.
     * 
     * @param id id of the column
     * @param label header label
     * @param prop property name (usually starting with a capital letter or path)
     */
    public PropCol(String id, String label, String prop) {
        this(id, label, prop, null);
    }

    /**
     * Parse _prop as dot separted string to the array of property names forming an access path to the property.
     */
    private void initPropPath() {
        StringTokenizer tokenizer = new StringTokenizer(_prop, ".");
        List<String> l = new ArrayList<String>();
        while (tokenizer.hasMoreTokens()) {
            l.add(tokenizer.nextToken());
        }
        _propPath = l.toArray(new String[0]);
    }

    /**
     * {@inheritDoc}
     */
    public String getId() {
        return _id;
    }

    /**
     * {@inheritDoc}
     */
    public String getHeaderLabel() {
        return _headerLabel;
    }

    /**
     * {@inheritDoc}
     */
    public Object getValue(IRow row) {
        if (row != null) {
            try {
                Object base = row;
                for (int i = 0; i < _propPath.length; i++) {
                    String propName = _propPath[i];
                    Method getter = base.getClass().getMethod("get" + propName, new Class[] {});
                    base = getter.invoke(base, new Object[] {});
                }
                if (_accessor == null) {
                    return base;
                } else {
                    return _accessor.getValue(base);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Class<?> getContentClass(IRow row) {
        if (row != null) {
            try {
                Object base = row;
                for (int i = 0; i < _propPath.length; i++) {
                    String propName = _propPath[i];
                    Method getter = base.getClass().getMethod("get" + propName, new Class[] {});
                    if (i == _propPath.length - 1) {
                        return getter.getReturnType();
                    } else {
                        base = getter.invoke(base, new Object[] {});
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Check whether there is a real modification between two (possible null) objects.
     * 
     * @param o1 object1
     * @param o2 object 2
     * @return true if a real modification has been detected
     */
    protected boolean isRealModification(Object o1, Object o2) {
        if (o1 == null && o2 == null) {
            return false;
        }
        if (o1 != null && o2 == null) {
            return true;
        }
        if (o2 != null && o1 == null) {
            return true;
        }
        return !o1.equals(o2);
    }

    /**
     * {@inheritDoc}
     */
    public void setValue(IRow row, Object value) {
        Object oldValue = getValue(row);
        if (isRealModification(oldValue, value)) {
            try {
                Object base = row;
                for (int i = 0; i < _propPath.length - 1; i++) {
                    String propName = _propPath[i];
                    Method getter = base.getClass().getMethod("get" + propName, new Class[] {});
                    base = getter.invoke(base, new Object[] {});
                }
                if (_accessor == null) {
                    Class<?> clazz;
                    if (value == null) {
                        clazz = getContentClass(row);
                    } else {
                        clazz = value.getClass();
                        if (clazz.equals(Boolean.class)) {
                            clazz = Boolean.TYPE;
                        } else if (clazz.equals(Integer.class)) {
                            clazz = Integer.TYPE;
                        } else if (clazz.equals(Double.class)) {
                            clazz = Double.TYPE;
                        }
                    }
                    Method setter = base.getClass().getMethod("set" + _propPath[_propPath.length - 1],
                            new Class[] {clazz});
                    setter.invoke(base, new Object[] {value});
                } else {
                    _accessor.setValue(base, value);
                }
                fireValueChanged(row, this, oldValue, value);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Could not set value " + e.getLocalizedMessage());
            }
        }
    }

    /**
     * {@inheritDoc} Sorting default behaviour: compare <code>toString()</code>.
     */
    @SuppressWarnings("unchecked")
    public int compare(IRow r1, IRow r2) {
        Object val1 = getValue(r1);
        Object val2 = getValue(r2);
        if (val1 == null && val2 == null) {
            return 0;
        }
        if (val1 == null) {
            return -1;
        }
        if (val2 == null) {
            return 1;
        }
        // check for comparable types
        if (val1.getClass().equals(val2.getClass())) {
            if (val1 instanceof Comparable) {
                return ((Comparable) val1).compareTo(val2);
            }
        }
        return val1.toString().compareTo(val2.toString());
    }

    /**
     * {@inheritDoc}
     */
    public boolean supportsSorting() {
        return _supportSorting;
    }

    /**
     * Set whether the column should support sorting.
     * 
     * @param supportSorting true if sorting should be supported
     */
    public void setSupportSorting(boolean supportSorting) {
        _supportSorting = supportSorting;
    }

    /**
     * {@inheritDoc}
     */
    public Class<?> getContentClass() {
        return _contentClass;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEditable() {
        return _editable;
    }

    /**
     * Set whether the column should be editable.
     * 
     * @param editable true for allow edit
     */
    public void setEditable(boolean editable) {
        _editable = editable;
    }
}
