A wittle client for the TreeFs Storage Service (aka: treefs)
============================================================

Rules
=====
* Every DSL “keyword” becomes a Java method
* Every DSL “connection” becomes an interface
* When you have a “mandatory” choice (you can’t skip the next keyword), every keyword of that choice is a method in the current interface. If only one keyword is possible, then there is only one method
* When you have an “optional” keyword, the current interface extends the next one (with all its keywords / methods)
* When you have a “repetition” of keywords, the method representing the repeatable keyword returns the interface itself, instead of the next interface
* Every DSL subdefinition becomes a parameter. This will allow for recursiveness

TreeFs client Grammar
==================
Grammar ::= (

)

Tool for generating grammer
===========================
"unquietcode.tools.flapi:flapi:0.5"


