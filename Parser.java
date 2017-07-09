

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;



public class Parser {
	public static final int _EOF = 0;
	public static final int _lowerword_c = 1;
	public static final int _upperword_c = 2;
	public static final int _number_c = 3;
	public static final int _literal_c = 4;
	public static final int maxT = 33;

	static final boolean T = true;
	static final boolean x = false;
	static final int minErrDist = 2;

	public Token t;    // last recognized token
	public Token la;   // lookahead token
	int errDist = minErrDist;
	
	public Scanner scanner;
	public Errors errors;

	KlassBuilder builder = new KlassBuilder();
KlassBuilder.Klass currentKlass = null;
KlassBuilder.Interfaze currentInterfaze = null;



	public Parser(Scanner scanner) {
		this.scanner = scanner;
		errors = new Errors();
	}

	void SynErr (int n) {
		if (errDist >= minErrDist) errors.SynErr(la.line, la.col, n);
		errDist = 0;
	}

	public void SemErr (String msg) {
		if (errDist >= minErrDist) errors.SemErr(t.line, t.col, msg);
		errDist = 0;
	}
	
	void Get () {
		for (;;) {
			t = la;
			la = scanner.Scan();
			if (la.kind <= maxT) {
				++errDist;
				break;
			}

			la = t;
		}
	}
	
	void Expect (int n) {
		if (la.kind==n) Get(); else { SynErr(n); }
	}
	
	boolean StartOf (int s) {
		return set[s][la.kind];
	}
	
	void ExpectWeak (int n, int follow) {
		if (la.kind == n) Get();
		else {
			SynErr(n);
			while (!StartOf(follow)) Get();
		}
	}
	
	boolean WeakSeparator (int n, int syFol, int repFol) {
		int kind = la.kind;
		if (kind == n) { Get(); return true; }
		else if (StartOf(repFol)) return false;
		else {
			SynErr(n);
			while (!(set[syFol][kind] || set[repFol][kind] || set[0][kind])) {
				Get();
				kind = la.kind;
			}
			return StartOf(syFol);
		}
	}
	
	void JTD2DOT() {
		Definition();
		while (la.kind == 5 || la.kind == 11) {
			Definition();
		}
<<<<<<< HEAD
		builder.printClasses(); builder.printInterfaces(); builder.ruleThemAll(); 
=======
		builder.processPrebuiltinRelations() ;builder.printClasses(); builder.printInterfaces();
>>>>>>> cheeki
	}

	void Definition() {
		if (la.kind == 5) {
			ClassDefinition();
		} else if (la.kind == 11) {
			InterfaceDefinition();
		} else SynErr(34);
	}

	void ClassDefinition() {
		Expect(5);
		ClassName();
		KlassBuilder.Klass clazz = new KlassBuilder.Klass(t.val); builder.klassMap.put(t.val, clazz); currentKlass = clazz; 
		if (la.kind == 6) {
			Get();
			ClassName();
			clazz.addExtends(t.val); KlassBuilder.Relation rel = new KlassBuilder.Relation(); rel.setType(KlassBuilder.RelationType.GENERALIZATION); builder.prebuiltRelations.add(new KlassBuilder.PrebuiltRelation(currentKlass.name,t.val, rel));
			while (la.kind == 7) {
				Get();
				ClassName();
				clazz.addExtends(t.val); KlassBuilder.Relation relI = new KlassBuilder.Relation(); relI.setType(KlassBuilder.RelationType.GENERALIZATION); builder.prebuiltRelations.add(new KlassBuilder.PrebuiltRelation(currentKlass.name,t.val, relI));
			}
		}
		if (la.kind == 8) {
			Get();
			ClassName();
			clazz.addImplements(t.val); KlassBuilder.Relation rel = new KlassBuilder.Relation(); rel.setType(KlassBuilder.RelationType.REALIZATION); builder.prebuiltRelations.add(new KlassBuilder.PrebuiltRelation(currentKlass.name,t.val, rel));
			while (la.kind == 7) {
				Get();
				ClassName();
				clazz.addImplements(t.val); KlassBuilder.Relation relI = new KlassBuilder.Relation(); relI.setType(KlassBuilder.RelationType.REALIZATION); builder.prebuiltRelations.add(new KlassBuilder.PrebuiltRelation(currentKlass.name,t.val, relI));
			}
		}
		Expect(9);
		while (StartOf(1)) {
			Object entity = ClassEntity();
			if(entity instanceof KlassBuilder.Method){ clazz.addMethod(((KlassBuilder.Method)entity)); }else{ clazz.addAttribute(((KlassBuilder.Attribute)entity)); } 
		}
		Expect(10);
	}

