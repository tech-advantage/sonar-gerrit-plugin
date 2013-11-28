Sonar Gerrit Plugin
===================

This plugins reports Sonar violations to your Gerrit server. Sonar analyse full project, but only changed files are commented on Gerrit.

Currently plugin always reports +1 for Code Review, as it's still in development. However, you should always treat these comments as hints to improve, not as direct errors.

Installation
------------

There are no packages yet. Clone this repository, package it and put a package to your sonar plugins directory.

```bash
mvn package
cp target/sonar-gerrit-plugin-1.0.jar $SONAR_DIR/plugins
$SONAR_DIR/bin/your-architecture-here/sonar.sh restart
```

Configure Jenkins
-----------------

This plugin is intended to use with Gerrit Trigger plugin: https://wiki.jenkins-ci.org/display/JENKINS/Gerrit+Trigger.

You need to create a Gerrit user with a HTTP password for him. Then add this user to Non-Interactive Users group.

Then you need to set up Sonar plugin in Jenkins. Log in as admin, Manage Jenkins - Configure System - Sonar - Advanced... - Additional properties: add and andjust your settings:

```
-DGERRIT_HTTP_PORT=8080 -DGERRIT_HTTP_USERNAME=sonar -DGERRIT_HTTP_PASSWORD=sonar_password
```

Last step is to add Post-Build action - Sonar to every job you want to.

License
-------

This project is licenced under Apache License.

