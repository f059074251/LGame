package loon.action;

import loon.LSystem;
import loon.action.map.Field2D;
import loon.action.sprite.ISprite;
import loon.utils.Array;
import loon.utils.Easing;
import loon.utils.TArray;

public class ActionTween extends ActionTweenBase<ActionTween> {

	private static int combinedAttrsLimit = 3;
	private static int funPointsLimit = 0;

	public static void setCombinedAttributesLimit(int limit) {
		ActionTween.combinedAttrsLimit = limit;
	}

	public static void setfunPointsLimit(int limit) {
		ActionTween.funPointsLimit = limit;
	}

	private static final ActionTweenPool.Callback<ActionTween> poolCallback = new ActionTweenPool.Callback<ActionTween>() {
		@Override
		public void onPool(ActionTween obj) {
			obj.reset();
		}

		@Override
		public void onUnPool(ActionTween obj) {
			obj.reset();
		}
	};

	private static final ActionTweenPool<ActionTween> pool = new ActionTweenPool<ActionTween>(
			20, poolCallback) {
		@Override
		protected ActionTween create() {
			return new ActionTween();
		}
	};

	/**
	 * 从当前ActionBind数值到指定目标(大多数时候，调用此状态已经足够)
	 * 
	 * @param target
	 *            具体的操作对象
	 * @param tweenType
	 *            需要转变的接口
	 * @param duration
	 *            持续时间
	 * @return
	 */
	public static ActionTween to(ActionBind target, int tweenType,
			float duration) {
		ActionTween tween = pool.get();
		tween.setup(target, tweenType, duration);
		tween.ease(Easing.QUAD_INOUT);
		tween.path(ActionControl.SMOOTH);
		return tween;
	}

	/**
	 * 从注入的数值演变到当前值
	 * 
	 * @param target
	 * @param tweenType
	 * @param duration
	 * @return
	 */
	public static ActionTween from(ActionBind target, int tweenType,
			float duration) {
		ActionTween tween = pool.get();
		tween.setup(target, tweenType, duration);
		tween.ease(Easing.QUAD_INOUT);
		tween.path(ActionControl.SMOOTH);
		tween.isFrom = true;
		return tween;
	}

	/**
	 * 直接注入当前对象为指定数值
	 * 
	 * @param target
	 * @param tweenType
	 * @return
	 */
	public static ActionTween set(ActionBind target, int tweenType) {
		ActionTween tween = pool.get();
		tween.setup(target, tweenType, 0);
		tween.ease(Easing.QUAD_INOUT);
		return tween;
	}

	/**
	 * 直接调用一个ActionCallback方法
	 * 
	 * @param callback
	 * @return
	 */
	public static ActionTween call(ActionCallback callback) {
		ActionTween tween = pool.get();
		tween.setup(null, -1, 0);
		tween.setCallback(callback);
		tween.setCallbackTriggers(ActionMode.START);
		return tween;
	}

	/**
	 * 制作一个无状态的空ActionTween对象
	 * 
	 * @return
	 */
	public static ActionTween mark() {
		ActionTween tween = pool.get();
		tween.setup(null, -1, 0);
		return tween;
	}

	public static int getPoolSize() {
		return pool.size();
	}

	public static void resize(int minCapacity) {
		pool.resize(minCapacity);
	}

	private int type;
	private Easing equation;
	private ActionPath path;

	private boolean isFrom;
	private boolean isRelative;
	private boolean isRepeat;

	private int _combinedAttrsSize;
	private int _funPointsSize;

	private final float[] startValues = new float[combinedAttrsLimit];
	private final float[] targetValues = new float[combinedAttrsLimit];
	private final float[] funPoints = new float[funPointsLimit
			* combinedAttrsLimit];

	private float[] accessorBuffer = new float[combinedAttrsLimit];
	private float[] pathBuffer = new float[(2 + funPointsLimit)
			* combinedAttrsLimit];

	private Array<ActionEvent> actionEvents;

	private ActionTween() {
		reset();
	}

	public ActionTween moveTo(float endX, float endY) {
		return moveTo(endX, endY, false, 8);
	}

	public ActionTween moveTo(float endX, float endY, int speed) {
		return moveTo(endX, endY, false, speed);
	}

	public ActionTween moveTo(float endX, float endY, boolean flag) {
		return moveTo(LSystem.viewSize.newField2D(), endX, endY, flag, 8);
	}

