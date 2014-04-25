/* 
 * Copyright (C) 2002-2012 XimpleWare, info@ximpleware.com
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package com.ximpleware.extended;

import com.ximpleware.extended.xpath.*;

public class UnionExpr extends Expr {
    public intHash ih;

    public Expr e;

    public UnionExpr next;

    UnionExpr current;

    int state;

    public UnionExpr(Expr e1) {
        e = e1;
        next = null;
        current = this;
        ih = null;
        state = 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ximpleware.xpath.Expr#evalBoolean(com.ximpleware.VTDNav)
     */
    public boolean evalBoolean(VTDNavHuge vn) {
        if (e.isNodeSet()==false)
            return e.evalBoolean(vn);
        else {
            boolean a = false;
            vn.push2();
            // record teh stack size
            int size = vn.contextStack2.size;
            try {
                a = (evalNodeSet(vn) != -1);
            } catch (Exception e) {
            }
            //rewind stack
            vn.contextStack2.size = size;
            reset(vn);
            vn.pop2();
            return a;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ximpleware.xpath.Expr#evalNumber(com.ximpleware.VTDNav)
     */
    public double evalNumber(VTDNavHuge vn) {
        if (e.isNodeSet()==false)
            return e.evalNumber(vn);
        int a = -1;
        vn.push2();
        int size = vn.contextStack2.size;
        try {
            a = evalNodeSet(vn);
            if (a != -1) {
                if (vn.getTokenType(a) == VTDNavHuge.TOKEN_ATTR_NAME) {
                    a++;
                } else if (vn.getTokenType(a) == VTDNavHuge.TOKEN_STARTING_TAG) {
                    a = vn.getText();
                }
            }
        } catch (Exception e) {

        }
        vn.contextStack2.size = size;
        reset(vn);
        vn.pop2();
        try {
            if (a != -1)
                return vn.parseDouble(a);
        } catch (NavExceptionHuge e) {
        }
        return Double.NaN;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ximpleware.xpath.Expr#evalNodeSet(com.ximpleware.VTDNav)
     */
    public int evalNodeSet(VTDNavHuge vn) throws XPathEvalExceptionHuge, NavExceptionHuge {
        int a;
        if (this.next == null) {
            return e.evalNodeSet(vn);
        } else {
            while (true) {
                switch (state) {
                case 0:
                    if (ih==null) 
                        ih = new intHash();
                    if (current != null) {
                        vn.push2();
                        while ((a = current.e.evalNodeSet(vn)) != -1) {
                            if (isUnique(a)) {
                                state = 1;
                                return a;
                            }
                        }
                        state = 2;
                        vn.pop2();
                        break;
                    } else
                        state = 3;
                    break;

                case 1:
                    while ((a = current.e.evalNodeSet(vn)) != -1) {
                        if (isUnique(a)) {
                            state = 1;
                            return a;
                        }
                    }
                    state = 2;
                    vn.pop2();
                    break;

                case 2:
                    current = current.next;
                    if (current != null) {
                        vn.push2();
                        while ((a = current.e.evalNodeSet(vn)) != -1) {
                            if (isUnique(a)) {
                                state = 1;
                                return a;
                            }
                        }
                        vn.pop2();
                        break;
                    } else
                        state = 3;
                    break;

                case 3:
                    return -1;

                default:
                    throw new XPathEvalExceptionHuge(
                            "Invalid state evaluating UnionExpr");
                }
            }
        }

        /*
         * default: throw new XPathEvalException( "Invalid state evaluating
         * PathExpr");
         */
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ximpleware.xpath.Expr#evalString(com.ximpleware.VTDNav)
     */
    public String evalString(VTDNavHuge vn) {
        if (e.isNodeSet()==false){
            return e.evalString(vn);
        }
        vn.push2();
        int size = vn.contextStack2.size;
        int a = -1;
        try {
            a = evalNodeSet(vn);
            if (a != -1) {
                if (vn.getTokenType(a) == VTDNavHuge.TOKEN_ATTR_NAME) {
                    a++;
                }
                if (vn.getTokenType(a) == VTDNavHuge.TOKEN_STARTING_TAG) {
                    a = vn.getText();
                }
            }
        } catch (Exception e) {
        }
        vn.contextStack2.size = size;
        reset(vn);
        vn.pop2();
        try {
            if (a != -1)
                return vn.toString(a);
        } catch (NavExceptionHuge e) {
        }
        return "";
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ximpleware.xpath.Expr#reset(com.ximpleware.VTDNav)
     */
    public void reset(VTDNavHuge vn) {
        // travese el list and reset every expression
        e.reset(vn);
        current = this;
        UnionExpr tmp = this.next;
        while (tmp != null) {
            tmp.e.reset(vn);
            tmp = tmp.next;
        }
        if (ih != null)
            ih.reset();
        state = 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        // TODO Auto-generated method stub

        if (this.next == null)
            return this.e.toString();
        else
            return this.e.toString() + " | " + this.next.toString();

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ximpleware.xpath.Expr#isNumerical()
     */
    public boolean isNumerical() {
        // TODO Auto-generated method stub
        return e.isNumerical();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ximpleware.xpath.Expr#isNodeSet()
     */
    public boolean isNodeSet() {
        // TODO Auto-generated method stub
        return e.isNodeSet();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ximpleware.xpath.Expr#isString()
     */
    public boolean isString() {
        // TODO Auto-generated method stub
        return e.isString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ximpleware.xpath.Expr#isBoolean()
     */
    public boolean isBoolean() {
        // TODO Auto-generated method stub
        return e.isBoolean();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ximpleware.xpath.Expr#requireContextSize()
     */
    public boolean requireContextSize() {
        // boolean b = false;
        UnionExpr tmp = this;
        while (tmp != null) {
            if (tmp.e.requireContextSize()== true){
                return true;
            }
            tmp = tmp.next;
        }        
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ximpleware.xpath.Expr#setContextSize(int)
     */
    public void setContextSize(int size) {
        current = this;
        current.e.setContextSize(size);
        UnionExpr tmp = this.next;
        while (tmp != null) {
            tmp.e.setContextSize(size);
            tmp = tmp.next;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ximpleware.xpath.Expr#setPosition(int)
     */
    public void setPosition(int pos) {
       
        current = this;
        current.e.setPosition(pos);
        UnionExpr tmp = this.next;
        while (tmp != null) {
            tmp.e.setPosition(pos);
            tmp = tmp.next;
        }

    }

    public boolean isUnique(int i) {
        return ih.isUnique(i);

    }
    
    public int adjust(int n){        
	    int i = e.adjust(n);
	    if (ih!=null && i==ih.e)
        {}
	    else
	    ih = new intHash(i);
        UnionExpr tmp = this.next;
        while (tmp != null) {
            tmp.e.adjust(n);
            tmp = tmp.next;
        }
        return i;
    }

}