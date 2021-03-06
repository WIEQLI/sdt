package mil.navy.nrl.sdt3d;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

public class SdtCheckboxCellRenderer implements TreeCellRenderer
{

	private JTree checkboxTree;

	private JCheckBox checkBox;

	protected DefaultTreeCellRenderer defaultRenderer = new DefaultTreeCellRenderer();

	Vector<TreePath> checkedPaths = new Vector<TreePath>();

	boolean partiallyChecked = false;

	boolean fullyChecked = false;

	Color selectionBorderColor, selectionForeground, selectionBackground,
			textForeground, textBackground;


	protected JCheckBox getCheckbox()
	{
		return checkBox;
	}


	// ljt testing
	Vector<TreePath> getCheckedPaths()
	{
		return checkedPaths;
	}


	public SdtCheckboxCellRenderer(JTree tree)
	{
		this.checkboxTree = tree;
		checkBox = new JCheckBox();
		checkBox.setSize(checkBox.getPreferredSize());
		selectionBorderColor = UIManager.getColor("Tree.selectionBorderColor");
		selectionForeground = UIManager.getColor("Tree.selectionForeground");
		selectionBackground = UIManager.getColor("Tree.selectionBackground");
		textForeground = UIManager.getColor("Tree.textForeground");
		textBackground = UIManager.getColor("Tree.textBackground");
	}


	private boolean isNodeChecked(TreePath path)
	{
		if (path == null)
			return false;
		Object node = path.getLastPathComponent();
		if (checkedPaths.contains(path) || ((SdtCheckboxNode) node).isSelected())
			return true;

		return false;
	}


	private boolean areAnyChildrenChecked(TreePath path)
	{
		if ((path == null) || (checkedPaths == null))
			return false;

		for (int i = 0; i < checkedPaths.size(); i++)
		{
			TreePath checkedPath = checkedPaths.elementAt(i);
			if (path.isDescendant(checkedPath))
				return true;
		}
		return false;
	}


	private boolean isPathPartiallyChecked(TreePath path)
	{

		if (path == null)
			return false;
		Object node = path.getLastPathComponent();

		// Find any children for the node
		for (int i = 0; i < checkboxTree.getModel().getChildCount(node); i++)
		{
			// Check to see if any children are checked or have children
			Object child = checkboxTree.getModel().getChild(node, i);
			TreePath childPath = path.pathByAddingChild(child);

			if (isNodeChecked(childPath) || isPathPartiallyChecked(childPath))
				return true;
		}
		return false;
	}


	private boolean isPathFullyChecked(TreePath path)
	{
		if (path == null)
			return false;
		Object node = path.getLastPathComponent();

		// Find any children for the node
		for (int i = 0; i < checkboxTree.getModel().getChildCount(node); i++)
		{
			// Check to see if any children are not checked or have unchecked children
			Object child = checkboxTree.getModel().getChild(node, i);
			TreePath childPath = path.pathByAddingChild(child);

			if (!isNodeChecked(childPath) || !isPathFullyChecked(childPath))
				return false;
		}
		return true;
	}


	void resetParents(TreePath path, boolean selected)
	{
		if (path == null)
			return;

		// Find any parents in the path and remove/add our selection
		TreePath parent = path.getParentPath();

		if (parent != null)
		{
			Object node = parent.getLastPathComponent();
			((SdtCheckboxNode) node).setSelected(selected);
			if (!selected)
			{
				checkedPaths.remove(parent);
			}
			else
			{
				if (!checkedPaths.contains(parent))
				{
					checkedPaths.add(parent);
				}
			}
		}
		resetParents(parent, selected);
	}


	private void toggleChildren(TreePath path, boolean selected)
	{
		if (path == null)
			return;
		Object node = path.getLastPathComponent();
		((SdtCheckboxNode) node).setSelected(selected);

		// Find any children for the node
		for (int i = 0; i < checkboxTree.getModel().getChildCount(node); i++)
		{
			// Check to see if any children have children
			Object child = checkboxTree.getModel().getChild(node, i);
			TreePath childPath = path.pathByAddingChild(child);
			Object childNode = childPath.getLastPathComponent();
			// Toggle the child
			((SdtCheckboxNode) childNode).setSelected(selected);
			if (selected)
			{
				// ljt put this restriction in our own function for safety!
				if (!checkedPaths.contains(childPath))
					checkedPaths.add(childPath);
			}
			else
				checkedPaths.remove(childPath);

			// Toggle any children of the child
			toggleChildren(childPath, selected);
		}
		return;
	}


	public void resetCheckbox(TreePath path, boolean selected)
	{
		if (path == null)
			return;
		Object node = path.getLastPathComponent();

		if (!selected)
		{
			checkedPaths.remove(path);
			toggleChildren(path, false);
			resetParents(path, false);
			((SdtCheckboxNode) node).setSelected(false);
		}
		else
		{
			if (!checkedPaths.contains(path))
				checkedPaths.add(path);
			toggleChildren(path, true);
			resetParents(path, true);
			((SdtCheckboxNode) node).setSelected(true);
		}
		checkboxTree.repaint();
	}


	public void repaint()
	{
		checkboxTree.repaint(); // ljt need me?
	}


	public void mouseEventToggleCheckbox(TreePath path, boolean newNode)
	{
		if (path == null)
			return;
		Object node = path.getLastPathComponent();

		// If it's a new node, we don't want to toggle but act on initial selection
		// we set new node to false when we are deleting nodes too
		if (checkedPaths.contains(path))// && !((SdtCheckboxNode)node).isSelected())
		{
			checkedPaths.remove(path);
			toggleChildren(path, false);
			resetParents(path, false);
			((SdtCheckboxNode) node).setSelected(false);
		}
		else
		// if (((SdtCheckboxNode)node).isSelected())
		{
			if (!checkedPaths.contains(path))
				checkedPaths.add(path);
			toggleChildren(path, true);
			resetParents(path, true);
			((SdtCheckboxNode) node).setSelected(true);
		}

		checkboxTree.repaint();
	}