	public ActionTween moveTo(float endX, float endY, boolean flag, int speed) {
		return moveTo(LSystem.viewSize.newField2D(), endX, endY, flag, speed);
	}

	public ActionTween moveTo(Field2D map, float endX, float endY,
			boolean flag, int speed) {
		if (map.inside(endX, endY)) {
			MoveTo move = new MoveTo(map, endX, endY, flag, speed);
			move.setDelay(0);
			return event(move);
		} else {
			MoveBy moveby = new MoveBy(endX, endY, speed);
			return event(moveby);
		}
	}

	public ActionTween moveBy(float endX, float endY) {
		return moveBy(endX, endY, 8);
	}

	public ActionTween moveBy(float endX, float endY, int speed) {
		MoveBy moveby = new MoveBy(endX, endY, speed);
		return event(moveby);
	}

	public ActionTween fadeIn(float speed) {
		return fadeTo(ISprite.TYPE_FADE_IN, speed);
	}

	public ActionTween fadeOut(float speed) {
		return fadeTo(ISprite.TYPE_FADE_OUT, speed);
	}

	public ActionTween fadeTo(int fadeMode, float speed) {
		FadeTo fade = new FadeTo(fadeMode, (int) speed);
		fade.setDelay(0);
		return event(fade);
	}

	public ActionTween rotateTo(float angle) {
		return rotateTo(angle, 6f);
	}

	public ActionTween rotateTo(float angle, float speed) {
		RotateTo rotate = new RotateTo(angle, speed);
		rotate.setDelay(0);
		return event(rotate);
	}

	public ActionTween scaleTo(float sx, float sy) {
		return scaleTo(sx, sy, 0.1f);
	}

	public ActionTween scaleTo(float sx, float sy, float speed) {
		ScaleTo scale = new ScaleTo(sx, sy);
		scale.setDelay(0);
		scale.setSpeed(speed);
		return event(scale);
	}

	public ActionTween showTo(boolean v) {
		ShowTo show = new ShowTo(v);
		show.setDelay(0);
		return event(show);
	}

	public TArray<ActionEvent> getActionEvents() {
		if (actionEvents == null) {
			return new TArray<ActionEvent>(0);
		}
		return new TArray<ActionEvent>(actionEvents);
	}

	@Override
	public ActionTween delay(float d) {
		super.delay(delay);
		if (actionEvents != null && d > 0) {
			DelayTo delay = new DelayTo(d);
			delay.setDelay(0);
			return event(delay);
		} else {
			return this;
		}
	}

	public ActionTween repeat(float time) {
		return repeat(1, time);
	}

	@Override
	public ActionTween repeat(int count, float time) {
		super.repeat(count, time);
		if (actionEvents == null) {
			return this;
		}
		isRepeat = true;
		boolean update = count > 1;
		ReplayTo replay = new ReplayTo(null, update);
		if (update) {
			replay.count = count;
		}
		event(replay);
		return delay(time);
	}

	@Override
	public ActionTween repeatBackward(int count, float time) {
		super.repeatBackward(count, time);
		if (actionEvents == null) {
			return this;
		}
		isRepeat = true;
		boolean update = count > 1;
		ReplayTo replay = new ReplayTo(null, update);
		if (update) {
			replay.count = count;
		}
		event(replay);
		return delay(time);
	}

	/**
	 * 自定义事件请在此处注入
	 * 
	 * @param event
	 * @return
	 */
	public ActionTween event(ActionEvent event) {
		if (actionEvents == null) {
			actionEvents = new Array<ActionEvent>();
		}
		actionEvents.add(event);
		return this;
	}

	@Override
	protected void reset() {
		super.reset();
		_target = null;
		actionEvents = null;
		currentActionEvent = null;
		type = -1;
		equation = null;
		path = null;
		isFrom = isRelative = false;
		_combinedAttrsSize = _funPointsSize = 0;
		if (accessorBuffer.length != combinedAttrsLimit) {
			accessorBuffer = new float[combinedAttrsLimit];
		}
		if (pathBuffer.length != (2 + funPointsLimit) * combinedAttrsLimit) {
			pathBuffer = new float[(2 + funPointsLimit) * combinedAttrsLimit];
		}
	}

