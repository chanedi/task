package com.lufax.task.repository;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.tasks.TaskManager;
import com.intellij.tasks.TaskRepository;
import com.intellij.tasks.impl.TaskManagerImpl;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class LoadTaskRepositoriesSharedInProjectsListener implements ProjectManagerListener {

    @Override
    public void projectOpened(@NotNull Project project) {
        ProjectManagerListener.super.projectOpened(project);
        TaskManagerImpl manager = (TaskManagerImpl) TaskManager.getManager(project);
        @NotNull List<TaskRepository> repositories = Arrays.asList(manager.getAllRepositories());
        Map<String, TaskRepository> sharedRepositories = TaskRepositoriesSharedInProjects.getInstance().getRepositoryMap();
        for (int i = 0; i < repositories.size(); i++) {
            TaskRepository repository = repositories.get(i);
            if (!(repository instanceof SuperGenericRepository)) {
                continue;
            }
            TaskRepository sharedRepository = sharedRepositories.remove(((SuperGenericRepository) repository).getId());
            if (sharedRepository != null) {
                repositories.set(i, sharedRepository);
            } else if (((SuperGenericRepository) repository).isSharedInProjects()) {
                ((SuperGenericRepository) repository).setSharedInProjects(false);
            }
        }
        repositories.addAll(sharedRepositories.values());
        manager.setRepositories(repositories);
    }
}
