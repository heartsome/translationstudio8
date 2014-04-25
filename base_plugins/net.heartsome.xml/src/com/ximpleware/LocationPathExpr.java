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

package com.ximpleware;
import com.ximpleware.xpath.*;
// if the context node is text(),
// then many axis simply won't work
/**
 * LocationPathExpr implements the location path expression
 * as defined in XPath spec
 */
public class LocationPathExpr extends Expr{

		public static final int ABSOLUTE_PATH =0,
					RELATIVE_PATH =1;
		Step s;
		Step currentStep;
		int pathType;
		int state;
	    //FastIntBuffer fib; // for uniqueness checking
	    intHash ih;
	    
		public static final int START = 0, // initial state
					   END= 1,   // return to begin
				      TERMINAL= 2, // no more next step
				      FORWARD = 3, // 
				      BACKWARD= 4;
								
		
		public LocationPathExpr(){
			state = START;
			s = null;
			pathType = RELATIVE_PATH;
			currentStep = null;
			//fib = new FastIntBuffer(8);// page size 256 = 2^ 8
			ih = new intHash();
		}
		final public void optimize2(){}
		final public void optimize(){
			// get to last step
			Step ts = s;
			if (ts==null)
				return;
			while(ts.nextS!=null){
				ts = ts.nextS;
			}
			
			while(ts.prevS !=null){
				// logic of optmize here
				if (ts.axis_type == AxisType.CHILD0
						|| ts.axis_type == AxisType.CHILD
						|| ts.axis_type == AxisType.ATTRIBUTE){
					switch(ts.prevS.axis_type){
					 case AxisType.CHILD:
						 if (ts.prevS.nt.testType == com.ximpleware.NodeTest.NODE){
							 ts.prevS.axis_type = AxisType.CHILD0;
							 ts.prevS.nt.testType = NodeTest.NAMETEST;
							 ts.prevS.nt.type= 0;
							 ts.prevS.nt.nodeName = "*";
						 }
						 break;
					 case AxisType.DESCENDANT: 
						 ts.prevS.axis_type = AxisType.DESCENDANT0;
						 break;
					 case AxisType.DESCENDANT_OR_SELF:						 
						 ts.prevS.axis_type = AxisType.DESCENDANT_OR_SELF0;
						 break;
					 case AxisType.PRECEDING:						 
						 ts.prevS.axis_type = AxisType.PRECEDING0;
						 break;
					 case AxisType.FOLLOWING:
						 ts.prevS.axis_type = AxisType.FOLLOWING0;
						 break;
					 case AxisType.FOLLOWING_SIBLING:
						 ts.prevS.axis_type = AxisType.FOLLOWING_SIBLING0;
						 ts.prevS.nt.testType = NodeTest.NAMETEST;
						 ts.prevS.nt.type= 0;
						 ts.prevS.nt.nodeName = "*";
						 break;
					 case AxisType.PRECEDING_SIBLING:
					 	 ts.prevS.axis_type = AxisType.PRECEDING_SIBLING0;
					 	 ts.prevS.nt.testType = NodeTest.NAMETEST;
						 ts.prevS.nt.type= 0;
						 ts.prevS.nt.nodeName = "*";
						 break;
					}
				}
				
				ts= ts.prevS;
			}
			// rewrite steps
		}
		
		final public void setStep(Step st){
			s = st;
		}
		
		final public void setPathType(int ptype){
			pathType = ptype;
		}
//		 Improved version for uniqueness checking
		final public boolean isUnique(int i){
		    return ih.isUnique(i);
		}
		
		final public void reset(VTDNav vn){
			state = START;
			Step temp = s;
			ih.reset();
			currentStep = null;
			while(temp!=null){
				temp.reset(vn);
				temp = temp.nextS;
			}	
			/*cached = false; 
			if (cachedNodeSet != null){
				cachedNodeSet.clear();
			}*/
		}

		public String toString(){
			String st = "";
			Step ts = s;
			if (pathType == LocationPathExpr.ABSOLUTE_PATH){
				st = st+"/";
			}
			
			if (ts == null)
				return st;
			else
				return st + ts;
		}

		final public boolean evalBoolean(VTDNav vn){
			boolean a = false;
			vn.push2();
			// record stack size
			int size = vn.contextStack2.size;
		        try{	
				a = (evalNodeSet(vn) != -1);
			
			}catch (Exception e){
			}
			//rewind stack
			vn.contextStack2.size = size;
			reset(vn);
			vn.pop2();
			return a;
		}

		final public double evalNumber(VTDNav vn){
			double d = Double.NaN;
			int a = -1;
	        vn.push2();
	        int size = vn.contextStack2.size;
	        try {
	            a = evalNodeSet(vn);
	            if (a != -1) {
	            	int t = vn.getTokenType(a);
	                if (t == VTDNav.TOKEN_ATTR_NAME) {
	                	d = vn.parseDouble(a+1);
	                } else if (t == VTDNav.TOKEN_STARTING_TAG || t ==VTDNav.TOKEN_DOCUMENT) {
	                    String s = vn.getXPathStringVal();
	                    d  = Double.parseDouble(s);
	                }else if (t == VTDNav.TOKEN_PI_NAME) {
	                	if (a+1 < vn.vtdSize || vn.getTokenType(a+1)==VTDNav.TOKEN_PI_VAL)
	                		d = vn.parseDouble(a+1);               
	                }else 
	                	d = vn.parseDouble(a);
	            }
	        } catch (Exception e) {

	        }
	        vn.contextStack2.size = size;
	        reset(vn);
	        vn.pop2();
	        //return s;
			return d;
		}
	
		final public String evalString(VTDNav vn){ 	
			String s="";
			int a = -1;
	        vn.push2();
	        int size = vn.contextStack2.size;
	        try {
	            a = evalNodeSet(vn);
	            if (a != -1) {
	            	int t = vn.getTokenType(a);
	            	switch(t){
					case VTDNav.TOKEN_STARTING_TAG:
					case VTDNav.TOKEN_DOCUMENT:
						s = vn.getXPathStringVal();
						break;
					case VTDNav.TOKEN_ATTR_NAME:
						s = vn.toString(a + 1);
						break;
					case VTDNav.TOKEN_PI_NAME:
						//if (a + 1 < vn.vtdSize
						//		|| vn.getTokenType(a + 1) == VTDNav.TOKEN_PI_VAL)
							s = vn.toString(a + 1);
						break;
					default:
						s = vn.toString(a);
						break;
					}
	            }
	        } catch (Exception e) {

	        }
	        vn.contextStack2.size = size;
	        reset(vn);
	        vn.pop2();
	        return s;	
		}

		final public boolean  isNodeSet(){
			return true;
		}

		final public boolean  isNumerical(){
			return false;
		}
		
