// Copyright 2000-2022 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.lufax.task.toolwindow;

import com.intellij.configurationStore.XmlSerializer;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.project.Project;
import com.intellij.tasks.TaskManager;
import com.intellij.tasks.TaskRepository;
import com.intellij.tasks.impl.TaskManagerImpl;
import com.intellij.util.xmlb.annotations.Tag;
import com.intellij.util.xmlb.annotations.XCollection;
import com.lufax.task.repository.SuperGenericRepository;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@State(name = "com.lufax.task.toolwindow.config.TaskUpdateConfigState", storages = @Storage(StoragePathMacros.WORKSPACE_FILE))
public class TaskUpdateConfigsState implements PersistentStateComponent<TaskUpdateConfigsState.Config> {

  private TaskRepository selectedTaskRepository;
  private final Project project;
  private Map<TaskRepository, TaskUpdateConfig> updateConfigMap = new HashMap<>();
  private final Config config = new Config();

  public static TaskUpdateConfigsState getInstance(@NotNull Project project) {
    return project.getService(TaskUpdateConfigsState.class);
  }

  public TaskUpdateConfigsState(@NotNull Project project) {
    this.project = project;
  }

  @Nullable
  @Override
  public Config getState() {
    if (selectedTaskRepository != null) {
      TaskRepository[] taskRepositories = new TaskRepository[1];
      taskRepositories[0] = selectedTaskRepository;
      config.selectServer = XmlSerializer.serialize(taskRepositories);
    }

    TaskManagerImpl taskManager = (TaskManagerImpl) TaskManager.getManager(project);
    List<TaskRepository> repositoriesOrderWillBeLoad = TaskManagerImpl.loadRepositories(XmlSerializer.serialize(taskManager.getAllRepositories()));
    for (int i = 0; i < repositoriesOrderWillBeLoad.size(); i++) {
      for (TaskRepository repository : taskManager.getAllRepositories()) {
        if (repository.equals(repositoriesOrderWillBeLoad.get(i))) {
          repositoriesOrderWillBeLoad.set(i, repository);
          break;
        }
      }
    }
    config.updateConfigList = new ArrayList<>(repositoriesOrderWillBeLoad.size());
    for (TaskRepository taskRepository : repositoriesOrderWillBeLoad) {
      TaskUpdateConfig updateConfig = getUpdateConfig(taskRepository);
      if (updateConfig == null) {
        updateConfig = new TaskUpdateConfig(taskRepository);
      }
      config.updateConfigList.add(updateConfig);
    }

    return config;
  }

  @Override
  public void loadState(@NotNull Config config) {
    TaskManagerImpl taskManager = (TaskManagerImpl) TaskManager.getManager(project);
    TaskRepository[] allRepositories = taskManager.getAllRepositories();

    ArrayList<TaskRepository> selectServers = TaskManagerImpl.loadRepositories(config.selectServer);
    selectedTaskRepository = selectServers.size() > 0 ? selectServers.get(0) : null;
    if (selectedTaskRepository != null) {
      for (TaskRepository taskRepository : allRepositories) {
        if (selectedTaskRepository.equals(taskRepository)) {
          selectedTaskRepository = taskRepository;
          break;
        }
      }
    }
    updateConfigMap = new HashMap<>();
    for (int i = 0; i < allRepositories.length; i++) {
      TaskUpdateConfig updateConfig;
      if (i >= config.updateConfigList.size()) {
        updateConfig = new TaskUpdateConfig(allRepositories[i]);
      } else {
        updateConfig = config.updateConfigList.get(i);
      }
      updateConfigMap.put(allRepositories[i], updateConfig);
    }
  }

  public TaskRepository getSelectedTaskRepository() {
    TaskManagerImpl manager = (TaskManagerImpl) TaskManager.getManager(project);
    TaskRepository[] repositories = manager.getAllRepositories();
    if (repositories.length == 0) {
      selectedTaskRepository = null;
      return null;
    }

    if (selectedTaskRepository != null) {
      for (TaskRepository repository : repositories) {
        if (repository.equals(selectedTaskRepository)) {
          return selectedTaskRepository;
        } else if (repository instanceof SuperGenericRepository && ((SuperGenericRepository) repository).idEquals(selectedTaskRepository)) {
          return selectedTaskRepository;
        }
      }
    }
    if (repositories.length > 0) {
      selectedTaskRepository = repositories[0];
    }
    return selectedTaskRepository;
  }

  public void setSelectedTaskRepository(TaskRepository selectedTaskRepository) {
    this.selectedTaskRepository = selectedTaskRepository;
  }

  public TaskUpdateConfig getUpdateConfig() {
    return getUpdateConfig(selectedTaskRepository);
  }

  public TaskUpdateConfig getUpdateConfig(TaskRepository repository) {
    if (repository == null) {
      return null;
    }
    TaskUpdateConfig updateConfig = updateConfigMap.get(repository);
    if (repository instanceof SuperGenericRepository) {
      updateConfig = ((SuperGenericRepository) repository).getUpdateConfig();
    }
    if (updateConfig == null) {
      for (TaskRepository taskRepository : updateConfigMap.keySet()) {
        if (repository instanceof SuperGenericRepository) {
          if (((SuperGenericRepository) repository).idEquals(taskRepository)) {
            updateConfig = updateConfigMap.get(taskRepository);
            break;
          } else {
            continue;
          }
        } else if (taskRepository.getClass().equals(repository.getClass()) && taskRepository.getPresentableName().equals(repository.getPresentableName())) {
          updateConfig = updateConfigMap.get(taskRepository);
        }
      }
      updateConfig(repository, updateConfig);
    }
    if (updateConfig == null) {
      updateConfig = new TaskUpdateConfig(repository);
      updateConfig(repository, updateConfig);
    }
    return updateConfig;
  }

  public void updateConfig(TaskUpdateConfig updateConfig) {
    updateConfig(selectedTaskRepository, updateConfig);
  }

  public void updateConfig(TaskRepository taskRepository, TaskUpdateConfig updateConfig) {
    updateConfigMap.put(taskRepository, updateConfig);
    if (taskRepository instanceof SuperGenericRepository) {
      ((SuperGenericRepository) taskRepository).setUpdateConfig(updateConfig);
    }
  }

  public static class Config {
    @XCollection
    protected List<TaskUpdateConfig> updateConfigList = new ArrayList<>();
    @Tag("selectRepository")
    protected Element selectServer = new Element("selectRepository");
  }
}
