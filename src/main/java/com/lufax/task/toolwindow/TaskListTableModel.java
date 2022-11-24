package com.lufax.task.toolwindow;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.tasks.Task;
import com.intellij.tasks.TaskRepository;
import com.intellij.tasks.generic.GenericTask;
import com.intellij.ui.AnActionButton;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import com.lufax.task.repository.SuperGenericTask;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class TaskListTableModel extends ListTableModel<Task> {

    private static final Logger LOG = Logger.getInstance(TaskListTableModel.class);

    private TaskRepository taskRepository;

    public TaskListTableModel() {
        super(new ColumnInfo<Task, String>("Id") {
            @Override
            public @Nullable String valueOf(Task task) {
                return task.getId();
            }
        }, new ColumnInfo<Task, String>("Summary") {
            @Override
            public @Nullable String valueOf(Task task) {
                return task.getSummary();
            }
        }, new ColumnInfo<Task, String>("ReleaseDate") {
            @Override
            public @Nullable String valueOf(Task task) {
                if (task instanceof SuperGenericTask) {
                    return ((SuperGenericTask) task).getReleaseDate() == null ? "" : ((SuperGenericTask) task).getReleaseDate();
                }
                return "";
            }
        }, new ColumnInfo<Task, String>("Status") {
            @Override
            public @Nullable String valueOf(Task task) {
                if (task instanceof SuperGenericTask) {
                    return ((SuperGenericTask) task).getStatus() == null ? "" : ((SuperGenericTask) task).getStatus();
                }
                return task.getState() == null ? "" : task.getState().name();
            }
        }, new ColumnInfo<Task, String>("Description") {
            @Override
            public @Nullable String valueOf(Task task) {
                return task.getDescription();
            }
        });
    }

    public void updateTaskRepository(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
        try {
            setItems(Arrays.asList(taskRepository.getIssues("", 0, 10, false)));
        } catch (Exception e) {
            LOG.warn(e.getMessage(), e);
        }
    }
}
