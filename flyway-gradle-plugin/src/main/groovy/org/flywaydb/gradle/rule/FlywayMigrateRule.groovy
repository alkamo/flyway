package org.flywaydb.gradle.rule

import org.flywaydb.gradle.FlywayContainer
import org.flywaydb.gradle.task.AbstractFlywayTask
import org.flywaydb.gradle.task.FlywayMigrateTask
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer

public class FlywayMigrateRule extends AbstractFlywayRule {

    @Override
    Class<AbstractFlywayTask> getBaseClass() {
        return FlywayMigrateTask.class
    };

    public FlywayMigrateRule(Project project, ExtensionContainer extensions) {
        super(project, extensions);
    }
}

