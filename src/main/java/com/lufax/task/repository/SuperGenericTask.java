package com.lufax.task.repository;

import com.intellij.tasks.TaskRepository;
import com.intellij.tasks.generic.GenericTask;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

public class SuperGenericTask extends GenericTask {
  private @Nls String myStatus;
  private @Nls String myReleaseDate;

  public SuperGenericTask(final String id, final @Nls String summary, final TaskRepository repository) {
    super(id, summary, repository);
  }

  @Nullable
  public String getStatus() {
    return myStatus;
  }

  public void setStatus(@Nullable @Nls String status) {
    myStatus = status;
  }

  @Nullable
  public String getReleaseDate() {
    return myReleaseDate;
  }

  public void setReleaseDate(@Nullable @Nls String releaseDate) {
    myReleaseDate = releaseDate;
  }

}