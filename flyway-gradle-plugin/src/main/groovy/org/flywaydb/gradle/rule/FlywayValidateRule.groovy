package org.flywaydb.gradle.rule

import org.flywaydb.gradle.task.AbstractFlywayTask
import org.flywaydb.gradle.task.FlywayValidateTask
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer

public class FlywayValidateRule extends AbstractFlywayRule {

    @Override
    Class<AbstractFlywayTask> getBaseClass() {
        return FlywayValidateTask.class
    };

    public FlywayValidateRule(Project project, ExtensionContainer extensions) {
        super(project, extensions);
    }
}

