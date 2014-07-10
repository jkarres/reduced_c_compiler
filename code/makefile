
SOURCES = \
	AbstractFunctionPointer.java\
	ArgumentMemoryLocation.java\
	ArrayDeref.java\
	ArrayName.java\
	ArrayReference.java\
	ArrayType.java\
	AssemblyWriter.java\
	BooleanType.java\
	BooleanValue.java\
	BoundFunction.java\
	BoundStructFunc.java\
	Callable.java\
	CDerefRes.java\
	CharType.java\
	CharValue.java\
	ConstErr.java\
	ConstExpr.java\
	ConstExprOrConstErrOrReportedErr.java\
	ConstExprOrErr.java\
	ConstExprOrReportedErr.java\
	ConstExprOrUnreportedErr.java\
	CRV.java\
	CVar.java\
	DebugLogger.java\
	Err.java\
	ErrorMsg.java\
	ErrorPrinter.java\
	ErrorSTO.java\
	Expr.java\
	ExprOrErr.java\
	ExprOrNothingOrReportedErr.java\
	ExprOrReportedErr.java\
	FloatType.java\
	FloatValue.java\
	Formatter.java\
	ForScope.java\
	FuncPtrFromArrayDeref.java\
	FuncPtrFromDeref.java\
	FuncPtrFromStruct.java\
	FunctionPointer.java\
	FunctionScope.java\
	FunctionSTO.java\
	FunctionType.java\
	FunctionValue.java\
	GlobalScope.java\
	HasLocation.java\
	IncompleteStructType.java\
	IntType.java\
	IntValue.java\
	IntValueOrNothingOrReportedErr.java\
	LabelMaker.java\
	Lexer.java\
	LineNumberPushbackStream.java\
	LocalMemoryLocation.java\
	MemoryLocation.java\
	MLV.java\
	MyParser.java\
	NameAndType.java\
	NameConstExprOrErrPair.java\
	NameConstExprOrReportedErrPair.java\
	NamedFunction.java\
	NameExprOrNothingOrReportedErrPair.java\
	NCDerefRes.java\
	NCRV.java\
	NCVar.java\
	NMLV.java\
	NonlocalMemoryLocation.java\
	Nothing.java\
	NullType.java\
	NullValue.java\
	NumericType.java\
	NumericValue.java\
	ObjectTypeOrErr.java\
	ObjectTypeOrReportedErr.java\
	ObjectValue.java\
	Operator.java\
	OtherScope.java\
	ParamDecl.java\
	ParamDeclOrErr.java\
	ParamDeclOrReportedErr.java\
	PointerType.java\
	PointerValue.java\
	RC.java\
	RCdbg.java\
	Register.java\
	ReportedErr.java\
	ReturnableTypeOrErr.java\
	ReturnableTypeOrReportedErr.java\
	RV.java\
	Scope.java\
	STO.java\
	StringValue.java\
	StructAccess.java\
	StructScope.java\
	StructType.java\
	SymbolTable.java\
	ThisExpr.java\
	Token.java\
	Type.java\
	TypeDefOrErr.java\
	TypeDefOrReportedErr.java\
	TypedefType.java\
	TypeOrErr.java\
	TypeOrReportedErr.java\
	UnboundFunction.java\
	UnknownStructType.java\
	UnreportedErr.java\
	Value.java\
	VoidExpr.java\
	VoidType.java\

new:
	make clean
	make rc

debug:
	make clean
	make rcdbg

rc: $(SOURCES) parser.java sym.java
	javac RC.java
	cp RC.sh RC
	chmod 755 RC

rcdbg: $(SOURCES) parser.java sym.java
	javac RCdbg.java
	cp RCdbg.sh RC
	chmod 755 RC

parser.java: rc.cup
	javacup < rc.cup

CC=cc
compile:
	$(CC) rc.s input.c output.s $(LINKOBJ)

clean:
	-@rm -f *.class RC parser.java sym.java 

tar:
	tar cvf rc.tar $(SOURCES) Makefile RC.sh

#       Makes a backup called backups/MMDDMM:SS.tar.Z
backup:
	-@make clean
	-@mkdir backups
	tar cvf - $(SOURCES) Makefile RC.sh | compress > backups/`date +%m%d%R`.tar.Z
