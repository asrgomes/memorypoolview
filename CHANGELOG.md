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

# Changelog

## 2.0.0

- Updated the plugin build for Java 25 and the VisualVM 2.2.x API baseline.
- Migrated VisualVM and NetBeans dependencies to Maven Central coordinates compatible with current VisualVM releases.
- Reworked the Memory Pools view into a responsive all-pools grid with three charts per row, sorted by maximum pool size.
- Removed the memory-pool tab selector and scroll pane so charts fit the available view area.
- Modernized Java code with Java 25 language style, package-level JSpecify `@NullMarked`, and explicit nullable API boundaries.
- Hardened JMX, VisualVM, and Swing boundary handling for absent connections, missing attributes, and missing resources.
- Added public Markdown Javadocs, minimal project documentation, agent instructions, and bundled CDDL-1.0 license files.
- Normalized copyright and CDDL headers across source and text files.
- Removed tracked `.DS_Store` metadata files and ignored future Finder metadata.
