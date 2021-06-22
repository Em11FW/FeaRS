package com.reaveal.methocomplete.auto;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class StartMethodChangesMonitoringAction extends AnAction {
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

        if (MethodChangesTracker.GetInstance().IsActive() == true)
            return;
        MethodChangesTracker.GetInstance().Start();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        if (MethodChangesTracker.GetInstance().IsActive() == true) {
            e.getPresentation().setEnabled(false);
            return;
        }
        e.getPresentation().setEnabled(true);
    }
}
