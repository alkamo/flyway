/**
 * Copyright 2010-2016 Boxfuse GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flywaydb.gradle.rule;

import org.flywaydb.gradle.FlywayContainer
import org.flywaydb.gradle.FlywayExtension;
import org.flywaydb.gradle.task.AbstractFlywayTask;
import org.gradle.api.Rule;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionContainer

abstract class AbstractFlywayRule implements Rule {
    protected final ExtensionContainer extensions;
    protected final Project project;

    public AbstractFlywayRule(Project project, ExtensionContainer extensions) {
        this.extensions = extensions;
        this.project = project;
    }

    abstract Class<AbstractFlywayTask> getBaseClass()

    @Override
    public String toString() {
        return String.format("Pattern: %s<ConfigurationName>: %s", getPrefix(), getInternalDescription());
    }

    public String getInternalDescription() {
        return getBaseClass().getDeclaredField("baseDescription").get(null)
    }

    protected String getPrefix() {
        return getBaseClass().getDeclaredField("baseName").get(null)
    }

    public void apply(String taskName) {
        if (taskName.startsWith(getPrefix())) {

            String dbName = taskName.substring(getPrefix().length());

            FlywayExtension flywayExtension = project.getExtensions().findByType(FlywayExtension.class)

            FlywayContainer subExtension = flywayExtension.databases[dbName]

            if (subExtension != null) {
                createTask(taskName, subExtension);
            }
        }
    }

    AbstractFlywayTask createTask(String name, final FlywayContainer extension) {
        AbstractFlywayTask task = project.getTasks().create(name, getBaseClass());
        task.setDescription(String.format("${getInternalDescription()} belonging to ${extension.getName()}."));
        task.setGroup("Flyway");
        task.setCurrentExtension(extension);
        return task;
    }

    public String getDescription() {
        return this.toString();
    }
}