		final protected void transition_child(VTDNav vn)throws NavException{
			
			vn.toElement(VTDNav.P);
			if (currentStep.prevS != null){
				currentStep = currentStep.prevS;
				state = BACKWARD;
			}else{
				state = END;								
			}
		}
		/**
		 * For processing node tests that are element specific
		 * @param vn
		 * @return
		 * @throws XPathEvalException
		 * @throws NavException
		 */
		final protected int process_child(VTDNav vn)throws XPathEvalException,NavException{
		    int result;
		    boolean b = false, b1 = false;
		    int k=1;
		    Predicate t= null;
		    //System.out.println(" index-============= " + vn.getCurrentIndex());
		    //System.out.println(" depth ==============" + vn.getCurrentDepth());
		    switch(state){
		    	case START:
		    	    //if (currentStep.nt.testType < NodeTest.TEXT){
		    	        // first search for any predicate that 
		    	        // requires contextSize
		    	        // if so, compute its context size
		    	        // if size > 0
		    	        // set context
		    	        // if size ==0 
		    	        // immediately set the state to backward or end
		    	        t = currentStep.p;
		    	        while(t!=null){
		    	            if (t.requireContext){
		    	                int i = computeContextSize(t,vn);
		    	                if (i==0){
		    	                    b1 = true;
		    	                    break;
		    	                }else
		    	                    t.setContextSize(i);
		    	            }
		    	            t = t.nextP;
		    	        }
		    	        if (b1){
		    	            state = END;
		    	            break;
		    	        }
		    	        
						 b=vn.toElement(VTDNav.FIRST_CHILD);
						 state =  END;
						 if (b ){
						 do {
							if ((currentStep.nt_eval || currentStep.nt.eval(vn)) 
									&& ((!currentStep.hasPredicate) || currentStep.evalPredicates(vn))) {
								if (currentStep.nextS != null){
									//currentStep.position++;
									state =  FORWARD;
									currentStep = currentStep.nextS;
								}
								else {
									state =  TERMINAL;
									result = vn.getCurrentIndex();
									if ( isUnique(result)){
										return result;
									}
								}
							break;
							}
						} while (vn.toElement(VTDNav.NS));
						if (state == END)
						    vn.toElement(VTDNav.PARENT);
					 }

		    	    break;
		    	case END:
					currentStep =null;
					// reset();
					return -1;
					
		    	case FORWARD:
		    	    //if (currentStep.nt.testType < NodeTest.TEXT){
		    	        t = currentStep.p;
		    	        while(t!=null){
		    	            if (t.requireContext){
		    	                int i = computeContextSize(t,vn);
		    	                if (i==0){
		    	                    b1 = true;
		    	                    break;
		    	                }else
		    	                    t.setContextSize(i);
		    	            }
		    	            t = t.nextP;
		    	        }
		    	        if (b1){
		    	            currentStep = currentStep.prevS;
		    	            state = BACKWARD;
		    	            break;
		    	        }
		    	        
		   			 	state =  BACKWARD;
		   			 	forward: if (vn.toElement(VTDNav.FC)) {
							do {
								if ((currentStep.nt_eval || currentStep.nt.eval(vn)) 
										&& ((!currentStep.hasPredicate) || currentStep.evalPredicates(vn))) {
									if (currentStep.nextS != null) {
										 state =  FORWARD;
										currentStep = currentStep.nextS;
									} else {
										 state =  TERMINAL;
										result = vn.getCurrentIndex();
										if ( isUnique(result))
											return result;
									}
									break forward;
								}
							} while (vn.toElement(VTDNav.NS));
							vn.toElement(VTDNav.P);
							if (currentStep.hasPredicate)
								currentStep.resetP(vn);
							currentStep = currentStep.prevS;
						} else {
							//vn.toElement(VTDNav.P);
							currentStep = currentStep.prevS;
						}

		    	    break;
		    	
		    	case BACKWARD:
					//if (currentStep.nt.testType < NodeTest.TEXT) {
						//currentStep = currentStep.prevS;
						//b = false;
						if(currentStep.out_of_range){
							currentStep.out_of_range = false;
							if (currentStep.hasPredicate)
								currentStep.resetP(vn);
							transition_child(vn);
							break;
						}
						while (vn.toElement(VTDNav.NS)) {
							if ((currentStep.nt_eval || currentStep.nt.eval(vn)) 
									&& ((!currentStep.hasPredicate) || currentStep.evalPredicates(vn))) {
								b = true;
								break;
							}
						}
						if (b ) {
							state =  FORWARD;
							currentStep = currentStep.nextS;
						} else {
							if (currentStep.hasPredicate)
		    					currentStep.resetP(vn);
							transition_child(vn);
						}
					break;
		    	    
		    	case TERMINAL:
		    			if(currentStep.out_of_range){
		    				currentStep.out_of_range = false;
		    				if (currentStep.hasPredicate)
		    					currentStep.resetP(vn);
		    				transition_child(vn);
		    				break;
		    			}
					//if (currentStep.nt.testType < NodeTest.TEXT) {
						while (vn.toElement(VTDNav.NS)) {
							if ((currentStep.nt_eval || currentStep.nt.eval(vn)) 
									&& ((!currentStep.hasPredicate) || currentStep.evalPredicates(vn))) {
								// state =  TERMINAL;
								result = vn.getCurrentIndex();
								if ( isUnique(result))
									return result;
							}
						}
						if (currentStep.hasPredicate)
							currentStep.resetP(vn);
						transition_child(vn);
					break;

				default:
					throw new XPathEvalException("unknown state");
		    }
		    return -2;
		}
		final protected void transition_DDFP(VTDNav vn){
			vn.pop2();
			currentStep.ft = true;
			if(currentStep.hasPredicate)
				currentStep.resetP(vn);
			//System.out.println("  --++ pop in //");
			if (currentStep.prevS != null) {
				state =  BACKWARD;
				currentStep = currentStep.prevS;
			} else
				state =  END;
		}
		final protected int process_DDFP(VTDNav vn) 
		throws XPathEvalException, NavException {
		AutoPilot ap;
		boolean b = false, b1 = false;
	    Predicate t= null;
		int result;		
		
		switch(state){
			case START:
			case FORWARD:
			    if (vn.atTerminal){
			        if (state == START)
			            state = END;
			        else {
			            // no need to set_ft to true
			            // no need to resetP
			            state = BACKWARD;
			            currentStep = currentStep.prevS;
			        }
			        break;
			    }
			    
    	        t = currentStep.p;
    	        while(t!=null){
    	            if (t.requireContext){
    	                int i = computeContextSize(t,vn);
    	                if (i==0){
    	                    b1 = true;
    	                    break;
    	                }else
    	                    t.setContextSize(i);
    	            }
    	            t = t.nextP;
    	        }
    	        if (b1){
    	            if (state ==START)
    	                state = END;
    	            else {
    	                currentStep = currentStep.prevS;
    	                state = BACKWARD;
    	            }
    	            break;
    	        }
    	        
    		    String helper = null;
    			if (currentStep.nt.testType == NodeTest.NAMETEST){
    				helper = currentStep.nt.nodeName;
    			} else if (currentStep.nt.testType == NodeTest.NODE){
    				helper = "*";
    			} else
    				throw new XPathEvalException("can't run descendant "
    						+ "following, or following-sibling axis over comment(), pi(), and text()"); 
    			if (currentStep.o == null)
    				currentStep.o = ap = new AutoPilot(vn);
    			else {
    				ap = (AutoPilot) currentStep.o;
    				ap.bind(vn);
    			}
    			if (currentStep.ft ) {
    				if (currentStep.axis_type == AxisType.DESCENDANT_OR_SELF0 )
    					if (currentStep.nt.testType == NodeTest.NODE)
    						ap.setSpecial(true);
    					else
    						ap.setSpecial(false);
    				//currentStep.o = ap = new AutoPilot(vn);
    			    if (currentStep.axis_type == AxisType.DESCENDANT_OR_SELF0)
    					ap.selectElement(helper);
    				else if (currentStep.axis_type == AxisType.DESCENDANT0)
    					ap.selectElement_D(helper);
    				else if (currentStep.axis_type == AxisType.PRECEDING0)
    					ap.selectElement_P(helper);
    				else 
    					ap.selectElement_F(helper);
    				currentStep.ft = false;
    			}
    			if ( state ==  START)
    				 state =  END;

    			vn.push2(); // not the most efficient. good for now
    			//System.out.println("  --++ push in //");
    			b = false;
    			while(ap.iterate()){
    				if (!currentStep.hasPredicate || currentStep.evalPredicates(vn)){
    					b = true;
    					break;
    				}
    			}
    			if (b == false) {
    				vn.pop2();
    				//System.out.println("  --++ pop in //");
    				currentStep.ft = true;
    				if (currentStep.hasPredicate)
    					currentStep.resetP(vn);
    				if ( state ==  FORWARD){
    					state =  BACKWARD;
    					currentStep = currentStep.prevS;							
    				}						
    			} else {
    				if (currentStep.nextS != null){
    					state =  FORWARD;
    					currentStep = currentStep.nextS;
    				}
    				else {
    					//vn.pop();
    					state =  TERMINAL;
    					result = vn.getCurrentIndex();
    					if ( isUnique(result))
    						return result;
    				}
    			}
    			break;    	        
			    
			case END:
				currentStep = null;
				// reset();
				return -1;
			    
			case BACKWARD:
				//currentStep = currentStep.prevS;
				if (currentStep.out_of_range){
					currentStep.out_of_range = false;
					transition_DDFP(vn);
					break;
				}
				ap = (AutoPilot) currentStep.o;
				//vn.push();
				//b = false;
				while(ap.iterate()){
					if (!currentStep.hasPredicate || currentStep.evalPredicates(vn)){
						b = true;
						break;
					}
				}
				if (b ) {
					if (currentStep.nextS != null) {
						//vn.push();
						//System.out.println("  --++ push in //");
						 state =  FORWARD;
						currentStep = currentStep.nextS;
					} else {
						 state =  TERMINAL;
						result = vn.getCurrentIndex();
						if ( isUnique(result))
							return result;
					}									
				} else {
					transition_DDFP(vn);
				}
				break;
			    
			case TERMINAL:
				if (currentStep.out_of_range){
					currentStep.out_of_range = false;
					transition_DDFP(vn);
					break;
				}
			    ap = (AutoPilot) currentStep.o;
			    b = false;
			    while (ap.iterate()) {
			        if (!currentStep.hasPredicate || currentStep.evalPredicates(vn)) {
			            b = true;
			            break;
			        }
			    }
			    if (b ) {
			        //if (currentStep.evalPredicates(vn)) {
			        result = vn.getCurrentIndex();
			        if (isUnique(result))
			            return result;
			        //}
			    } else {
			    	transition_DDFP(vn);
			    }
            break;

			default:
			    throw new XPathEvalException("unknown state");
        }
	    return -2;
	}
	
		final protected int process_DDFP2(VTDNav vn) throws XPathEvalException,
			NavException {
		AutoPilot ap;
		boolean b = false, b1 = false;
		Predicate t = null;
		int result;

		switch (state) {
		case START:
		case FORWARD:
			/*if (vn.atTerminal) {
				if (state == START)
					state = END;
				else {
					// no need to set_ft to true
					// no need to resetP
					state = BACKWARD;
					currentStep = currentStep.prevS;
				}
				break;
			}*/

			t = currentStep.p;
			while (t != null) {
				if (t.requireContext) {
					int i = computeContextSize(t, vn);
					if (i == 0) {
						b1 = true;
						break;
					} else
						t.setContextSize(i);
				}
				t = t.nextP;
			}
			if (b1) {
				if (state == START)
					state = END;
				else {
					currentStep = currentStep.prevS;
					state = BACKWARD;
				}
				break;
			}

			String helper = null;
			/*if (currentStep.nt.testType == NodeTest.NAMETEST) {
				helper = currentStep.nt.nodeName;
			} else if (currentStep.nt.testType == NodeTest.NODE) {
				helper = "*";
			} else
				throw new XPathEvalException(
						"can't run descendant "
								+ "following, or following-sibling axis over comment(), pi(), and text()");*/
			if (currentStep.o == null)
				currentStep.o = ap = new AutoPilot(vn);
			else {
				ap = (AutoPilot) currentStep.o;
				ap.bind(vn);
			}
			if (currentStep.ft) {

				/*if (currentStep.axis_type == AxisType.DESCENDANT_OR_SELF)
					if (currentStep.nt.testType == NodeTest.NODE)
						ap.setSpecial(true);
					else
						ap.setSpecial(false);*/
				// currentStep.o = ap = new AutoPilot(vn);
				if (currentStep.axis_type == AxisType.DESCENDANT_OR_SELF)
					ap.selectNode();
				else if (currentStep.axis_type == AxisType.DESCENDANT)
					ap.selectDescendantNode();
				else if (currentStep.axis_type == AxisType.PRECEDING)
					ap.selectPrecedingNode();
				else
					ap.selectFollowingNode();
				currentStep.ft = false;
			}
			if (state == START)
				state = END;

			vn.push2(); // not the most efficient. good for now
			// System.out.println("  --++ push in //");
			b = false;
			while (ap.iterate2()) {
				if ((currentStep.nt_eval || currentStep.nt.eval2(vn)) 
						&& ((!currentStep.hasPredicate) || currentStep.evalPredicates(vn))) {
					b = true;
					break;
				}
			}
			if (b == false) {
				vn.pop2();
				// System.out.println("  --++ pop in //");
				currentStep.ft = true;
				if (currentStep.hasPredicate)
					currentStep.resetP(vn);
				if (state == FORWARD) {
					state = BACKWARD;
					currentStep = currentStep.prevS;
				}
			} else {
				if (currentStep.nextS != null) {
					state = FORWARD;
					currentStep = currentStep.nextS;
				} else {
					// vn.pop();
					state = TERMINAL;
					result = vn.getCurrentIndex();
					if (isUnique(result))
						return result;
				}
			}
			break;

		case END:
			currentStep = null;
			// reset();
			return -1;

		case BACKWARD:
			if(currentStep.out_of_range){
				currentStep.out_of_range = false;
				transition_DDFP(vn);
				break;
			}
			// currentStep = currentStep.prevS;
			ap = (AutoPilot) currentStep.o;
			// vn.push();
			//b = false;
			while (ap.iterate2()) {
				if ((currentStep.nt_eval || currentStep.nt.eval2(vn)) 
						&& ((!currentStep.hasPredicate) || currentStep.evalPredicates(vn))) {
					b = true;
					break;
				}
			}
			if (b ) {
				if (currentStep.nextS != null) {
					// vn.push();
					// System.out.println("  --++ push in //");
					state = FORWARD;
					currentStep = currentStep.nextS;
				} else {
					state = TERMINAL;
					result = vn.getCurrentIndex();
					if (isUnique(result))
						return result;
				}
			} else 
				transition_DDFP(vn);
			break;

		case TERMINAL:
			if(currentStep.out_of_range){
				currentStep.out_of_range = false;
				transition_DDFP(vn);
				break;
			}
			ap = (AutoPilot) currentStep.o;
			b = false;
			while (ap.iterate2()) {
				if ((currentStep.nt_eval || currentStep.nt.eval2(vn)) 
						&& ((!currentStep.hasPredicate) || currentStep.evalPredicates(vn))) {
					b = true;
					break;
				}
			}
			if (b) {
				//if (currentStep.evalPredicates(vn)) {
				result = vn.getCurrentIndex();
				if (isUnique(result))
					return result;
				//}
			} else{ 
				transition_DDFP(vn);
			}
			break;

		default:
			throw new XPathEvalException("unknown state");
		}
		return -2;
	}
	
		final protected int process_parent(VTDNav vn)
	throws XPathEvalException, NavException{
	    boolean b1 = false;
	    Predicate t= null;
	    int result;
		switch ( state) {
			case  START:
			case  FORWARD:
    	        t = currentStep.p;
    	        while(t!=null){
    	            if (t.requireContext){
    	                int i = computeContextSize(t,vn);
    	                if (i==0){
    	                    b1 = true;
    	                    break;
    	                }else
    	                    t.setContextSize(i);
    	            }
    	            t = t.nextP;
    	        }
    	        if (b1){
    	            if (state == FORWARD){
    	                state = BACKWARD;
    	                currentStep = currentStep.prevS;
    	            }else 
    	                state = END;
    	            break;
    	        }
    	        
    			if (vn.getCurrentDepth() == -1) {
    				if ( state ==  START)
    					 state =  END;
    				else {
    					//vn.pop();
    					 state =  BACKWARD;
    					currentStep = currentStep.prevS;
    				}
    			} else {
    				vn.push2();
    				vn.toElement(VTDNav.P); // must return true
    				if ((currentStep.nt_eval || currentStep.nt.eval(vn)) 
    						&& ((!currentStep.hasPredicate) || currentStep.evalPredicates(vn))){
    				    if (currentStep.nextS != null) {
    					    state =  FORWARD;
    					   currentStep = currentStep.nextS;
    				    } else {
    					    state =  TERMINAL;
    					   result = vn.getCurrentIndex();
    						if ( isUnique(result))
    							return result;
    				    }
    				}else{
    					vn.pop2();
    					if (currentStep.hasPredicate)
    						currentStep.resetP(vn);
    					if ( state ==  START)
    						 state =  END;
    					else {								
    						 state =  BACKWARD;
    						currentStep = currentStep.prevS;
    					}
    				}
    			}

    			break;				
    			
    		case  END:
    			currentStep = null;
    			// reset();
    		    return -1;
    			
    		case  BACKWARD:
    		case  TERMINAL:
    			if (currentStep.prevS == null) {
    			    vn.pop2();
    				 state =  END;
    				break;
    			}else {
    				vn.pop2();
    				 state =  BACKWARD;
    				currentStep = currentStep.prevS;
    				break;
    			}
    			
    		default:
    			throw new  XPathEvalException("unknown state");
		
		}
	    return -2;
	}
	
