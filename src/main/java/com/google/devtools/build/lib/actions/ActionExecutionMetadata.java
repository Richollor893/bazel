// Copyright 2014 The Bazel Authors. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.devtools.build.lib.actions;

import com.google.devtools.build.lib.cmdline.RepositoryMapping;
import com.google.devtools.build.lib.concurrent.ThreadSafety.ThreadSafe;
import javax.annotation.Nullable;

/**
 * An interface for an {@link Action}, containing only side-effect-free query methods for
 * information needed during both action analysis and execution.
 *
 * <p>The split between {@link Action} and {@link ActionExecutionMetadata} is somewhat arbitrary,
 * other than that all methods with side effects must belong to the former.
 */
public interface ActionExecutionMetadata extends ActionAnalysisMetadata {

  /**
   * If this executable can supply verbose information, returns a string that can be used as a
   * progress message while this executable is running. A return value of {@code null} indicates no
   * message should be reported.
   */
  @Nullable
  String getProgressMessage();

  /**
   * A variant of {@link #getProgressMessage} that additionally takes the {@link RepositoryMapping}
   * of the main repository, which can be used by the implementation to emit labels with apparent
   * instead of canonical repository names. A return value of {@code null} indicates no message
   * should be reported.
   *
   * <p>The default implementation simply returns the result of {@link #getProgressMessage}.
   */
  @Nullable
  default String getProgressMessage(RepositoryMapping mainRepositoryMapping) {
    return getProgressMessage();
  }

  /**
   * Returns a human-readable description of the inputs to {@link #getKey(ActionKeyContext)}. Used
   * in the output from '--explain', and in error messages for '--check_up_to_date' and
   * '--check_tests_up_to_date'. May return null, meaning no extra information is available.
   *
   * <p>If the return value is non-null, for consistency it should be a multiline message of the
   * form:
   *
   * <pre>
   *   <var>Summary</var>
   *     <var>Fieldname</var>: <var>value</var>
   *     <var>Fieldname</var>: <var>value</var>
   *     ...
   * </pre>
   *
   * where each line after the first one is intended two spaces, and where any fields that might
   * contain newlines or other funny characters are escaped using {@link
   * com.google.devtools.build.lib.shell.ShellUtils#shellEscape}. For example:
   *
   * <pre>
   *   Compiling foo.cc
   *     Command: /usr/bin/gcc
   *     Argument: '-c'
   *     Argument: foo.cc
   *     Argument: '-o'
   *     Argument: foo.o
   * </pre>
   */
  @Nullable
  String describeKey();

  /**
   * Get the {@link RunfilesSupplier} providing runfiles needed by this action.
   */
  RunfilesSupplier getRunfilesSupplier();

  /**
   * Returns true iff the getInputs set is known to be complete.
   *
   * <p>For most Actions, this always returns true, but in some cases (e.g. C++ compilation), inputs
   * are dynamically discovered from the previous execution of the Action, and so before the initial
   * execution, this method will return false in those cases.
   *
   * <p>Any builder <em>must</em> unconditionally execute an Action for which this method returns
   * false, regardless of all other inferences made by its dependency analysis. In addition, all
   * prerequisites mentioned in the (possibly incomplete) value returned by getInputs must also be
   * built first, as usual.
   */
  @ThreadSafe
  boolean inputsDiscovered();

  /** Returns true iff {@link #inputsDiscovered()} may ever return false. */
  @ThreadSafe
  boolean discoversInputs();

  /**
   * Returns true if the action may create output artifacts whose contents aren't generated by this
   * action, and also, this action does not consume its input artifacts' contents.
   *
   * <p>This is rarely true. Symlink actions are an example where this is true: their outputs'
   * contents are equal to their inputs' contents, and a symlink action does not consume its inputs'
   * contents.
   *
   * <p>This property is relevant for action rewinding and top-level output fetching.
   */
  default boolean mayInsensitivelyPropagateInputs() {
    return false;
  }
}
