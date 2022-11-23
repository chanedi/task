package com.lufax.task.utils;

import com.intellij.openapi.project.Project;
import com.intellij.ui.TextFieldWithAutoCompletion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SwingUtils {

    public static TextFieldWithAutoCompletion<String> createTextFieldWithCompletion(@Nullable Project project, String text, final List<String> variants) {
        final TextFieldWithAutoCompletion.StringsCompletionProvider provider = new TextFieldWithAutoCompletion.StringsCompletionProvider(variants, null) {
            @Nullable
            @Override
            public String getPrefix(@NotNull String text, int offset) {
                final int i = text.lastIndexOf('{', offset - 1);
                if (i < 0) {
                    return "";
                }
                return text.substring(i, offset);
            }
        };
        return new TextFieldWithAutoCompletion<>(project, provider, true, text);
    }

}