	void InterfaceDefinition() {
		Expect(11);
		ClassName();
		KlassBuilder.Interfaze interfaze = new KlassBuilder.Interfaze(t.val); builder.interfazeMap.put(t.val, interfaze);  currentInterfaze = interfaze; 
		if (la.kind == 6) {
			Get();
			ClassName();
			interfaze.addExtends(t.val); KlassBuilder.Relation rel = new KlassBuilder.Relation(); rel.setType(KlassBuilder.RelationType.GENERALIZATION); builder.prebuiltRelations.add(new KlassBuilder.PrebuiltRelation(currentInterfaze.name,t.val, rel)); 
			while (la.kind == 7) {
				Get();
				ClassName();
				interfaze.addExtends(t.val); KlassBuilder.Relation relI = new KlassBuilder.Relation(); relI.setType(KlassBuilder.RelationType.GENERALIZATION); builder.prebuiltRelations.add(new KlassBuilder.PrebuiltRelation(currentInterfaze.name,t.val, relI)); 
			}
		}
		Expect(9);
		while (StartOf(1)) {
			KlassBuilder.Method ieout = InterfaceEntity();
			interfaze.addMethod(ieout); 
		}
		Expect(10);
	}

	void ClassName() {
		String name=""; 
		UpperName();
		name+=t.val; 
		while (la.kind == 1 || la.kind == 2 || la.kind == 3) {
			if (la.kind == 2) {
				UpperName();
			} else if (la.kind == 1) {
				LowerName();
			} else {
				Number();
			}
			name+=t.val; 
		}
		t.val = name; 
	}

	Object  ClassEntity() {
		Object  entity_out;
		KlassBuilder.Modifyers generalModifyer = null; 
		if (StartOf(2)) {
			KlassBuilder.Modifyers modifyer = Modifyers();
			generalModifyer = modifyer; 
		}
		Object especializer_out = Especializer();
		entity_out = especializer_out;
		if(especializer_out instanceof KlassBuilder.Method){
		KlassBuilder.Method meth = ((KlassBuilder.Method)especializer_out);
		meth.isStatic = generalModifyer != null? generalModifyer.isStatic : false;
		meth.modifyer = generalModifyer != null? generalModifyer.access : KlassBuilder.AccessModifyer.DEFAULT;
		}else{
		KlassBuilder.Attribute attr = ((KlassBuilder.Attribute)especializer_out);
		attr.isStatic = generalModifyer != null? generalModifyer.isStatic : false;
		attr.modifyer = generalModifyer != null? generalModifyer.access : KlassBuilder.AccessModifyer.DEFAULT;
		}
		
		return entity_out;
	}

	KlassBuilder.Method  InterfaceEntity() {
		KlassBuilder.Method  ieout_out;
		KlassBuilder.Modifyers generalModifyer = null; 
		if (StartOf(2)) {
			KlassBuilder.Modifyers modifyer = Modifyers();
			generalModifyer = modifyer; 
		}
		String type = "void"; 
		if (la.kind == 1 || la.kind == 2) {
			String typeI = CanonicalType();
			type = typeI; 
		} else if (la.kind == 15) {
			VoidType();
		} else SynErr(35);
		EntityName();
		String name = t.val; 
		ArrayList mic = MethodInterfaceCompletition();
		ieout_out = new KlassBuilder.Method();
		ieout_out.name = name;
		ieout_out.returnType = type;
		ieout_out.paramethers = mic;
		ieout_out.isStatic = generalModifyer != null? generalModifyer.isStatic : false;
		ieout_out.modifyer = generalModifyer != null? generalModifyer.access : KlassBuilder.AccessModifyer.DEFAULT;
		
		return ieout_out;
	}

