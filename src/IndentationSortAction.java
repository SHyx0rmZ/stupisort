import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;

public class IndentationSortAction extends AnAction {
    public void actionPerformed(AnActionEvent event) {
        final Project project = event.getData(DataKeys.PROJECT);
        final Editor editor = event.getData(DataKeys.EDITOR);

        if (project != null && editor != null) {
            final IndentationSortCommand sortCommand = new IndentationSortCommand();
            Runnable sortRunner = new Runnable() {
                @Override
                public void run() {
                    sortCommand.sort(project, editor);
                }
            };

            CommandProcessor commandProcessor = CommandProcessor.getInstance();

            commandProcessor.executeCommand(project, sortRunner, "Sort by indentation", null);
        }
    }
}
