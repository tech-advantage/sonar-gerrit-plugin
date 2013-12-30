Gerrit Plugin for SonarQube™
============================

This plugin reports SonarQube™ issues on your patchsets to your Gerrit server. SonarQube™ analyses full project, but only files included in patchset are commented on Gerrit

Currently plugin always reports +1 for Code Review, as it's still in development. However, you should always treat these comments as hints to improve, not as direct errors.

I recommend you to try this plugin in conjunction with our other plugin: [Sonar File Alerts Plugin](https://github.com/TouK/sonar-file-alerts-plugin). It alters SonarQube™'s behaviour so alerts can be raised on a file level, not only project level.

Requirements
------------

- Gerrit 2.8 is required (REST API for reviews was introduced in this version)
- SonarQube™ 4.0 (plugin is build against 4.0's API)

*Note: you should be able to build against SonarQube™'s 3.x API with ease, but it won't be supported. Just change version in pom.xml and fix small code issues.*

Installation
------------

There is a build package available here: [sonar-gerrit-plugin-1.0.jar](https://github.com/TouK/sonar-gerrit-plugin/releases/download/sonar-gerrit-plugin-1.0/sonar-gerrit-plugin-1.0.jar).

Or you can build it for yourself. Clone this repository, package it and put a package to your sonar plugins directory.

```bash
mvn package
cp target/sonar-gerrit-plugin-1.0.jar $SONAR_DIR/plugins
$SONAR_DIR/bin/your-architecture-here/sonar.sh restart
```

Configure Jenkins
-----------------

This plugin is intended to use with Gerrit Trigger plugin: https://wiki.jenkins-ci.org/display/JENKINS/Gerrit+Trigger on a Jenkins server.

You need to create a Gerrit user with a HTTP password for him. Then add this user to Non-Interactive Users group.

Then you need to set up Sonar plugin in Jenkins. Log in as admin, Manage Jenkins - Configure System - Sonar - Advanced... - Additional properties: add and adjust your settings:

```
-DGERRIT_HTTP_PORT=8080 -DGERRIT_HTTP_USERNAME=sonar -DGERRIT_HTTP_PASSWORD=sonar_password
```

Last step is to add Post-Build action - SonarQube™ to every Jenkin's job you want to.

How does it work?
-----------------

As SonarQube™ analysis starts, plugin connects to Gerrit and asks what files were changed in a patchset. Sonar iterates on every file and if file is contained in a patchset, plugin collects it's violations and alerts.

When analysis is finished, plugin connects to Gerrit again with collected results.

License
-------

This project is licenced under Apache License.

