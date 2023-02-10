package com.lufax.task.repository;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import org.jetbrains.annotations.NotNull;

public class LoadTaskRepositoriesSharedInProjectsListener implements ProjectManagerListener {

    @Override
    public void projectOpened(@NotNull Project project) {
        ProjectManagerListener.super.projectOpened(project);
        TaskRepositoriesSharedInProjects.refreshTaskRepository(project);
    }


}
