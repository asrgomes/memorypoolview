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

# Memory Pool View

VisualVM plugin that adds memory-pool and garbage-collector charts for a connected JVM.

## Requirements

- JDK 25
- Maven 3.9.15 or newer
- VisualVM 2.2.1 or another VisualVM release that provides the same API baseline

## Build

```sh
mvn -DskipTests clean verify
```

The plugin package is generated at:

```text
target/nbm/memorypoolview-2.0.0.nbm
```

## License

CDDL 1.0. See `LICENSE.txt`.
