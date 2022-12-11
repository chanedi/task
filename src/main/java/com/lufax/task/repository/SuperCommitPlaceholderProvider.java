package com.lufax.task.repository;

import com.intellij.tasks.LocalTask;
import com.intellij.tasks.Task;
import com.intellij.tasks.TaskRepository;
import com.intellij.tasks.impl.DefaultCommitPlaceholderProvider;
import com.lufax.task.toolwindow.actions.GotoLocalTaskAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.lufax.task.repository.SelectorBasedSuperResponseHandler.*;

public class SuperCommitPlaceholderProvider extends DefaultCommitPlaceholderProvider {

    @Override
    public String @NotNull [] getPlaceholders(TaskRepository repository) {
        return new String[] { "id", "number", "summary", "project", "taskType",
                CUSTOM_FIELD_1, CUSTOM_FIELD_2, CUSTOM_FIELD_3, CUSTOM_FIELD_4, CUSTOM_FIELD_5};
    }

    @Nullable
    @Override
    public String getPlaceholderValue(LocalTask task, String placeholder) {
        try {
            return super.getPlaceholderValue(task, placeholder);
        } catch (IllegalArgumentException e) {
        }

        Task selectedTask = GotoLocalTaskAction.getSelectedTask();
        if (selectedTask != null && selectedTask instanceof SuperGenericTask) {
            if (CUSTOM_FIELD_1.equals(placeholder)) {
                return ((SuperGenericTask) selectedTask).getCustomField1();
            }
            if (CUSTOM_FIELD_2.equals(placeholder)) {
                return ((SuperGenericTask) selectedTask).getCustomField2();
            }
            if (CUSTOM_FIELD_3.equals(placeholder)) {
                return ((SuperGenericTask) selectedTask).getCustomField3();
            }
            if (CUSTOM_FIELD_4.equals(placeholder)) {
                return ((SuperGenericTask) selectedTask).getCustomField4();
            }
            if (CUSTOM_FIELD_5.equals(placeholder)) {
                return ((SuperGenericTask) selectedTask).getCustomField5();
            }
        }

        throw new IllegalArgumentException(placeholder);
    }
}
