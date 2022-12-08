// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.lufax.task.repository;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.tasks.TaskRepository;
import com.intellij.tasks.TaskRepositorySubtype;
import com.intellij.tasks.config.TaskRepositoryEditor;
import com.intellij.tasks.impl.BaseRepositoryType;
import com.intellij.util.Consumer;
import com.intellij.util.xmlb.XmlSerializer;
import icons.TaskEnhanceIcons;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class SuperGenericRepositoryType extends BaseRepositoryType<SuperGenericRepository> {

  @NotNull
  @Override
  public String getName() {
    return "SuperGeneric";
  }

  @NotNull
  @Override
  public Icon getIcon() {
    return TaskEnhanceIcons.Super;
  }

  @NotNull
  @Override
  public TaskRepository createRepository() {
    SuperGenericRepository repository = new SuperGenericRepository(this);
    return repository;
  }

  @Override
  public Class<SuperGenericRepository> getRepositoryClass() {
    return SuperGenericRepository.class;
  }

  @Override
  public @NotNull TaskRepositoryEditor createEditor(SuperGenericRepository repository, Project project, Consumer<? super SuperGenericRepository> changeListener) {
    return new SuperGenericRepositoryEditor(project, repository, changeListener);
  }

  @Override
  public List<TaskRepositorySubtype> getAvailableSubtypes() {
    return Arrays.asList(
            this,
            new WizardKanbanRepository(),
            new WizardWorkBenchRepository(),
            new IReleaseDevTaskRepository(),
            new IReleaseStoryRepository()
    );
  }

  public class SuperGenericSubtype implements TaskRepositorySubtype {
    private final String myName;
    private final Icon myIcon;

    SuperGenericSubtype(String name, Icon icon) {
      myName = name;
      myIcon = icon;
    }

    @Override
    public String getName() {
      return myName + " [S]";
    }

    @Override
    public Icon getIcon() {
      return myIcon;
    }

    @Override
    public TaskRepository createRepository() {
      Element element;
      try {
        String configFileName = StringUtil.toLowerCase(myName) + ".xml";

        URL resourceUrl = SuperGenericRepository.class.getResource("/connectors/" + configFileName);
        if (resourceUrl == null) {
          throw new AssertionError("Repository configuration file '" + configFileName + "' not found");
        }
        element = JDOMUtil.loadResource(resourceUrl);
      }
      catch (Exception e) {
        throw new AssertionError(e);
      }
      SuperGenericRepository repository = XmlSerializer.deserialize(element, SuperGenericRepository.class);
      repository.setRepositoryType(SuperGenericRepositoryType.this);
      repository.setSubtypeName(getName());
      return repository;
    }
  }

  // Subtypes:
  public final class IReleaseStoryRepository extends SuperGenericSubtype {
    public IReleaseStoryRepository() {
      super("IReleaseStory", TaskEnhanceIcons.IRelease);
    }
  }

  public final class IReleaseDevTaskRepository extends SuperGenericSubtype {
    public IReleaseDevTaskRepository() {
      super("IReleaseDevTask", TaskEnhanceIcons.IRelease);
    }
  }

  public final class WizardWorkBenchRepository extends SuperGenericSubtype {
    public WizardWorkBenchRepository() {
      super("WizardWorkBench", TaskEnhanceIcons.Wizard);
    }
  }

  public final class WizardKanbanRepository extends SuperGenericSubtype {
    public WizardKanbanRepository() {
      super("WizardKanban", TaskEnhanceIcons.Wizard);
    }
  }

}
