VTD 当前版本 2.11
com.ximpleware.* ，java_cup.* 为 VTD 源码，请不要修改。

2.11 新特性

Version 2.11, simultaneously available  in C, Java, C++, and C#, is the latest release of VTD-XML. So what is new? The shortly answer: 
(1) It is more standards-compliant by conforming strictly to XPath 1.0 spec’s notion of node(). 
(2) It  introduces major performance improvement for XPath expressions involving simple position index.
(3)This release introduces major performance improvement for XPath expression containing complex predicates involving  absolute location path expressions. 
(4) It also contains various bug releases as reported by VTD-XML users.

1. Change to Node() Interpretation
Before 2.11, node() in a location step in an XPath expression will be interpreted as equivalent to *, 
i.e., an element node with any name. With 2.11 the same node() will be interpreted either one of “element(), text(), comment(), 
or processing-instruction(), as defined by XPath 1.0 spec.

2. Performance Improvement for Simple Position Index
A quick example is “a[2]/b[1].”  A simple position index is basically a constant index value in predicate. 2.11′s XPath engine 
is now smart enough to detect this use case and allow for early escaping from the execution loop, resulting in faster execution 
performance. The amount of improvement depends on how frequent the simple index is used in each location step. In some cases, 
a 50% to 70% execution speedup is possible.

3.Performance Improvement for Predicates Containing Absolute Path Expressions
A quick example is //a[//abc/@val='1']. Notice that predicate contains //abc, which is an absolute path expression. Before 2.11, this expression 
will trigger repetitive evaluation of //abc to determine whether the predicate is true or false.  The processing cost would increase rapidly 
with respect to the size of the document. This release would intelligently cache the evaluation result so the corresponding XPath  
is evaluated only once. Please notice that this feature is enabled by default, if you can turn it off (we don’t recommend it) 
 by invoking AutoPilot’s enableCaching’s method and give it a “false.”

How much of an improvement can you expect to see? Depending on size of documents, complexity of predicates and other things. 
Sometime you will be achieve astonishing results. Consider the following expression.

//CDResults[../../../TargetName/@Value="//SiteInformation["TargetName/@Value!=//SiteInformation[1]/TargetName/@Value and TargetName/@Value!=//SiteInformation[TargetName/@Value!=//SiteInformation[1]/TargetName/@Value”][1]/TargetName/@Value][1]/TargetName/@Value]/BottomCD/@Value

Running this document on a 22MB xml document  in Java would take many hours in virtually all XPath implementation including 2.10 version of VTD-XML. With 2.11, it took less than 5 seconds on a commodity, 3 year old PC.

4.Bug fixes
There are other bug fixes, covering XMLModifier’s deletion capabilities and permissiveness of deletion of sub-nodes.