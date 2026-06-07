<!--
Copyright (c) 2011-2026, Kirk Pepperdine.

The contents of this file are subject to the terms of the
Common Development and Distribution License (the "License").
You may not use this file except in compliance with the License.

You can obtain a copy of the license at http://www.opensource.org/licenses/CDDL-1.0.
See the License for the specific language governing permissions
and limitations under the License.

When distributing Covered Code, include this CDDL HEADER in each
file and include the License file.
If applicable, add the following below this CDDL HEADER, with the
fields enclosed by brackets "[]" replaced with your own identifying
information: Portions Copyright [yyyy] [name of copyright owner]
-->

# Agent Instructions

- Target VisualVM 2.2.1 on JDK 25. VisualVM 2.2.1 publishes Maven API artifacts as `org.graalvm.visualvm.api:*:2.2`.
- Keep NetBeans dependencies on `org.netbeans.api:*:RELEASE220` unless the target VisualVM baseline changes. Newer NetBeans artifacts such as `RELEASE300` are not the supported host platform for VisualVM 2.2.1 plugins.
- Build with JDK 25 and Maven 3.9.15 or newer. Verify with `mvn -DskipTests clean verify`.
- The generated plugin artifact is `target/nbm/memorypoolview-<version>.nbm`.
- Packages are `@NullMarked` with JSpecify. Add `@Nullable` at VisualVM, Swing, JMX, and NetBeans API boundaries where null can be returned or accepted.
- Do not add runtime dependencies unless they are valid NetBeans/VisualVM module dependencies. JSpecify is compile-time only and should stay `provided`.
- Preserve the Memory Pools layout: one all-pools grid, three charts per row, sorted by maximum size descending, without adding a scroll pane.
- Keep the root `LICENSE.txt` and `src/main/resources/com/kodewerk/visualvm/memorypoolview/license.txt` aligned; the NBM plugin configuration uses the resource copy.
