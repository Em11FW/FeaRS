package com.reaveal.methocomplete.auto;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.util.IconLoader;
import com.reaveal.methocomplete.OddsAndEnds;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 *  Note about "MethodSuggestionComponent" dimension and size:
 *  - We now have a list of "MethodSuggestionComponent" attached to a JPanel (cards) via GroupLayout
 *  - So the size varies based on number of items to fill verticlly. If we have one card it fill this whole height
 *      and setting Preferred height has no effect.
 *  Also note, the size of Java editor inside each "MethodSuggestionComponent" works as *filling* the parrent (card),
 *      unless a Max size is set.
 */
public class MethodSuggestionComponent extends JPanel {

    Font titleFont = new Font("Helvetica", Font.PLAIN, 15);
    Font codeFont = new Font("menlo", Font.PLAIN, 12);

    Color textColor =  new Color(42, 52, 64);
    Color backgroundColor = new Color(218, 221, 222);

    JLabel cur_code_index;
    CustomEditorTextField m_editor;

    List<String> m_suggestions_code, m_suggestions_link;
    int active_code_index;

    MethodSuggestionComponent(ServerResult res) {
        super(true);

        assert res.based_on.size() > 0;
        assert res.codeSnippets.size() > 0;


        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        this.setBackground(backgroundColor);
        this.setBorder(BorderFactory.createRaisedBevelBorder());
        //this.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.GRAY));
//        this.setBorder(BorderFactory.createCompoundBorder(
//                BorderFactory.createEmptyBorder(0, 20, 0, 20),
//                BorderFactory.createMatteBorder(2, 2, 2, 2, Color.BLACK)
//        ));


        JLabel based_txt = new JLabel("Based on");
        based_txt.setFont(titleFont);
        based_txt.setForeground(textColor);

        String label_text_html = "";
        for (int i = 0; i < res.based_on.size(); i++) {
            label_text_html += res.based_on.get(i);
            if (i != res.based_on.size() - 1)
                label_text_html += "<br>";
        }
        JLabel inputMethod1 = new JLabel("<html>" + label_text_html + "</html>", SwingConstants.LEFT);
        inputMethod1.setFont(codeFont);
        inputMethod1.setOpaque(true);
        inputMethod1.setBackground(Color.LIGHT_GRAY);
        inputMethod1.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(168, 173, 179)),
                BorderFactory.createEmptyBorder(0,10,0,0) //top,left,bottom,right
        ));


        JLabel result_txt = new JLabel("<html>We found <b>" + res.codeSnippets.size() + "</b> method recommendation"+(res.codeSnippets.size()>1?"s":"")+":</html>");
        result_txt.setFont(titleFont);
        result_txt.setForeground(textColor);
        //result.setOpaque(true);
        //result.setBackground(Color.WHITE);

        m_suggestions_code = new ArrayList<>(res.codeSnippets);
        m_suggestions_link = new ArrayList<>(res.codeSnippetsLink);

        cur_code_index = new JLabel("?");
        JLabel total_code_index = new JLabel("/ " + m_suggestions_code.size());


        JLabel next_btn = new JLabel(IconLoader.getIcon("/icons/next.svg"));
        next_btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int t = (active_code_index + 1) % (m_suggestions_code.size());
                ShowCode(t);
            }
        });

        JLabel prev_btn = new JLabel(IconLoader.getIcon("/icons/prev.svg"));
        prev_btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int t = active_code_index== 0 ? m_suggestions_code.size() - 1 : active_code_index - 1;
                ShowCode(t);
            }
        });


        m_editor = new CustomEditorTextField("?????", MethodChangesTracker.GetInstance().GetProject(),
                FileTypeRegistry.getInstance().getFileTypeByExtension("java"));
        // How set editor size? Read MethodSuggestionComponent's javadoc
