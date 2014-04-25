/*
 *  File: DummyRow.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.examples.table;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import org.eclipse.swt.graphics.Image;

import de.jaret.util.misc.PropertyObservableBase;
import de.jaret.util.ui.table.model.IRow;

/**
 * Simple test row.
 * 
 * @author Peter Kliem
 * @version $Id: DummyRow.java,v 1.1 2012-05-07 01:34:39 jason Exp $
 */
public class DummyRow extends PropertyObservableBase implements IRow {
    public static enum TestEnum {
        ENUMVAL1, ENUMVAL2, ENUMVAL3
    };

    private String t1;
    private String t2;
    private String t3;
    private boolean b1;
    private Date d1;
    private Image img;
    private int integer = 0;
    private double adouble = 0.0;
    private TestEnum enumProperty = TestEnum.ENUMVAL1;
    private String x1;

    private int _riskProb = 1;
    private int _riskSeverity = 1;
    private Risk _risk = new Risk(_riskProb, _riskSeverity);

    public class Risk {
        private int _riskProb = 1;
        private int _riskSeverity = 1;

        public Risk(int riskProb, int riskSeverity) {
            _riskProb = riskProb;
            _riskSeverity = riskSeverity;
        }

        /**
         * @return the riskProb
         */
        public int getRiskProb() {
            return _riskProb;
        }

        /**
         * @return the riskSeverity
         */
        public int getRiskSeverity() {
            return _riskSeverity;
        }

        @Override
        public String toString() {
            return "Risk " + _riskProb * _riskSeverity;
        }
    }

    public DummyRow(String t1, String t2, String t3, boolean b1, Date d1) {
        this(t1, t2, t3, b1, d1, null);
    }

    public DummyRow(String t1, String t2, String t3, boolean b1, Date d1, Image img) {
        this.t1 = t1;
        this.t2 = t2;
        this.t3 = t3;
        this.b1 = b1;
        this.d1 = d1;
        this.img = img;
        integer = (int) (Math.random() * 100);
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

    public String getId() {
        return Integer.toString(hashCode());
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

    public String getX1() {
        return x1;
    }

    public void setX1(String x1) {
        this.x1 = x1;
        firePropertyChange("X1", null, x1);
    }

    /**
     * @return Returns the b1.
     */
    public boolean getB1() {
        return b1;
    }

    /**
     * @param b1 The b1 to set.
     */
    public void setB1(boolean b1) {
        this.b1 = b1;
        firePropertyChange("B1", null, b1);
    }

    /**
     * @return Returns the d1.
     */
    public Date getD1() {
        return d1;
    }

    /**
     * @param d1 The d1 to set.
     */
    public void setD1(Date d1) {
        this.d1 = d1;
        firePropertyChange("D1", null, d1);
    }

    /**
     * Setter for d1 trying to parse a string.
     * 
     * @param dateString
     */
    public void setD1(String dateString) {
        Date d = null;
        if (dateString == null || dateString.trim().length() == 0) {
            setD1(d);
            return;
        }
        try {
            DateFormat df = DateFormat.getDateInstance();
            d = df.parse(dateString);
        } catch (ParseException e) {
            // ignore
        }
        if (d == null) {
            try {
                DateFormat df = DateFormat.getDateTimeInstance();
                d = df.parse(dateString);
            } catch (ParseException e) {
                // ignore
            }
        }
        if (d != null) {
            setD1(d);
        } else {
            throw new RuntimeException("could not parse date");
        }
    }

    /**
     * @return Returns the img.
     */
    public Image getImg() {
        return img;
    }

    /**
     * @param img The img to set.
     */
    public void setImg(Image img) {
        this.img = img;
        firePropertyChange("Img", null, img);
    }

    /**
     * @return Returns the adouble.
     */
    public double getAdouble() {
        return adouble;
    }

    /**
     * @param adouble The adouble to set.
     */
    public void setAdouble(double adouble) {
        this.adouble = adouble;
        firePropertyChange("Adouble", null, adouble);
    }

    /**
     * @return Returns the integer.
     */
    public int getInteger() {
        return integer;
    }

    /**
     * @param integer The integer to set.
     */
    public void setInteger(int integer) {
        this.integer = integer;
        firePropertyChange("Integer", null, integer);
    }

    /**
     * @return the enumProperty
     */
    public TestEnum getEnumProperty() {
        return enumProperty;
    }

    /**
     * @param enumProperty the enumProperty to set
     */
    public void setEnumProperty(TestEnum enumProperty) {
        this.enumProperty = enumProperty;
        firePropertyChange("EnumProperty", null, enumProperty);
    }

    /**
     * @return the risk
     */
    public Risk getRisk() {
        return _risk;
    }

    /**
     * @param risk the risk to set
     */
    public void setRisk(Risk risk) {
        _risk = risk;
        firePropertyChange("Risk", null, risk);
        setRiskProb(risk.getRiskProb());
        setRiskSeverity(risk.getRiskSeverity());
    }

    /**
     * @return the riskProb
     */
    public int getRiskProb() {
        return _riskProb;
    }

    /**
     * @param riskProb the riskProb to set
     */
    public void setRiskProb(int riskProb) {
        if (riskProb != _riskProb) {
            _riskProb = riskProb;
            firePropertyChange("RiskProb", null, riskProb);
            setRisk(new Risk(_riskProb, _riskSeverity));
        }
    }

    /**
     * @return the riskSeverity
     */
    public int getRiskSeverity() {
        return _riskSeverity;
    }

    /**
     * @param riskSeverity the riskSeverity to set
     */
    public void setRiskSeverity(int riskSeverity) {
        if (riskSeverity != _riskSeverity) {
            _riskSeverity = riskSeverity;
            firePropertyChange("RiskSeverity", null, riskSeverity);
            setRisk(new Risk(_riskProb, _riskSeverity));
        }
    }
}
