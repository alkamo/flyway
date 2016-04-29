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
package org.flywaydb.gradle.task

import org.flywaydb.core.Flyway
import org.flywaydb.core.api.FlywayException
import org.flywaydb.core.internal.util.Location
import org.flywaydb.core.internal.util.StringUtils
import org.flywaydb.core.internal.util.jdbc.DriverDataSource
import org.flywaydb.gradle.FlywayExtensionBase
import org.flywaydb.gradle.FlywayExtension
import org.flywaydb.gradle.FlywayContainer
import org.flywaydb.gradle.ListPropertyInstruction
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * A base class for all flyway tasks.
 */
abstract class AbstractFlywayTask extends DefaultTask {
    /**
     * Property name prefix for placeholders that are configured through System properties.
     */
    private static final String PLACEHOLDERS_PROPERTY_PREFIX = "flyway.placeholders."

    /**
     * The flyway {} block in the build script.
     */
    protected FlywayExtension masterExtension

    private FlywayContainer currentExtension

    AbstractFlywayTask() {
        group = 'Flyway'
        masterExtension = project.flyway
    }

    void setCurrentExtension (FlywayContainer extension){
        this.currentExtension = extension
    }

    @TaskAction
    def runTask() {
        if (isJavaProject()) {
            def classLoader = Thread.currentThread().getContextClassLoader()
            project.sourceSets.each {
                def classesUrl = it.output.classesDir.toURI().toURL()
                logger.debug("Adding directory to Classpath: " + classesUrl)
                classLoader.addURL(classesUrl)

                def resourcesUrl = it.output.resourcesDir.toURI().toURL()
                logger.debug("Adding directory to Classpath: " + resourcesUrl)
                classLoader.addURL(resourcesUrl)
            }
            project.configurations.getByName('testRuntime').resolvedConfiguration.resolvedArtifacts.each { artifact ->
                def artifactUrl = artifact.file.toURI().toURL()
                logger.debug("Adding Dependency to Classpath: " + artifactUrl)
                classLoader.addURL(artifactUrl)
            }
        }

        if (project.flyway.databases.size() == 0) {
            try {
                run(createFlyway())
            } catch (Exception e) {
                handleException(e)
            }
        } else if (null != currentExtension) {
            try {
                run(currentExtension.getName(), createFlyway(currentExtension))
            } catch (Exception e) {
                handleException(e)
            }
        } else{
            project.flyway.databases.each { flywayLocal ->
                logger.info "Executing ${this.getName()} for ${flywayLocal.name}"
                try {
                    run(flywayLocal.name, createFlyway(flywayLocal))
                } catch (Exception e) {
                    throw new FlywayException(
                            "Error occurred while executing ${this.getName()} for ${flywayLocal.name}", e);
                }
            }
        }
    }

    /** Executes the task's custom behavior. */
    def abstract run(Flyway flyway)

    /** Creates a new, configured flyway instance */
    protected def createFlyway() {
        createFlyway(project.flyway)
    }