	KlassBuilder.Modifyers  Modifyers() {
		KlassBuilder.Modifyers  modifyer_out;
		modifyer_out = new KlassBuilder.Modifyers(); 
		if (la.kind == 21 || la.kind == 22 || la.kind == 23) {
			KlassBuilder.AccessModifyer modifyer = AccessModifyer();
			modifyer_out.access = modifyer; 
			if (la.kind == 24) {
				boolean isStatic = ScopeModifyer();
				modifyer_out.isStatic = isStatic; 
			}
		} else if (la.kind == 24) {
			boolean isStatic = ScopeModifyer();
			modifyer_out.isStatic = isStatic; 
			if (la.kind == 21 || la.kind == 22 || la.kind == 23) {
				KlassBuilder.AccessModifyer modifyer = AccessModifyer();
				modifyer_out.access = modifyer; 
			}
		} else SynErr(36);
		return modifyer_out;
	}

	Object  Especializer() {
		Object  especializer;
		especializer = null; 
		if (la.kind == 15) {
			Object function = FunctionEspecializer();
			especializer = function; 
		} else if (la.kind == 1 || la.kind == 2) {
			Object anomalous = AnomalousEspecializer();
			especializer = anomalous; 
		} else SynErr(37);
		return especializer;
	}

	String  CanonicalType() {
		String  type_out;
		if (la.kind == 2) {
			UpperName();
		} else if (la.kind == 1) {
			LowerName();
		} else SynErr(38);
		type_out = t.val; 
		if (la.kind == 16) {
			String po = ArrayOperator();
			type_out += po; 
		}
		return type_out;
	}

	void VoidType() {
		Expect(15);
	}

	void EntityName() {
		String name=""; 
		if (la.kind == 2) {
			UpperName();
		} else if (la.kind == 1) {
			LowerName();
		} else SynErr(39);
		name+=t.val; 
		while (la.kind == 1 || la.kind == 2 || la.kind == 3) {
			if (la.kind == 2) {
				UpperName();
			} else if (la.kind == 1) {
				LowerName();
			} else {
				Number();
			}
			name+=t.val; 
		}
		t.val = name; 
	}

	ArrayList  MethodInterfaceCompletition() {
		ArrayList  mic_out;
		mic_out = new ArrayList<>(); 
		Expect(12);
		if (la.kind == 1 || la.kind == 2) {
			String typeIC = CanonicalType();
			String type = typeIC; 
			EntityName();
			String name = t.val; 
			mic_out.add(new KlassBuilder.Paramether(name, type)); 
			while (la.kind == 7) {
				Get();
				String _type = CanonicalType();
				String type2 = _type; 
				EntityName();
				String name2 = t.val; 
				mic_out.add(new KlassBuilder.Paramether(name2, type2)); 
			}
		}
		Expect(13);
		Expect(14);
		return mic_out;
	}

	Object  FunctionEspecializer() {
		Object  function_out;
		KlassBuilder.Method method = new KlassBuilder.Method(); function_out = method; 
		VoidType();
		method.returnType = "void"; 
		EntityName();
		method.name = t.val; 
		ArrayList paramethers = MethodCompletition();
		method.paramethers = paramethers; 
		return function_out;
	}

	Object  AnomalousEspecializer() {
		Object  anomalous_out;
		anomalous_out = null; 
		String _type = CanonicalType();
		String type = _type; 
		EntityName();
		String name = t.val; 
		if (la.kind == 12) {
			ArrayList paramethers = MethodCompletition();
			KlassBuilder.Method method = new KlassBuilder.Method(); anomalous_out = method; method.name = name; method.returnType = type; method.paramethers = paramethers; 
		} else if (StartOf(3)) {
			KlassBuilder.Relation ac = AttributeCompletition();
			if(ac != null){builder.prebuiltRelations.add(new KlassBuilder.PrebuiltRelation(currentKlass.name ,KlassBuilder.recoverEntityName(type), ac));} 
			KlassBuilder.Attribute attr = new KlassBuilder.Attribute(); anomalous_out = attr; attr.name = name; attr.returnType = type; 
		} else SynErr(40);
		return anomalous_out;
	}

