package template;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import core.log.LoggerHelper;

public class TemplateReader {
	static SAXReader reader = new SAXReader();
	public static void loadTemplate(String path, Object temp){
		if(temp == null){
			return;
		}
		Document doc;
		try {
			File file = new File(path);
			doc = reader.read(file);
			Element rootElem = doc.getRootElement();
			
			readTemplate(rootElem, temp);
		} catch (DocumentException e) {
			LoggerHelper.getLogger().error(e);
		}
			
	}
	
	public static void readTemplate(Element rootElem, Object conTemp){
		Iterator iter = rootElem.elementIterator();
//		Class<?>[] clsList = null;
//		HashMap<String, Class<?>> clsDic = null;
		while(iter.hasNext()){
			Element elem = (Element)iter.next();
			Field field;
			try {
				if(elem.getName() == "Define"){
					continue;
				}
				field = conTemp.getClass().getField(elem.getName() + "Dic");
				HashMap<Object, Object> dic = (HashMap<Object, Object>) field.get(conTemp);
				
//				if(clsList == null){
//					clsList = conTemp.getClass().getDeclaredClasses();
//					clsDic = new HashMap<String, Class<?>>();
//					for(int i=0; i<clsList.length; i++){
//						clsDic.put(clsList[i].getSimpleName(), clsList[i]);
//					}
//				}
//				Class<?> cls = clsDic.get(elem.getName()); 
				
				Class<?> cls = Class.forName(conTemp.getClass().getName() + "$" + elem.getName());
				if(cls != null){
					Constructor constructor = cls.getDeclaredConstructor(conTemp.getClass());
					Object temp = constructor.newInstance(conTemp);
					
					List<Attribute> attrs = elem.attributes();
					for(Attribute attr : attrs){
						Method setter = temp.getClass().getMethod("set" + attr.getName(), String.class);
						setter.invoke(temp, attr.getValue());
					}
					
					Field idField = temp.getClass().getField("id");
					dic.put(idField.get(temp), temp);
					
					readTemplate(elem, temp);
				}
			} catch (NoSuchFieldException | SecurityException | InstantiationException | IllegalAccessException | NoSuchMethodException | IllegalArgumentException | InvocationTargetException | ClassNotFoundException e) {
				LoggerHelper.getLogger().error(e);
			}
		}
	}
}
