package loon.action.sprite;

import loon.opengl.GLEx;
import loon.utils.TArray;

public class MovieSprite extends DisplayObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private TArray<DisplayObject> _childs = new TArray<DisplayObject>();

	public MovieSprite() {

	}

	@Override
	protected void addedToStage() {
		for (DisplayObject object : _childs) {
			object.addedToStage();
		}
	}

	@Override
	protected void removedFromStage() {
		for (DisplayObject object : _childs) {
			object.removedFromStage();
		}
	}

	public DisplayObject addChild(DisplayObject object) {
		if (object.getParent() != null && (object instanceof MovieSprite)) {
			MovieSprite parent = (MovieSprite) object.getParent();
			parent.removeChild(object);
		}
		_childs.add(object);
		object.setParent(this);
		return object;
	}

	public DisplayObject addChildAt(DisplayObject object, int index) {
		if (object.getParent() != null && (object instanceof MovieSprite)) {
			MovieSprite parent = (MovieSprite) object.getParent();
			parent.removeChild(object);
		}
		_childs.insert(index, object);
		object.setParent(this);
		return object;
	}

	public DisplayObject removeChild(DisplayObject object) {
		if (_childs.remove(object)) {
			object.setParent(null);
		}
		return object;
	}

	public DisplayObject removeChildAt(int index) {
		if (index >= _childs.size) {
			return null;
		}
		DisplayObject object = _childs.get(index);
		_childs.removeIndex(index);
		object.setParent(null);
		return object;
	}

	public int getChildIndex(DisplayObject object) {
		return _childs.indexOf(object);
	}

	public DisplayObject getChildAt(int index) {
		if (index < 0 || index >= _childs.size) {
			return null;
		}
		return _childs.get(index);
	}

	public DisplayObject getChildByName(String name) {
		if (null != name) {
			for (DisplayObject object : _childs) {
				if (null != object.getName() && object.getName().equals(name)) {
					return object;
				}
			}
		}
		return null;
	}

	public int numChildren() {
		return _childs.size;
	}

	public void removeAllChildren() {
		_childs.clear();
	}

	@Override
	protected void enterFrame(long time) {
		for (DisplayObject object : _childs) {
			object.enterFrame(time);
		}
	}

	protected void onScaleChange(float scaleX, float scaleY) {
		for (DisplayObject object : _childs) {
			object.onScaleChange(scaleX, scaleY);
		}
	}

	@Override
	public void update(long elapsedTime) {
		for (DisplayObject object : _childs) {
			object.enterFrame(elapsedTime);
		}
	}

	@Override
	public void createUI(GLEx g) {
		for (DisplayObject object : _childs) {
			if (object.isVisible()) {
				object.createUI(g);
			}
		}
	}

	@Override
	public void close() {
		for (DisplayObject object : _childs) {
			object.close();
		}
		_childs.clear();
	}

}