    /** Creates a new, configured flyway instance for a specific extension
     * @param localExtension The extension that is currently being processed.
     */
    protected def createFlyway(FlywayExtensionBase localExtension) {
        def flyway = new Flyway()
        flyway.setDataSource(new DriverDataSource(
                Thread.currentThread().getContextClassLoader(),
                prop("driver", localExtension),
                prop("url", localExtension),
                prop("user", localExtension),
                prop("password", localExtension)))

        propSet(flyway, 'table', localExtension)

        String baselineVersion = prop('baselineVersion', localExtension)
        if (baselineVersion != null) {
            flyway.setBaselineVersionAsString(baselineVersion)
        }

        propSet(flyway, 'baselineDescription', localExtension)
        propSet(flyway, 'sqlMigrationPrefix', localExtension)
        propSet(flyway, 'repeatableSqlMigrationPrefix', localExtension)
        propSet(flyway, 'sqlMigrationSeparator', localExtension)
        propSet(flyway, 'sqlMigrationSuffix', localExtension)
        propSet(flyway, 'encoding', localExtension)
        propSetAsBoolean(flyway, 'placeholderReplacement', localExtension)
        propSet(flyway, 'placeholderPrefix', localExtension)
        propSet(flyway, 'placeholderSuffix', localExtension)

        String target = prop('target', localExtension)
        if (target != null) {
            flyway.setTargetAsString(target)
        }

        propSetAsBoolean(flyway, 'outOfOrder', localExtension)
        propSetAsBoolean(flyway, 'validateOnMigrate', localExtension)
        propSetAsBoolean(flyway, 'cleanOnValidationError', localExtension)
        propSetAsBoolean(flyway, 'ignoreFutureMigrations', localExtension)
        propSetAsBoolean(flyway, 'cleanDisabled', localExtension)
        propSetAsBoolean(flyway, 'baselineOnMigrate', localExtension)
        propSetAsBoolean(flyway, 'skipDefaultResolvers', localExtension)
        propSetAsBoolean(flyway, 'skipDefaultCallbacks', localExtension)

        propSetAsList(flyway, 'schemas', masterExtension.schemasInstruction, localExtension)

        flyway.setLocations(Location.FILESYSTEM_PREFIX + project.projectDir + '/src/main/resources/db/migration')
        propSetAsList(flyway, 'locations', masterExtension.locationsInstruction, localExtension)

        propSetAsList(flyway, 'resolvers', masterExtension.resolversInstruction, localExtension, true)
        propSetAsList(flyway, 'callbacks', masterExtension.callbacksInstruction, localExtension, true)

        propSetAsMap(
                flyway,
                'placeholders',
                masterExtension.placeholdersInstruction,
                PLACEHOLDERS_PROPERTY_PREFIX,
                localExtension)

        flyway
    }

    /**
     * @param throwable Throwable instance to be handled
     */
    private void handleException(Throwable throwable) {
        String message = "Error occurred while executing ${this.getName()}"
        throw new FlywayException(collectMessages(throwable, message), throwable)
    }

    /**
     * Collect error messages from the stack trace
     * @param throwable Throwable instance from which the message should be build
     * @param message the message to which the error message will be appended
     * @param depth number of levels in the stack trace
     * @return a String containing the composed messages
     */
    private String collectMessages(Throwable throwable, String message) {
        if (throwable != null) {
            message += "\n" + throwable.getMessage()
            collectMessages(throwable.getCause(), message)
        } else {
            message
        }
    }

    /**
     * Sets this property on this Flyway instance if a value has been defined.
     * @param flyway The Flyway instance.
     * @param property The property to set.
     * @param localExtension The extension that is currently being processed.
     */
    private void propSet(Flyway flyway, String property, FlywayExtensionBase localExtension) {
        String value = prop(property, localExtension);
        if (value != null) {
            // use method call instead of property as it does not work nice with overload GROOVY-6084
            flyway."set${property.capitalize()}"(value)
        }
    }
    /**
     * Sets this property on this Flyway instance if a value has been defined.
     * @param flyway The Flyway instance.
     * @param property The property to set.
     * @param localExtension The extension that is currently being processed.
     */
    private void propSetAsBoolean(Flyway flyway, String property, FlywayExtensionBase localExtension) {
        String value = prop(property, localExtension);
        if (value != null) {
            flyway."set${property.capitalize()}"(value.toBoolean())
        }
    }
    /**
     * Sets this property on this Flyway instance if a value has been defined.
     * @param flyway The Flyway instance.
     * @param property The property to set.
     * @param instruction The instruction on whether to prioritize or merge conflicting lists.
     * @param localExtension The extension that is currently being processed.
     * @param asClassNames Indicator of whether the call to Flyway uses set<property>AsClassName.
     */
    private void propSetAsList(
            Flyway flyway,
            String property,
            ListPropertyInstruction instruction,
            FlywayExtensionBase localExtension,
            Boolean asClassNames = false) {
        String propertyName = "flyway.${property}"
        def sysProperty = System.getProperty(propertyName)
        List<String> propertyValues = []
        String methodName = "set${property.capitalize()}"
        if (asClassNames) {
            methodName += "AsClassNames"
        }
        if (sysProperty != null) {
            mergeList(propertyValues, StringUtils.tokenizeToStringArray(sysProperty, ","))
        }
        if ((propertyValues.size() == 0
                ||  instruction == ListPropertyInstruction.MERGE)
                && project.hasProperty(propertyName)) {
            mergeList(propertyValues, StringUtils.tokenizeToStringArray(project[propertyName].toString(), ","))
        }
        if ((propertyValues.size() == 0
                || instruction == ListPropertyInstruction.MERGE)
                && (localExtension."${property}" != null)) {
            mergeList(propertyValues, localExtension."${property}")
        }
        if ((propertyValues.size() == 0
                ||  instruction == ListPropertyInstruction.MERGE)
                && (masterExtension."${property}" != null)) {
            mergeList(propertyValues, masterExtension."${property}")
        }
        if (propertyValues.size() > 0) {
            flyway."${methodName}"(propertyValues.toArray(new String[0]))
        }
    }