	final protected int process_parent2(VTDNav vn)
	throws XPathEvalException, NavException{
	    boolean b1 = false;
	    Predicate t= null;
	    int result;
		switch ( state) {
			case  START:
			case  FORWARD:
    	        t = currentStep.p;
    	        while(t!=null){
    	            if (t.requireContext){
    	                int i = computeContextSize(t,vn);
    	                if (i==0){
    	                    b1 = true;
    	                    break;
    	                }else
    	                    t.setContextSize(i);
    	            }
    	            t = t.nextP;
    	        }
    	        if (b1){
    	            if (state == FORWARD){
    	                state = BACKWARD;
    	                currentStep = currentStep.prevS;
    	            }else 
    	                state = END;
    	            break;
    	        }
    	        
    			if (vn.getCurrentDepth() == -1) {
    				if ( state ==  START)
    					 state =  END;
    				else {
    					//vn.pop();
    					 state =  BACKWARD;
    					currentStep = currentStep.prevS;
    				}
    			} else {
    				vn.push2();
    				vn.toNode(VTDNav.P); // must return true
    				if ((currentStep.nt_eval || currentStep.nt.eval2(vn)) 
    						&& ((!currentStep.hasPredicate) || currentStep.evalPredicates(vn))){
    				    if (currentStep.nextS != null) {
    					    state =  FORWARD;
    					   currentStep = currentStep.nextS;
    				    } else {
    					    state =  TERMINAL;
    					   result = vn.getCurrentIndex();
    						if ( isUnique(result))
    							return result;
    				    }
    				}else{
    					vn.pop2();
    					if (currentStep.hasPredicate)
    						currentStep.resetP(vn);
    					if ( state ==  START)
    						 state =  END;
    					else {								
    						 state =  BACKWARD;
    						currentStep = currentStep.prevS;
    					}
    				}
    			}

    			break;				
    			
    		case  END:
    			currentStep = null;
    			// reset();
    		    return -1;
    			
    		case  BACKWARD:
    		case  TERMINAL:
    			if (currentStep.prevS == null) {
    			    vn.pop2();
    				 state =  END;
    				break;
    			}else {
    				vn.pop2();
    				 state =  BACKWARD;
    				currentStep = currentStep.prevS;
    				break;
    			}
    			
    		default:
    			throw new  XPathEvalException("unknown state");
		
		}
	    return -2;
	}
	
	final protected int process_ancestor( VTDNav vn)
	throws XPathEvalException, NavException{
	    int result;
	    boolean b = false, b1 = false;
	    //int contextSize;
	    Predicate t= null;
	    
	    switch(state){
	    	case START:
	    		
	    	    t = currentStep.p;
	    	    while (t != null) {
	    	        if (t.requireContext) {
	    	            int i = computeContextSize( t, vn);
	    	            if (i == 0) {
	    	                b1 = true;
	    	                break;
	    	            } else
	    	                t.setContextSize(i);
	    	        }
	    	        t = t.nextP;
	    	    }
	    	    if (b1) {
	    	        state = END;
	    	        break;
	    	    }

	    	    state = END;
	    	    if (vn.getCurrentDepth() != -1) {
	    	        vn.push2();

	    	        while (vn.toElement(VTDNav.P)) {
	    	            if ((currentStep.nt_eval || currentStep.nt.eval(vn)) 
	    	            		&& ((!currentStep.hasPredicate) || currentStep.evalPredicates(vn))) {
	    	                if (currentStep.nextS != null) {
	    	                    state = FORWARD;
	    	                    currentStep = currentStep.nextS;
	    	                    break;
	    	                } else {
	    	                    //vn.pop();
	    	                    state = TERMINAL;
	    	                    result = vn.getCurrentIndex();
	    	                    if (isUnique(result))
	    	                        return result;
	    	                }
	    	            }
	    	        }
	    	        if (state == END) {
	    	        	if (currentStep.hasPredicate)
	    	        		currentStep.resetP(vn);
	    	            vn.pop2();
	    	        }
	    	    }
	    	    break;
    	        
	    	case END:   
				currentStep =null;
				// reset();
			    return -1;
			    
	    	case FORWARD:	    	    
	    	     t = currentStep.p;
	    	     while(t!=null){
	    	        if (t.requireContext){
	    	             int i = computeContextSize(t,vn);
	    	             if (i==0){
	    	                 b1 = true;
	    	                 break;
	    	             }else
	    	                 t.setContextSize(i);
	    	        }
	    	        t = t.nextP;
	    	    }
	    	    if (b1){
	    	        currentStep = currentStep.prevS;
	    	        state = BACKWARD;
	    	        break;
	    	    }
			    state =  BACKWARD;
			   	vn.push2();
					
			   	while(vn.toElement(VTDNav.P)){
			   		if ((currentStep.nt_eval || currentStep.nt.eval(vn)) 
			   				&& ((!currentStep.hasPredicate) || currentStep.evalPredicates(vn))){
			   			if (currentStep.nextS != null){
			   				 state =  FORWARD;
			   				currentStep = currentStep.nextS;
			   				break;
			   			}
			   			else {
			   				//vn.pop();
			   				 state =  TERMINAL;
			   				result = vn.getCurrentIndex();
							if ( isUnique(result))
								return result;
			   			}
			   		}							
			   	}
			   	if ( state == BACKWARD){
			   		if (currentStep.hasPredicate)
			   			currentStep.resetP(vn);
					vn.pop2();
			   		currentStep=currentStep.prevS;
			   	}			    
			  	break;
	    	    
	    	case BACKWARD:
				b = false;
				vn.push2();

				while (vn.toElement(VTDNav.P)) {
					if ((currentStep.nt_eval || currentStep.nt.eval(vn)) 
							&& ((!currentStep.hasPredicate) || currentStep.evalPredicates(vn))) {
						if (currentStep.nextS!= null) {
							 state =  FORWARD;
							currentStep = currentStep.nextS;
							b = true;
							break;
						} else {
							//vn.pop();
							 state =  TERMINAL;
							result = vn.getCurrentIndex();
							if ( isUnique(result))
								return result;
						}
					}
				}
				if (b==false){
					vn.pop2();
					if (currentStep.prevS!=null) {
						if (currentStep.hasPredicate)
							currentStep.resetP(vn);
						state =  BACKWARD;
						currentStep = currentStep.prevS;
					}
					else {
						 state =  END;
					}
				}
				break;
				
	    	case TERMINAL:			
	    	    while (vn.toElement(VTDNav.P)) {
				if ((currentStep.nt_eval || currentStep.nt.eval(vn)) 
						&& ((!currentStep.hasPredicate) || currentStep.evalPredicates(vn))) {
					result = vn.getCurrentIndex();
					if ( isUnique(result))
						return result;
				}
			}
			vn.pop2();
			
			if (currentStep.prevS!=null) {
				if (currentStep.hasPredicate)
					currentStep.resetP(vn);
				 state =  BACKWARD;
				currentStep = currentStep.prevS;
			}
			else {
				 state =  END;
			}
			break;
		
		default:
			throw new  XPathEvalException("unknown state");
	    }
	    return -2;
	}
	
	
	final protected int process_ancestor2( VTDNav vn)
	throws XPathEvalException, NavException{
	    int result;
	    boolean b = false, b1 = false;
	    //int contextSize;
	    Predicate t= null;
	    
	    switch(state){
	    	case START:
	    		
	    	    t = currentStep.p;
	    	    while (t != null) {
	    	        if (t.requireContext) {
	    	            int i = computeContextSize( t, vn);
	    	            if (i == 0) {
	    	                b1 = true;
	    	                break;
	    	            } else
	    	                t.setContextSize(i);
	    	        }
	    	        t = t.nextP;
	    	    }
	    	    if (b1) {
	    	        state = END;
	    	        break;
	    	    }

	    	    state = END;
	    	    //if (vn.getCurrentDepth() != -1) {
	    	        vn.push2();

	    	        while (vn.toNode(VTDNav.P)) {
	    	            if ((currentStep.nt_eval || currentStep.nt.eval2(vn)) 
	    	            		&& ((!currentStep.hasPredicate) || currentStep.evalPredicates(vn))) {
	    	                if (currentStep.nextS != null) {
	    	                    state = FORWARD;
	    	                    currentStep = currentStep.nextS;
	    	                    break;
	    	                } else {
	    	                    //vn.pop();
	    	                    state = TERMINAL;
	    	                    result = vn.getCurrentIndex();
	    	                    if (isUnique(result))
	    	                        return result;
	    	                }
	    	            }
	    	        }
	    	        if (state == END) {
	    	        	if (currentStep.hasPredicate)
	    	        		currentStep.resetP(vn);
	    	            vn.pop2();
	    	        }
	    	   // }
	    	    break;
    	        
	    	case END:   
				currentStep =null;
				// reset();
			    return -1;
			    
	    	case FORWARD:	    	    
	    	     t = currentStep.p;
	    	     while(t!=null){
	    	        if (t.requireContext){
	    	             int i = computeContextSize(t,vn);
	    	             if (i==0){
	    	                 b1 = true;
	    	                 break;
	    	             }else
	    	                 t.setContextSize(i);
	    	        }
	    	        t = t.nextP;
	    	    }
	    	    if (b1){
	    	        currentStep = currentStep.prevS;
	    	        state = BACKWARD;
	    	        break;
	    	    }
			    state =  BACKWARD;
			   	vn.push2();
					
			   	while(vn.toNode(VTDNav.P)){
			   		if ((currentStep.nt_eval || currentStep.nt.eval2(vn)) 
			   				&& ((!currentStep.hasPredicate) || currentStep.evalPredicates(vn))){
			   			if (currentStep.nextS != null){
			   				 state =  FORWARD;
			   				currentStep = currentStep.nextS;
			   				break;
			   			}
			   			else {
			   				//vn.pop();
			   				 state =  TERMINAL;
			   				result = vn.getCurrentIndex();
							if ( isUnique(result))
								return result;
			   			}
			   		}							
			   	}
			   	if ( state == BACKWARD){
			   		if (currentStep.hasPredicate)
			   			currentStep.resetP(vn);
					vn.pop2();
			   		currentStep=currentStep.prevS;
			   	}			    
			  	break;
	    	    
	    	case BACKWARD:
				b = false;
				vn.push2();

				while (vn.toNode(VTDNav.P)) {
					if ((currentStep.nt_eval || currentStep.nt.eval2(vn)) 
							&& ((!currentStep.hasPredicate) || currentStep.evalPredicates(vn))) {
						if (currentStep.nextS!= null) {
							 state =  FORWARD;
							currentStep = currentStep.nextS;
							b = true;
							break;
						} else {
							//vn.pop();
							 state =  TERMINAL;
							result = vn.getCurrentIndex();
							if ( isUnique(result))
								return result;
						}
					}
				}
				if (b==false){
					vn.pop2();
					if (currentStep.prevS!=null) {
						if (currentStep.hasPredicate)
							currentStep.resetP(vn);
						state =  BACKWARD;
						currentStep = currentStep.prevS;
					}
					else {
						 state =  END;
					}
				}
				break;
				
	    	case TERMINAL:			
	    	    while (vn.toNode(VTDNav.P)) {
				if ((currentStep.nt_eval || currentStep.nt.eval2(vn)) 
						&& ((!currentStep.hasPredicate) || currentStep.evalPredicates(vn))) {
					result = vn.getCurrentIndex();
					if ( isUnique(result))
						return result;
				}
			}
			vn.pop2();
			
			if (currentStep.prevS!=null) {
				if (currentStep.hasPredicate)
					currentStep.resetP(vn);
				 state =  BACKWARD;
				currentStep = currentStep.prevS;
			}
			else {
				 state =  END;
			}
			break;
		
		default:
			throw new  XPathEvalException("unknown state");
	    }
	    return -2;
	}
	
