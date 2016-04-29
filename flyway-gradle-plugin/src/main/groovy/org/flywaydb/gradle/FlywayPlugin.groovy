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

import org.flywaydb.gradle.rule.FlywayBaselineRule
import org.flywaydb.gradle.rule.FlywayCleanRule
import org.flywaydb.gradle.rule.FlywayInfoRule
import org.flywaydb.gradle.rule.FlywayMigrateRule
import org.flywaydb.gradle.rule.FlywayRepairRule
import org.flywaydb.gradle.rule.FlywayValidateRule;
import org.flywaydb.gradle.task.FlywayCleanTask;
import org.flywaydb.gradle.task.FlywayInfoTask;
import org.flywaydb.gradle.task.FlywayBaselineTask;
import org.flywaydb.gradle.task.FlywayMigrateTask;
import org.flywaydb.gradle.task.FlywayRepairTask;
import org.flywaydb.gradle.task.FlywayValidateTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * Registers the plugin's tasks.
 *
 * @author Ben Manes (ben.manes@gmail.com)
 */

public class FlywayPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getExtensions().create("flyway", FlywayExtension.class);
        project.flyway.extensions.databases = project.container(FlywayContainer.class);
        project.getTasks().create(FlywayCleanTask.baseName, FlywayCleanTask.class);
        project.getTasks().create(FlywayBaselineTask.baseName, FlywayBaselineTask.class);
        project.getTasks().create(FlywayMigrateTask.baseName, FlywayMigrateTask.class);
        project.getTasks().create(FlywayValidateTask.baseName, FlywayValidateTask.class);
        project.getTasks().create(FlywayInfoTask.baseName, FlywayInfoTask.class);
        project.getTasks().create(FlywayRepairTask.baseName, FlywayRepairTask.class);

        project.tasks.addRule(new FlywayCleanRule(project, project.extensions));
        project.tasks.addRule(new FlywayBaselineRule(project, project.extensions));
        project.tasks.addRule(new FlywayMigrateRule(project, project.extensions));
        project.tasks.addRule(new FlywayValidateRule(project, project.extensions));
        project.tasks.addRule(new FlywayInfoRule(project, project.extensions));
        project.tasks.addRule(new FlywayRepairRule(project, project.extensions));
    }
}



