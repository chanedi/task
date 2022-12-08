// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.lufax.task.repository;

import com.alibaba.fastjson.JSONPath;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.tasks.generic.ResponseType;
import com.intellij.tasks.generic.Selector;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xmlb.annotations.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Mikhail Golubev
 */
@Tag("JsonSuperResponseHandler")
public final class JsonPathSuperResponseHandler extends SelectorBasedSuperResponseHandler {

  private static final Logger LOG = Logger.getInstance(SuperGenericRepository.class);

  private static final Map<Class<?>, String> JSON_TYPES = ContainerUtil.newHashMap(
          new Pair<>(Map.class, "JSON object"),
          new Pair<>(List.class, "JSON array"),
          new Pair<>(String.class, "JSON string"),
          new Pair<>(Integer.class, "JSON number"),
          new Pair<>(Double.class, "JSON number"),
          new Pair<>(Boolean.class, "JSON boolean")
  );

  private final Map<String, JSONPath> myCompiledCache = new HashMap<>();

  /**
   * Serialization constructor
   */
  @SuppressWarnings("UnusedDeclaration")
  public JsonPathSuperResponseHandler() {
  }

  public JsonPathSuperResponseHandler(SuperGenericRepository repository) {
    super(repository);
  }

  @Nullable
  private Object extractRawValue(@NotNull Selector selector, @NotNull String source) throws Exception {
    if (StringUtil.isEmpty(selector.getPath())) {
      return null;
    }
    JSONPath jsonPath = lazyCompile(selector.getPath());
    Object result = jsonPath.eval(source);
    LOG.info((String) result);
    return result;
  }

  @Nullable
  private <T> T extractValueAndCheckType(@NotNull Selector selector, @NotNull String source, Class<T> cls) throws Exception {
    final Object value = extractRawValue(selector, source);
    if (value == null) {
      return null;
    }
    if (!(cls.isInstance(value))) {
      throw new Exception(
              String.format("JsonPath expression '%s' should match %s. Got '%s' instead",
                      selector.getPath(), JSON_TYPES.get(cls), value));
    }
    @SuppressWarnings("unchecked")
    T casted = (T)value;
    return casted;
  }

  @NotNull
  @Override
  protected List<Object> selectTasksList(@NotNull String response, int max) throws Exception {
    @SuppressWarnings("unchecked")
    List<Object> list = (List<Object>)extractValueAndCheckType(getSelector(TASKS), response, List.class);
    if (list == null) {
      return ContainerUtil.emptyList();
    }
    return ContainerUtil.getFirstItems(ContainerUtil.map2List(list, o -> o.toString()), max);
  }

  @Nullable
  @Override
  protected String selectString(@NotNull Selector selector, @NotNull Object context) throws Exception {
    //return extractValueAndCheckType((String)context, selector, String.class);
    final Object value = extractRawValue(selector, (String)context);
    if (value == null) {
      return null;
    }
    if (value instanceof String || value instanceof Number || value instanceof Boolean) {
      return value.toString();
    }
    throw new Exception(String.format("JsonPath expression '%s' should match string value. Got '%s' instead",
            selector.getPath(), value));
  }

  @Nullable
  @Override
  protected Boolean selectBoolean(@NotNull Selector selector, @NotNull Object context) throws Exception {
    return extractValueAndCheckType(selector, (String)context, Boolean.class);
  }

  @SuppressWarnings("UnusedDeclaration")
  @Nullable
  private Long selectLong(@NotNull Selector selector, @NotNull String source) throws Exception {
    return extractValueAndCheckType(selector, source, Long.class);
  }

  @NotNull
  private JSONPath lazyCompile(@NotNull String path) throws Exception {
    JSONPath jsonPath = myCompiledCache.get(path);
    if (jsonPath == null) {
      jsonPath = JSONPath.compile(path);
      myCompiledCache.put(path, jsonPath);
    }
    return jsonPath;
  }

  @NotNull
  @Override
  public ResponseType getResponseType() {
    return ResponseType.JSON;
  }
}
