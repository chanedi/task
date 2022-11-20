// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.lufax.task;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.tasks.Task;
import com.intellij.tasks.TaskBundle;
import com.intellij.tasks.generic.GenericRepository;
import com.intellij.tasks.generic.ResponseType;
import com.intellij.tasks.generic.TemplateVariable;
import com.intellij.util.net.HTTPMethod;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;
import com.lufax.task.utils.HttpUtils;
import org.apache.commons.httpclient.HttpMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.intellij.tasks.generic.GenericRepositoryUtil.concat;

/**
 * @author Evgeny.Zakrevsky
 */
@Tag("SuperGeneric")
public class SuperGenericRepository extends GenericRepository {

    private static final Logger LOG = Logger.getInstance(SuperGenericRepository.class);
    @Attribute("id")
    private String id;
    @Attribute("loginURLWithToken")
    private String loginURLWithToken;
    @Attribute("loginWithTokenMethodType")
    private HTTPMethod loginWithTokenMethodType = HTTPMethod.GET;
    @Attribute("loginSuccessCookieName")
    private String loginSuccessCookieName;

    public SuperGenericRepository() {
        resetToDefaults();
    }

    public SuperGenericRepository(SuperGenericRepositoryType superGenericRepositoryType) {
        super(superGenericRepositoryType);
    }

    /**
     * Cloning constructor
     */
    public SuperGenericRepository(final SuperGenericRepository other) {
        super(other);
        this.id = other.id;
        this.loginURLWithToken = other.loginURLWithToken;
        this.loginSuccessCookieName = other.loginSuccessCookieName;
    }

    public boolean testLoginConnection(@NotNull Project project) {
        @Nullable CancellableConnection myConnection = new CancellableConnection() {
            @Override
            protected void doTest() {
                String result = s_executeMethod(getLoginMethod());
                throw new ProcessNeedResultException(result);
            }

            @Override
            public void cancel() {

            }
        };

        return tryConnection(project, myConnection);
    }

    public boolean testTaskConnection(@NotNull Project project) {
        @Nullable CancellableConnection myConnection = new CancellableConnection() {
            @Override
            protected void doTest() throws Exception {
                s_executeMethod(getLoginMethod());
                String result = s_executeMethod(getTaskListMethod(1, 0));
                throw new ProcessNeedResultException(result);
            }

            @Override
            public void cancel() {
            }
        };

        return tryConnection(project, myConnection);
    }

    public boolean testSingleTaskConnection(@NotNull Project project) {
        @Nullable CancellableConnection myConnection = new CancellableConnection() {
            @Override
            protected void doTest() throws Exception {
                String result = s_executeMethod(getSingleTaskMethod("1"));
                throw new ProcessNeedResultException(result);
            }

            @Override
            public void cancel() {
            }
        };

        return tryConnection(project, myConnection);       }

