package core.pool;

public interface IPoolable {
	void init();
	void clear();
	boolean inUse();
}