	final protected int process_ancestor_or_self(VTDNav vn)
	throws XPathEvalException,NavException{
	    boolean b = false, b1 = false;
	    Predicate t= null;
	    int result;
		switch ( state) {
			case  START:
	    	    t = currentStep.p;
	    	    while (t != null) {
	    	        if (t.requireContext) {
	    	            int i = computeContextSize( t, vn);
	    	            if (i == 0) {
	    	                b1 = true;
	    	                break;
	    	            } else
	    	                t.setContextSize(i);
	    	        }
	    	        t = t.nextP;
	    	    }
	    	    if (b1) {
	    	        state = END;
	    	        break;
	    	    }
				state =  END;
				vn.push2();
				
				if (currentStep.ft){						
					currentStep.ft = false;
					if ((currentStep.nt_eval || currentStep.nt.eval(vn)) 
							&& ((!currentStep.hasPredicate) || currentStep.evalPredicates(vn))) {
						if (currentStep.nextS != null) {
							state =  FORWARD;
							currentStep = currentStep.nextS;
							break;
						} else {
							//vn.pop();
							state =  TERMINAL;
							if (vn.atTerminal)
							    result = vn.LN;
							else 
							    result = vn.getCurrentIndex();
							if ( isUnique(result))
								return result;
						}
					}
				}
				
					while (vn.toElement(VTDNav.P)) {
						if ((currentStep.nt_eval || currentStep.nt.eval(vn)) 
								&& ((!currentStep.hasPredicate) || currentStep.evalPredicates(vn))) {
							if (currentStep.nextS != null) {
								 state =  FORWARD;
								currentStep = currentStep.nextS;
								break;
							} else {
								//vn.pop();
								 state =  TERMINAL;
								result = vn.getCurrentIndex();
								if ( isUnique(result))
									return result;
							}
						}
					}
				
				if ( state ==  END) {
					if (currentStep.hasPredicate)
						currentStep.resetP(vn);
					vn.pop2();
				}

				break;
				
			case  FORWARD:
	    	     t = currentStep.p;
	    	     while(t!=null){
	    	        if (t.requireContext){
	    	             int i = computeContextSize(t,vn);
	    	             if (i==0){
	    	                 b1 = true;
	    	                 break;
	    	             }else
	    	                 t.setContextSize(i);
	    	        }
	    	        t = t.nextP;
	    	    }
	    	    if (b1){
	    	        currentStep = currentStep.prevS;
	    	        state = BACKWARD;
	    	        break;
	    	    }
				 state =  BACKWARD;
					vn.push2();
					if (currentStep.ft ) {
						currentStep.ft = false;
						
						if ((currentStep.nt_eval || currentStep.nt.eval(vn)) 
								&& ((!currentStep.hasPredicate) || currentStep.evalPredicates(vn))) {
							if (currentStep.nextS != null) {
								 state =  FORWARD;
								currentStep = currentStep.nextS;
								break;
							} else {
								//vn.pop();
								 state =  TERMINAL;
								 if (vn.atTerminal)
								     result = vn.LN;
								 else 
								     result = vn.getCurrentIndex();
								if ( isUnique(result))
									return result;
							}
						}
					} 
						while (vn.toElement(VTDNav.P)) {
							if ((currentStep.nt_eval || currentStep.nt.eval(vn))
									&& ((!currentStep.hasPredicate) || currentStep.evalPredicates(vn))) {
								if (currentStep.nextS != null) {
									 state =  FORWARD;
									currentStep = currentStep.nextS;
									break;
								} else {
									//vn.pop();
									 state =  TERMINAL;
									result = vn.getCurrentIndex();
									if ( isUnique(result))
										return result;
								}
							}
						}
					
					if ( state ==  BACKWARD) {
						if (currentStep.hasPredicate)
							currentStep.resetP(vn);
						currentStep.ft = true;
						vn.pop2();
						currentStep = currentStep.prevS;
					}
					break;
					
			case  END:
				currentStep = null;
				// reset();
		    	return -1;
				
			
			case  BACKWARD:
				//b = false;
				vn.push2();

				while (vn.toElement(VTDNav.P)) {
					if ((currentStep.nt_eval || currentStep.nt.eval(vn)) 
							&& ((!currentStep.hasPredicate) || currentStep.evalPredicates(vn))) {
						if (currentStep.nextS != null) {
							 state =  FORWARD;
							currentStep = currentStep.nextS;
							b = true;
							break;
						} else {
							//vn.pop();
							 state =  TERMINAL;
							result = vn.getCurrentIndex();
							if ( isUnique(result))
								return result;
						}
					}
				}
				if (b == false) {
					vn.pop2();
					if (currentStep.hasPredicate)
						currentStep.resetP(vn);
					if (currentStep.prevS != null) {
						currentStep.ft = true;
						 state =  BACKWARD;
						currentStep = currentStep.prevS;
					} else {
						 state =  END;
					}
				}
				break;
			
			case  TERMINAL:
				while (vn.toElement(VTDNav.P)) {
					if ((currentStep.nt_eval || currentStep.nt.eval(vn)) 
							&& ((!currentStep.hasPredicate) || currentStep.evalPredicates(vn))) {
						result = vn.getCurrentIndex();
						if ( isUnique(result))
							return result;
					}
				}
				vn.pop2();
				if (currentStep.hasPredicate)
					currentStep.resetP(vn);
				if (currentStep.prevS!=null) {
					currentStep.ft = true;
					 state =  BACKWARD;
					currentStep = currentStep.prevS;
				}
				else {
					 state =  END;
				}
				break;
				
			
			default:
				throw new  XPathEvalException("unknown state");
		}
	    return -2;
	}
	
	final protected int process_ancestor_or_self2(VTDNav vn)
	throws XPathEvalException,NavException{
	    boolean b = false, b1 = false;
	    Predicate t= null;
	    int result;
		switch ( state) {
			case  START:
	    	    t = currentStep.p;
	    	    while (t != null) {
	    	        if (t.requireContext) {
	    	            int i = computeContextSize( t, vn);
	    	            if (i == 0) {
	    	                b1 = true;
	    	                break;
	    	            } else
	    	                t.setContextSize(i);
	    	        }
	    	        t = t.nextP;
	    	    }
	    	    if (b1) {
	    	        state = END;
	    	        break;
	    	    }
				state =  END;
				vn.push2();
				
				if (currentStep.ft){						
					currentStep.ft = false;
					if ((currentStep.nt_eval || currentStep.nt.eval2(vn)) 
							&& ((!currentStep.hasPredicate) || currentStep.evalPredicates(vn))) {
						if (currentStep.nextS != null) {
							state =  FORWARD;
							currentStep = currentStep.nextS;
							break;
						} else {
							//vn.pop();
							state =  TERMINAL;
							if (vn.atTerminal)
							    result = vn.LN;
							else 
							    result = vn.getCurrentIndex();
							if ( isUnique(result))
								return result;
						}
					}
				}
				
					while (vn.toNode(VTDNav.P)) {
						if ((currentStep.nt_eval || currentStep.nt.eval2(vn)) 
								&& ((!currentStep.hasPredicate) || currentStep.evalPredicates(vn))) {
							if (currentStep.nextS != null) {
								 state =  FORWARD;
								currentStep = currentStep.nextS;
								break;
							} else {
								//vn.pop();
								 state =  TERMINAL;
								result = vn.getCurrentIndex();
								if ( isUnique(result))
									return result;
							}
						}
					}
				
				if ( state ==  END) {
					if (currentStep.hasPredicate)
						currentStep.resetP(vn);
					vn.pop2();
				}

				break;
				
			case  FORWARD:
	    	     t = currentStep.p;
	    	     while(t!=null){
	    	        if (t.requireContext){
	    	             int i = computeContextSize(t,vn);
	    	             if (i==0){
	    	                 b1 = true;
	    	                 break;
	    	             }else
	    	                 t.setContextSize(i);
	    	        }
	    	        t = t.nextP;
	    	    }
	    	    if (b1){
	    	        currentStep = currentStep.prevS;
	    	        state = BACKWARD;
	    	        break;
	    	    }
				 state =  BACKWARD;
					vn.push2();
					if (currentStep.ft ) {
						currentStep.ft = false;
						
						if ((currentStep.nt_eval || currentStep.nt.eval2(vn)) 
								&& ((!currentStep.hasPredicate) || currentStep.evalPredicates(vn))) {
							if (currentStep.nextS != null) {
								 state =  FORWARD;
								currentStep = currentStep.nextS;
								break;
							} else {
								//vn.pop();
								 state =  TERMINAL;
								 if (vn.atTerminal)
								     result = vn.LN;
								 else 
								     result = vn.getCurrentIndex();
								if ( isUnique(result))
									return result;
							}
						}
					} 
						while (vn.toNode(VTDNav.P)) {
							if ((currentStep.nt_eval || currentStep.nt.eval2(vn)) 
									&& ((!currentStep.hasPredicate) || currentStep.evalPredicates(vn))) {
								if (currentStep.nextS != null) {
									 state =  FORWARD;
									currentStep = currentStep.nextS;
									break;
								} else {
									//vn.pop();
									 state =  TERMINAL;
									result = vn.getCurrentIndex();
									if ( isUnique(result))
										return result;
								}
							}
						}
					
					if ( state ==  BACKWARD) {
						if (currentStep.hasPredicate)
							currentStep.resetP(vn);
						currentStep.ft = true;
						vn.pop2();
						currentStep = currentStep.prevS;
					}
					break;
					
			case  END:
				currentStep = null;
				// reset();
		    	return -1;
				
			
			case  BACKWARD:
				//b = false;
				vn.push2();

				while (vn.toNode(VTDNav.P)) {
					if ((currentStep.nt_eval || currentStep.nt.eval2(vn)) 
							&& ((!currentStep.hasPredicate) || currentStep.evalPredicates(vn))) {
						if (currentStep.nextS != null) {
							 state =  FORWARD;
							currentStep = currentStep.nextS;
							b = true;
							break;
						} else {
							//vn.pop();
							 state =  TERMINAL;
							result = vn.getCurrentIndex();
							if ( isUnique(result))
								return result;
						}
					}
				}
				if (b == false) {
					vn.pop2();
					if (currentStep.hasPredicate)
						currentStep.resetP(vn);
					if (currentStep.prevS != null) {
						currentStep.ft = true;
						 state =  BACKWARD;
						currentStep = currentStep.prevS;
					} else {
						 state =  END;
					}
				}
				break;
			
			case  TERMINAL:
				while (vn.toNode(VTDNav.P)) {
					if ((currentStep.nt_eval || currentStep.nt.eval2(vn)) 
							&& ((!currentStep.hasPredicate) || currentStep.evalPredicates(vn))) {
						result = vn.getCurrentIndex();
						if ( isUnique(result))
							return result;
					}
				}
				vn.pop2();
				if (currentStep.hasPredicate)
					currentStep.resetP(vn);
				if (currentStep.prevS!=null) {
					currentStep.ft = true;
					 state =  BACKWARD;
					currentStep = currentStep.prevS;
				}
				else {
					 state =  END;
				}
				break;
				
			
			default:
				throw new  XPathEvalException("unknown state");
		}
	    return -2;
	}
	