	private void setup(ActionBind target, int tweenType, float duration) {
		if (duration < 0) {
			throw new RuntimeException("Duration can't be negative .");
		}
		this._target = target;
		this.type = tweenType;
		this.duration = duration;
	}

	public ActionTween ease(Easing ease) {
		this.equation = ease;
		return this;
	}

	public ActionTween target(float targetValue) {
		targetValues[0] = targetValue;
		return this;
	}

	public ActionTween target(float targetValue1, float targetValue2) {
		targetValues[0] = targetValue1;
		targetValues[1] = targetValue2;
		return this;
	}

	public ActionTween target(float targetValue1, float targetValue2,
			float targetValue3) {
		targetValues[0] = targetValue1;
		targetValues[1] = targetValue2;
		targetValues[2] = targetValue3;
		return this;
	}

	public ActionTween target(float... targetValues) {
		if (targetValues.length > combinedAttrsLimit) {
			return this;
		}
		System.arraycopy(targetValues, 0, this.targetValues, 0,
				targetValues.length);
		return this;
	}

	public ActionTween targetRelative(float targetValue) {
		isRelative = true;
		targetValues[0] = isInitialized() ? targetValue + startValues[0]
				: targetValue;
		return this;
	}

	public ActionTween targetRelative(float targetValue1, float targetValue2) {
		isRelative = true;
		targetValues[0] = isInitialized() ? targetValue1 + startValues[0]
				: targetValue1;
		targetValues[1] = isInitialized() ? targetValue2 + startValues[1]
				: targetValue2;
		return this;
	}

	public ActionTween targetRelative(float targetValue1, float targetValue2,
			float targetValue3) {
		isRelative = true;
		targetValues[0] = isInitialized() ? targetValue1 + startValues[0]
				: targetValue1;
		targetValues[1] = isInitialized() ? targetValue2 + startValues[1]
				: targetValue2;
		targetValues[2] = isInitialized() ? targetValue3 + startValues[2]
				: targetValue3;
		return this;
	}

	public ActionTween targetRelative(float... targetValues) {
		if (targetValues.length > combinedAttrsLimit) {
			return this;
		}
		for (int i = 0; i < targetValues.length; i++) {
			this.targetValues[i] = isInitialized() ? targetValues[i]
					+ startValues[i] : targetValues[i];
		}

		isRelative = true;
		return this;
	}

	public ActionTween funPoint(float targetValue) {
		if (_funPointsSize == funPointsLimit) {
			return this;
		}
		funPoints[_funPointsSize] = targetValue;
		_funPointsSize += 1;
		return this;
	}

	public ActionTween funPoint(float targetValue1, float targetValue2) {
		if (_funPointsSize == funPointsLimit) {
			return this;
		}
		funPoints[_funPointsSize * 2] = targetValue1;
		funPoints[_funPointsSize * 2 + 1] = targetValue2;
		_funPointsSize += 1;
		return this;
	}

	public ActionTween funPoint(float targetValue1, float targetValue2,
			float targetValue3) {
		if (_funPointsSize == funPointsLimit) {
			return this;
		}
		funPoints[_funPointsSize * 3] = targetValue1;
		funPoints[_funPointsSize * 3 + 1] = targetValue2;
		funPoints[_funPointsSize * 3 + 2] = targetValue3;
		_funPointsSize += 1;
		return this;
	}

	public ActionTween funPoint(float... targetValues) {
		if (_funPointsSize == funPointsLimit) {
			return this;
		}
		System.arraycopy(targetValues, 0, funPoints, _funPointsSize
				* targetValues.length, targetValues.length);
		_funPointsSize += 1;
		return this;
	}

	public ActionTween path(ActionPath path) {
		this.path = path;
		return this;
	}

	public ActionBind getTarget() {
		return _target;
	}

	public int getType() {
		return type;
	}

	public Easing getEasing() {
		return equation;
	}

	public float[] getTargetValues() {
		return targetValues;
	}

	public int getCombinedAttributesCount() {
		return _combinedAttrsSize;
	}

	@Override
	public void free() {
		pool.free(this);
		ActionControl.get().removeAllActions(_target);
	}

