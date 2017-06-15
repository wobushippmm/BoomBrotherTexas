package core.pool;

import java.lang.reflect.ParameterizedType;
import java.util.LinkedList;

import core.log.LoggerHelper;

// 简单对象池实现
public class ObjectPool<T extends IPoolable> {
	private int size = 0;
	private int maxSize = 0;
	private LinkedList<T> busyList = new LinkedList<T>();
	private LinkedList<T> freeList = new LinkedList<T>();
	private Class<T> clazz = null;
	public ObjectPool(Class<T> clazz, int size, int maxSize){
		this.size = size;
		this.maxSize = maxSize;
		this.clazz = clazz;

		try {
			for(int i=0; i<size; i++){
				busyList.add(clazz.newInstance());	
			}
		} catch (InstantiationException | IllegalAccessException e) {
			LoggerHelper.getLogger().error(e);
		}
	}
	public T borrowObj(){
		T obj = null;
		if(freeList.size() > 0){
			obj = freeList.removeFirst();
		}else if(size < maxSize){
			size++;
			try {
				obj = clazz.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				LoggerHelper.getLogger().error(e);
			}
		}else{
			return null;
		}
		obj.init();
		busyList.add(obj);
		return obj;
	}
	public void returnObj(T obj){
		obj.clear();
		busyList.remove(obj);
		freeList.add(obj);
	}
	public int getSize(){
		return size;
	}
	public int getMaxSize(){
		return maxSize;
	}
}