	public void toggleCheckbox(TreePath path, boolean newNode)
	{
		if (path == null)
			return;
		Object node = path.getLastPathComponent();

		// If it's a new node, we don't want to toggle but act on initial selection
		// we set new node to false when we are deleting nodes too
		if ((checkedPaths.contains(path) && !((SdtCheckboxNode) node).isSelected()) ||
			(newNode && !((SdtCheckboxNode) node).isSelected()) ||
			// non leaves aren't in checked paths list but can be toggled
			(((SdtCheckboxNode) node).partiallyChecked) && !((SdtCheckboxNode) node).isSelected())
		{
			checkedPaths.remove(path);
			toggleChildren(path, false);
			resetParents(path, false);
			((SdtCheckboxNode) node).setSelected(false);
		}
		// else
		if (((SdtCheckboxNode) node).isSelected())
		{
			if (!checkedPaths.contains(path))
				checkedPaths.add(path);
			toggleChildren(path, true);
			resetParents(path, true);
			((SdtCheckboxNode) node).setSelected(true);
		}

		checkboxTree.repaint();
	}


	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus)
	{
		boolean nodeChecked = false;
		boolean anyChildrenChecked = false;
		Component returnValue = null;
		SdtCheckboxNode node = null;

		if ((value != null) && value instanceof SdtCheckboxNode)
			node = (SdtCheckboxNode) value;

		TreePath path = tree.getPathForRow(row);
		if (!(nodeChecked = isNodeChecked(path)))
		{
			anyChildrenChecked = areAnyChildrenChecked(path);
		}
		if (nodeChecked || anyChildrenChecked)
		{
			checkBox.setSelected(true);
			checkBox.setForeground(selectionForeground);
			checkBox.setBackground(selectionBackground);
		}
		else
		{
			checkBox.setSelected(false);
			checkBox.setForeground(textForeground);
			checkBox.setBackground(textBackground);
		}

		if (isPathPartiallyChecked(path))
		{
			checkBox.setSelected(true);
			nodeChecked = false;
			partiallyChecked = true;
			fullyChecked = false;
		}
		else
			partiallyChecked = false;

		if (isPathFullyChecked(path))
		{
			nodeChecked = true;
			partiallyChecked = false;
			fullyChecked = true;
		}

		node.setPartiallyChecked(partiallyChecked);
		// Get the default components from the default renderer and build our own icon
		Component c = defaultRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		checkBox.setForeground(c.getForeground());
		if (c instanceof JLabel)
		{
			JLabel label = (JLabel) c;
			label.setText(node.getText());
			// Augment the icon to include the checkbox
			label.setIcon(new CheckboxIcon(label.getIcon()));
		}
		returnValue = c;
		return returnValue;
	}

	// LJT this was borrowed - we need to make our own
	/** Combine a JCheckBox's checkbox with another icon. */
	private final class CheckboxIcon implements Icon
	{
		private final Icon icon;

		private final int w;

		private final int h;


		private CheckboxIcon(Icon icon)
		{
			if (icon == null)
			{
				icon = new Icon()
					{
						@Override
						public int getIconHeight()
						{
							return 0;
						}


						@Override
						public int getIconWidth()
						{
							return 0;
						}


						@Override
						public void paintIcon(Component c, Graphics g, int x, int y)
						{
						}
					};
			}
			this.icon = icon;
			this.w = icon.getIconWidth();
			this.h = icon.getIconHeight();
		}


		@Override
		public int getIconWidth()
		{
			return checkBox.getPreferredSize().width + w;
		}


		@Override
		public int getIconHeight()
		{
			return Math.max(checkBox.getPreferredSize().height, h);
		}


		@Override
		public void paintIcon(Component c, Graphics g, int x, int y)
		{
			if (c.getComponentOrientation().isLeftToRight())
			{
				int xoffset = checkBox.getPreferredSize().width;
				int yoffset = (getIconHeight() - icon.getIconHeight()) / 2;
				icon.paintIcon(c, g, x + xoffset, y + yoffset);
				paintCheckBox(g, x, y);
			}
			else
			{
				int yoffset = (getIconHeight() - icon.getIconHeight()) / 2;
				icon.paintIcon(c, g, x, y + yoffset);
				paintCheckBox(g, x + icon.getIconWidth(), y);
			}
		}


		private void paintCheckBox(Graphics g, int x, int y)
		{
			int yoffset;
			boolean db = checkBox.isDoubleBuffered();
			checkBox.setDoubleBuffered(false);
			try
			{
				yoffset = (getIconHeight() - checkBox.getPreferredSize().height) / 2;
				g = g.create(x, y + yoffset, getIconWidth(), getIconHeight());
				if (!partiallyChecked)
				{
					checkBox.paint(g);
				}
				else if (partiallyChecked)
				{
					checkBox.setSelected(false);
					checkBox.paint(g);
					final int WIDTH = 2;
					g.setColor(UIManager.getColor("CheckBox.foreground"));
					Graphics2D g2d = (Graphics2D) g;
					g2d.setStroke(new BasicStroke(WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
					g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					int w = checkBox.getWidth();
					int h = checkBox.getHeight();
					g.drawLine(w / 4 + 2, h / 2 - WIDTH / 2 + 1, w / 4 + w / 2 - 3, h / 2 - WIDTH / 2 + 1);
				}
				g.dispose();
			}
			finally
			{
				checkBox.setDoubleBuffered(db);
			}
		}
	}

}
