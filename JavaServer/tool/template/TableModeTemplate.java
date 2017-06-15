package template;
import java.util.HashMap;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.ParseException;
public class TableModeTemplate{
public static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
public static TableModeTemplate instance;
public TableModeTemplate(){instance = this;}
public HashMap<Object, TableModeTemp> TableModeTempDic = new HashMap<Object, TableModeTemp>();
public class TableModeTemp{
public TableModeTemp(){}
public int id = 0;
public void setid(String value){id = Integer.parseInt(value);}
public String name = "";
public void setname(String value){name = value;}
public int seatNum = 0;
public void setseatNum(String value){seatNum = Integer.parseInt(value);}
public int minLevel = 0;
public void setminLevel(String value){minLevel = Integer.parseInt(value);}
public int gold = 0;
public void setgold(String value){gold = Integer.parseInt(value);}
public int blinds = 0;
public void setblinds(String value){blinds = Integer.parseInt(value);}
public int step = 0;
public void setstep(String value){step = Integer.parseInt(value);}
}
}