//        m_editor.setSize(50,50);
//        m_editor.setPreferredSize(new Dimension(300,100));
//        m_editor.setMaximumSize(new Dimension(500,100));
        //m_editor.setMaximumSize(new Dimension(30,30));
        //ed.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        //this.add(ed, BorderLayout.CENTER);


        JPanel actionButtonsPanel = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(super.getPreferredSize().width, 30);
            }
        };
        actionButtonsPanel.setBackground(backgroundColor);
        actionButtonsPanel.setLayout(new BoxLayout(actionButtonsPanel, BoxLayout.X_AXIS));
        actionButtonsPanel.add(Box.createHorizontalGlue());
        JLabel like_btn = new JLabel(IconLoader.getIcon("/icons/like.svg"));
        actionButtonsPanel.add(like_btn);
        actionButtonsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        JLabel dislike_btn = new JLabel(IconLoader.getIcon("/icons/dislike.svg"));
        actionButtonsPanel.add(dislike_btn);
        actionButtonsPanel.add(Box.createHorizontalGlue());
        JLabel link_btn = new JLabel(IconLoader.getIcon("/icons/link.svg"));
        actionButtonsPanel.add(link_btn);
        actionButtonsPanel.add(Box.createHorizontalGlue());
        JLabel copy_btn = new JLabel(IconLoader.getIcon("/icons/copy.svg"));
        actionButtonsPanel.add(copy_btn);
        actionButtonsPanel.add(Box.createHorizontalGlue());
        JLabel discard_btn = new JLabel(IconLoader.getIcon("/icons/discard.svg"));
        actionButtonsPanel.add(discard_btn);
        actionButtonsPanel.add(Box.createHorizontalGlue());


        like_btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                OddsAndEnds.showInfoBalloon("Liked", "You're welcome.");
            }
        });

        dislike_btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                OddsAndEnds.showInfoBalloon("Disliked", "Failure is instructive. Thanks for the feedback.");
            }
        });

        link_btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                BrowserUtil.browse(m_suggestions_link.get(active_code_index));
//                StringSelection stringSelection = new StringSelection(m_suggestions_link.get(active_code_index));
//                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
//                clipboard.setContents(stringSelection, null);
//                OddsAndEnds.showInfoBalloon("Link to source of suggestion copied to you clipboard.", "");
            }
        });

        copy_btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                String codeAndLink = String.format("//Code snippet source: %s\n%s", m_suggestions_link.get(active_code_index), m_suggestions_code.get(active_code_index));
                StringSelection stringSelection = new StringSelection(codeAndLink);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(stringSelection, null);
                OddsAndEnds.showInfoBalloon("Code Snippet Copied", "");
            }
        });


        discard_btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                JLabel btnPanel = (JLabel) e.getSource();
                Container methodSuggestionCard = btnPanel.getParent().getParent();
                Container cards = methodSuggestionCard.getParent();
                cards.remove(methodSuggestionCard);
                cards.revalidate();
                cards.repaint();
                OddsAndEnds.showInfoBalloon("Removed", "");
            }
        });


        GroupLayout.Group mainContent_h = layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(based_txt)
                .addComponent(inputMethod1)
                .addGap(10)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(result_txt)
                        //.addGap(50)
                        .addComponent(prev_btn)
                        .addComponent(cur_code_index)
                        .addComponent(total_code_index)
                        .addComponent(next_btn)
                        .addGap(10)
                )
                .addComponent(m_editor)
                .addComponent(actionButtonsPanel);


        GroupLayout.Group mainContent_v = layout.createSequentialGroup()
                .addComponent(based_txt)
                .addComponent(inputMethod1)
                .addGap(10)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(result_txt, GroupLayout.Alignment.LEADING)
                        .addComponent(prev_btn, GroupLayout.Alignment.TRAILING)
                        .addComponent(cur_code_index, GroupLayout.Alignment.TRAILING)
                        .addComponent(total_code_index, GroupLayout.Alignment.TRAILING)
                        .addComponent(next_btn, GroupLayout.Alignment.TRAILING)
                )
                .addComponent(m_editor)
                .addComponent(actionButtonsPanel);


        layout.setHorizontalGroup(layout.createSequentialGroup().addGroup(mainContent_h));
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addGroup(mainContent_v));

        active_code_index = 0;
        ShowCode(active_code_index);

    }

    void ShowCode(int i) {
        active_code_index = i;
        cur_code_index.setText(String.valueOf(active_code_index + 1));
        m_editor.setText(m_suggestions_code.get(active_code_index));

    }


    @Override
    public Dimension getMaximumSize() {
        // Limiting one axis (height)
        return new Dimension(super.getMaximumSize().width, 400); //
    }
}
