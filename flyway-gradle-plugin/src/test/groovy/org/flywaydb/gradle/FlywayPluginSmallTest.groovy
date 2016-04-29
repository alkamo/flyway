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

import org.flywaydb.core.Flyway
import org.flywaydb.core.api.callback.FlywayCallback
import org.flywaydb.core.internal.util.jdbc.DriverDataSource
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

class FlywayPluginSmallTest {

    private String defaultUrl = "jdbc:hsqldb:file:/db/flyway_test;shutdown=true"
    private String overrideUrl = "jdbc:hsqldb:file:/db/flyway_test_override;shutdown=true"
    protected Project project;
    /**
     * it gets flyway instance created by project task based on current configuration
     * @return
     */
    private Flyway getFlyway() {
        // task is not relevant for this test, all use the same abstract implementation
        return project.tasks.flywayMigrate.createFlyway()
    }
    /**
     * it gets flyway instance created by project task based on specified configuration
     * @return
     */
    private Flyway getFlyway(String localExtensionName) {
        // task is not relevant for this test, all use the same abstract implementation
        return project.tasks.flywayMigrate.createFlyway(project.flyway.databases[localExtensionName])
    }

    @Before
    public void setup() {
        project = ProjectBuilder.builder().build()
        project.apply plugin: 'org.flywaydb.flyway'
    }

    @Test
    public void checkIfTaskArePresent() {
        assert project.tasks.findByName('flywayClean')
        assert project.tasks.findByName('flywayInfo')
        assert project.tasks.findByName('flywayBaseline')
        assert project.tasks.findByName('flywayMigrate')
        assert project.tasks.findByName('flywayRepair')
        assert project.tasks.findByName('flywayValidate')
    }

    @Test
    public void checkIfRulesArePresent() {
        project.flyway {
            url = defaultUrl
            databases {
                testA {
                }
                testB {
                }
            }
        }

        assert project.tasks.findByName('flywayCleantestA')
        assert project.tasks.findByName('flywayInfotestA')
        assert project.tasks.findByName('flywayBaselinetestA')
        assert project.tasks.findByName('flywayMigratetestA')
        assert project.tasks.findByName('flywayRepairtestA')
        assert project.tasks.findByName('flywayValidatetestA')

        assert project.tasks.findByName('flywayCleantestB')
        assert project.tasks.findByName('flywayInfotestB')
        assert project.tasks.findByName('flywayBaselinetestB')
        assert project.tasks.findByName('flywayMigratetestB')
        assert project.tasks.findByName('flywayRepairtestB')
        assert project.tasks.findByName('flywayValidatetestB')
    }

    @Test
    public void validateBasicExtensionProperties() {
        project.flyway {
            url = defaultUrl
            databases {
                overrides {
                    url = overrideUrl
                }
                defaults {
                }
            }
        }

        DriverDataSource dataSource = getFlyway().getDataSource()

        assert dataSource.getUrl() == defaultUrl

        DriverDataSource overrides = getFlyway('overrides').getDataSource()

        assert overrides.getUrl() == overrideUrl

        DriverDataSource defaults = getFlyway('defaults').getDataSource()

        assert defaults.getUrl() == defaultUrl
    }

    @Test
    public void validateDataSourceWithCustomDriver() {
        String dbUrl = "jdbc:custom:file:/db/flyway_test;shutdown=true"
        String overrideDbUrl = "jdbc:custom:file:/db/flyway_test_override;shutdown=true"
        project.flyway {
            url = dbUrl
            driver = 'org.hsqldb.jdbcDriver'
            databases {
                overrides {
                    url = overrideDbUrl
                }
                defaults {
                }
            }
        }

        DriverDataSource dataSource = getFlyway().getDataSource()

        assert dataSource.getUrl() == dbUrl
        assert dataSource.getDriver() instanceof org.hsqldb.jdbcDriver

        DriverDataSource overrideDataSource = getFlyway('overrides').getDataSource()

        assert overrideDataSource.getUrl() == overrideDbUrl
        //TODO: Figure out how to test overriding the driver
        assert overrideDataSource.getDriver() instanceof org.hsqldb.jdbcDriver

        DriverDataSource defaultDataSource = getFlyway('defaults').getDataSource()

        assert dataSource.getUrl() == dbUrl
        assert dataSource.getDriver() instanceof org.hsqldb.jdbcDriver
    }

