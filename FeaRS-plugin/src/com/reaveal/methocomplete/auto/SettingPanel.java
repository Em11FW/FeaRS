package com.reaveal.methocomplete.auto;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

public class SettingPanel extends JPanel {
    Font myFont = new Font("Arial Rounded MT", Font.BOLD, 14);

    JButton startStop_btn;
    JSlider ts;

    SettingPanel(Project project) {
        super(true);

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));


        JPanel settingPanel = new JPanel();
        settingPanel.setLayout(new BoxLayout(settingPanel, BoxLayout.X_AXIS));
        settingPanel.add(Box.createHorizontalGlue());
        JSlider slider = CreateSlider();
        settingPanel.add(slider);
        settingPanel.add(Box.createHorizontalGlue());
        settingPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createMatteBorder(1, 1, 1, 1, Color.LIGHT_GRAY),
                "Sensitivity", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.CENTER, myFont, Color.GRAY));
        this.add(settingPanel);



        startStop_btn = new JButton("Initializing....");
        UpdateStartStopButtonText();
        startStop_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                if (MethodChangesTracker.GetInstance().IsActive())
                    MethodChangesTracker.GetInstance().Stop();
                else
                    MethodChangesTracker.GetInstance().Start();
            }
        });
        startStop_btn.setFont(myFont);
        startStop_btn.setAlignmentX(CENTER_ALIGNMENT);
        JPanel panel = new JPanel() {
            @Override
            public Dimension getMaximumSize() {
                // This way we can setMaximumSize in one axis. Cool!
                return new Dimension(super.getMaximumSize().width, 50);
            }
        };
        panel.setLayout(new BorderLayout(0, 0));
        panel.add(startStop_btn);
        this.add(panel);
    }

    void UpdateStartStopButtonText()
    {
        if(MethodChangesTracker.GetInstance().IsActive())
        {
            startStop_btn.setText("Stop Tracking");
            startStop_btn.setIcon(IconLoader.getIcon("/icons/stop.svg"));
        }
        else
        {
            startStop_btn.setText("Start Tracking");
            startStop_btn.setIcon(IconLoader.getIcon("/icons/start.svg"));
        }

    }


    JSlider CreateSlider() {
        int initialValue = MethodChangesTracker.GetInstance().GetCurrentThreshold();
        ts = new JSlider(JSlider.HORIZONTAL, 1, 4, initialValue);
        ts.setSnapToTicks(true);
        ts.setMajorTickSpacing(1);
        ts.setPaintTicks(true);
        Hashtable<Integer, JLabel> labels = new Hashtable<Integer, JLabel>();
        labels.put(1, new JLabel("Low"));
        labels.put(2, new JLabel("Medium"));
        labels.put(3, new JLabel("High"));
        labels.put(4, new JLabel("Intense"));
        ts.setLabelTable(labels);
        ts.setPaintLabels(true);
        ts.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                if (!source.getValueIsAdjusting()) {
                    int value = source.getValue();
                    MethodChangesTracker.GetInstance().SetCurrentThreshold(value);
                    System.out.println(value);
                }
            }
        });

        ts.setPreferredSize(new Dimension(400, 30));
        ts.setMinimumSize(new Dimension(300, 20));
        ts.setMaximumSize(new Dimension(500, 40));

        return ts;
    }
}
