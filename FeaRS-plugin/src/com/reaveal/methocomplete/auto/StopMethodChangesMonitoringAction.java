package com.reaveal.methocomplete.auto;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class StopMethodChangesMonitoringAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        if (MethodChangesTracker.GetInstance().IsActive() == false)
            return;
        MethodChangesTracker.GetInstance().Stop();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        if (MethodChangesTracker.GetInstance().IsActive() == false) {
            e.getPresentation().setEnabled(false);
            return;
        }
        e.getPresentation().setEnabled(true);
    }
}
