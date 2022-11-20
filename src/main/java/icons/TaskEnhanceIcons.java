package icons;

import com.intellij.ui.IconManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public final class TaskEnhanceIcons {

    private static @NotNull Icon load(@NotNull String path, long cacheKey, int flags) {
        return IconManager.getInstance().loadRasterizedIcon(path, TaskEnhanceIcons.class.getClassLoader(), cacheKey, flags);
    }

    /** 13x13 */ public static final @NotNull Icon ToolWindowTasks = load("icons/tasks.svg", 2022103114000001L, 2);

    /** 16x16 */ public static final @NotNull Icon Super = load("icons/super.svg", 2022110914000001L, 2);

}
