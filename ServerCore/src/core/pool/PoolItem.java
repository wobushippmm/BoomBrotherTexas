package core.pool;

public class PoolItem implements IPoolable {
	protected boolean _inUse = false;
	@Override
	public void init() {
		_inUse = true;
	}

	@Override
	public void clear() {
		_inUse = false;
	}

	@Override
	public boolean inUse() {
		return _inUse;
	}

}
