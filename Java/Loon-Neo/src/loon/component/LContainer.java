/**
 * 
 * Copyright 2008 - 2009
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * @project loon
 * @author cping
 * @email：javachenpeng@yahoo.com
 * @version 0.1
 */
package loon.component;

import loon.opengl.GLEx;
import loon.utils.CollectionUtils;
import loon.utils.LayerSorter;

public abstract class LContainer extends LComponent {

	protected boolean locked;

	private final static LayerSorter<LComponent> compSorter = new LayerSorter<LComponent>(
			false);

	private LComponent[] childs = new LComponent[0];

	private int childCount = 0;

	private LComponent latestInserted = null;

	public LContainer(int x, int y, int w, int h) {
		super(x, y, w, h);
		this.setFocusable(false);
	}

	@Override
	public boolean isContainer() {
		return true;
	}

	public void add(LComponent comp) {
		if (this == comp) {
			return;
		}
		if (this.contains(comp)) {
			return;
		}
		if (comp.getContainer() != null) {
			comp.setContainer(null);
		}
		comp.setContainer(this);
		this.childs = CollectionUtils.expand(this.childs, 1, false);
		this.childs[0] = comp;
		this.childCount++;
		this.desktop.setDesktop(comp);
		if (desktop != null) {
			if (this.input == null) {
				this.input = desktop.input;
			}
			if (comp.input == null) {
				comp.input = desktop.input;
			}
		}
		this.sortComponents();
		this.latestInserted = comp;
	}

	public void add(LComponent comp, int index) {
		if (comp.getContainer() != null) {
			throw new IllegalStateException(comp
					+ " already reside in another container!!!");
		}
		comp.setContainer(this);
		LComponent[] newChilds = new LComponent[this.childs.length + 1];
		this.childCount++;
		int ctr = 0;
		for (int i = 0; i < this.childCount; i++) {
			if (i != index) {
				newChilds[i] = this.childs[ctr];
				ctr++;
			}
		}
		this.childs = newChilds;
		this.childs[index] = comp;
		this.desktop.setDesktop(comp);
		this.sortComponents();
		this.latestInserted = comp;
	}

	public boolean contains(LComponent comp) {
		if (comp == null) {
			return false;
		}
		if (childs == null) {
			return false;
		}
		for (int i = 0; i < this.childCount; i++) {
			if (childs[i] != null && comp.equals(childs[i])) {
				return true;
			}
		}
		return false;
	}

	public int remove(LComponent comp) {
		final int size = this.childCount;
		for (int i = size - 1; i > -1; i--) {
			if (this.childs[i] == comp) {
				this.remove(i);
				return i;
			}
		}
		return -1;
	}

	public boolean removeTag(Object tag) {
		boolean flag = false;
		final int size = this.childCount;
		for (int i = size - 1; i > -1; i--) {
			if (this.childs[i].Tag == tag || tag.equals(this.childs[i].Tag)) {
				this.remove(i);
				flag = true;
			}
		}
		return flag;
	}
	
	public boolean removeNotTag(Object tag) {
		boolean flag = false;
		final int size = this.childCount;
		for (int i = size - 1; i > -1; i--) {
			if (!tag.equals(this.childs[i].Tag)) {
				this.remove(i);
				flag = true;
			}
		}
		return flag;
	}

	public boolean removeUIName(String name) {
		boolean flag = false;
		final int size = this.childCount;
		for (int i = size - 1; i > -1; i--) {
			if (name.equals(this.childs[i].getUIName())) {
				this.remove(i);
				flag = true;
			}
		}
		return flag;
	}
	
	public boolean removeNotUIName(String name) {
		boolean flag = false;
		final int size = this.childCount;
		for (int i = size - 1; i > -1; i--) {
			if (!name.equals(this.childs[i].getUIName())) {
				this.remove(i);
				flag = true;
			}
		}
		return flag;
	}

	public boolean removeName(String name) {
		boolean flag = false;
		final int size = this.childCount;
		for (int i = size - 1; i > -1; i--) {
			if (name.equals(this.childs[i].getName())) {
				this.remove(i);
				flag = true;
			}
		}
		return flag;
	}
	
	public boolean removeNotName(String name) {
		boolean flag = false;
		final int size = this.childCount;
		for (int i = size - 1; i > -1; i--) {
			if (!name.equals(this.childs[i].getName())) {
				this.remove(i);
				flag = true;
			}
		}
		return flag;
	}
	
	public LComponent remove(int index) {
		LComponent comp = this.childs[index];

		this.desktop.setComponentStat(comp, false);
		comp.setContainer(null);
		// comp.dispose();
		this.childs = CollectionUtils.cut(this.childs, index);
		this.childCount--;

		return comp;
	}

	public void clear() {
		this.desktop.clearComponentsStat(this.childs);
		for (int i = 0; i < this.childCount; i++) {
			this.childs[i].setContainer(null);
			// this.childs[i].dispose();
		}
		this.childs = new LComponent[0];
		this.childCount = 0;
	}

	public void replace(LComponent oldComp, LComponent newComp) {
		int index = this.remove(oldComp);
		this.add(newComp, index);
	}

