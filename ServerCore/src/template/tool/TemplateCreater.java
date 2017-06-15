package template.tool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;


public class TemplateCreater {
	/**
	 * xml 格式
	 * <ClassName> 配置类名
	 * 	<Define> 每个xml有一个类型定义标签
	 *  	<ItemClassName1 id="int">
	 *  		<ItemClassName2 id="string" name="string"/>
	 *  	</ItemClassName1>
	 *  </Define>
	 *  <ItemClassName1 id="1"> 数据
	 *  	<ItemClassName2 id="strName" name="string"/>
	 *  </ItemClassName1>
	 * </ClassName>
	 * 
	 * @param path
	 */
	public SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public String convertToCode(File file, String packName){
		SAXReader reader = new SAXReader();
		if(!file.isDirectory() && file.getName().substring(file.getName().lastIndexOf(".")).equals(".xml")){
			try {
				Document doc = reader.read(file);
				Element rootElem = doc.getRootElement();
				Element defineElem = (Element)rootElem.element("Define").elementIterator().next();
				
				String code = packName.equals("") ? "" : "package " + packName + ";\n";
				code += "import java.util.HashMap;\n";
				code += "import java.text.SimpleDateFormat;\n";
				code += "import java.util.Date;\n";
				code += "import java.text.ParseException;\n";
				code += "public class " + rootElem.getName() + "{\n";
				code += "public static SimpleDateFormat format = new SimpleDateFormat(\"yyyy-MM-dd HH:mm:ss\");\n";
				code += "public static " + rootElem.getName() + " instance;\n";
				code += "public " + rootElem.getName() + "(){instance = this;}\n";
				code += "public HashMap<Object, " + defineElem.getName() + "> " + defineElem.getName() + "Dic = new HashMap<Object, " + defineElem.getName() + ">();\n";
				code += codeClass(defineElem);
				code += "}";
				return code;
			} catch (DocumentException e) {
				e.printStackTrace();
			}
		}
		return "";
	}
	public String codeClass(Element defElem){
		String code = "public class " + defElem.getName() + "{\n";
		code += "public " + defElem.getName() + "(){}\n";
		List<Attribute> attrs = defElem.attributes();
		for(Attribute attr : attrs){
			switch(attr.getValue().toLowerCase()){
			case "int":
				code += "public int " + attr.getName() + " = 0;\n";
				code += "public void set" + attr.getName() + "(String value){" + attr.getName() + " = Integer.parseInt(value);}\n";
				break;
			case "long":
				code += "public long " + attr.getName() + " = 0;\n";
				code += "public void set" + attr.getName() + "(String value){" + attr.getName() + " = Long.parseLong(value);}\n";
				break;
			case "float":
				code += "public float " + attr.getName() + " = 0;\n";
				code += "public void set" + attr.getName() + "(String value){" + attr.getName() + " = Float.parseFloat(value);}\n";
				break;
			case "double":
				code += "public double " + attr.getName() + " = 0;\n";
				code += "public void set" + attr.getName() + "(String value){" + attr.getName() + " = Double.parseDouble(value);}\n";
				break;
			case "string":
				code += "public String " + attr.getName() + " = \"\";\n";
				code += "public void set" + attr.getName() + "(String value){" + attr.getName() + " = value;}\n";
				break;
			case "date":
				code += "public Date " + attr.getName() + " = null;\n";
				code += "public void set" + attr.getName() + "(String value) throws ParseException{" + attr.getName() + " = format.parse(value);}\n";
				break;
			}
		}
		
		Iterator iter = defElem.elementIterator();
		while(iter.hasNext()){
			Element elem = (Element)iter.next();
			code += "public HashMap<Object, " + elem.getName() + "> " + elem.getName() + "Dic = new HashMap<Object, " + elem.getName() + ">();\n";
			code += codeClass(elem);
		}
		
		code += "}\n";
		return code;
	}
	
	public static void main(String[] args){
		if(args.length < 1)return;
		String path = args[0];
		String packName = args.length > 1 ? args[1] : "";
		
		TemplateCreater creater = new TemplateCreater();
		File dir = new File(path);
		if(dir.exists() && dir.isDirectory()){
			File[] files = dir.listFiles();
			for(File file :files){
				String code = creater.convertToCode(file, packName);
				OutputStream os;
				try {
					os = new FileOutputStream(file.getPath().substring(0, file.getPath().lastIndexOf(".")) + ".java");
					os.write(code.getBytes());
					os.flush();
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
		
	}
}
