package template;
import java.util.HashMap;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.ParseException;
public class HeroTemplate{
public static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
public static HeroTemplate instance;
public HeroTemplate(){instance = this;}
public HashMap<Object, HeroTemp> HeroTempDic = new HashMap<Object, HeroTemp>();
public class HeroTemp{
public HeroTemp(){}
public int id = 0;
public void setid(String value){id = Integer.parseInt(value);}
public String name = "";
public void setname(String value){name = value;}
public int type = 0;
public void settype(String value){type = Integer.parseInt(value);}
public long prop1 = 0;
public void setprop1(String value){prop1 = Long.parseLong(value);}
public float prop2 = 0;
public void setprop2(String value){prop2 = Float.parseFloat(value);}
public double prop3 = 0;
public void setprop3(String value){prop3 = Double.parseDouble(value);}
public Date pro4 = null;
public void setpro4(String value) throws ParseException{pro4 = format.parse(value);}
public HashMap<Object, HasSkillTemp> HasSkillTempDic = new HashMap<Object, HasSkillTemp>();
public class HasSkillTemp{
public HasSkillTemp(){}
public int id = 0;
public void setid(String value){id = Integer.parseInt(value);}
public int type = 0;
public void settype(String value){type = Integer.parseInt(value);}
}
}
}