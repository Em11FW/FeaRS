package com.reaveal.methocomplete.manual;

import com.intellij.codeInsight.generation.PsiMethodMember;
import com.intellij.ide.util.MemberChooser;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;


public class ManualMethodSelectionUI extends MemberChooser<PsiMethodMember> {
    public ManualMethodSelectionUI(PsiMethodMember[] elements, @NotNull Project project) {
        super(elements, false, true, project);
        setCopyJavadocVisible(false);
        setTitle("Select Methods");
    }
}