	@Override
	protected void initializeOverride() {
		if (_target == null) {
			return;
		}

		ActionType.getValues(_target, type, startValues);

		for (int i = 0; i < _combinedAttrsSize; i++) {
			targetValues[i] += isRelative ? startValues[i] : 0;

			for (int ii = 0; ii < _funPointsSize; ii++) {
				funPoints[ii * _combinedAttrsSize + i] += isRelative ? startValues[i]
						: 0;
			}

			if (isFrom) {
				float tmp = startValues[i];
				startValues[i] = targetValues[i];
				targetValues[i] = tmp;
			}
		}
	}

	private ActionEvent currentActionEvent;

	private Array<ActionEvent> repeatList;

	@Override
	protected boolean actionEventOver() {
		if (actionEvents == null) {
			return true;
		}
		if (actionEvents != null) {
			if (currentActionEvent != null && !currentActionEvent.isComplete()) {
				return false;
			} else if (currentActionEvent != null
					&& currentActionEvent.isComplete()) {
				if (repeatList == null) {
					repeatList = new Array<ActionEvent>();
				}
				if (!(currentActionEvent instanceof ReplayTo)) {
					repeatList.add(currentActionEvent.reverse());
				}
			}
			ActionEvent event = actionEvents.first();
			if (event != currentActionEvent && event != null) {
				actionEvents.remove(0);
				if (isRepeat) {
					if (event instanceof ReplayTo && repeatList != null
							&& repeatList.size() > 0) {
						ReplayTo replayTo = ((ReplayTo) event);
						int size = replayTo.count - 1;
						if (size > 0) {
							for (int i = 0; i < size; i++) {
								repeatList.addFront(new ReplayTo(null));
								repeatList.addFront(new DelayTo(0));
							}
						}
						replayTo.set(repeatList);
						repeatList.clear();
					}
				}
				ActionControl.get().addAction(event, _target);
				currentActionEvent = event;
			}
		}
		if (currentActionEvent != null && !currentActionEvent.isComplete()) {
			return false;
		}
		return (actionEvents == null || actionEvents.size() == 0);
	}

	@Override
	protected void update(int step, int lastStep, boolean isIterationStep,
			float delta) {
		if (_target == null || equation == null) {
			return;
		}
		if (!isIterationStep && step > lastStep) {
			ActionType.setValues(_target, type,
					isReverse(lastStep) ? startValues : targetValues);
			return;
		}

		if (!isIterationStep && step < lastStep) {
			ActionType.setValues(_target, type,
					isReverse(lastStep) ? targetValues : startValues);
			return;
		}

		if (duration < 0.00000000001f && delta > -0.00000000001f) {
			ActionType.setValues(_target, type, isReverse(step) ? targetValues
					: startValues);
			return;
		}

		if (duration < 0.00000000001f && delta < 0.00000000001f) {
			ActionType.setValues(_target, type, isReverse(step) ? startValues
					: targetValues);
			return;
		}

		float time = isReverse(step) ? duration - getCurrentTime()
				: getCurrentTime();

		float t = equation.apply(time, duration, false);

		if (_funPointsSize == 0 || path == null) {
			for (int i = 0; i < _combinedAttrsSize; i++) {
				accessorBuffer[i] = startValues[i] + t
						* (targetValues[i] - startValues[i]);
			}
		} else {
			for (int i = 0; i < _combinedAttrsSize; i++) {
				pathBuffer[0] = startValues[i];
				pathBuffer[1 + _funPointsSize] = targetValues[i];
				for (int ii = 0; ii < _funPointsSize; ii++) {
					pathBuffer[ii + 1] = funPoints[ii * _combinedAttrsSize + i];
				}

				accessorBuffer[i] = path.compute(t, pathBuffer,
						_funPointsSize + 2);
			}
		}

		ActionType.setValues(_target, type, accessorBuffer);
	}

	@Override
	protected void forceStartValues() {
		if (_target == null) {
			return;
		}
		ActionType.setValues(_target, type, startValues);
	}

	@Override
	protected void forceEndValues() {
		if (_target == null) {
			return;
		}
		ActionType.setValues(_target, type, targetValues);
	}

	@Override
	public ActionTween build() {
		if (_target == null) {
			return this;
		}
		_combinedAttrsSize = ActionType
				.getValues(_target, type, accessorBuffer);
		return this;
	}

	protected boolean containsTarget(ActionBind target) {
		return this._target == target;
	}

	protected boolean containsTarget(ActionBind target, int tweenType) {
		return this._target == target && this.type == tweenType;
	}
}
