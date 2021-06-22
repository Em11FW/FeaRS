package com.reaveal.methocomplete.auto;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaCodeFragmentFactory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.ui.EditorTextField;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

class CustomEditorTextField extends EditorTextField
{
    // >>>>>>>> Scroll for EditorTextField
    // https://intellij-support.jetbrains.com/hc/en-us/community/posts/206759275-EditorTextField-and-surrounding-JBScrollPane

    final Rectangle INVISBLE_BOUND_RECT = new Rectangle(-100, -100, 0,0);
    Rectangle lastBoundBeforeInvisible;
    boolean isVisible=true;

    public CustomEditorTextField(Document document, Project project, FileType fileType, boolean isViewer, boolean oneLineMode)
    {
        super(document,project,fileType,isViewer,oneLineMode);
    }

    public CustomEditorTextField(@NotNull String text, Project project, FileType fileType) {
        this(EditorFactory.getInstance().createDocument(text), project, fileType, true, false);
        setMinimumSize(new Dimension(100,150));
    }

    @Override
    protected EditorEx createEditor()
    {
        EditorEx editor = super.createEditor();
        editor.setVerticalScrollbarVisible(true);
        editor.setHorizontalScrollbarVisible(true);
        addLineNumberToEditor(editor);

        //Because this editor initialized after all of my initialization (and after an unknown delay)
        // So we can't call loadMainEditorWindowContent() before and we have to do it here
        //loadMainEditorWindowContent();

        return editor;
    }

    private String getSelectedText()
    {
        String s = getEditor().getSelectionModel().getSelectedText();
        if(s==null)
            s = "";
        return s;
    }

    private void addLineNumberToEditor(EditorEx editor)
    {
        EditorSettings settings = editor.getSettings();
        settings.setLineNumbersShown(true);
        editor.reinitSettings();
    }

    public void _setVisible(boolean newStatus)
    {
        super.setVisible(newStatus);
    }
    public void setVisible(boolean newStatus)
    {
        // if we use normal behaviour of setVisible(), while visibility is False the KeyBinding doesn't work strangely.
        if(isVisible == newStatus) return;

        if(newStatus==false)
        {
            isVisible = false;
            lastBoundBeforeInvisible = this.getBounds();
            setBounds(INVISBLE_BOUND_RECT);
        }
        else
        {
            isVisible = true;
            setBounds(lastBoundBeforeInvisible);
        }
    }
}
