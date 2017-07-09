import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class KlassBuilder {

    public Map<String, Klass> klassMap = new HashMap<>();
    public Map<String, Interfaze> interfazeMap = new HashMap<>();

    public void printClasses() {
        System.out.println("\n\n --- Classes : ");
        for (String className : klassMap.keySet()) {
            klassMap.get(className).print();
        }
    }

    public void printInterfaces() {
        System.out.println("\n\n --- Interfaces : ");
        for (String interfaceName : interfazeMap.keySet()) {
            interfazeMap.get(interfaceName).print();
        }
    }

    public static enum RelationType {
        ASSOCIATION, DASSOCIATION, AGGREGATION, DAGGREGATION, COMPOSITION, DCOMPOSITION, DEPENDENCY, DDEPENDENCY
    }

    public static enum AccessModifyer {
        PUBLIC("+"),
        PRIVATE("-"),
        PROTECTED("#"),
        DEFAULT("");

        private final String nType;

        AccessModifyer(String type) {

            this.nType = type;
        }

        public String getType() {
            return this.nType;
        }
    }

    public static class Modifyers {
        public boolean isStatic;
        public AccessModifyer access;
        public Modifyers(){
            isStatic = false;
            access = KlassBuilder.AccessModifyer.DEFAULT;
        }
    }

    public static class Interfaze {
		public int id;
        public ArrayList<Method> methodsL;
        public ArrayList<String> extendsL; //key for the KlassMap
        public String name;

        public Interfaze(String n) {
            this.name = n;
            methodsL = new ArrayList<>();
            extendsL = new ArrayList<>();
        }

        public void print(){
            System.out.println("\n\n    @Interface " + this.name);

            System.out.println("-Methods");
            for (KlassBuilder.Method meth : this.methodsL) {
                System.out.print("\t" + (meth.modifyer.getType()) + " " + (meth.isStatic ? "s" : "") + " "+meth.returnType+" " + meth.name + "(");
                for (KlassBuilder.Paramether par : meth.paramethers) {
                    System.out.print(par.type + " " + par.name + ",");
                }
                System.out.println(")");
            }
        }

        public void addMethod(Method m) {
            this.methodsL.add(m);
        }

        public void addExtends(String klassKey) {
            this.extendsL.add(klassKey);
        }
    }

    public static class Klass {
		public int id;
        public ArrayList<Attribute> attributesL;
        public ArrayList<Method> methodsL;
        public ArrayList<String> extendsL; //key for the KlassMap
        public ArrayList<String> implementsL; //key for the KlassMap
        public String name;

        public Klass(String n) {
            this.name = n;
            methodsL = new ArrayList<>();
            extendsL = new ArrayList<>();
            implementsL = new ArrayList<>();
            attributesL = new ArrayList<>();
        }

        public void print(){
            System.out.println("\n\n    @Class " + this.name);

            System.out.println("- Attributes");

            for (KlassBuilder.Attribute attr : this.attributesL) {
                System.out.println("\t" + (attr.modifyer.getType()) + " " + (attr.isStatic ? "s" : "") + " "+attr.returnType+" " + attr.name);
            }

            System.out.println("- Methods");
            for (KlassBuilder.Method meth : this.methodsL) {
                System.out.print("\t" + (meth.modifyer.getType()) + " " + (meth.isStatic ? "s" : "") + " "+meth.returnType+" " + meth.name + "(");
                for (KlassBuilder.Paramether par : meth.paramethers) {
                    System.out.print(par.type + " " + par.name + ",");
                }
                System.out.println(")");
            }
        }

        public void addAttribute(Attribute a) {
            this.attributesL.add(a);
        }

        public void addMethod(Method m) {
            this.methodsL.add(m);
        }

        public void addExtends(String klassKey) {
            this.extendsL.add(klassKey);
        }

        public void addImplements(String klassKey) {
            this.implementsL.add(klassKey);
        }
    }

    public static class Paramether {
        public String name;
        public String type;
        public Paramether(){}
        public Paramether(String n, String t) {
            this.name = n;
            this.type = t;
        }
    }

    public static class Method {
        public String name;
        public String returnType;
        public AccessModifyer modifyer;
        public boolean isStatic;
        public ArrayList<Paramether> paramethers;

        void addParamether(Paramether p) {
            paramethers.add(p);
        }

        public Method(){}
    }

    public static class Attribute {
        public String name;
        public String returnType;
        public AccessModifyer modifyer;
        public boolean isStatic;
        public Attribute(){}
    }

	public void ruleThemAll() {
		String dotHeader = + "digraph {\n"
		+ "fontname = \"Bitstream Vera Sans\"\n"
		+ "fontsize = 12\n"
		+ "node [\n"
		+ "fontname = \"Bitstream Vera Sans\"\n"
		+ "fontsize = 12\n"
		+ "shape = \"record\"\n"
		+ "]\n"
		+ "edge [\n"
		+ "fontname = \"Bitstream Vera Sans\"\n"
		+ "fontsize = 12\n"
		+ "]\n";
		String dotFooter = "}";

		int modelId = 1;

		for (Map.Entry<String, Klass> entry : klassMap.entrySet()) {
			Klass klass = entry.getValue();
			klass.id = modelId;
			"[ label = <{<b>" + Room + "</b> ||}>]"
			modelId++;
		}
	}
}













