package com.uber.okbuck.extension;

import com.google.common.collect.ImmutableSet;
import com.uber.okbuck.core.dependency.ExternalDependency;
import com.uber.okbuck.core.manager.JetifierManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.gradle.api.Project;

public class JetifierExtension {

  public static final String DEFAULT_JETIFIER_VERSION = "1.0.0-beta02";
  private static final List<String> BLACKLISTED_DEPS =
      Arrays.asList(
          "com.android.tools.build.jetifier:jetifier-core",
          "com.android.tools.build.jetifier:jetifier-processor",
          "com.google.code.gson:gson",
          "commons-cli:commons-cli",
          "org.jdom:jdom2",
          "org.jetbrains:annotations",
          "org.ow2.asm:asm-commons",
          "org.ow2.asm:asm-tree",
          "org.ow2.asm:asm-util",
          "org.ow2.asm:asm",
          "org.jetbrains.kotlin:.*");

  /** Jetifier jar version */
  public String version;

  /** Enable jetify to act on aars only */
  private boolean aarOnly;

  /** Stores the dependencies which are excluded from being jetified. */
  private List<String> exclude = new ArrayList<>();

  private final boolean enableJetifier;

  @Nullable private List<Pattern> excludePatterns;

  JetifierExtension(Project project) {
    version = DEFAULT_JETIFIER_VERSION;
    enableJetifier = JetifierManager.isJetifierEnabled(project);
  }

  private List<Pattern> getExcludePatterns() {
    if (excludePatterns == null) {
      excludePatterns =
          new ImmutableSet.Builder<String>()
              .addAll(exclude)
              .addAll(BLACKLISTED_DEPS)
              .build()
              .stream()
              .map(Pattern::compile)
              .collect(Collectors.toList());
    }
    return excludePatterns;
  }

  public boolean shouldJetify(String group, String name, String packaging) {
    if (aarOnly && packaging.equals(ExternalDependency.JAR)) {
      return false;
    }
    return enableJetifier
        && getExcludePatterns()
            .stream()
            .noneMatch(pattern -> pattern.matcher(group + ":" + name).matches());
  }
}