	final protected int process_self(VTDNav vn)
		throws XPathEvalException,NavException{
	    boolean b1 = false;
	    Predicate t= null;
	    int result;
		switch( state){
		  case  START:
		  case  FORWARD:
  	        t = currentStep.p;
	        while(t!=null){
	            if (t.requireContext){
	                int i = computeContextSize(t,vn);
	                if (i==0){
	                    b1 = true;
	                    break;
	                }else
	                    t.setContextSize(i);
	            }
	            t = t.nextP;
	        }
	        if (b1){
	            if (state == FORWARD){
	                state = BACKWARD;
	                currentStep = currentStep.prevS;
	            }else 
	                state = END;
	            break;
	        }
		  	if ((currentStep.nt_eval || currentStep.nt.eval(vn)) 
		  			&& ((!currentStep.hasPredicate) || currentStep.evalPredicates(vn))){
		  		if (currentStep.nextS!=null){
		  			 state =  FORWARD;
		  			currentStep = currentStep.nextS;
		  		}
		  		else{
		  			 state =  TERMINAL;
		  			 if (vn.atTerminal )
		  			     result = vn.LN;
		  			 else 
		  			     result = vn.getCurrentIndex();
					if ( isUnique(result))
						return result;
		  		}
		  	}else {
		  		if (currentStep.hasPredicate)
		  			currentStep.resetP(vn);
		  		if ( state ==  START)
		  			 state =  END;
		  		else 
		  			 state =  BACKWARD;
		  	}
		    break;
		  	
		  case  END:
		  	currentStep = null;
		  	// reset();
		  	return -1;
		  	
		  case  BACKWARD:
		  case  TERMINAL:
		  	if (currentStep.prevS!=null){
	  			 state =  BACKWARD;
	  			currentStep= currentStep.prevS;
	  		}else{
	  			 state =  END;				  			
	  		}
		  	break;
		  
		  default:
			throw new  XPathEvalException("unknown state");
		}
	    return -2;
	}
	
	final protected int process_self2(VTDNav vn) throws XPathEvalException,
			NavException {
		boolean b1 = false;
		Predicate t = null;
		int result;
		switch (state) {
		case START:
		case FORWARD:
			t = currentStep.p;
			while (t != null) {
				if (t.requireContext) {
					int i = computeContextSize(t, vn);
					if (i == 0) {
						b1 = true;
						break;
					} else
						t.setContextSize(i);
				}
				t = t.nextP;
			}
			if (b1) {
				if (state == FORWARD) {
					state = BACKWARD;
					currentStep = currentStep.prevS;
				} else
					state = END;
				break;
			}
			if ((currentStep.nt_eval || currentStep.nt.eval2(vn)) 
					&& ((!currentStep.hasPredicate) || currentStep.evalPredicates(vn))) {
				if (currentStep.nextS != null) {
					state = FORWARD;
					currentStep = currentStep.nextS;
				} else {
					state = TERMINAL;
					if (vn.atTerminal)
						result = vn.LN;
					else
						result = vn.getCurrentIndex();
					if (isUnique(result))
						return result;
				}
			} else {
				if (currentStep.hasPredicate)
					currentStep.resetP(vn);
				if (state == START)
					state = END;
				else
					state = BACKWARD;
			}
			break;

		case END:
			currentStep = null;
			// reset();
			return -1;

		case BACKWARD:
		case TERMINAL:
			if (currentStep.prevS != null) {
				state = BACKWARD;
				currentStep = currentStep.prevS;
			} else {
				state = END;
			}
			break;

		default:
			throw new XPathEvalException("unknown state");
		}
		return -2;
	}
	
	
	final protected int process_namespace(VTDNav vn)
	throws XPathEvalException,NavException {
	    AutoPilot ap = null;
	    boolean b1 = false;
	    Predicate t= null;
	    int temp;
		switch( state){
		case  START:
		case  FORWARD:
		    
	        t = currentStep.p;
	        while(t!=null){
	            if (t.requireContext){
	                int i = computeContextSize(t,vn);
	                if (i==0){
	                    b1 = true;
	                    break;
	                }else
	                    t.setContextSize(i);
	            }
	            t = t.nextP;
	        }
	        if (b1){
	            if (state == FORWARD){
	                state = BACKWARD;
	                currentStep = currentStep.prevS;
	            }else 
	                state = END;
	            break;
	        }
	        
			if (vn.atTerminal){
				if (state ==START)
					state = END;
				else {
					state = BACKWARD;
					currentStep  = currentStep.prevS;
				}
			} else {
				
                if (currentStep.ft ) {
                    if (currentStep.o == null)
                        currentStep.o = ap = new AutoPilot(vn);
                    else {
                        ap = (AutoPilot) currentStep.o;
                        ap.bind(vn);
                        //ap.set_ft(true);
                    }
                    if (currentStep.nt.testType == NodeTest.NODE)
                    	ap.selectNameSpace("*");
                    else
                    	ap.selectNameSpace(currentStep.nt.nodeName);
                    currentStep.ft = false;
                }
                if (state == START)
                    state = END;
                vn.push2();
                //vn.setAtTerminal(true);
                while ((temp = ap.iterateNameSpace()) != -1) {
                    if ((!currentStep.hasPredicate) || currentStep.evalPredicates(vn)) {
                        break;
                    }
                }
                if (temp == -1) {
                	vn.pop2();
                    currentStep.ft = true;
                    if (currentStep.hasPredicate)
                    	currentStep.resetP(vn);
                    vn.atTerminal=(false);
                    if (state == FORWARD) {
                        state = BACKWARD;
                        currentStep = currentStep.prevS;
                    }
                } else {
                	vn.atTerminal=(true);
                    if (currentStep.nextS != null) {
                        vn.LN = temp;
                        state = FORWARD;
                        currentStep = currentStep.nextS;
                    } else {
                        //vn.pop();
                        state = TERMINAL;
                        if (isUnique(temp)) {
                            vn.LN = temp;
                            return temp;
                        }
                    }

                }
            }
			break;
			
		case  END:
			currentStep = null;
			// reset();
	  		return -1;
	  		
		case  BACKWARD:
			ap = (AutoPilot) currentStep.o;
			//vn.push();
			while( (temp = ap.iterateNameSpace()) != -1){
				if ((!currentStep.hasPredicate)|| currentStep.evalPredicates(vn)){
					break;
				}							
			}
			if (temp == -1) {
				vn.pop2();
				currentStep.ft = true;
				if (currentStep.hasPredicate)
					currentStep.resetP(vn);
				vn.atTerminal=(false);
				if (currentStep.prevS != null) {
					state =  BACKWARD;
					currentStep = currentStep.prevS;
				} else
					state =  END;
			} else {
				if (currentStep.nextS != null) {
					state =  FORWARD;
					currentStep = currentStep.nextS;
				} else {
					state =  TERMINAL;
					if ( isUnique(temp)){
					    vn.LN = temp;
						return temp;
					}
				}
			}
			break;
			
		case  TERMINAL:
			ap = (AutoPilot) currentStep.o;
			while( (temp = ap.iterateNameSpace()) != -1){
				if (!currentStep.hasPredicate || currentStep.evalPredicates(vn)){
					break;
				}							
			}
			if (temp != -1) 
				if (isUnique(temp)){
				    vn.LN = temp;
					return temp;
				}
			vn.atTerminal=(false);
			if (currentStep.hasPredicate)
				currentStep.resetP(vn);
			if (currentStep.prevS == null) {
				currentStep.ft = true;
				vn.pop2();
				 state =  END;
			} else {
				 state =  BACKWARD;
				 vn.pop2();
				currentStep.ft = true;
				currentStep = currentStep.prevS;
			}
			
			break;					
		
		default:
			throw new  XPathEvalException("unknown state");
	}
	    return -2;
	}
	
	final protected int process_following_sibling2(VTDNav vn)
	throws XPathEvalException,NavException{
	    boolean b = false, b1 = false;
	    Predicate t= null;
	    int result;
		switch( state){
		  case  START:
		  case  FORWARD:

  	        t = currentStep.p;
	        while(t!=null){
	            if (t.requireContext){
	                int i = computeContextSize(t,vn);
	                if (i==0){
	                    b1 = true;
	                    break;
	                }else
	                    t.setContextSize(i);
	            }
	            t = t.nextP;
	        }
	        if (b1){
	            if (state == FORWARD){
	                state = BACKWARD;
	                currentStep = currentStep.prevS;
	            }else 
	                state = END;
	            break;
	        }
		  	if ( state ==  START)
		  		 state =  END;
		  	else
		  		 state =  BACKWARD;
		  	vn.push2();
		  	while (vn.toNode(VTDNav.NS)){
		  		if ((currentStep.nt_eval || currentStep.nt.eval2(vn)) 
		  				&& ((!currentStep.hasPredicate) || currentStep.evalPredicates(vn))){
		  			if (currentStep.nextS!=null){
		  				 state =  FORWARD;
		  				currentStep = currentStep.nextS;
		  				break;
		  			} else {
		  				 state =  TERMINAL;
		  				result = vn.getCurrentIndex();
						if ( isUnique(result))
							return result;
		  			}
		  		}
		  	}
		  	if (state ==END){
		  		if (currentStep.hasPredicate)
		  			currentStep.resetP(vn);	
		  		vn.pop2();
		  	}else  	if ( state ==  BACKWARD){
		  		if (currentStep.hasPredicate)
		  			currentStep.resetP(vn);	
		  		vn.pop2();
		  		currentStep = currentStep.prevS;
		  	}
		    break;
		  	 
		  case  END:
		  	currentStep = null;
		  	// reset();
		  	return -1;
		  	
		  case  BACKWARD:
		  	while (vn.toNode(VTDNav.NS)){
		  		if ((currentStep.nt_eval || currentStep.nt.eval2(vn)) 
		  				&& ((!currentStep.hasPredicate) || currentStep.evalPredicates(vn))){
		  			if (currentStep.nextS!=null){
		  				 state =  FORWARD;
		  				currentStep = currentStep.nextS;
		  				b = true;
		  				break;
		  			} else {
		  				 state =  TERMINAL;
		  				result = vn.getCurrentIndex();
						if ( isUnique(result))
							return result;
		  			}
		  		}
		  	}
		    if (b==false){
		    	vn.pop2();
		    	if (currentStep.hasPredicate)
		    		currentStep.resetP(vn);
		    	if (currentStep.prevS==null){
		    		 state =  END;
		    	}else{
		    		 state =  BACKWARD;
		    		currentStep = currentStep.prevS;
		    	}
		    }
		  	break;
		  
		  case  TERMINAL:
		  	while (vn.toNode(VTDNav.NS)){
		  		if ((currentStep.nt_eval || currentStep.nt.eval2(vn)) 
		  				&& ((!currentStep.hasPredicate) || currentStep.evalPredicates(vn))){
		  			// state =  TERMINAL;
		  			result = vn.getCurrentIndex();
					if ( isUnique(result))
						return result;
		  		}
		  	}
		  	vn.pop2();
		  	if (currentStep.hasPredicate)
		  		currentStep.resetP(vn);
		  	if(currentStep.prevS!=null){
		  		currentStep = currentStep.prevS;
		  		state =  BACKWARD;
		  	}else{
		  		state =  END;
		  	}
		  	break;

		  default:
			throw new  XPathEvalException("unknown state");
		}
	    return -2;
	}
	
	
	final protected int process_following_sibling(VTDNav vn)
	throws XPathEvalException,NavException{
	    boolean b = false, b1 = false;
	    Predicate t= null;
	    int result;
		switch( state){
		  case  START:
		  case  FORWARD:

  	        t = currentStep.p;
	        while(t!=null){
	            if (t.requireContext){
	                int i = computeContextSize(t,vn);
	                if (i==0){
	                    b1 = true;
	                    break;
	                }else
	                    t.setContextSize(i);
	            }
	            t = t.nextP;
	        }
	        if (b1){
	            if (state == FORWARD){
	                state = BACKWARD;
	                currentStep = currentStep.prevS;
	            }else 
	                state = END;
	            break;
	        }
		  	if ( state ==  START)
		  		 state =  END;
		  	else
		  		 state =  BACKWARD;
		  	vn.push2();
		  	while (vn.toElement(VTDNav.NS)){
		  		if ((currentStep.nt_eval || currentStep.nt.eval(vn)) 
		  				&& ((!currentStep.hasPredicate) || currentStep.evalPredicates(vn))){
		  			if (currentStep.nextS!=null){
		  				 state =  FORWARD;
		  				currentStep = currentStep.nextS;
		  				break;
		  			} else {
		  				 state =  TERMINAL;
		  				result = vn.getCurrentIndex();
						if ( isUnique(result))
							return result;
		  			}
		  		}
		  	}
		  	if (state ==END){
		  		if (currentStep.hasPredicate)
		  			currentStep.resetP(vn);
		  		vn.pop2();
		  	}else if ( state ==  BACKWARD){	
		  		if (currentStep.hasPredicate)
		  			currentStep.resetP(vn);
		  		vn.pop2();
		  		currentStep = currentStep.prevS;				  		
		  	}
		    break;
		  	 
		  case  END:
		  	currentStep = null;
		  	// reset();
		  	return -1;
		  	
		  case  BACKWARD:
		  	while (vn.toElement(VTDNav.NS)){
		  		if ((currentStep.nt_eval || currentStep.nt.eval(vn)) 
		  				&& ((!currentStep.hasPredicate) || currentStep.evalPredicates(vn))){
		  			if (currentStep.nextS!=null){
		  				state =  FORWARD;
		  				currentStep = currentStep.nextS;
		  				b = true;
		  				break;
		  			} else {
		  				state =  TERMINAL;
		  				result = vn.getCurrentIndex();
						if ( isUnique(result))
							return result;
		  			}
		  		}
		  	}
		    if (b==false){
		    	vn.pop2();
		    	if (currentStep.hasPredicate)
		    		currentStep.resetP(vn);
		    	if (currentStep.prevS==null){
		    		 state =  END;
		    	}else{
		    		 state =  BACKWARD;
		    		currentStep = currentStep.prevS;
		    	}
		    }
		  	break;
		  
		  case  TERMINAL:
		  	while (vn.toElement(VTDNav.NS)){
		  		if ((currentStep.nt_eval || currentStep.nt.eval(vn)) 
		  				&& ((!currentStep.hasPredicate) || currentStep.evalPredicates(vn))){
		  			// state =  TERMINAL;
		  			result = vn.getCurrentIndex();
					if ( isUnique(result))
						return result;
		  		}
		  	}
		  	vn.pop2();
		  	if (currentStep.hasPredicate)
		  		currentStep.resetP(vn);
		  	if(currentStep.prevS!=null){
		  		currentStep = currentStep.prevS;
		  		 state =  BACKWARD;
		  	}else{
		  		 state =  END;
		  	}
		  	break;

		  default:
			throw new XPathEvalException("unknown state");
		}
	    return -2;
	}
	