	@Override
	public void update(long timer) {
		if (isClose) {
			return;
		}
		if (!this.isVisible()) {
			return;
		}
		synchronized (childs) {
			super.update(timer);
			LComponent component;
			for (int i = 0; i < this.childCount; i++) {
				component = childs[i];
				if (component != null) {
					component.update(timer);
				}
			}
		}
	}

	@Override
	public void validatePosition() {
		if (isClose) {
			return;
		}
		super.validatePosition();
		for (int i = 0; i < this.childCount; i++) {
			this.childs[i].validatePosition();
		}
		if (!this.elastic) {
			for (int i = 0; i < this.childCount; i++) {
				if (this.childs[i].getX() > this.getWidth()
						|| this.childs[i].getY() > this.getHeight()
						|| this.childs[i].getX() + this.childs[i].getWidth() < 0
						|| this.childs[i].getY() + this.childs[i].getHeight() < 0) {
					setElastic(true);
					break;
				}
			}
		}
	}

	@Override
	protected void validateSize() {
		super.validateSize();

		for (int i = 0; i < this.childCount; i++) {
			this.childs[i].validateSize();
		}
	}

	@Override
	public void createUI(GLEx g) {
		if (isClose) {
			return;
		}
		if (!this.isVisible()) {
			return;
		}
		synchronized (childs) {
			super.createUI(g);
			if (this.elastic) {
				g.setClip(this.getScreenX(), this.getScreenY(),
						this.getWidth(), this.getHeight());
			}
			this.renderComponents(g);
			if (this.elastic) {
				g.clearClip();
			}
		}
	}

	protected void renderComponents(GLEx g) {
		for (int i = this.childCount - 1; i >= 0; i--) {
			this.childs[i].createUI(g);
		}
	}

	public void sendToFront(LComponent comp) {
		if (this.childCount <= 1 || this.childs[0] == comp) {
			return;
		}
		if (childs[0] == comp) {
			return;
		}
		for (int i = 0; i < this.childCount; i++) {
			if (this.childs[i] == comp) {
				this.childs = CollectionUtils.cut(this.childs, i);
				this.childs = CollectionUtils.expand(this.childs, 1, false);
				this.childs[0] = comp;
				this.sortComponents();
				break;
			}
		}
	}

	public void sendToBack(LComponent comp) {
		if (this.childCount <= 1 || this.childs[this.childCount - 1] == comp) {
			return;
		}
		if (childs[this.childCount - 1] == comp) {
			return;
		}
		for (int i = 0; i < this.childCount; i++) {
			if (this.childs[i] == comp) {
				this.childs = CollectionUtils.cut(this.childs, i);
				this.childs = CollectionUtils.expand(this.childs, 1, true);
				this.childs[this.childCount - 1] = comp;
				this.sortComponents();
				break;
			}
		}
	}

	public void sortComponents() {
		compSorter.sort(this.childs);
	}

	protected void transferFocus(LComponent component) {
		for (int i = 0; i < this.childCount; i++) {
			if (component == this.childs[i]) {
				int j = i;
				do {
					if (--i < 0) {
						i = this.childCount - 1;
					}
					if (i == j) {
						return;
					}
				} while (!this.childs[i].requestFocus());

				break;
			}
		}
	}

	protected void transferFocusBackward(LComponent component) {
		for (int i = 0; i < this.childCount; i++) {
			if (component == this.childs[i]) {
				int j = i;
				do {
					if (++i >= this.childCount) {
						i = 0;
					}
					if (i == j) {
						return;
					}
				} while (!this.childs[i].requestFocus());

				break;
			}
		}
	}

	public boolean isSelected() {
		if (!super.isSelected()) {
			for (int i = 0; i < this.childCount; i++) {
				if (this.childs[i].isSelected()) {
					return true;
				}
			}
			return false;

		} else {
			return true;
		}
	}

	public boolean isElastic() {
		return this.elastic;
	}

	public void setElastic(boolean b) {
		if (getWidth() > 32 || getHeight() > 32) {
			this.elastic = b;
		} else {
			this.elastic = false;
		}
	}

	public LComponent findComponent(int x1, int y1) {
		if (!this.intersects(x1, y1)) {
			return null;
		}
		for (int i = 0; i < this.childCount; i++) {
			if (this.childs[i].intersects(x1, y1)) {
				LComponent comp = (!this.childs[i].isContainer()) ? this.childs[i]
						: ((LContainer) this.childs[i]).findComponent(x1, y1);
				LContainer container = comp.getContainer();
				if (container != null && container instanceof LScrollContainer) {
					if (container.contains(comp)
							&& (comp.getWidth() >= container.getWidth() || comp
									.getHeight() >= container.getHeight())) {
						return comp.getContainer();
					}
				}
				return comp;
			}
		}
		return this;
	}

	public int getComponentCount() {
		return this.childCount;
	}

	public LComponent[] getComponents() {
		return this.childs;
	}

	public LComponent get() {
		return this.latestInserted;
	}

	@Override
	public void close() {
		super.close();
		if (autoDestroy) {
			if (childs != null) {
				for (LComponent c : childs) {
					if (c != null) {
						c.close();
						c = null;
					}
				}
			}
		}
	}

}