    public boolean tryConnection(@NotNull Project project, @Nullable CancellableConnection myConnection) {
        TestConnectionTask task = new TestConnectionTask(project, TaskBundle.message("dialog.title.test.connection"), true) {

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setText(TaskBundle.message("progress.text.connecting.to", getUrl()));
                indicator.setFraction(0);
                indicator.setIndeterminate(true);
                try {
                    Future<Exception> future = ApplicationManager.getApplication().executeOnPooledThread(myConnection);
                    while (true) {
                        try {
                            myException = future.get(100, TimeUnit.MILLISECONDS);
                            return;
                        }
                        catch (TimeoutException ignore) {
                            try {
                                indicator.checkCanceled();
                            }
                            catch (ProcessCanceledException e) {
                                myException = e;
                                myConnection.cancel();
                                return;
                            }
                        }
                        catch (Exception e) {
                            myException = e;
                            return;
                        }
                    }
                }
                catch (Exception e) {
                    myException = e;
                }
            }

        };
        ProgressManager.getInstance().run(task);
        Exception e = task.myException;
        if (e == null) {
            Messages.showMessageDialog(project, TaskBundle.message("dialog.message.connection.successful"),
                    TaskBundle.message("dialog.title.connection"), Messages.getInformationIcon());
        }
        else if (e instanceof ProcessNeedResultException) {
            String message = e.getMessage();
            Messages.showMessageDialog(project, TaskBundle.message("dialog.message.connection.successful") + ". Returned message is: \n\n" + unicodeDecode(message),
                    TaskBundle.message("dialog.title.connection"), Messages.getInformationIcon());
        }
        else if (!(e instanceof ProcessCanceledException)) {
            String message = e.getMessage();
            if (e instanceof UnknownHostException) {
                message = TaskBundle.message("dialog.message.unknown.host", message);
            }
            if (message == null) {
                LOG.error(e);
                message = TaskBundle.message("dialog.message.unknown.error");
            }
            Messages.showErrorDialog(project, StringUtil.capitalize(message), TaskBundle.message("dialog.title.error"));
        }
        return e == null;
    }

    @Override
    public Task[] getIssues(@Nullable final String query, final int max, final long since) throws Exception {
        if (StringUtil.isEmpty(getTasksListUrl())) {
            throw new Exception("'Task list URL' configuration parameter is mandatory");
        }
        if (!isLoginAnonymously() && !isUseHttpAuthentication()) {
            s_executeMethod(getLoginMethod());
        }
        String responseBody = s_executeMethod(getTaskListMethod(1, 0));
        Task[] tasks = getActiveResponseHandler().parseIssues(responseBody, max);
        if (getResponseType() == ResponseType.TEXT) {
            return tasks;
        }
        if (StringUtil.isNotEmpty(getSingleTaskUrl()) && getDownloadTasksInSeparateRequests()) {
            for (int i = 0; i < tasks.length; i++) {
                tasks[i] = findTask(tasks[i].getId());
            }
        }
        return tasks;
    }

    @Nullable
    @Override
    public Task findTask(@NotNull final String id) throws Exception {
        return getActiveResponseHandler().parseIssue(s_executeMethod(getSingleTaskMethod(id)));
    }

    public HttpMethod getHttpMethod(String requestUrl, HTTPMethod type, List<TemplateVariable> requestTemplateVariables) {
        return HttpUtils.getHttpMethod(this, requestUrl, type, requestTemplateVariables);
    }

    public HttpMethod getLoginMethod() {
        return getHttpMethod(getLoginUrl(), getLoginMethodType(), getAllTemplateVariables());
    }

    public HttpMethod getTaskListMethod(final int max, final long since) {
        List<TemplateVariable> variables = concat(getAllTemplateVariables(),
                new TemplateVariable("max", String.valueOf(max)),
                new TemplateVariable("since", String.valueOf(since)));
        return getHttpMethod(getTasksListUrl(), getTasksListMethodType(), variables);
    }

    public HttpMethod getSingleTaskMethod(@NotNull final String id) {
        List<TemplateVariable> variables = concat(getAllTemplateVariables(), new TemplateVariable("id", id));
        return getHttpMethod(getSingleTaskUrl(), getSingleTaskMethodType(), variables);
    }

    private abstract class TestConnectionTask extends com.intellij.openapi.progress.Task.Modal {
        protected Exception myException;

        public TestConnectionTask(@Nullable Project project, @NlsContexts.DialogTitle @NotNull String title, boolean canBeCancelled) {
            super(project, title, canBeCancelled);
        }
    }

    private String s_executeMethod(HttpMethod method) {
        String result = null;
        try {
            Method reflectMethod = GenericRepository.class.getDeclaredMethod("executeMethod", HttpMethod.class);
            reflectMethod.setAccessible(true);
            result = (String) reflectMethod.invoke(this, method);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        LOG.info("executeMethod result:" + (result.length() > 500 ? result.substring(0, 500) : result));
        return result;
    }

    public String unicodeDecode(String string) {
        Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
        Matcher matcher = pattern.matcher(string);
        char ch;
        while (matcher.find()) {
            ch = (char) Integer.parseInt(matcher.group(2), 16);
            string = string.replace(matcher.group(1), ch + "");
        }
        return string;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLoginUrlWithToken() {
        return loginURLWithToken;
    }

    public void setLoginUrlWithToken(String loginURLWithToken) {
        this.loginURLWithToken = loginURLWithToken;
    }

    public HTTPMethod getLoginWithTokenMethodType() {
        return loginWithTokenMethodType;
    }

    public void setLoginWithTokenMethodType(HTTPMethod loginWithTokenMethodType) {
        this.loginWithTokenMethodType = loginWithTokenMethodType;
    }

    public String getLoginSuccessCookieName() {
        return loginSuccessCookieName;
    }

    public void setLoginSuccessCookieName(String loginSuccessCookieName) {
        this.loginSuccessCookieName = loginSuccessCookieName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SuperGenericRepository)) return false;
        SuperGenericRepository that = (SuperGenericRepository)o;
        if (!Objects.equals(getId(), that.getId())) return false;
        return true;
    }

    @Override
    public @NotNull SuperGenericRepository clone() {
        return new SuperGenericRepository(this);
    }
}