	ArrayList  MethodCompletition() {
		ArrayList  paramethers_out;
		paramethers_out = new ArrayList<>(); 
		Expect(12);
		if (la.kind == 1 || la.kind == 2) {
			String typeE = CanonicalType();
			EntityName();
			String nameE = t.val; 
			paramethers_out.add(new KlassBuilder.Paramether(nameE, typeE)); 
			while (la.kind == 7) {
				Get();
				String typeI = CanonicalType();
				EntityName();
				String nameI = t.val; 
				paramethers_out.add(new KlassBuilder.Paramether(nameI, typeI)); 
			}
		}
		Expect(13);
		Expect(9);
		Expect(10);
		if (la.kind == 14) {
			Get();
		}
		return paramethers_out;
	}

	KlassBuilder.Relation  AttributeCompletition() {
		KlassBuilder.Relation  ac_out;
		ac_out = null; 
		if (StartOf(4)) {
			KlassBuilder.Relation relation = Relation();
			ac_out = relation; 
		}
		Expect(14);
		return ac_out;
	}

	KlassBuilder.Relation  Relation() {
		KlassBuilder.Relation  relation_out;
		KlassBuilder.RelationType type = RelationType();
		relation_out = new KlassBuilder.Relation(); relation_out.setType(type); relation_out.explicit = true; 
		if (la.kind == 18) {
			Get();
			Multiplicity();
			relation_out.headlabel = t.val; 
			Expect(19);
			Multiplicity();
			relation_out.taillabel = t.val; 
		}
		if (la.kind == 20) {
			Title();
			relation_out.title = t.val; 
		}
		return relation_out;
	}

	void UpperName() {
		Expect(2);
	}

	void LowerName() {
		Expect(1);
	}

	String  ArrayOperator() {
		String  po_o;
		po_o = "["; 
		Expect(16);
		if (la.kind == 3) {
			Number();
			po_o+=t.val; 
		}
		Expect(17);
		po_o+="]"; 
		return po_o;
	}

	void Number() {
		Expect(3);
	}

	KlassBuilder.RelationType  RelationType() {
		KlassBuilder.RelationType  type_out;
		type_out = null; 
		switch (la.kind) {
		case 25: {
			Get();
			type_out = KlassBuilder.RelationType.ASSOCIATION; 
			break;
		}
		case 26: {
			Get();
			type_out = KlassBuilder.RelationType.DASSOCIATION; 
			break;
		}
		case 27: {
			Get();
			type_out = KlassBuilder.RelationType.AGGREGATION; 
			break;
		}
		case 28: {
			Get();
			type_out = KlassBuilder.RelationType.DAGGREGATION; 
			break;
		}
		case 29: {
			Get();
			type_out = KlassBuilder.RelationType.COMPOSITION; 
			break;
		}
		case 30: {
			Get();
			type_out = KlassBuilder.RelationType.DCOMPOSITION; 
			break;
		}
		case 31: {
			Get();
			type_out = KlassBuilder.RelationType.DEPENDENCY; 
			break;
		}
		case 32: {
			Get();
			type_out = KlassBuilder.RelationType.DDEPENDENCY; 
			break;
		}
		default: SynErr(41); break;
		}
		return type_out;
	}

	void Multiplicity() {
		Expect(4);
	}

	void Title() {
		Expect(20);
		Expect(4);
	}

	KlassBuilder.AccessModifyer  AccessModifyer() {
		KlassBuilder.AccessModifyer  modifyer_out;
		modifyer_out = null; 
		if (la.kind == 21) {
			Get();
			modifyer_out = KlassBuilder.AccessModifyer.PUBLIC; 
		} else if (la.kind == 22) {
			Get();
			modifyer_out = KlassBuilder.AccessModifyer.PROTECTED; 
		} else if (la.kind == 23) {
			Get();
			modifyer_out = KlassBuilder.AccessModifyer.PRIVATE; 
		} else SynErr(42);
		return modifyer_out;
	}

	boolean  ScopeModifyer() {
		boolean  isStatic_out;
		Expect(24);
		isStatic_out = true; 
		return isStatic_out;
	}



	public void Parse() {
		la = new Token();
		la.val = "";		
		Get();
		JTD2DOT();
		Expect(0);

	}

