package com.reaveal.methocomplete;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassImpl;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.FileBasedIndex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PsiMethodUtil {

    /**
     * Return all methods defined by given classes (only owned by those classes, not their parents)
     */
    static public List<PsiMethod> GetAllOwnPsiMethods(List<PsiClass> allPsiClasses) {
        List<PsiMethod> allOwnMethods = new ArrayList<>();
        if (allPsiClasses.size() > 0)
            for (PsiClass c : allPsiClasses)
                allOwnMethods.addAll(PsiMethodUtil.GetAllOwnPsiMethods(c));
        return allOwnMethods;
    }

    static public List<PsiMethod> GetAllOwnPsiMethods(PsiClass psiClass) {
        return ((PsiClassImpl) psiClass).getOwnMethods();
    }

    static public List<PsiClass> GetAllPsiClasses(Project project) {
        List<PsiClass> allPsiClasses = new ArrayList<>();
        Collection<VirtualFile> allFiles = FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.NAME,
                JavaFileType.INSTANCE, GlobalSearchScope.projectScope(project));
        for (VirtualFile f : allFiles) {
            PsiFile psiFile = PsiManager.getInstance(project).findFile(f);
            PsiClass classInThisFile = PsiTreeUtil.findChildOfType(psiFile, PsiClass.class);
            if (classInThisFile == null)
                continue;
            allPsiClasses.add(classInThisFile);
        }
        return allPsiClasses;
    }

    static public PsiClass GetPsiClassFromContext(AnActionEvent e) {
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (psiFile == null || editor == null)
            return null;
        int offset = editor.getCaretModel().getOffset();
        PsiElement elementAt = psiFile.findElementAt(offset);
        PsiClass psiClass = PsiTreeUtil.getParentOfType(elementAt, PsiClass.class);
        return psiClass;
    }
}