    @Test
    public void validateDataSourceWithCredentials() {
        project.flyway {
            url = defaultUrl
            user = 'user'
            password = 'secret'
            databases {
                overrides {
                    url = overrideUrl
                    user = 'user_override'
                    password = 'secret_override'
                }
                defaults {
                }
            }
        }

        DriverDataSource dataSource = getFlyway().getDataSource()

        assert dataSource.getUrl() == defaultUrl
        assert dataSource.getUser() == 'user'
        assert dataSource.getPassword() == 'secret'

        DriverDataSource overrideDataSource = getFlyway('overrides').getDataSource()

        assert overrideDataSource.getUrl() == overrideUrl
        assert overrideDataSource.getUser() == 'user_override'
        assert overrideDataSource.getPassword() == 'secret_override'

        DriverDataSource defaultDataSource = getFlyway('defaults').getDataSource()

        assert defaultDataSource.getUrl() == defaultUrl
        assert defaultDataSource.getUser() == 'user'
        assert defaultDataSource.getPassword() == 'secret'
    }

    @Test
    public void validateExtensionTextProperties() {
        project.flyway {
            url = defaultUrl
            table = 'table'
            baselineDescription = 'baselineDescription'
            sqlMigrationPrefix = 'sqlMigrationPrefix'
            sqlMigrationSeparator = 'sqlMigrationSeparator'
            sqlMigrationSuffix = 'sqlMigrationSuffix'
            encoding = 'encoding'
            placeholderPrefix = 'placeholderPrefix'
            placeholderSuffix = 'placeholderSuffix'
            databases {
                overrides {
                    url = overrideUrl
                    table = 'table_override'
                    baselineDescription = 'baselineDescription_override'
                    sqlMigrationPrefix = 'sqlMigrationPrefix_override'
                    sqlMigrationSeparator = 'sqlMigrationSeparator_override'
                    sqlMigrationSuffix = 'sqlMigrationSuffix_override'
                    encoding = 'encoding_override'
                    placeholderPrefix = 'placeholderPrefix_override'
                    placeholderSuffix = 'placeholderSuffix_override'
                }
                defaults {
                }
            }
        }

        Flyway flyway = getFlyway()
        assert flyway.table == 'table'
        assert flyway.baselineDescription == 'baselineDescription'
        assert flyway.sqlMigrationPrefix == 'sqlMigrationPrefix'
        assert flyway.sqlMigrationSeparator == 'sqlMigrationSeparator'
        assert flyway.sqlMigrationSuffix == 'sqlMigrationSuffix'
        assert flyway.encoding == 'encoding'
        assert flyway.placeholderPrefix == 'placeholderPrefix'
        assert flyway.placeholderSuffix == 'placeholderSuffix'

        Flyway overrides = getFlyway('overrides')
        assert overrides.table == 'table_override'
        assert overrides.baselineDescription == 'baselineDescription_override'
        assert overrides.sqlMigrationPrefix == 'sqlMigrationPrefix_override'
        assert overrides.sqlMigrationSeparator == 'sqlMigrationSeparator_override'
        assert overrides.sqlMigrationSuffix == 'sqlMigrationSuffix_override'
        assert overrides.encoding == 'encoding_override'
        assert overrides.placeholderPrefix == 'placeholderPrefix_override'
        assert overrides.placeholderSuffix == 'placeholderSuffix_override'

        Flyway defaults = getFlyway('defaults')
        assert defaults.table == 'table'
        assert defaults.baselineDescription == 'baselineDescription'
        assert defaults.sqlMigrationPrefix == 'sqlMigrationPrefix'
        assert defaults.sqlMigrationSeparator == 'sqlMigrationSeparator'
        assert defaults.sqlMigrationSuffix == 'sqlMigrationSuffix'
        assert defaults.encoding == 'encoding'
        assert defaults.placeholderPrefix == 'placeholderPrefix'
        assert defaults.placeholderSuffix == 'placeholderSuffix'
    }

