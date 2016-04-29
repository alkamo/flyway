package org.flywaydb.gradle.rule

import org.flywaydb.gradle.task.AbstractFlywayTask
import org.flywaydb.gradle.task.FlywayRepairTask
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer

public class FlywayRepairRule extends AbstractFlywayRule {

    @Override
    Class<AbstractFlywayTask> getBaseClass() {
        return FlywayRepairTask.class
    };

    public FlywayRepairRule(Project project, ExtensionContainer extensions) {
        super(project, extensions);
    }
}

