package ch.cyberduck.ui.layout;

/*
 *  ch.cyberduck.ui.layout.ConstraintLayout.java
 *  Cyberduck
 *
 *  Copyright (c) 2001 jhlabs. All rights reserved.
 */

import java.awt.*;
import java.util.Hashtable;

/**
 * A base class for layouts which simplifies the business of building new
 * layouts with constraints.
 */
public class ConstraintLayout implements LayoutManager2 {

	protected final static int PREFERRED = 0;
	protected final static int MINIMUM = 1;
	protected final static int MAXIMUM = 2;

	protected int hMargin = 0;
	protected int vMargin = 0;
	private Hashtable constraints;
	protected boolean includeInvisible = false;

	public void addLayoutComponent(String constraint, Component c) {
		setConstraint(c, constraint);
	}

	public void addLayoutComponent(Component c, Object constraint) {
		setConstraint(c, constraint);
	}

	public void removeLayoutComponent(Component c) {
		if (constraints != null)
			constraints.remove(c);
	}

	public void setConstraint(Component c, Object constraint) {
		if (constraint != null) {
			if (constraints == null)
				constraints = new Hashtable();
			constraints.put(c, constraint);
		} else if (constraints != null)
			constraints.remove(c);
	}
	
	public Object getConstraint(Component c) {
		if (constraints != null)
			return constraints.get(c);
		return null;
	}
	
	public void setIncludeInvisible(boolean includeInvisible) {
		this.includeInvisible = includeInvisible;
	}

	public boolean getIncludeInvisible() {
		return includeInvisible;
	}

	protected boolean includeComponent(Component c) {
		return includeInvisible || c.isVisible();
	}
	
	public Dimension minimumLayoutSize (Container target) {
		return calcLayoutSize(target, MINIMUM);
	}
	
	public Dimension maximumLayoutSize (Container target) {
		return calcLayoutSize(target, MAXIMUM);
	}
	
	public Dimension preferredLayoutSize (Container target) {
		return calcLayoutSize(target, PREFERRED);
	}
	
	public Dimension calcLayoutSize (Container target, int type) {
		Dimension dim = new Dimension(0, 0);
		measureLayout(target, dim, type);
		Insets insets = target.getInsets();
		dim.width += insets.left + insets.right + 2*hMargin;
		dim.height += insets.top + insets.bottom + 2*vMargin;
		return dim;
	}

	public void invalidateLayout(Container target) {
	}
	
	public float getLayoutAlignmentX(Container parent) {
		return 0.5f;
	}

	public float getLayoutAlignmentY(Container parent) {
		return 0.5f;
	}

	public void layoutContainer(Container target)  {
		measureLayout(target, null, PREFERRED);
	}
	
	public void measureLayout(Container target, Dimension dimension, int type)  {
	}

	protected Dimension getComponentSize(Component c, int type) {
//            return c.getMaximumSize();
		if (type == MINIMUM)
			return c.getMinimumSize();
		if (type == MAXIMUM)
			return c.getMaximumSize();
		return c.getPreferredSize();
	}
}
