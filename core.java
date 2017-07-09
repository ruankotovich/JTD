
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class KlassBuilder {

    public Map<String, Klass> klassMap = new HashMap<>();
    public Map<String, Interfaze> interfazeMap = new HashMap<>();

    public static String recoverEntityName(String entity) {
        return entity.split("\\[")[0];
    }

    public static boolean isCollection(String entity) {
        return entity.contains("\\[");
    }

    public void printClasses() {
        System.out.println("\n\n --- Classes : ");
        for (String className : klassMap.keySet()) {
        	Klass klass = klassMap.get(className);
        	
            klass.print();
            klass.inferenceRelationsKlass(this);
        }
    }

    public void printInterfaces() {
        System.out.println("\n\n --- Interfaces : ");
        for (String interfaceName : interfazeMap.keySet()) {
            interfazeMap.get(interfaceName).print();
        }
    }

    public static enum RelationType {
        ASSOCIATION("association", 2),
        DASSOCIATION("dassociation", 2),
        AGGREGATION("aggregation", 3),
        DAGGREGATION("daggregation", 3),
        COMPOSITION("composition", 4),
        DCOMPOSITION("dcomposition", 4),
        DEPENDENCY("dependency", 0),
        DDEPENDENCY("ddependency", 0),
        GENERALIZATION("generalization", 1),
        REALIZATION("realization", 1);

        private final Integer weight;
        private final String typeName;

        private RelationType(String typeName, int weight) {
            this.typeName = typeName;
            this.weight = weight;
        }

        public String getTypeName() {
            return typeName;
        }

        public Integer getWeight() {
            return weight;
        }

        @Override
        public String toString() {
            return getTypeName();
        }

    }

    public static enum AccessModifyer {
        PUBLIC("+"),
        PRIVATE("-"),
        PROTECTED("#"),
        DEFAULT("-");

        private final String nType;

        AccessModifyer(String type) {
            this.nType = type;
        }

        public String getType() {
            return this.nType;
        }
    }

    public static class Relation {

        private RelationType type = null;
        public String title = "";
        public boolean explicit = false;
        public String headlabel = "";
        public String taillabel = "";
        public String style = "";
        public String arrowhead = "";
        public String dir = "";

        @Override
        public String toString() {
            return this.type.toString() + " @ \"" + headlabel + " to \"" + taillabel + "\" title \"" + title + "\"" + "\n"
                    + "style:" + style + "\tarrowhead:" + arrowhead + "\tdir:" + dir;
        }
        
        public String toStringUml() {
        	return "[style = \"" + style + "\" arrowhead = \"" + arrowhead +  "\" dir = \"" + dir + "\" taillabel = \"" + taillabel
        			+ "\" headlabet = \""	+ headlabel + "\" label = \"" + title + "\"]";
        }

        public RelationType getType() {
            return type;
        }

        public void setType(RelationType type) {
            this.type = type;
            switch (type) {
                case ASSOCIATION:
                    this.dir = "none";
                    this.style = "solid";
                    this.arrowhead = "open";
                    break;
                case DASSOCIATION:
                    this.dir = "forward";
                    this.style = "solid";
                    this.arrowhead = "open";
                    break;
                case AGGREGATION:
                    this.dir = "none";
                    this.style = "solid";
                    this.arrowhead = "odiamond";
                    break;
                case DAGGREGATION:
                    this.dir = "forward";
                    this.style = "solid";
                    this.arrowhead = "odiamond";
                    break;
                case COMPOSITION:
                    this.dir = "none";
                    this.style = "solid";
                    this.arrowhead = "diamond";
                    break;
                case DCOMPOSITION:
                    this.dir = "forward";
                    this.style = "solid";
                    this.arrowhead = "diamond";
                    break;
                case DEPENDENCY:
                    this.dir = "none";
                    this.style = "dashed";
                    this.arrowhead = "open";
                    break;
                case DDEPENDENCY:
                    this.dir = "forward";
                    this.style = "dashed";
                    this.arrowhead = "open";
                    break;
                case GENERALIZATION:
                    this.arrowhead = "onormal";
                    this.dir = "forward";
                    this.style = "solid";
                    break;
                case REALIZATION:
                    this.style = "dashed";
                    this.arrowhead = "onormal";
                    this.dir = "forward";
                    break;
            }
        }

    }

    public static class Modifyers {

        public boolean isStatic;
        public AccessModifyer access;

        public Modifyers() {
            isStatic = false;
            access = KlassBuilder.AccessModifyer.DEFAULT;
        }
    }

    public static class Interfaze {

        private HashMap<String, KlassBuilder.Relation> relationsWithInterfaces;
        private HashMap<String, KlassBuilder.Relation> relationsWithClasses;
        public int id;
        public ArrayList<Method> methodsL;
        public ArrayList<String> extendsL; //key for the KlassMap
        public String name;

        public Interfaze(String n) {
            this.name = n;
            methodsL = new ArrayList<>();
            extendsL = new ArrayList<>();
            relationsWithInterfaces = new HashMap<>();
            relationsWithClasses = new HashMap<>();
        }

        public boolean addIfMoreImportantInterface(String clazz, KlassBuilder.Relation rel) {
            KlassBuilder.Relation currentRelation = this.relationsWithInterfaces.put(clazz, rel);
            if (currentRelation != null) {
                if (currentRelation.getType().getWeight() < rel.getType().getWeight() && !currentRelation.explicit) {
                    this.relationsWithInterfaces.replace(clazz, rel);
                    return true;
                } else {
                    return false;
                }
            } else {
                return true;
            }
        }

        public boolean addIfMoreImportantClass(String clazz, KlassBuilder.Relation rel) {
            KlassBuilder.Relation currentRelation = this.relationsWithClasses.put(clazz, rel);
            if (currentRelation != null) {
                if (currentRelation.getType().getWeight() < rel.getType().getWeight() && !currentRelation.explicit) {
                    this.relationsWithClasses.replace(clazz, rel);
                    return true;
                } else {
                    return false;
                }
            } else {
                return true;
            }
        }

        public HashMap<String, Relation> getRelationsWithInterfaces() {
            return relationsWithInterfaces;
        }

        public void print() {
            System.out.println("\n\n    @Interface " + this.name);

            System.out.println("-Methods");
            for (KlassBuilder.Method meth : this.methodsL) {
                System.out.print("\t" + (meth.modifyer.getType()) + " " + (meth.isStatic ? "s" : "") + " " + meth.returnType + " " + meth.name + "(");
                for (KlassBuilder.Paramether par : meth.paramethers) {
                    System.out.print(par.type + " " + par.name + ",");
                }
                System.out.println(")");
            }

            System.out.println("\n\n-Relations with classes");

            for (Map.Entry<String, KlassBuilder.Relation> rel : relationsWithClasses.entrySet()) {
                System.out.println("\t- With class" + rel.getKey());
                System.out.println("\t\t");
                System.out.println(rel.getValue());
                System.out.println("\n\n");
            }

            System.out.println("\n\n-Relations with interfaces");

            for (Map.Entry<String, KlassBuilder.Relation> rel : relationsWithInterfaces.entrySet()) {
                System.out.println("\t- With interface " + rel.getKey());
                System.out.println("\t\t");
                System.out.println(rel.getValue());
                System.out.println("\n\n");
            }
        }

        public void addMethod(Method m) {
            this.methodsL.add(m);
        }

        public void addExtends(String klassKey) {
            this.extendsL.add(klassKey);
        }
        
        public void inferenceRelationsInterfaze(KlassBuilder builder) {
        	//Métodos
        	String name;
        	System.out.println("OI");
            for (Method method : methodsL) {
            	System.out.println("OI");
            	name = KlassBuilder.recoverEntityName(method.returnType);
            	if (name.charAt(0) >= 'A' && name.charAt(0) <= 'Z') {
            	  if (builder.klassMap.containsKey(name)) {
            		Relation r = new Relation();
            		r.setType(RelationType.DDEPENDENCY);
            		r.explicit = false;
            		
            		//Adicionar a relation no 
            	  }
            	  if (builder.interfazeMap.containsKey(name)) {
            		Relation r = new Relation();
            		r.setType(RelationType.DDEPENDENCY);
            		r.explicit = false;
            		
            		//ADicionar
            	  }
            	}
              
              for (Paramether p : method.paramethers) {
            	  name = KlassBuilder.recoverEntityName(p.type);
              	if (name.charAt(0) >= 'A' && name.charAt(0) <= 'Z') {
              	  if (builder.klassMap.containsKey(name)) {
              		Relation r = new Relation();
              		r.setType(RelationType.DDEPENDENCY);
              		r.explicit = false;
              		
              		
              	  }
              	  
              	  if (builder.interfazeMap.containsKey(name)) {
              		Relation r = new Relation();
              		r.setType(RelationType.DDEPENDENCY);
              		r.explicit = false;
              		
              	  }
              	}
              }
            }
        }
    }

    public static class Klass {

        private HashMap<String, KlassBuilder.Relation> relationsWithClasses;
        private HashMap<String, KlassBuilder.Relation> relationsWithInterfaces;
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
            relationsWithClasses = new HashMap<>();
            relationsWithInterfaces = new HashMap<>();
        }
        
        public void inferenceRelationsKlass(KlassBuilder builder) {
        	String name;
        	//Atributos
        	for (Attribute attr : attributesL) {
        		name = KlassBuilder.recoverEntityName(attr.returnType);
        		
        		if (name.charAt(0) >= 'A' && name.charAt(0) <= 'Z') {
              	  if (builder.klassMap.containsKey(name)) {
              		Relation r = new Relation();
              		r.setType(RelationType.DASSOCIATION);
              		r.explicit = false;
              		
              		if (KlassBuilder.isCollection(name)) {
              			r.headlabel = "0..*";
              		}
              		else r.headlabel = "1";
              		//Adicionar a relation no 
              	  }
              	}
        	}
        	
        	//Métodos
            for (Method method : methodsL) {
            	name = KlassBuilder.recoverEntityName(method.returnType);
            	if (name.charAt(0) >= 'A' && name.charAt(0) <= 'Z') {
            	  if (builder.klassMap.containsKey(name)) {
            		Relation r = new Relation();
            		r.setType(RelationType.DDEPENDENCY);
            		r.explicit = false;
            		
            		//Adicionar a relation no 
            	  }
            	  
            	  if (builder.interfazeMap.containsKey(name)) {
            		Relation r = new Relation();
            		r.setType(RelationType.DDEPENDENCY);
            		r.explicit = false;
            		
            		
            	  }
            	}
              
              for (Paramether p : method.paramethers) {
            	  name = KlassBuilder.recoverEntityName(p.type);
              	if (name.charAt(0) >= 'A' && name.charAt(0) <= 'Z') {
              	  if (builder.klassMap.containsKey(name)) {
              		Relation r = new Relation();
              		r.setType(RelationType.DDEPENDENCY);
              		r.explicit = false;
              		
              		
              	  }
              	  
              	  if (builder.interfazeMap.containsKey(name)) {
              		Relation r = new Relation();
              		r.setType(RelationType.DDEPENDENCY);
              		r.explicit = false;
              		
              		
              	  }
              	}
              }
            }
        }
        
        public boolean addIfMoreImportantClass(String clazz, KlassBuilder.Relation rel) {
            KlassBuilder.Relation currentRelation = this.relationsWithClasses.put(clazz, rel);
            if (currentRelation != null) {
                if (currentRelation.getType().getWeight() < rel.getType().getWeight() && !currentRelation.explicit) {
                    this.relationsWithClasses.replace(clazz, rel);
                    return true;
                } else {
                    return false;
                }
            } else {
                return true;
            }
        }

        public boolean addIfMoreImportantInterface(String clazz, KlassBuilder.Relation rel) {
            KlassBuilder.Relation currentRelation = this.relationsWithInterfaces.put(clazz, rel);
            if (currentRelation != null) {
                if (currentRelation.getType().getWeight() < rel.getType().getWeight() && !currentRelation.explicit) {
                    this.relationsWithInterfaces.replace(clazz, rel);
                    return true;
                } else {
                    return false;
                }
            } else {
                return true;
            }
        }

        public HashMap<String, Relation> getRelationsWithClasses() {
            return relationsWithClasses;
        }

        public HashMap<String, Relation> getRelationsWithInterfaces() {
            return relationsWithInterfaces;
        }

        public void print() {
            System.out.println("\n\n    @Class " + this.name);

            System.out.println("- Attributes");

            for (KlassBuilder.Attribute attr : this.attributesL) {
                System.out.println("\t" + (attr.modifyer.getType()) + " " + (attr.isStatic ? "s" : "") + " " + attr.returnType + " " + attr.name);
            }

            System.out.println("- Methods");
            for (KlassBuilder.Method meth : this.methodsL) {
                System.out.print("\t" + (meth.modifyer.getType()) + " " + (meth.isStatic ? "s" : "") + " " + meth.returnType + " " + meth.name + "(");
                for (KlassBuilder.Paramether par : meth.paramethers) {
                    System.out.print(par.type + " " + par.name + ",");
                }
                System.out.println(")");
            }

            System.out.println("- Relations with classes");
            for (Map.Entry<String, Relation> rel : this.relationsWithClasses.entrySet()) {
                System.out.println("\t- With class " + rel.getKey());
                System.out.println("\t\t");
                System.out.println(rel.getValue());
                System.out.println("\n\n");
                //System.out.println(rel.getValue().toStringUml());
                System.out.println("\n\n");
            }
            System.out.println("- Relations with interfaces");
            for (Map.Entry<String, Relation> rel : this.relationsWithInterfaces.entrySet()) {
                System.out.println("\t- With interfaces " + rel.getKey());
                System.out.println("\t\t");
                System.out.println(rel.getValue());
                System.out.println("\n\n");
                
                //System.out.println(rel.getValue().toStringUml());
                System.out.println("\n\n");
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

        public Paramether() {
        }

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

        public String toStringUml() {
            String msg = modifyer.getType() + (isStatic ? "<u>" : "") + name + (isStatic ? "</u>" : "") + "(";
            boolean flag = false;
            for (KlassBuilder.Paramether par : paramethers) {
                if (flag) {
                    msg += ", ";
                }
                msg += (par.name + ": " + par.type);
                flag = true;
            }
            msg += "): " + returnType;
            return msg;
        }

        public Method() {
        }
    }

    public static class Attribute {

        public String name;
        public String returnType;
        public AccessModifyer modifyer;
        public boolean isStatic;

        public Attribute() {
        }

      public String toStringUml() {
        return (modifyer.getType()) + (isStatic? "<u>" : "") + name + (isStatic? "</u>" : "") + ": " + returnType;
      }
    }

    public void ruleThemAll() {
        String dotHeader = "digraph {\n"
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
        String dot = "";
        int modelId = 1;

        //Gerar as boxes das Classes
        for (Map.Entry<String, Klass> entry : klassMap.entrySet()) {
            Klass klass = entry.getValue();
            klass.id = modelId;
            dot += Integer.toString(modelId) + " [ label = <{<b>" + entry.getKey() + "</b>|";

            //Atributos
            for (Attribute attr : klass.attributesL) {
                String aux;
                aux = attr.toStringUml();

                dot += aux + "<br align=\"left\"/>";
            }

            dot += "|";

            for (Method method : klass.methodsL) {
                dot += method.toStringUml() + "<br align=\"left\"/>";
            }

            dot += "}>]\n";
            
            klass.inferenceRelationsKlass(this);
            
            modelId++;
        }

        //Gerar as boxes das Interfaces
        for (Map.Entry<String, Interfaze> entry : interfazeMap.entrySet()) {
            Interfaze inter = entry.getValue();
            inter.id = modelId;
            dot += Integer.toString(modelId) + " [ label = <{&lt;&lt;<i>" + entry.getKey() + "</i>&gt;&gt;||";

            for (Method method : inter.methodsL) {
                dot += method.toStringUml() + "<br align=\"left\"/>";
            }

            dot += "}>]\n";

            inter.inferenceRelationsInterfaze(this);
            
            modelId++;
        }

        // Direções dos grafos vai aqui
        
        
        
        //
        dot = dotHeader + dot + dotFooter;
        System.out.println(dot);

        try {
            PrintWriter writer = new PrintWriter("diagram.dot", "UTF-8");
            writer.println(dot);
            writer.close();
        } catch (IOException e) {
            // do something
        }
    }
}
