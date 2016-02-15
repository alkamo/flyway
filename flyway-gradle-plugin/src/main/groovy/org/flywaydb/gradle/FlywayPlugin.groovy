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
package org.flywaydb.gradle

import org.flywaydb.gradle.task.FlywayCleanTask;
import org.flywaydb.gradle.task.FlywayInfoTask;
import org.flywaydb.gradle.task.FlywayBaselineTask;
import org.flywaydb.gradle.task.FlywayMigrateTask;
import org.flywaydb.gradle.task.FlywayRepairTask;
import org.flywaydb.gradle.task.FlywayValidateTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Rule;
import org.gradle.api.Task;

/**
 * Registers the plugin's tasks.
 *
 * @author Ben Manes (ben.manes@gmail.com)
 */

public class FlywayPlugin implements Plugin<Project> {
    public void apply(Project project) {
        project.extensions.create("flyway", FlywayExtension.class);
        project.flyway.extensions.databases = project.container(FlywayContainer);
        project.getTasks().create("flywayClean", FlywayCleanTask.class);
        project.getTasks().create("flywayBaseline", FlywayBaselineTask.class);
        project.getTasks().create("flywayMigrate", FlywayMigrateTask.class);
        project.getTasks().create("flywayValidate", FlywayValidateTask.class);
        project.getTasks().create("flywayInfo", FlywayInfoTask.class);
        project.getTasks().create("flywayRepair", FlywayRepairTask.class);
    }

}

