/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.nifi.bootstrap.command;

/**
 * Enumeration of Bootstrap Command Statuses with status codes
 */
public enum CommandStatus {
    RUNNING(-1),

    SUCCESS(0),

    ERROR(1),

    STOPPED(3),

    COMMUNICATION_FAILED(4),

    FAILED(5);

    private final int status;

    CommandStatus(final int status) {
        this.status = status;
    }

    /**
     * Get Status Code for use with System.exit()
     *
     * @return Status Code
     */
    public int getStatus() {
        return status;
    }
}