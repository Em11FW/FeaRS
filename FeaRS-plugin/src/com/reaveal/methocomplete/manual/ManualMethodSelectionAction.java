package com.reaveal.methocomplete.manual;

import com.intellij.codeInsight.generation.PsiMethodMember;
import com.intellij.ide.util.MemberChooser;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.reaveal.methocomplete.PsiMethodUtil;
import com.reaveal.methocomplete.auto.MethodChangesTracker;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ManualMethodSelectionAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {

        /*
            This plugin has two entry points (two entry actions):
                1. ManualMethodSelectionAction
                2. StartMethodChangesMonitoringAction
            Only these Actions have access to e.getProject().
            So both entries should make sure the ToolWindow is initialized.
         */

        MethodChangesTracker.GetInstance().InitializeToolWindow(e.getProject());

        List<PsiClass> allPsiClasses = PsiMethodUtil.GetAllPsiClasses(e.getProject());
        List<PsiMethod> allOwnMethods = PsiMethodUtil.GetAllOwnPsiMethods(allPsiClasses);

        PsiMethodMember classMembers[] = new PsiMethodMember[allOwnMethods.size()];
        for (int i = 0; i < allOwnMethods.size(); i++)
            classMembers[i] = new PsiMethodMember(allOwnMethods.get(i));

        MemberChooser memberChooserDlg = new ManualMethodSelectionUI(classMembers, e.getProject());
        memberChooserDlg.show();
        if (memberChooserDlg.isOK()) {
            List<PsiMethodMember> list = memberChooserDlg.getSelectedElements();
            if (list.size() > 0) {
                Set<PsiMethod> methods = new HashSet<>();
                for(PsiMethodMember p: list)
                    methods.add(p.getElement());

                MethodChangesTracker.GetInstance().ProcessNewlyAddedMethods(methods);

//                // TODO: Send to Fencai
//                String selectedMethods_debug = "";
//                for (PsiMethodMember m : list)
//                    selectedMethods_debug += m.getElement().getContainingClass().getName() + "::" + m.getElement().getName() + "\n";
//
//                Messages.showInfoMessage(e.getProject(), selectedMethods_debug, "Result");
            }
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Editor editor = e.getData(LangDataKeys.EDITOR);

        if (editor == null) {
            e.getPresentation().setEnabled(false);
            return;
        }
        e.getPresentation().setEnabled(true);
    }
}