    @Test
    public void validateExtensionVersionProperties() {
        // as strings
        project.flyway {
            url = defaultUrl
            baselineVersion = '1.3'
            target = '2.3'
            databases {
                overrides {
                    baselineVersion = '3.3'
                    target = '4.3'
                }
                defaults {
                }
            }
        }

        Flyway flyway = getFlyway()

        assert flyway.baselineVersion.toString() == '1.3'
        assert flyway.target.toString() == '2.3'

        Flyway overrides = getFlyway('overrides')

        assert overrides.baselineVersion.toString() == '3.3'
        assert overrides.target.toString() == '4.3'

        Flyway defaults = getFlyway('defaults')

        assert defaults.baselineVersion.toString() == '1.3'
        assert defaults.target.toString() == '2.3'

        // as numbers
        project.flyway {
            url = defaultUrl
            baselineVersion = 2
            target = 3
        }
        project.flyway.databases['overrides'].baselineVersion = 4
        project.flyway.databases['overrides'].target = 5

        flyway = getFlyway()

        assert flyway.baselineVersion.toString() == '2'
        assert flyway.target.toString() == '3'

        overrides = getFlyway('overrides')

        assert overrides.baselineVersion.toString() == '4'
        assert overrides.target.toString() == '5'

        defaults = getFlyway('defaults')

        assert defaults.baselineVersion.toString() == '2'
        assert defaults.target.toString() == '3'
    }

    @Test
    public void validateExtensionBooleanProperties() {
        project.flyway {
            url = defaultUrl
            outOfOrder = true
            validateOnMigrate = true
            cleanOnValidationError = true
            baselineOnMigrate = true
            placeholderReplacement = true
            databases {
                overrides {
                    url = overrideUrl
                    outOfOrder = false
                    validateOnMigrate = false
                    cleanOnValidationError = false
                    baselineOnMigrate = false
                    placeholderReplacement = false
                }
                defaults {
                }
            }
        }

        Flyway flyway = getFlyway()
        assert flyway.outOfOrder
        assert flyway.validateOnMigrate
        assert flyway.cleanOnValidationError
        assert flyway.baselineOnMigrate
        assert flyway.placeholderReplacement

        Flyway overrides = getFlyway('overrides')
        assert !overrides.outOfOrder
        assert !overrides.validateOnMigrate
        assert !overrides.cleanOnValidationError
        assert !overrides.baselineOnMigrate
        assert !overrides.placeholderReplacement

        Flyway defaults = getFlyway('defaults')
        assert defaults.outOfOrder
        assert defaults.validateOnMigrate
        assert defaults.cleanOnValidationError
        assert defaults.baselineOnMigrate
        assert defaults.placeholderReplacement

        project.flyway {
            url = defaultUrl
            outOfOrder = false
            validateOnMigrate = false
            cleanOnValidationError = false
            baselineOnMigrate = false
            placeholderReplacement = false
        }

        project.flyway.databases['overrides'].url = overrideUrl
        project.flyway.databases['overrides'].outOfOrder = true
        project.flyway.databases['overrides'].validateOnMigrate = true
        project.flyway.databases['overrides'].cleanOnValidationError = true
        project.flyway.databases['overrides'].baselineOnMigrate = true
        project.flyway.databases['overrides'].placeholderReplacement = true

        flyway = getFlyway()
        assert !flyway.outOfOrder
        assert !flyway.validateOnMigrate
        assert !flyway.cleanOnValidationError
        assert !flyway.baselineOnMigrate
        assert !flyway.placeholderReplacement

        overrides = getFlyway('overrides')
        assert overrides.outOfOrder
        assert overrides.validateOnMigrate
        assert overrides.cleanOnValidationError
        assert overrides.baselineOnMigrate
        assert overrides.placeholderReplacement

        defaults = getFlyway('defaults')
        assert !defaults.outOfOrder
        assert !defaults.validateOnMigrate
        assert !defaults.cleanOnValidationError
        assert !defaults.baselineOnMigrate
        assert !defaults.placeholderReplacement
    }

