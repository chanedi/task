package com.lufax.task.repository;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.tasks.LocalTask;
import com.intellij.tasks.TaskRepository;
import com.intellij.tasks.impl.DefaultCommitPlaceholderProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SuperCommitPlaceholderProvider extends DefaultCommitPlaceholderProvider {


    @Override
    public String @NotNull [] getPlaceholders(TaskRepository repository) {
        return new String[] { "id", "number", "summary", "project", "taskType"};
    }

    @Nullable
    @Override
    public String getPlaceholderValue(LocalTask task, String placeholder) {
        try {
            return super.getPlaceholderValue(task, placeholder);
        } catch (IllegalArgumentException e) {
        }

        

        throw new IllegalArgumentException(placeholder);
    }
}
