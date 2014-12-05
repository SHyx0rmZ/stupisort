import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.undo.DocumentReference;
import com.intellij.openapi.command.undo.UndoManager;
import com.intellij.openapi.command.undo.UndoableAction;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.TextRange;
import com.sun.javafx.beans.annotations.NonNull;
import sun.misc.Compare;
import sun.misc.Sort;

import java.util.ArrayList;
import java.util.HashMap;

public class IndentationSortCommand implements UndoableAction {
    public void sort(Project project, Editor editor) {
        Document document = editor.getDocument();

        CaretModel caretModel = editor.getCaretModel();

        if (caretModel.getCaretCount() != 1) {
            Messages.showMessageDialog(project, "Can only sort by indentation when using a single caret.", "Too many carets", Messages.getErrorIcon());

            return;
        }

        ArrayList<String> lines = parseLines(document, caretModel);
        CharSequence content = getSortedContent(lines);

        updateDocumentContent(document, content);
        notifyUndoManager(project);
    }

    private CharSequence getSortedContent(ArrayList<String> lines) {
        String[] lineArray = lines.toArray(new String[lines.size()]);

        Sort.quicksort(lineArray, new Compare() {
            @Override
            public int doCompare(Object o1, Object o2) {
                String s1 = (String)o1;
                String s2 = (String)o2;

                return s1.compareTo(s2);
            }
        });
        
        StringBuilder content = new StringBuilder();
        
        for (String line : lineArray) {
            content.append(line);
        }

        return content;
    }

    private void notifyUndoManager(Project project) {
        UndoManager undoManager = UndoManager.getInstance(project);

        undoManager.undoableActionPerformed(this);
    }

    private void updateDocumentContent(Document document, CharSequence content) {
        Application application = ApplicationManager.getApplication();
        AccessToken lock = application.acquireWriteActionLock(document.getClass());

        document.setText(content);
        lock.finish();
    }

    private ArrayList<String> parseLines(Document document, CaretModel caretModel) {
        ArrayList<String> lines = new ArrayList<String>(document.getLineCount());
        HashMap<Integer, Integer> lineIndentations = new HashMap<Integer, Integer>();
        Integer caretLine = caretModel.getPrimaryCaret().getLogicalPosition().line;
        HashMap<Integer, ArrayList<String>> ll = new HashMap<Integer, ArrayList<String>>();

        for (int i = 0; i < document.getLineCount(); ++i) {
            int start = document.getLineStartOffset(i);
            int end = document.getLineEndOffset(i) + document.getLineSeparatorLength(i);

            TextRange lineRange = new TextRange(start, end);

            String line = document.getText(lineRange);
            Integer indentation = 0;

            for (int j = 0; j < line.length(); ++j) {
                if (line.charAt(j) == ' ') {
                    ++indentation;
                } else {
                    break;
                }
            }

            lines.add(line);
            lineIndentations.put(i, indentation);
        }

        StringBuilder x = new StringBuilder();

        for (Integer i : lineIndentations.keySet()) {
            if (lineIndentations.get(i) == lineIndentations.get(caretLine)) {
                x.append(i);
                x.append(", ");
            }
        }

        Messages.showMessageDialog(x.toString(), "inde", Messages.getInformationIcon());

        for (int i = 0; i < lineIndentations.size(); ++i) {
            if (lineIndentations.get(i) == lineIndentations.get(caretLine)) {
                ArrayList<String> col = new ArrayList<String>();

                col.add(lines.get(i));

                for (int j = i + 1; j < lineIndentations.size() && lineIndentations.get(j) > lineIndentations.get(caretLine); ++j) {
                    col.add(lines.get(j));
                }

                ll.put(i, col);
            }
        }

        return lines;
    }

    public void undo() {
    }

    public void redo() {
    }

    public boolean isGlobal() {
        return false;
    }

    @NonNull
    public DocumentReference[] getAffectedDocuments() {
        return DocumentReference.EMPTY_ARRAY;
    }
}