    @Test
    public void validateExtensionListProperties() {
        project.flyway {
            url = defaultUrl
            schemas = ['schemaA', 'schemaB']
            locations = ['classpath:migrations1', 'migrations2', 'filesystem:/sql-migrations']
            placeholders = ['placeholderA': 'A', 'placeholderB': 'B']
            callbacks = ['org.flywaydb.gradle.DefaultFlywayCallback']
            databases {
                overrides {
                    url = defaultUrl
                    schemas = ['schemaZ', 'schemaY']
                    locations = ['classpath:migrations3',
                                 'migrations4',
                                 'filesystem:/sql-migrations-override']
                    placeholders = ['placeholderC': 'C', 'placeholderD': 'D']
                }
                defaults {
                }
            }
        }

        Flyway flyway = getFlyway()
        assert flyway.schemas == ['schemaA', 'schemaB']
        assert flyway.locations == ['classpath:migrations1',
                                    'classpath:migrations2',
                                    'filesystem:/sql-migrations']
        assert flyway.placeholders == ['placeholderA': 'A', 'placeholderB': 'B']
        assert flyway.callbacks[0] instanceof FlywayCallback

        Flyway overrides = getFlyway('overrides')
        assert overrides.schemas == ['schemaZ', 'schemaY']
        assert overrides.locations == ['classpath:migrations3',
                                       'classpath:migrations4',
                                       'filesystem:/sql-migrations-override']
        assert overrides.placeholders == ['placeholderC': 'C', 'placeholderD': 'D']
        assert overrides.callbacks[0] instanceof FlywayCallback

        Flyway defaults = getFlyway('defaults')
        assert defaults.schemas == ['schemaA', 'schemaB']
        assert defaults.locations == ['classpath:migrations1',
                                      'classpath:migrations2',
                                      'filesystem:/sql-migrations']
        assert defaults.placeholders == ['placeholderA': 'A', 'placeholderB': 'B']
        assert defaults.callbacks[0] instanceof FlywayCallback
    }

    @Test
    public void validateExtensionListPropertiesMerge() {
        project.flyway {
            url = defaultUrl
            schemas = ['schemaA', 'schemaB']
            locations = ['classpath:migrations1', 'migrations2', 'filesystem:/sql-migrations']
            placeholders = ['placeholderA': 'A', 'placeholderB': 'B']
            callbacks = ['org.flywaydb.gradle.DefaultFlywayCallback']
            schemasInstruction = ListPropertyInstruction.MERGE
            locationsInstruction = ListPropertyInstruction.MERGE
            placeholdersInstruction = ListPropertyInstruction.MERGE
            databases {
                merges {
                    url = defaultUrl
                    schemas = ['schemaZ', 'schemaY']
                    locations = ['classpath:migrations3',
                                 'migrations4',
                                 'filesystem:/sql-migrations-merge']
                    placeholders = ['placeholderC': 'C', 'placeholderD': 'D']
                }
            }
        }

        Flyway merges = getFlyway('merges')
        assert merges.schemas == ['schemaZ', 'schemaY',
                                  'schemaA', 'schemaB']
        assert merges.locations == ['classpath:migrations1',
                                    'classpath:migrations2',
                                    'classpath:migrations3',
                                    'classpath:migrations4',
                                    'filesystem:/sql-migrations',
                                    'filesystem:/sql-migrations-merge']
        assert merges.placeholders == ['placeholderA': 'A', 'placeholderB': 'B',
                                       'placeholderC': 'C', 'placeholderD': 'D']
        assert merges.callbacks[0] instanceof FlywayCallback
    }

}