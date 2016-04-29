package org.flywaydb.gradle.rule

import org.flywaydb.gradle.task.AbstractFlywayTask
import org.flywaydb.gradle.task.FlywayBaselineTask
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer

public class FlywayBaselineRule extends AbstractFlywayRule {

    @Override
    Class<AbstractFlywayTask> getBaseClass() {
        return FlywayBaselineTask.class
    };

    public FlywayBaselineRule(Project project, ExtensionContainer extensions) {
        super(project, extensions);
    }
}

