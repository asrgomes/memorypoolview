/*
 * Copyright (c) 2011-2026, Kirk Pepperdine.
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License (the "License").
 * You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the license at http://www.opensource.org/licenses/CDDL-1.0.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information: Portions Copyright [yyyy] [name of copyright owner]
 */

package com.kodewerk.visualvm.memorypoolview.gc;

import org.jspecify.annotations.Nullable;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/// Immutable snapshot of garbage-collection counters.
///
/// The record keeps the original JavaBean-style getters for existing chart
/// code while also exposing record accessors such as `count()`.
public record Collection(
        /// Total number of collections observed.
        long count,
        /// Cumulative collection duration in milliseconds.
        long totalDuration,
        /// Most recent collection duration in milliseconds.
        long lastDuration) {
    /// Creates an empty snapshot with zero counts and durations.
    public Collection() {
        this(0, 0, 0);
    }

    /// Returns the total number of collections observed.
    public long getCount() {
        return count;
    }

    /// Returns the cumulative collection duration in milliseconds.
    public long getTotalDuration() {
        return totalDuration;
    }

    /// Returns the most recent collection duration in milliseconds.
    public long getLastDuration() {
        return lastDuration;
    }

    /// Returns whether all values required for a new sample are present.
    public boolean differFrom(@Nullable Long collectionTime, @Nullable Long collectionCount, @Nullable Long lastDuration) {
        return collectionTime != null && collectionCount != null && lastDuration != null;
    }

    /// Formats a number with a space grouping separator for chart details.
    public static String formatNumber(long number) {
        var formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        var symbols = formatter.getDecimalFormatSymbols();
        symbols.setGroupingSeparator(' ');
        formatter.setDecimalFormatSymbols(symbols);
        return formatter.format(number);
    }

    /// Calculates the midpoint delta between two collection durations.
    public static long calculateDeltaBetween(Collection before, Collection after) {
        return (after.lastDuration - before.lastDuration) / 2;
    }
}