	final protected int process_preceding_sibling(VTDNav vn)
	throws XPathEvalException,NavException {
	    boolean b = false, b1 = false;
	    Predicate t= null;
	    int result;
	    switch(state){
		  case  START:
		  case  FORWARD:
  	        t = currentStep.p;
	        while(t!=null){
	            if (t.requireContext){
	                int i = computeContextSize(t,vn);
	                if (i==0){
	                    b1 = true;
	                    break;
	                }else
	                    t.setContextSize(i);
	            }
	            t = t.nextP;
	        }
	        if (b1){
	            if (state == FORWARD){
	                state = BACKWARD;
	                currentStep = currentStep.prevS;
	            }else 
	                state = END;
	            break;
	        }  
		  	if ( state ==  START)
		  		 state =  END;
		  	else
		  		 state =  BACKWARD;
		  	vn.push2();
		  	while (vn.toElement(VTDNav.PS)){
		  		if ((currentStep.nt_eval || currentStep.nt.eval(vn)) 
		  				&& ((!currentStep.hasPredicate) || currentStep.evalPredicates(vn))){
		  			if (currentStep.nextS!=null){
		  				 state =  FORWARD;
		  				currentStep = currentStep.nextS;
		  				break;
		  			} else {
		  				 state =  TERMINAL;
		  				result = vn.getCurrentIndex();
						if ( isUnique(result))
							return result;
		  			}
		  		}
		  	}
		  	if (state ==END){
		  		if (currentStep.hasPredicate)
		  			currentStep.resetP(vn);
		  		vn.pop2();
		  	}else if ( state ==  BACKWARD){
		  		if (currentStep.hasPredicate)
			  		currentStep.resetP(vn);
			  	vn.pop2();
		  		currentStep = currentStep.prevS;				  		
		  	}
		  	 break;
		  	 
		  case  END:
		  	currentStep = null;
		  	// reset();
		  	return -1;
		  
		  case  BACKWARD:
		  	while (vn.toElement(VTDNav.PS)){
		  		if ((currentStep.nt_eval || currentStep.nt.eval(vn)) 
		  				&& ((!currentStep.hasPredicate) || currentStep.evalPredicates(vn))){
		  			if (currentStep.nextS!=null){
		  				 state =  FORWARD;
		  				currentStep = currentStep.nextS;
		  				b = true;
		  				break;
		  			} else {
		  				 state =  TERMINAL;
		  				result = vn.getCurrentIndex();
						if ( isUnique(result))
							return result;
		  			}
		  		}
		  	}
		    if (b==false){
		    	vn.pop2();
		    	if (currentStep.hasPredicate)
			  		currentStep.resetP(vn);
		    	if (currentStep.prevS==null){
		    		 state =  END;
		    	}else{
		    		 state =  BACKWARD;
		    		currentStep = currentStep.prevS;
		    	}
		    }
		  	break;
		  
		  case  TERMINAL:
		  	while (vn.toElement(VTDNav.PS)){
		  		if ((currentStep.nt_eval || currentStep.nt.eval(vn)) 
		  				&& ((!currentStep.hasPredicate) || currentStep.evalPredicates(vn))){
		  			// state =  TERMINAL;
		  			result = vn.getCurrentIndex();
					if ( isUnique(result))
						return result;
		  		}
		  	}
		  	if (currentStep.hasPredicate)
		  		currentStep.resetP(vn);
		  	vn.pop2();
		  	if(currentStep.prevS!=null){
		  		currentStep = currentStep.prevS;
		  		state =  BACKWARD;
		  	}else{
		  		state =  END;
		  	}
		  	break;
		  
		  default:
			throw new  XPathEvalException("unknown state");
		}
	    return -2;
	}
	
	final protected int process_preceding_sibling2(VTDNav vn)
	throws XPathEvalException,NavException {
	    boolean b = false, b1 = false;
	    Predicate t= null;
	    int result;
	    switch(state){
		  case  START:
		  case  FORWARD:
  	        t = currentStep.p;
	        while(t!=null){
	            if (t.requireContext){
	                int i = computeContextSize(t,vn);
	                if (i==0){
	                    b1 = true;
	                    break;
	                }else
	                    t.setContextSize(i);
	            }
	            t = t.nextP;
	        }
	        if (b1){
	            if (state == FORWARD){
	                state = BACKWARD;
	                currentStep = currentStep.prevS;
	            }else 
	                state = END;
	            break;
	        }  
		  	if ( state ==  START)
		  		 state =  END;
		  	else
		  		 state =  BACKWARD;
		  	vn.push2();
		  	while (vn.toNode(VTDNav.PS)){
		  		if ((currentStep.nt_eval || currentStep.nt.eval2(vn)) 
		  				&& ((!currentStep.hasPredicate) || currentStep.evalPredicates(vn))){
		  			if (currentStep.nextS!=null){
		  				 state =  FORWARD;
		  				currentStep = currentStep.nextS;
		  				break;
		  			} else {
		  				 state =  TERMINAL;
		  				result = vn.getCurrentIndex();
						if ( isUnique(result))
							return result;
		  			}
		  		}
		  	}
		  	if (state ==END){
		  		if (currentStep.hasPredicate)
		  			currentStep.resetP(vn);
		  		vn.pop2();
		  	}else if ( state ==  BACKWARD){
		  		if (currentStep.hasPredicate)
			  		currentStep.resetP(vn);
			  	vn.pop2();
		  		currentStep = currentStep.prevS;				  		
		  	}
		  	break;
		  	 
		  case  END:
		  	currentStep = null;
		  	// reset();
		  	return -1;
		  
		  case  BACKWARD:
		  	while (vn.toNode(VTDNav.PS)){
		  		if ((currentStep.nt_eval || currentStep.nt.eval2(vn)) 
		  				&& ((!currentStep.hasPredicate) || currentStep.evalPredicates(vn))){
		  			if (currentStep.nextS!=null){
		  				 state =  FORWARD;
		  				currentStep = currentStep.nextS;
		  				b = true;
		  				break;
		  			} else {
		  				 state =  TERMINAL;
		  				result = vn.getCurrentIndex();
						if ( isUnique(result))
							return result;
		  			}
		  		}
		  	}
		    if (b==false){
		    	vn.pop2();
		    	if (currentStep.hasPredicate)
		    		currentStep.resetP(vn);
		    	if (currentStep.prevS==null){
		    		 state =  END;
		    	}else{
		    		 state =  BACKWARD;
		    		currentStep = currentStep.prevS;
		    	}
		    }
		  	break;
		  
		  case  TERMINAL:
		  	while (vn.toNode(VTDNav.PS)){
		  		if ((currentStep.nt_eval || currentStep.nt.eval2(vn)) 
		  				&& ((!currentStep.hasPredicate) || currentStep.evalPredicates(vn))){
		  			// state =  TERMINAL;
		  			result = vn.getCurrentIndex();
					if ( isUnique(result))
						return result;
		  		}
		  	}
		  	vn.pop2();
		  	if (currentStep.hasPredicate)
		  		currentStep.resetP(vn);
		  	if(currentStep.prevS!=null){
		  		currentStep = currentStep.prevS;
		  		 state =  BACKWARD;
		  	}else{
		  		 state =  END;
		  	}
		  	break;
		  
		  default:
			throw new  XPathEvalException("unknown state");
		}
	    return -2;
	}
	
	final protected int process_attribute(VTDNav vn)
	throws XPathEvalException,NavException {
	    AutoPilot ap = null;
	    boolean b1 = false;
	    Predicate t= null;
	    int temp;
		switch( state){
		case  START:
		case  FORWARD:
		    
	        t = currentStep.p;
	        while(t!=null){
	            if (t.requireContext){
	                int i = computeContextSize(t,vn);
	                if (i==0){
	                    b1 = true;
	                    break;
	                }else
	                    t.setContextSize(i);
	            }
	            t = t.nextP;
	        }
	        if (b1){
	            if (state == FORWARD){
	                state = BACKWARD;
	                currentStep = currentStep.prevS;
	            }else 
	                state = END;
	            break;
	        }
	        
			if (vn.atTerminal){
				if (state ==START)
					state = END;
				else {
					state = BACKWARD;
					currentStep  = currentStep.prevS;
				}
			} else {
                if (currentStep.ft) {
                    if (currentStep.o == null)
                        currentStep.o = ap = new AutoPilot(vn);
                    else {
                        ap = (AutoPilot) currentStep.o;
                        ap.bind(vn);
                        //ap.set_ft(true);
                    }
                    if (currentStep.nt.testType == NodeTest.NODE)
                    	ap.selectAttr("*");
                    else if (currentStep.nt.localName != null)
                        ap.selectAttrNS(currentStep.nt.URL,
                                currentStep.nt.localName);
                    else
                        ap.selectAttr(currentStep.nt.nodeName);
                    currentStep.ft = false;
                }
                if (state == START)
                    state = END;
                vn.atTerminal=(true);
                while ((temp = ap.iterateAttr2()) != -1) {
                    if ( !currentStep.hasPredicate || currentStep.evalPredicates(vn)) {
                        break;
                    }
                }
                if (temp == -1) {
                    currentStep.ft = true;
                    if (currentStep.hasPredicate)
                    	currentStep.resetP(vn);
                    vn.atTerminal=(false);
                    if (state == FORWARD) {
                        state = BACKWARD;
                        currentStep = currentStep.prevS;
                    }
                } else {

                    if (currentStep.nextS != null) {
                        vn.LN = temp;
                        state = FORWARD;
                        currentStep = currentStep.nextS;
                    } else {
                        //vn.pop();
                        state = TERMINAL;
                        if (isUnique(temp)) {
                            vn.LN = temp;
                            return temp;
                        }
                    }

                }
            }
			break;
			
		case  END:
			currentStep = null;
			// reset();
	  		return -1;
	  		
		case  BACKWARD:
			ap = (AutoPilot) currentStep.o;
			//vn.push();
			while( (temp = ap.iterateAttr2()) != -1){
				if (!currentStep.hasPredicate || currentStep.evalPredicates(vn)){
					break;
				}							
			}
			if (temp == -1) {
				currentStep.ft = true;
				if (currentStep.hasPredicate)
					 currentStep.resetP(vn);
				vn.atTerminal=(false);
				if (currentStep.prevS != null) {
					state =  BACKWARD;
					currentStep = currentStep.prevS;
				} else
					state =  END;
			} else {
				if (currentStep.nextS != null) {
					state =  FORWARD;
					currentStep = currentStep.nextS;
				} else {
					state =  TERMINAL;
					if ( isUnique(temp)){
					    vn.LN = temp;
						return temp;
					}
				}
			}
			break;
			
		case  TERMINAL:
			ap = (AutoPilot) currentStep.o;
			while( (temp = ap.iterateAttr2()) != -1){
				if (!currentStep.hasPredicate || currentStep.evalPredicates(vn)){
					break;
				}							
			}
			if (temp != -1) 
				if (isUnique(temp)){
				    vn.LN = temp;
					return temp;
				}
			vn.atTerminal=(false);
   		    if (currentStep.hasPredicate)
   		    	currentStep.resetP(vn);
   		    currentStep.ft = true;
			if (currentStep.prevS == null) {
				state =  END;
			} else {
				state =  BACKWARD;
				currentStep = currentStep.prevS;
			}
			
			break;					
		
		default:
			throw new  XPathEvalException("unknown state");
	}
	    return -2;
	}
	
