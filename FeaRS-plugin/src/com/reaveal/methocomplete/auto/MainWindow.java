package com.reaveal.methocomplete.auto;


import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.reaveal.methocomplete.Constants;
import com.reaveal.methocomplete.OddsAndEnds;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.Hashtable;
import java.util.List;

public class MainWindow extends JPanel {

    Font myFont2 = new Font("Arial Rounded MT", Font.PLAIN, 17);
    MethodSuggestionComponent[] boxes;
    JLabel info;
    JPanel cards = null;
    JSlider ts;


    MainWindow(Project project) {
        super(true);
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        this.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel settingPanel = new JPanel();
        settingPanel.setAlignmentX(LEFT_ALIGNMENT); // Important
        settingPanel.setLayout(new BoxLayout(settingPanel, BoxLayout.X_AXIS));
        settingPanel.add(Box.createHorizontalGlue());
        JSlider slider = CreateSlider();
        settingPanel.add(slider);
        settingPanel.add(Box.createHorizontalGlue());
        settingPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createMatteBorder(1, 1, 1, 1, Color.LIGHT_GRAY),
                "Sensitivity", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.CENTER, myFont2, Color.GRAY));
        this.add(settingPanel);

        this.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel top = new JPanel();
        top.setAlignmentX(LEFT_ALIGNMENT);  // Important
        top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
        top.add(Box.createRigidArea(new Dimension(5, 0)));
        info = new JLabel("No Recommendation");
        info.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        info.setFont(myFont2);
        info.setOpaque(true);
        info.setForeground(new Color(70, 70, 70));
        //info.setAlignmentX(LEFT_ALIGNMENT);
        top.add(info);
        top.add(Box.createHorizontalGlue());
//        JButton refresh_btn = new JButton("Refresh");
//        top.add(refresh_btn);
//        top.add(Box.createRigidArea(new Dimension(5,0)));
        //top.setBackground(Color.RED);
        this.add(top);

        this.add(Box.createRigidArea(new Dimension(0, 10)));

        cards = new JPanel();
//        cards.setBackground(Color.RED);
//        cards.setPreferredSize(new Dimension(200, 300));
//        cards.setMinimumSize(new Dimension(200, 0));
//        cards.setMaximumSize(new Dimension(200, 3000));
//        cards.setSize(new Dimension(200, 3000));
        this.add(cards);
        JBScrollPane listScroller = new JBScrollPane(cards);
        listScroller.setAlignmentX(LEFT_ALIGNMENT); // Important
        listScroller.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        this.add(listScroller);
    }

    public void UpdateUI(List<ServerResult> results) {
        if(results==null || results.size()==0)
            info.setText("No Recommendation");
        else if(results.size()==1)
            info.setText("1 Recommendation Group Found");
        else
            info.setText(results.size()+" Recommendation Groups Found");

        OddsAndEnds.showInfoBalloon(Constants.PLUGIN_NAME, results.size() + " recommendation" + (results.size()>1?"s":"") + " received.");
        cards.removeAll();
        PopulateCards(results);
        cards.revalidate();
        cards.repaint();
    }

    void PopulateCards(List<ServerResult> results) {

        GroupLayout glayout = new GroupLayout(cards);
        //cards.setBackground(Color.WHITE);
        cards.setLayout(glayout);

        GroupLayout.Group parallelGroup = glayout.createParallelGroup();
        GroupLayout.Group sequentialGroup = glayout.createSequentialGroup();

        for (int i = 0; i < results.size(); i++) {
            JPanel l = new MethodSuggestionComponent(results.get(i));
            l.setPreferredSize(new Dimension(200,300));
            parallelGroup.addComponent(l);
            sequentialGroup.addComponent(l);
            sequentialGroup.addGap(10);
        }


        glayout.setHorizontalGroup(glayout.createSequentialGroup()
                .addGap(10)
                .addGroup(glayout.createParallelGroup().addGap(10).addGroup(parallelGroup).addGap(30))
                .addGap(10)
        );

        glayout.setVerticalGroup(glayout.createParallelGroup()
                .addGap(10) //is this needed?
                .addGroup(glayout.createSequentialGroup().addGap(10).addGroup(sequentialGroup).addGap(30))
                .addGap(10) //is this needed?
        );
    }

    JSlider CreateSlider() {
        int initialValue = MethodChangesTracker.GetInstance().GetCurrentThreshold();
        ts = new JSlider(JSlider.HORIZONTAL, 1, 3, initialValue);
        ts.setSnapToTicks(true);
        ts.setMajorTickSpacing(1);
        ts.setPaintTicks(true);
        Hashtable<Integer, JLabel> labels = new Hashtable<Integer, JLabel>();
        labels.put(1, new JLabel("Low"));
        labels.put(2, new JLabel("Medium"));
        labels.put(3, new JLabel("High"));
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

//        ts.setPreferredSize(new Dimension(400, 30));
//        ts.setMinimumSize(new Dimension(300, 20));
//        ts.setMaximumSize(new Dimension(500, 40));

        return ts;
    }
}
