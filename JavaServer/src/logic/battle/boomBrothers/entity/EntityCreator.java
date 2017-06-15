package logic.battle.boomBrothers.entity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import template.HeroTemplate;
import template.HeroTemplate.HeroTemp;
import logic.battle.boomBrothers.BattleField;
import logic.battle.boomBrothers.BattlePlayer;
import core.log.LoggerHelper;
import core.pool.ObjectPool;


public class EntityCreator {
	private Logger log = LoggerHelper.getLogger();
	public static EntityCreator instance;
	
	public ObjectPool<BattleEntity> entityPool = new ObjectPool<BattleEntity>(BattleEntity.class, 1000, 100000);
	public ObjectPool<MapEntity> mapPool = new ObjectPool<MapEntity>(MapEntity.class, 20, 100);
	
	public int[][] terrain = null;
	public int step = 20; // 步长
	
	public EntityCreator(){
		instance = this;
		loadTerrain();
	}
	
	public BattleEntity createHero(BattleField bf, BattlePlayer player){
		BattleEntity entity = entityPool.borrowObj();
		entity.id = bf.newLocalID();
		
		HeroTemp temp = HeroTemplate.instance.HeroTempDic.get(player.heroID);
		entity.heroName = temp.name;
		entity.speed = 300;
		entity.hp = entity.hpMax = 500;
		
		return entity;
	}
	
	public BattleEntity createBaseCamp(BattleField bf){
		BattleEntity entity = entityPool.borrowObj();
		entity.id = bf.newLocalID();
		
		entity.hp = entity.hpMax = 1000;
		return entity;
	}
	
	public BattleEntity createCreature(BattleField bf, int tempID){
		BattleEntity entity = entityPool.borrowObj();
		entity.id = bf.newLocalID();
		
		entity.hp = 0;
		entity.hpMax = 100;
		return entity;
	}
	
	public void loadTerrain(){
		File file = new File("map/Map.map");
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			ArrayList<String> lines = new ArrayList<String>();
			String line = null;
			while((line = reader.readLine()) != null){
				lines.add(line);
				
			}
			if(lines.size() > 0){
				// x 左右		y 上下 
				terrain = new int[lines.get(0).length()][lines.size()];
				char one = "1".charAt(0);
				for(int j=0; j<lines.size(); j++){
					line = lines.get(j);
					for(int i=0; i<line.length(); i++){
						terrain[i][j] = line.charAt(i) == one ? 1 : 0;
					}
				}
			}
		} catch (IOException e) {
			log.error(e);
		}
	}
}
