package org.esa.beam.visat.toolviews.imageinfo;

import com.jidesoft.swing.TitledSeparator;
import org.esa.beam.framework.ui.UIUtils;
import org.esa.beam.framework.ui.tool.ToolButtonFactory;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

class MoreOptionsPane {
    private static ImageIcon[] icons;
    private static ImageIcon[] rolloverIcons;

    private final ColorManipulationForm colorManipulationForm;
    private final JPanel contentPanel;
    private final JLabel[] headerLabels;
    private final TitledSeparator headerSeparator;
    private final AbstractButton headerButton;

    private JComponent component;
    private boolean collapsed;

    MoreOptionsPane(ColorManipulationForm colorManipulationForm) {
        this.colorManipulationForm = colorManipulationForm;

        if (icons == null) {
            icons = new ImageIcon[]{
                    UIUtils.loadImageIcon("icons/PanelCollapse12.png"),
                    UIUtils.loadImageIcon("icons/PanelExpand12.png"),
            };
            rolloverIcons = new ImageIcon[]{
                    ToolButtonFactory.createRolloverIcon(icons[0]),
                    ToolButtonFactory.createRolloverIcon(icons[1]),
            };
        }

        // printDefaults(UIManager.getLookAndFeelDefaults(), "UIManager.getLookAndFeelDefaults()");

        headerLabels = new JLabel[]{
                new JLabel("More Options"),
                new JLabel("Less Options"),
        };
        Color headerLabelColor = UIManager.getLookAndFeelDefaults().getColor("TitledBorder.titleColor");
        if (headerLabelColor != null) {
            headerLabels[0].setForeground(headerLabelColor);
            headerLabels[1].setForeground(headerLabelColor);
        }

        component = new JLabel(); // dummy
        collapsed = true;

        headerButton = ToolButtonFactory.createButton(icons[0], false);
        headerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setCollapsed(!isCollapsed());
            }
        });

        final JPanel titleBar = new JPanel(new BorderLayout(2, 2));
        titleBar.add(headerButton, BorderLayout.WEST);
        headerSeparator = new TitledSeparator(headerLabels[0], TitledSeparator.TYPE_PARTIAL_ETCHED, SwingConstants.LEFT);
        titleBar.add(headerSeparator, BorderLayout.CENTER);

        contentPanel = new JPanel(new BorderLayout(2, 2));
        contentPanel.add(titleBar, BorderLayout.NORTH);
    }

    private void printDefaults(UIDefaults uiDefaults, String name) {
        System.out.printf(">>>> %s >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n", name);
        for (Map.Entry<Object, Object> objectObjectEntry : uiDefaults.entrySet()) {
            System.out.printf("  %s = %s\n", objectObjectEntry.getKey(), objectObjectEntry.getValue());
        }
        System.out.printf("<<<< %s <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<\n", name);
    }

    public JPanel getContentPanel() {
        return contentPanel;
    }

    public JComponent getComponent() {
        return component;
    }

    public void setComponent(JComponent component) {
        contentPanel.remove(this.component);
        this.component = component;
        updateState();
    }

    public boolean isCollapsed() {
        return collapsed;
    }

    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
        updateState();
    }

    private void updateState() {
        if (collapsed) {
            contentPanel.remove(this.component);
        } else {
            contentPanel.add(this.component, BorderLayout.CENTER);
        }
        final int i = collapsed ? 0 : 1;
        headerSeparator.setLabelComponent(headerLabels[i]);
        headerButton.setIcon(icons[i]);
        headerButton.setRolloverIcon(rolloverIcons[i]);
        colorManipulationForm.revalidateToolViewPaneControl();
    }
}
