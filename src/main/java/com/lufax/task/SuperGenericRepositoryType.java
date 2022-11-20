// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.lufax.task;

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
import icons.TasksCoreIcons;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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
    repository.setId(UUID.randomUUID().toString());
    return repository;
  }

  @Override
  public Class<SuperGenericRepository> getRepositoryClass() {
    return SuperGenericRepository.class;
  }

  @NotNull
  @Override
  public TaskRepositoryEditor createEditor(final SuperGenericRepository repository,
                                           final Project project,
                                           final Consumer<? super SuperGenericRepository> changeListener) {
    return new SuperGenericRepositoryEditor(project, repository, changeListener);
  }

  @Override
  public List<TaskRepositorySubtype> getAvailableSubtypes() {
    return Arrays.asList(
            this,
            new IReleaseRepository()
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
      return myName + " [G]";
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

        URL resourceUrl = SuperGenericRepository.class.getResource("connectors/" + configFileName);
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
  public final class IReleaseRepository extends SuperGenericSubtype {
    public IReleaseRepository() {
      super("IRelease", TasksCoreIcons.Asana);
    }
  }

}