	private static final boolean[][] set = {
		{T,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x},
		{x,T,T,x, x,x,x,x, x,x,x,x, x,x,x,T, x,x,x,x, x,T,T,T, T,x,x,x, x,x,x,x, x,x,x},
		{x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,T,T,T, T,x,x,x, x,x,x,x, x,x,x},
		{x,x,x,x, x,x,x,x, x,x,x,x, x,x,T,x, x,x,x,x, x,x,x,x, x,T,T,T, T,T,T,T, T,x,x},
		{x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,T,T,T, T,T,T,T, T,x,x}

	};
} // end Parser


class Errors {
	public int count = 0;                                    // number of errors detected
	public java.io.PrintStream errorStream = System.out;     // error messages go to this stream
	public String errMsgFormat = "-- line {0} col {1}: {2}"; // 0=line, 1=column, 2=text
	
	protected void printMsg(int line, int column, String msg) {
		StringBuffer b = new StringBuffer(errMsgFormat);
		int pos = b.indexOf("{0}");
		if (pos >= 0) { b.delete(pos, pos+3); b.insert(pos, line); }
		pos = b.indexOf("{1}");
		if (pos >= 0) { b.delete(pos, pos+3); b.insert(pos, column); }
		pos = b.indexOf("{2}");
		if (pos >= 0) b.replace(pos, pos+3, msg);
		errorStream.println(b.toString());
	}
	
	public void SynErr (int line, int col, int n) {
		String s;
		switch (n) {
			case 0: s = "EOF expected"; break;
			case 1: s = "lowerword_c expected"; break;
			case 2: s = "upperword_c expected"; break;
			case 3: s = "number_c expected"; break;
			case 4: s = "literal_c expected"; break;
			case 5: s = "\"class\" expected"; break;
			case 6: s = "\"extends\" expected"; break;
			case 7: s = "\",\" expected"; break;
			case 8: s = "\"implements\" expected"; break;
			case 9: s = "\"{\" expected"; break;
			case 10: s = "\"}\" expected"; break;
			case 11: s = "\"interface\" expected"; break;
			case 12: s = "\"(\" expected"; break;
			case 13: s = "\")\" expected"; break;
			case 14: s = "\";\" expected"; break;
			case 15: s = "\"void\" expected"; break;
			case 16: s = "\"[\" expected"; break;
			case 17: s = "\"]\" expected"; break;
			case 18: s = "\"@\" expected"; break;
			case 19: s = "\"to\" expected"; break;
			case 20: s = "\"title\" expected"; break;
			case 21: s = "\"public\" expected"; break;
			case 22: s = "\"protected\" expected"; break;
			case 23: s = "\"private\" expected"; break;
			case 24: s = "\"static\" expected"; break;
			case 25: s = "\"association\" expected"; break;
			case 26: s = "\"dassociation\" expected"; break;
			case 27: s = "\"aggregation\" expected"; break;
			case 28: s = "\"daggregation\" expected"; break;
			case 29: s = "\"composition\" expected"; break;
			case 30: s = "\"dcomposition\" expected"; break;
			case 31: s = "\"dependency\" expected"; break;
			case 32: s = "\"ddependency\" expected"; break;
			case 33: s = "??? expected"; break;
			case 34: s = "invalid Definition"; break;
			case 35: s = "invalid InterfaceEntity"; break;
			case 36: s = "invalid Modifyers"; break;
			case 37: s = "invalid Especializer"; break;
			case 38: s = "invalid CanonicalType"; break;
			case 39: s = "invalid EntityName"; break;
			case 40: s = "invalid AnomalousEspecializer"; break;
			case 41: s = "invalid RelationType"; break;
			case 42: s = "invalid AccessModifyer"; break;
			default: s = "error " + n; break;
		}
		printMsg(line, col, s);
		count++;
	}

	public void SemErr (int line, int col, String s) {	
		printMsg(line, col, s);
		count++;
	}
	
	public void SemErr (String s) {
		errorStream.println(s);
		count++;
	}
	
	public void Warning (int line, int col, String s) {	
		printMsg(line, col, s);
	}
	
	public void Warning (String s) {
		errorStream.println(s);
	}
} // Errors


class FatalError extends RuntimeException {
	public static final long serialVersionUID = 1L;
	public FatalError(String s) { super(s); }
}
