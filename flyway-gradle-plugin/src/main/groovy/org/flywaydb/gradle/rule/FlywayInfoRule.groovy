package org.flywaydb.gradle.rule;

import org.flywaydb.gradle.FlywayContainer;
import org.flywaydb.gradle.task.AbstractFlywayTask;
import org.flywaydb.gradle.task.FlywayInfoTask
import org.flywaydb.gradle.task.FlywayMigrateTask;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionContainer;


public class FlywayInfoRule extends AbstractFlywayRule {

    @Override
    Class<AbstractFlywayTask> getBaseClass() {
        return FlywayInfoTask.class
    };

    public FlywayInfoRule(Project project, ExtensionContainer extensions) {
        super(project, extensions);
    }
}

