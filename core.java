
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class KlassBuilder {

  public Map<String, Klass> klassMap = new HashMap<>();
  public Map<String, Interfaze> interfazeMap = new HashMap<>();
  public ArrayList<PrebuiltRelation> prebuiltRelations = new ArrayList<>();

  public static class PrebuiltRelation {

    public String from;
    public String to;
    public KlassBuilder.Relation relation;

    public PrebuiltRelation(String from, String to, Relation relation) {
      this.from = from;
      this.to = to;
      this.relation = relation;
    }
  }

  public void processPrebuiltinRelations() {
    for (PrebuiltRelation relation : prebuiltRelations) {
      Object from;
      Object to;

      from = klassMap.get(relation.from);

      if (from == null) {
        from = interfazeMap.get(relation.from);
      }

      to = klassMap.get(relation.to);

      if (to == null) {
        to = interfazeMap.get(relation.to);
      }

      if(relation.from.equals(relation.to)){
        return;
      }else if (from == null || to == null) {
        throw new Error("[CORE ERROR] Some of the builtin were not found : [" + (from!=null ?"":relation.from+" not found.") + " | " + (to!=null?"":relation.to+" not found.") + "], make sure it really exists");
      } else if (to instanceof KlassBuilder.Klass && relation.relation.getType() == RelationType.REALIZATION) {
        throw new Error("[CORE ERROR] Cannot implement a class.");
      } else if (to instanceof KlassBuilder.Klass && from instanceof KlassBuilder.Interfaze && (relation.relation.getType() == RelationType.GENERALIZATION || relation.relation.getType() == RelationType.REALIZATION)) {
        throw new Error("[CORE ERROR] Interface can neither implement a class nor extend a class.");
      }

      replaceIfMoreImportant(from, to, relation.relation);

    }
  }

  public static String recoverEntityName(String entity) {
    return entity.split("\\[")[0];
  }

  public static boolean isCollection(String entity) {
    return entity.contains("[");
  }

  public boolean replaceIfMoreImportant(Object from, Object to, KlassBuilder.Relation rel) {
    HashMap<String, Relation> relationsFrom;

    String fromName;
    String toName;

    if (from instanceof KlassBuilder.Klass) {
      KlassBuilder.Klass fromObj = ((KlassBuilder.Klass) from);
      fromName = fromObj.name;

      if (to instanceof KlassBuilder.Klass) {
        KlassBuilder.Klass toObj = ((KlassBuilder.Klass) to);
        toName = toObj.name;
        relationsFrom = fromObj.getRelationsWithClasses();
      } else {
        KlassBuilder.Interfaze toObj = ((KlassBuilder.Interfaze) to);
        toName = toObj.name;
        relationsFrom = fromObj.getRelationsWithInterfaces();
      }

    } else {
      KlassBuilder.Interfaze fromObj = ((KlassBuilder.Interfaze) from);
      fromName = fromObj.name;

      if (to instanceof KlassBuilder.Klass) {
        KlassBuilder.Klass toObj = ((KlassBuilder.Klass) to);
        toName = toObj.name;
        relationsFrom = fromObj.getRelationsWithClasses();
      } else {
        KlassBuilder.Interfaze toObj = ((KlassBuilder.Interfaze) to);
        toName = toObj.name;
        relationsFrom = fromObj.getRelationsWithInterfaces();
      }

    }

    KlassBuilder.Relation relationFromTo = relationsFrom.get(toName);

    if (relationFromTo != null) { // there's a relation from to
    if (!relationFromTo.explicit && (relationFromTo.getType().getWeight() < rel.getType().getWeight() || rel.explicit)) {
      relationsFrom.replace(toName, rel);
    } else {
      return false;
    }
  } else {
    relationsFrom.put(toName, rel);
  }
  return true;
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
  ASSOCIATION("association", 1),
  DASSOCIATION("dassociation", 1),
  AGGREGATION("aggregation", 3),
  DAGGREGATION("daggregation", 3),
  COMPOSITION("composition", 4),
  DCOMPOSITION("dcomposition", 4),
  DEPENDENCY("dependency", 0),
  DDEPENDENCY("ddependency", 0),
  GENERALIZATION("generalization", 2),
  REALIZATION("realization", 2);

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
  public String arrowtail = "";
  public String dir = "";

  @Override
  public String toString() {
    return this.type.toString() + " @ \"" + headlabel + " to \"" + taillabel + "\" title \"" + title + "\"" + "\n"
    + "style:" + style + "\tarrowhead:" + arrowhead + "\tarrowtail:" + arrowtail + "\tdir:" + dir;
  }

  public String toStringUml() {
    return "[style = \"" + style + "\" arrowhead = \"" + arrowhead + "\" arrowtail = \"" + arrowtail + "\" dir = \"" + dir + "\" taillabel = \""
    + (taillabel.replaceAll("\"", ""))
    + "\" headlabel = \"" + (headlabel.replaceAll("\"", "")) + "\" label = \"" + (title.replaceAll("\"", "")) + "\"]";
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
      this.dir = "back";
      this.style = "solid";
      this.arrowtail = "odiamond";
      break;
      case DAGGREGATION:
      this.dir = "back";
      this.style = "solid";
      this.arrowtail = "odiamond";
      break;
      case COMPOSITION:
      this.dir = "back";
      this.style = "solid";
      this.arrowtail = "diamond";
      break;
      case DCOMPOSITION:
      this.dir = "back";
      this.style = "solid";
      this.arrowtail = "diamond";
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

  public HashMap<String, Relation> getRelationsWithInterfaces() {
    return relationsWithInterfaces;
  }

  public HashMap<String, Relation> getRelationsWithClasses() {
    return relationsWithClasses;
  }

  public void print() {
    System.out.println("@ Interface " + this.name);

    System.out.println("- Methods");
    for (KlassBuilder.Method meth : this.methodsL) {
      System.out.print("\t" + (meth.modifyer.getType()) + " " + (meth.isStatic ? "_" : "") + " " + meth.returnType + " " + meth.name + "(");
      for (KlassBuilder.Paramether par : meth.paramethers) {
        System.out.print(par.type + " " + par.name + ",");
      }
      System.out.println(")");
    }

    System.out.println("\n\n- Relations with classes");

    for (Map.Entry<String, KlassBuilder.Relation> rel : relationsWithClasses.entrySet()) {
      System.out.println("\t- With class" + rel.getKey());
      System.out.println("\t\t" + rel.getValue());
      System.out.println("\n");
    }

    System.out.println("\n\n- Relations with interfaces");

    for (Map.Entry<String, KlassBuilder.Relation> rel : relationsWithInterfaces.entrySet()) {
      System.out.println("\t- With interface " + rel.getKey());
      System.out.println("\t\t" + rel.getValue());
      System.out.println("\n");
    }

    System.out.println("# End\n\n");
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
    for (Method method : methodsL) {
      name = KlassBuilder.recoverEntityName(method.returnType);
      if (name.charAt(0) >= 'A' && name.charAt(0) <= 'Z') {
        if (builder.klassMap.containsKey(name)) {
          Relation r = new Relation();
          r.setType(RelationType.DDEPENDENCY);
          r.explicit = false;

          builder.replaceIfMoreImportant(this, builder.klassMap.get(name), r);
        }
        if (builder.interfazeMap.containsKey(name)) {
          Relation r = new Relation();
          r.setType(RelationType.DDEPENDENCY);
          r.explicit = false;

          builder.replaceIfMoreImportant(this, builder.interfazeMap.get(name), r);
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

            builder.replaceIfMoreImportant(this, builder.klassMap.get(name), r);
          }

          if (builder.interfazeMap.containsKey(name)) {
            Relation r = new Relation();
            r.setType(RelationType.DDEPENDENCY);
            r.explicit = false;

            builder.replaceIfMoreImportant(this, builder.interfazeMap.get(name), r);
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

          if (KlassBuilder.isCollection(attr.returnType)) {
            r.headlabel = "0..*";
          } else {
            r.headlabel = "1";
          }

          builder.replaceIfMoreImportant(this, builder.klassMap.get(name), r);
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

          builder.replaceIfMoreImportant(this, builder.klassMap.get(name), r);
        }

        if (builder.interfazeMap.containsKey(name)) {
          Relation r = new Relation();
          r.setType(RelationType.DDEPENDENCY);
          r.explicit = false;

          builder.replaceIfMoreImportant(this, builder.interfazeMap.get(name), r);
        }
      }

      for (Paramether p : method.paramethers) {
        name = KlassBuilder.recoverEntityName(p.type);
        if (name.charAt(0) >= 'A' && name.charAt(0) <= 'Z') {
          if (builder.klassMap.containsKey(name)) {
            Relation r = new Relation();
            r.setType(RelationType.DDEPENDENCY);
            r.explicit = false;

            builder.replaceIfMoreImportant(this, builder.klassMap.get(name), r);
          }

          if (builder.interfazeMap.containsKey(name)) {
            Relation r = new Relation();
            r.setType(RelationType.DDEPENDENCY);
            r.explicit = false;

            builder.replaceIfMoreImportant(this, builder.interfazeMap.get(name), r);
          }
        }
      }
    }
  }

  public HashMap<String, Relation> getRelationsWithClasses() {
    return relationsWithClasses;
  }

  public HashMap<String, Relation> getRelationsWithInterfaces() {
    return relationsWithInterfaces;
  }

  public void print() {
    System.out.println("@ Class " + this.name);

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
      System.out.println("\t\t" + rel.getValue());
      System.out.println("\n");
    }
    System.out.println("- Relations with interfaces");
    for (Map.Entry<String, Relation> rel : this.relationsWithInterfaces.entrySet()) {
      System.out.println("\t- With interfaces " + rel.getKey());
      System.out.println("\t\t" + rel.getValue());
      System.out.println("\n");
    }
    System.out.println("# End\n\n");
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
    return (modifyer.getType()) + (isStatic ? "<u>" : "") + name + (isStatic ? "</u>" : "") + ": " + returnType;
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

    // PROCESSAR AS RELAÇÕES INFERENCIADAS
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

    dot += "}>]\n\n";

    // PROFESSAR AS RELAÇÕES INFERENCIADAS
    inter.inferenceRelationsInterfaze(this);

    modelId++;
  }

  // Direções dos grafos vai aqui
  for (Map.Entry<String, Klass> entry : klassMap.entrySet()) {
    for (Map.Entry<String, Relation> klassRelation : entry.getValue().relationsWithClasses.entrySet()) {
      dot += entry.getValue().id + " -> " + klassMap.get(klassRelation.getKey()).id + " "
      + klassRelation.getValue().toStringUml() + "\n\n";
    }
    for (Map.Entry<String, Relation> klassInterfaces : entry.getValue().relationsWithInterfaces.entrySet()) {
      dot += entry.getValue().id + " -> " + interfazeMap.get(klassInterfaces.getKey()).id + " "
      + klassInterfaces.getValue().toStringUml() + "\n\n";
    }
  }

  for (Map.Entry<String, Interfaze> entry : interfazeMap.entrySet()) {
    for (Map.Entry<String, Relation> klassRelation : entry.getValue().relationsWithClasses.entrySet()) {
      dot += entry.getValue().id + " -> " + klassMap.get(klassRelation.getKey()).id + " "
      + klassRelation.getValue().toStringUml() + "\n\n";
    }
    for (Map.Entry<String, Relation> klassInterfaces : entry.getValue().relationsWithInterfaces.entrySet()) {
      dot += entry.getValue().id + " -> " + interfazeMap.get(klassInterfaces.getKey()).id + " "
      + klassInterfaces.getValue().toStringUml() + "\n\n";
    }
  }

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