	public int evalNodeSet(VTDNav vn) 
    	throws NavException,XPathEvalException{
        int result;
		if (currentStep == null) {
			if ( pathType ==  ABSOLUTE_PATH){
				vn.context[0]=-1;
				vn.atTerminal = false;
			}
			currentStep =  s;
			if (currentStep == null){
				if (state ==  START){
					state =  END;
					return 0;
				}
				else{
					return -1;
				}
			}
		}
		
		while (true) {
			switch (currentStep.axis_type) {

			case AxisType.CHILD0:
				if ( (result = process_child(vn))!=-2){
					return result;
				}
	    		break;
			case AxisType.CHILD:
			    if ( (result = process_child2(vn))!=-2){
				   return result;
			    }
			    break;
			case AxisType.DESCENDANT_OR_SELF0: 
			case AxisType.DESCENDANT0: 
			case AxisType.PRECEDING0: 
			case AxisType.FOLLOWING0:
				if ((result = process_DDFP(vn))!= -2)
			        return result;
			    break;
			case AxisType.DESCENDANT_OR_SELF:
			case AxisType.DESCENDANT:
			case AxisType.PRECEDING:								
			case AxisType.FOLLOWING:
			    if ((result = process_DDFP2(vn))!= -2)
			        return result;
			    break;
			case AxisType.PARENT:
			    if ((result = process_parent(vn))!= -2)
			        return result;
			    break;
			case AxisType.ANCESTOR:
			    if ((result = process_ancestor2(vn))!= -2)
			        return result;
			    break;
			case AxisType.ANCESTOR_OR_SELF:
			    if ((result = process_ancestor_or_self2(vn))!= -2)
			        return result;
			    break;
			case AxisType.SELF:
			    if ((result = process_self2(vn))!= -2)
			        return result;
			    break;
			case AxisType.FOLLOWING_SIBLING:
			    if ((result = process_following_sibling2(vn))!= -2)
			        return result;
			    break;
			case AxisType.FOLLOWING_SIBLING0:
			    if ((result = process_following_sibling(vn))!= -2)
			        return result;
			    break;
			case AxisType.PRECEDING_SIBLING:
			    if ((result = process_preceding_sibling2(vn))!= -2)
			        return result;
			    break;
			case AxisType.PRECEDING_SIBLING0:
			    if ((result = process_preceding_sibling(vn))!= -2)
			        return result;
			    break;
			case AxisType.ATTRIBUTE:
			    if ((result = process_attribute(vn))!= -2)
			        return result;
			    break;
			default:
				if ((result = process_namespace(vn))!= -2)
			        return result;
			}
		}
        
    }
    
	final public boolean isString(){
	    return false;
	}
	
	final public boolean isBoolean(){
	    return false;
	}
	
	// to support computer context size 
	// needs to add 
	final public boolean requireContextSize(){
	    return false;
	}
	
	// 
	final public void setContextSize(int size){	    
	}
	
	final protected int computeContextSize4PrecedingSibling(Predicate p, VTDNav vn)
	throws NavException,XPathEvalException{
		int i=0;
		vn.push2();
		while(vn.toElement(VTDNav.PREV_SIBLING)){
		    if (currentStep.eval(vn,p)){
		        i++;
		    }
		}			    
		vn.pop2();
		currentStep.resetP(vn,p);
		currentStep.out_of_range=false;
		//currentStep.o = ap;
		return i;
	}
	
	final protected int computeContextSize4PrecedingSibling2(Predicate p, VTDNav vn)
	throws NavException,XPathEvalException{	
		int i=0;
		vn.push2();
		while(vn.toNode(VTDNav.PREV_SIBLING)){
		    if (currentStep.eval2(vn,p)){
		        i++;
		    }
		}			    
		vn.pop2();
		currentStep.resetP(vn,p);
		currentStep.out_of_range=false;
		//currentStep.o = ap;
		return i;
	}
	
	final protected int computeContextSize4FollowingSibling(Predicate p, VTDNav vn)
	throws NavException,XPathEvalException{		
		int i=0;
		//AutoPilot ap = (AutoPilot)currentStep.o;
		vn.push2();
		while(vn.toElement(VTDNav.NEXT_SIBLING)){
		    if (currentStep.eval(vn,p)){
		        i++;
		    }
		}			    
	    vn.pop2();
	   	currentStep.resetP(vn,p);
		currentStep.out_of_range=false;
		//currentStep.o = ap;
		return i;
	}
	
	final protected int computeContextSize4FollowingSibling2(Predicate p, VTDNav vn)
	throws NavException,XPathEvalException{		
		int i=0;
		vn.push2();
		while(vn.toNode(VTDNav.NEXT_SIBLING)){
		    if (currentStep.eval2(vn,p)){
		        i++;
		    }
		}			    
	    vn.pop2();
	    currentStep.resetP(vn,p);
		currentStep.out_of_range=false;
		return i;
	}
	
	
	final protected int computeContextSize4Self(Predicate p, VTDNav vn)
	throws NavException,XPathEvalException{		
		int i = 0;
		AutoPilot ap = (AutoPilot)currentStep.o;
		//if (vn.toElement(VTDNav.PARENT)){
		if (currentStep.eval(vn,p)){
		   i++;
		}
		//}			    
		currentStep.resetP(vn,p);
		currentStep.out_of_range=false;
		currentStep.o = ap;
		return i;
	}
	
	final protected int computeContextSize4Self2(Predicate p, VTDNav vn)
	throws NavException,XPathEvalException{		
		int i = 0;
		AutoPilot ap = (AutoPilot)currentStep.o;
		//if (vn.toNode(VTDNav.PARENT)){
		if (currentStep.eval2(vn,p)){
		   i++;
		}
		//}			    
		currentStep.resetP(vn,p);
		currentStep.out_of_range=false;
		currentStep.o = ap;
		return i;
	}
	
	
	
	final protected int computeContextSize4AncestorOrSelf(Predicate p, VTDNav vn)
	throws NavException,XPathEvalException{
		int i=0;
		AutoPilot ap = (AutoPilot)currentStep.o;
		vn.push2();
		i = 0;
		do {
		    if (currentStep.eval(vn, p)) {
            	i++;
		       }
		}while(vn.toElement(VTDNav.PARENT));
		vn.pop2();
		currentStep.resetP(vn,p);
		currentStep.out_of_range=false;
		currentStep.o = ap;
		return i;
	}
	
	final protected int computeContextSize4AncestorOrSelf2(Predicate p, VTDNav vn)
	throws NavException,XPathEvalException{
		int i=0;
		AutoPilot ap = (AutoPilot)currentStep.o;
		vn.push2();
		i = 0;
		do {
		    if (currentStep.eval2(vn, p)) {
            	i++;
		    }
		}while(vn.toNode(VTDNav.PARENT));
		vn.pop2();
		currentStep.resetP(vn,p);
		currentStep.out_of_range=false;
		currentStep.o = ap;
		return i;
	}
	
	
	final protected int computeContextSize4Child2(Predicate p, VTDNav vn)
	throws NavException,XPathEvalException{
		int i=0;
		boolean b = vn.toNode(VTDNav.FIRST_CHILD);
		if (b) {
		    do {
		        if (currentStep.eval2(vn, p)) {
                	i++;
		        }
		    } while (vn.toNode(VTDNav.NS));	    		    
		    vn.toNode(VTDNav.PARENT);
		    currentStep.resetP(vn,p);
		    currentStep.out_of_range=false;
		    return i;
		} else
		    return 0;		
	}
	
	final protected int computeContextSize4Parent(Predicate p, VTDNav vn)
	throws NavException,XPathEvalException{
		int i=0;
		AutoPilot ap = (AutoPilot)currentStep.o;
		vn.push2();
		i = 0;
		if (vn.toElement(VTDNav.PARENT)){
		    if (currentStep.eval(vn,p)){
		        i++;
		    }
		}			    
		vn.pop2();
		currentStep.resetP(vn,p);
		currentStep.out_of_range=false;
		currentStep.o = ap;
		return i;
	}
	
	final protected int computeContextSize4Parent2(Predicate p, VTDNav vn)
	throws NavException,XPathEvalException{
		int i=0;
		AutoPilot ap = (AutoPilot)currentStep.o;
		vn.push2();
		i = 0;
		if (vn.toNode(VTDNav.PARENT)){
		    if (currentStep.eval2(vn,p)){
		        i++;
		    }
		}			    
		vn.pop2();
		currentStep.resetP(vn,p);
		currentStep.out_of_range=false;
		currentStep.o = ap;
		return i;
	}
	
	final protected int computeContextSize4Ancestor2(Predicate p, VTDNav vn)
	throws NavException,XPathEvalException{
		int i=0;
		AutoPilot ap = (AutoPilot)currentStep.o;
		 vn.push2();
			i = 0;
			while (vn.toNode(VTDNav.PARENT)) {
			    if (currentStep.eval2(vn, p)) {
             	i++;
		        }
			}				
			vn.pop2();
			currentStep.resetP(vn,p);
			currentStep.out_of_range=false;
			currentStep.o = ap;
			return i;
	}
	
	final protected int computeContextSize4DDFP(Predicate p, VTDNav vn)
	throws NavException,XPathEvalException{
	    String helper = null;
	    int i=0;
	    AutoPilot ap = (AutoPilot)currentStep.o;
		if (currentStep.nt.testType == NodeTest.NODE){
		    helper = "*";
		}else if (currentStep.nt.testType == NodeTest.NAMETEST){
			helper = currentStep.nt.nodeName;
		}else
			throw new XPathEvalException("can't run descendant "
					+ "following, or following-sibling axis over comment(), pi(), and text()");
		if (ap==null)
			ap = new AutoPilot(vn);
		else
			ap.bind(vn);
		if (currentStep.axis_type == AxisType.DESCENDANT_OR_SELF0 )
			if (currentStep.nt.testType == NodeTest.NODE)
				ap.setSpecial(true);
			else
				ap.setSpecial(false);
		//currentStep.o = ap = new AutoPilot(vn);
	    if (currentStep.axis_type == AxisType.DESCENDANT_OR_SELF0)
	        if (currentStep.nt.localName!=null)
	            ap.selectElementNS(currentStep.nt.URL,currentStep.nt.localName);
	        else 
	            ap.selectElement(helper);
		else if (currentStep.axis_type == AxisType.DESCENDANT0)
		    if (currentStep.nt.localName!=null)
		        ap.selectElementNS_D(currentStep.nt.URL,currentStep.nt.localName);
		    else 
		        ap.selectElement_D(helper);
		else if (currentStep.axis_type == AxisType.PRECEDING0)
		    if (currentStep.nt.localName!=null)
		        ap.selectElementNS_P(currentStep.nt.URL,currentStep.nt.localName);
		    else 
		        ap.selectElement_P(helper);
		else 
		    if (currentStep.nt.localName!=null)
		        ap.selectElementNS_F(currentStep.nt.URL,currentStep.nt.localName);
		    else 
		        ap.selectElement_F(helper);
	    vn.push2();
		while(ap.iterate()){
			if (currentStep.evalPredicates(vn,p)){
				i++;
			}
		}
		vn.pop2();
		currentStep.resetP(vn,p);
		currentStep.out_of_range=false;
		currentStep.o = ap;
		return i;
	}
	