    /**
     * Sets this property on this Flyway instance if a value has been defined.
     * @param flyway The Flyway instance.
     * @param property The property to set.
     * @param instruction The instruction on whether to prioritize or merge conflicting lists.
     * @param localExtension The extension that is currently being processed.
     */
    private void propSetAsMap(
            Flyway flyway,
            String property,
            ListPropertyInstruction instruction,
            String prefix,
            FlywayExtensionBase localExtension) {
        Map<String, String> propertyValues = [:]
        System.getProperties().each { String key, String value ->
            if (key.startsWith(prefix)) {
                propertyValues.put(key.substring(prefix.length()), value)
            }
        }
        if (propertyValues.isEmpty() ||  instruction == ListPropertyInstruction.MERGE) {
            Map<String, String> projectValues = [:]
            project.properties.keySet().each { String key ->
                if (key.startsWith(prefix)) {
                    projectValues.put(key.substring(prefix.length()), project.properties[key])
                }
            }
            mergeMap(propertyValues, projectValues)
        }
        if ((propertyValues.isEmpty() ||  instruction == ListPropertyInstruction.MERGE)
                && localExtension."${property}" != null) {
            mergeMap(propertyValues, localExtension."${property}")
        }
        if ((propertyValues.isEmpty() ||  instruction == ListPropertyInstruction.MERGE)
                && masterExtension."${property}" != null) {
            mergeMap(propertyValues, masterExtension."${property}")
        }
        if (propertyValues.size() > 0) {
            flyway."set${property.capitalize()}"(propertyValues)
        }
    }

    private void mergeList(
            List<?> masterList,
            String[] arrayToMerge) {

        arrayToMerge.each { listValue ->
            if (!(listValue in masterList)) {
                masterList.add(listValue)
            }
        }
    }

    private void mergeMap(
            Map<String, String> masterMap,
            Map<String, String> mapToMerge) {

        mapToMerge.each { key, value ->
            if (!(key in masterMap.keySet())) {
                masterMap[key] = value
            }
        }
    }



    /**
     * Retrieves the value of this property, first trying System Properties, then Gradle properties,
     * then the passed Flyway extension and finally the main Flyway properties.
     * @param property The property whose value to get.
     * @param localExtension The extension that is currently being processed.
     * @return The value. {@code null} if not found.
     */
    private String prop(String property, FlywayExtensionBase localExtension) {
        String propertyName = "flyway.${property}"
        System.getProperty(propertyName) ?:
                project.hasProperty(propertyName) ?
                        project[propertyName] :
                        localExtension[property] != null ?
                                localExtension[property] :
                                masterExtension[property]
    }

    protected boolean isJavaProject() {
        project.plugins.hasPlugin('java')
    }
}
