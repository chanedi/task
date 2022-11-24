package com.lufax.task.repository;

import com.intellij.tasks.TaskRepository;
import com.intellij.tasks.generic.GenericTask;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

public class SuperGenericTask extends GenericTask {
  private @Nls String myStatus;
  private @Nls String myReleaseDate;
  private @Nls String myCustomField1;
  private @Nls String myCustomField2;
  private @Nls String myCustomField3;
  private @Nls String myCustomField4;
  private @Nls String myCustomField5;

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

  public String getCustomField1() {
    return myCustomField1;
  }

  public void setCustomField1(String customField1) {
    this.myCustomField1 = customField1;
  }

  public String getCustomField2() {
    return myCustomField2;
  }

  public void setCustomField2(String customField2) {
    this.myCustomField2 = customField2;
  }

  public String getCustomField3() {
    return myCustomField3;
  }

  public void setCustomField3(String customField3) {
    this.myCustomField3 = customField3;
  }

  public String getCustomField4() {
    return myCustomField4;
  }

  public void setCustomField4(String customField4) {
    this.myCustomField4 = customField4;
  }

  public String getCustomField5() {
    return myCustomField5;
  }

  public void setCustomField5(String customField5) {
    this.myCustomField5 = customField5;
  }
}