	final protected int computeContextSize4DDFP2(Predicate p, VTDNav vn)
	throws NavException,XPathEvalException{
	    int i=0;
	    AutoPilot ap = (AutoPilot)currentStep.o;
		
		if (ap==null)
			ap = new AutoPilot(vn);
		else
			ap.bind(vn);
		
		//currentStep.o = ap = new AutoPilot(vn);
	    if (currentStep.axis_type == AxisType.DESCENDANT_OR_SELF)
	       ap.selectNode();
		else if (currentStep.axis_type == AxisType.DESCENDANT)
		   ap.selectDescendantNode();
		else if (currentStep.axis_type == AxisType.PRECEDING)
		   ap.selectPrecedingNode();
		else 
		   ap.selectFollowingNode();
	    vn.push2();
		while(ap.iterate2()){
			if (currentStep.eval2(vn,p)){
				i++;
			}
		}
		vn.pop2();
		currentStep.resetP(vn,p);
		currentStep.out_of_range=false;
		currentStep.o = ap;
		return i;
	}
	
	final protected int computeContextSize4Ancestor(Predicate p, VTDNav vn)
	throws NavException,XPathEvalException{
		int i=0;
		AutoPilot ap = (AutoPilot)currentStep.o;
		vn.push2();
		i = 0;
		while (vn.toElement(VTDNav.PARENT)) {
		    if (currentStep.eval(vn, p)) {
            	i++;
		    }	
		}				
		vn.pop2();
		currentStep.resetP(vn,p);
		currentStep.out_of_range=false;
		currentStep.o = ap;
		return i;		
	}
	
	final protected int computeContextSize4Child(Predicate p, VTDNav vn)
	throws NavException,XPathEvalException{
		int i=0;
		
    	boolean b = vn.toElement(VTDNav.FIRST_CHILD);
    	if (b) {
    	    do {
    	        if (currentStep.eval(vn, p)) {
                   	i++;
    	        }
    	    } while (vn.toElement(VTDNav.NS));	    		    
    	    vn.toElement(VTDNav.PARENT);
    	    currentStep.resetP(vn,p);
    	    currentStep.out_of_range=false;
    	    return i;
    	} else
    	    return 0;
		
	}
	final public int computeContextSize(Predicate p, VTDNav vn)
		throws NavException,XPathEvalException{
	    
	    boolean b = false;
	    //Predicate tp = null;
	    int i = 0;
	    AutoPilot ap = (AutoPilot)currentStep.o;
	    switch(currentStep.axis_type){
	    	case AxisType.CHILD0:
	    		return computeContextSize4Child(p,vn);
	    	case AxisType.CHILD:
	    	    return computeContextSize4Child2(p,vn);
	    	case AxisType.DESCENDANT_OR_SELF0:
			case AxisType.DESCENDANT0:
			case AxisType.PRECEDING0:								
			case AxisType.FOLLOWING0:			    
			    return computeContextSize4DDFP(p,vn);	
			case AxisType.DESCENDANT_OR_SELF:
			case AxisType.DESCENDANT:
			case AxisType.PRECEDING:								
			case AxisType.FOLLOWING:			    
			    return computeContextSize4DDFP2(p,vn);			  
			case AxisType.PARENT:
				return computeContextSize4Parent2(p,vn);				
			case AxisType.ANCESTOR:
			    return computeContextSize4Ancestor2(p,vn);				
			case AxisType.ANCESTOR_OR_SELF:
				return computeContextSize4AncestorOrSelf2(p,vn);				
			case AxisType.SELF:
				return computeContextSize4Self2(p,vn);			    
			case AxisType.FOLLOWING_SIBLING:
				return computeContextSize4FollowingSibling2(p,vn);
			case AxisType.FOLLOWING_SIBLING0:
				return computeContextSize4FollowingSibling(p,vn);
			case AxisType.PRECEDING_SIBLING:			    
				return computeContextSize4PrecedingSibling2(p,vn);
			case AxisType.PRECEDING_SIBLING0:			    
				return computeContextSize4PrecedingSibling(p,vn);
			case AxisType.ATTRIBUTE:
				if (ap==null)
					ap = new AutoPilot(vn);
				else
					ap.bind(vn);
				if (currentStep.nt.testType == NodeTest.NODE)
					ap.selectAttr("*");
				else if (currentStep.nt.localName!=null)
				    ap.selectAttrNS(currentStep.nt.URL,
			            currentStep.nt.localName);
				else 
				    ap.selectAttr(currentStep.nt.nodeName);
				i = 0;
				while(ap.iterateAttr2()!=-1){
				    if (currentStep.evalPredicates(vn,p)){
				        i++;
				    }
				}
          		currentStep.resetP(vn,p);
          		currentStep.o = ap;
				return i;
			    
			case AxisType.NAMESPACE:
				if (ap==null)
					ap = new AutoPilot(vn);
				else
					ap.bind(vn);
				if (currentStep.nt.testType == NodeTest.NODE)
                	ap.selectNameSpace("*");
                else
                	ap.selectNameSpace(currentStep.nt.nodeName);
				i=0;
				vn.push2();
				while(ap.iterateNameSpace()!=-1){
				    if (currentStep.evalPredicates(vn,p)){
				        i++;
				    }
				}	    
				vn.pop2();
				currentStep.resetP(vn,p);
				currentStep.o = ap;
				return i;
	    	default:
	    	    throw new XPathEvalException("axis not supported");
	    }
	    //return 8;
	}
	
	public void setPosition(int pos){
	    
	}
	
	final public int adjust(int n) {
	    int i;
        if (pathType == RELATIVE_PATH) {
            i = Math.min(intHash.determineHashWidth(n),6); // hash width 64 
        } else {
            i = intHash.determineHashWidth(n);
        }
        if (ih!=null && i<= ih.e)
        {}
        else 
            ih = new intHash(i);
        Step temp = s;
        while(temp!=null){
			temp.adjust(n);
			temp = temp.nextS;
		}	
        return i;
	}
	
	final protected void selectNodeType(TextIter ti){
		if (currentStep.nt.testType == NodeTest.TEXT )
			ti.selectText();
		else if (currentStep.nt.testType == NodeTest.COMMENT )
			ti.selectComment();
		else if (currentStep.nt.testType == NodeTest.PI0 )
			ti.selectPI0();
		else {
			ti.selectPI1(currentStep.nt.nodeName);
		}
		
	}
	
	final protected int process_child2(VTDNav vn)throws XPathEvalException,NavException{
	    int result;
	    boolean b = false, b1 = false;
	    Predicate t= null;
	    
	    switch(state){
		case START:
			// first search for any predicate that
			// requires contextSize
			// if so, compute its context size
			// if size > 0
			// set context
			// if size ==0
			// immediately set the state to backward or end
			t = currentStep.p;
			while (t != null) {
				if (t.requireContext) {
					int i = computeContextSize(t, vn);
					if (i == 0) {
						b1 = true;
						break;
					} else
						t.setContextSize(i);
				}
				t = t.nextP;
			}
			if (b1) {
				state = END;
				break;
			}

			b = vn.toNode(VTDNav.FIRST_CHILD);
			state = END;
			if (b) {
				do {
					if ((currentStep.nt_eval || currentStep.nt.eval2(vn)) 
							&& ((!currentStep.hasPredicate) || currentStep.evalPredicates(vn))) {
						if (currentStep.nextS != null) {
							// currentStep.position++;
							state = FORWARD;
							currentStep = currentStep.nextS;							
						} else {
							state = TERMINAL;
							result = vn.getCurrentIndex();
							if (isUnique(result)) {
								return result;
							}
						}
						break;
					}
				} while (vn.toNode(VTDNav.NS));
				if (state == END)
					vn.toNode(VTDNav.PARENT);
			}
			break;

		case END:
			currentStep = null;
			// reset();
			return -1;

		case FORWARD:

			t = currentStep.p;
			while (t != null) {
				if (t.requireContext) {
					int i = computeContextSize(t, vn);
					if (i == 0) {
						b1 = true;
						break;
					} else
						t.setContextSize(i);
				}
				t = t.nextP;
			}
			if (b1) {
				currentStep = currentStep.prevS;
				state = BACKWARD;
				break;
			}

			state = BACKWARD;
			forward: if (vn.toNode(VTDNav.FC)) {
				do {
					if ((currentStep.nt_eval || currentStep.nt.eval2(vn)) 
							&& ((!currentStep.hasPredicate) || currentStep.evalPredicates(vn))) {
						if (currentStep.nextS != null) {
							state = FORWARD;
							currentStep = currentStep.nextS;
						} else {
							state = TERMINAL;
							result = vn.getCurrentIndex();
							if (isUnique(result))
								return result;
						}
						break forward;
					}
				} while (vn.toNode(VTDNav.NS));
				vn.toNode(VTDNav.P);
				if (currentStep.hasPredicate)
					currentStep.resetP(vn);
				currentStep = currentStep.prevS;
			} else {
				// vn.toElement(VTDNav.P);
				currentStep = currentStep.prevS;
			}

			break;

		case BACKWARD:
			if (currentStep.out_of_range){
				currentStep.out_of_range = false;
				if (currentStep.hasPredicate)
					currentStep.resetP(vn);
				transition_child(vn);
				break;
			}

			// currentStep = currentStep.prevS;
			//b = false;
			while (vn.toNode(VTDNav.NS)) {
				if ((currentStep.nt_eval || currentStep.nt.eval2(vn)) 
						&& ((!currentStep.hasPredicate) || currentStep.evalPredicates(vn))) {
					b = true;
					break;
				}
			}
			if (b) {
				state = FORWARD;
				currentStep = currentStep.nextS;
			} else{
				if (currentStep.hasPredicate)
					currentStep.resetP(vn);
				transition_child(vn);
			}

			break;

		case TERMINAL:
			if (currentStep.out_of_range){
				currentStep.out_of_range = false;
				if (currentStep.hasPredicate)
					currentStep.resetP(vn);
				transition_child(vn);
				break;
			}
			while (vn.toNode(VTDNav.NS)) {
				if ((currentStep.nt_eval || currentStep.nt.eval2(vn)) 
						&& ((!currentStep.hasPredicate) || currentStep.evalPredicates(vn))) {
					// state = TERMINAL;
					result = vn.getCurrentIndex();
					if (isUnique(result))
						return result;
				}
			}
			if (currentStep.hasPredicate)
				currentStep.resetP(vn);
			transition_child(vn);

			break;

		default:
			throw new XPathEvalException("unknown state");
		}
	    return -2;
	}
	
	final public boolean isFinal(){
		return (pathType ==  ABSOLUTE_PATH);
	}
	
	final public void markCacheable(){
		Step temp = s;
		while(temp!=null){
			if (temp.p!=null){
				if (temp.p.expr!=null ){
					if (temp.p.expr.isFinal() && temp.p.expr.isNodeSet()){
						CachedExpr ce = new CachedExpr(temp.p.expr);
						temp.p.expr = ce;
					}
					temp.p.expr.markCacheable2();
				}
			}
			temp = temp.nextS;
		}
	}
	
	final public void markCacheable2(){
		markCacheable();
	}	
	
	final public void clearCache(){
		Step temp = s;
		while(temp!=null){
			if (temp.p!=null){
				temp.p.expr.clearCache();
			}
			temp = temp.nextS;
		}
	}
}

