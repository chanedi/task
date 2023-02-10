// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.lufax.task.repository;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.tasks.TaskRepository;
import com.intellij.tasks.TaskRepositoryType;
import com.intellij.tasks.impl.TaskManagerImpl;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Dmitry Avdeev
 */
@State(name = "TaskRepositoriesSharedInProjects", storages = @Storage("other.xml"))
public final class TaskRepositoriesSharedInProjects implements PersistentStateComponent<Element>, Disposable {
  private final List<TaskRepository> myRepositories = new ArrayList<>();

  public TaskRepositoriesSharedInProjects() {
    // remove repositories pertaining to non-existent types
    TaskRepositoryType.addEPListChangeListener(this, () -> {
      List<Class<?>> possibleRepositoryClasses = TaskRepositoryType.getRepositoryClasses();
      myRepositories.removeIf(repository -> {
        return !ContainerUtil.exists(possibleRepositoryClasses, clazz -> clazz.isAssignableFrom(repository.getClass()));
      });
    });
  }

  public static TaskRepositoriesSharedInProjects getInstance() {
    return ServiceManager.getService(TaskRepositoriesSharedInProjects.class);
  }

  public void addOrRemoveRepository(TaskRepository repository) {
    if (!(repository instanceof SuperGenericRepository)) {
      return;
    }
    if (StringUtil.isEmptyOrSpaces(((SuperGenericRepository) repository).getId())) {
      return;
    }
    for (int i = 0; i < myRepositories.size(); i++) {
      if (!((SuperGenericRepository) repository).idEquals(myRepositories.get(i))) {
        continue;
      }
      if (((SuperGenericRepository) repository).isSharedInProjects()) {
        myRepositories.set(i, repository);
      } else {
        myRepositories.remove(i);
      }
      return;
    }
    if (((SuperGenericRepository) repository).isSharedInProjects()) {
      myRepositories.add(repository);
    }
  }

  @Override
  public Element getState() {
    return XmlSerializer.serialize(myRepositories.toArray(new TaskRepository[0]));
  }

  @Override
  public void loadState(@NotNull Element state) {
    myRepositories.clear();
    myRepositories.addAll(TaskManagerImpl.loadRepositories(state));
  }

  @Override
  public void dispose() {}

  public Map<String, TaskRepository> getRepositoryMap() {
    Map<String, TaskRepository> map = new HashMap<>();
    for (TaskRepository myRepository : myRepositories) {
      if (!(myRepository instanceof SuperGenericRepository)) {
        continue;
      }
      map.put(((SuperGenericRepository) myRepository).getId(), myRepository);
    }
    return map;
  }
}
