/*
 * Copyright (c) 2015, 2017, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package test.java.lang.invoke.lib;

// Android-added: import of Function for filterException
import java.util.function.Function;

/**
 * Helper class used to catch and process VirtualMachineError with message "Out
 * of space in CodeCache". Some JSR292 tests run out of code cache size, so code
 * cache overflows and VME is thrown. This VME is considered as non-critical in
 * some JSR292 tests, so it should be processed to prevent test failure.
 */
public class CodeCacheOverflowProcessor {

    /**
     * Checks if an instance of Throwable is caused by VirtualMachineError with
     * message "Out of space in CodeCache". May be used as filter in method
     * {@code jdk.testlibrary.Utils.filterException}.
     *
     * @param t - Throwable to check.
     * @return true if Throwable is caused by VME, false otherwise.
     */
    public static Boolean isThrowableCausedByVME(Throwable t) {
        Throwable causeOfT = t;
        do {
            if (causeOfT instanceof VirtualMachineError
                    && causeOfT.getMessage().matches(".*[Oo]ut of space"
                            + " in CodeCache.*")) {
                return true;
            }
            causeOfT = causeOfT != null ? causeOfT.getCause() : null;
        } while (causeOfT != null && causeOfT != t);
        return false;
    }

    /**
     * Checks if the given test throws an exception caused by
     * VirtualMachineError with message "Out of space in CodeCache", and, if VME
     * takes place, processes it so that no exception is thrown, and prints its
     * stack trace. If test throws exception not caused by VME, this method just
     * re-throws this exception.
     *
     * @param test - test to check for and process VirtualMachineError.
     * @return - an exception caused by VME or null
     *           if test has thrown no exception.
     * @throws Throwable - if test has thrown an exception
     *                     that is not caused by VME.
     */
    public static Throwable runMHTest(ThrowingRunnable test) throws Throwable {
        Throwable t = filterException(test::run,
                CodeCacheOverflowProcessor::isThrowableCausedByVME);
        if (t != null) {
            System.err.printf("%nNon-critical exception caught becuse of"
                    + " code cache size is not enough to run all test cases.%n%n");
        }
        return t;
    }

    // BEGIN Android-changed: these interfaces methods taken from jdk.testlibrary
    /**
     * Interface same as java.lang.Runnable but with
     * method {@code run()} able to throw any Throwable.
     */
    public static interface ThrowingRunnable {
        void run() throws Throwable;
    }

    /**
     * Filters out an exception that may be thrown by the given
     * test according to the given filter.
     *
     * @param test - method that is invoked and checked for exception.
     * @param filter - function that checks if the thrown exception matches
     *                 criteria given in the filter's implementation.
     * @return - exception that matches the filter if it has been thrown or
     *           {@code null} otherwise.
     * @throws Throwable - if test has thrown an exception that does not
     *                     match the filter.
     */
    public static Throwable filterException(ThrowingRunnable test,
                                            Function<Throwable, Boolean> filter) throws Throwable {
        try {
            test.run();
        } catch (Throwable t) {
            if (filter.apply(t)) {
                return t;
            } else {
                throw t;
            }
        }
        return null;
    }
    // END Android-changed: these interfaces methods taken from jdk.testlibrary
}
