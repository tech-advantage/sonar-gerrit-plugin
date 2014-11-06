This project is based on original source on https://github.com/TouK/ which was abandonned.


Gerrit Plugin for SonarQube™
============================

This plugin reports SonarQube™ issues on your patchsets to your Gerrit server. SonarQube™ analyses full project, but only files included in patchset are commented on Gerrit
Plugin reports -1 or +1 for Code-Review (default) based on severity threshold (default:INFO).

Requirements
------------

- Gerrit 2.8 is required (REST API for reviews was introduced in this version)
- SonarQube™ 4.0 (plugin is build against 4.0's API)

*Note: you should be able to build against SonarQube™'s 3.x API with ease, but it won't be supported. Just change version in pom.xml and fix small code issues.*

Installation
------------

There is a build package available here: [sonar-gerrit-plugin-2.0-rc1.jar](https://github.com/tech-advantage/sonar-gerrit-plugin/releases/download/sonar-gerrit-plugin-2.0/sonar-gerrit-plugin-2.0-rc1.jar).
Or you can build it for yourself. Clone this repository, package it and put a package to your sonar plugins directory.

```bash
mvn clean package
cp target/sonar-gerrit-plugin-2.0.jar $SONAR_DIR/extentions/plugins
$SONAR_DIR/bin/your-architecture-here/sonar.sh restart
```

Configure Jenkins
-----------------

This plugin is intended to use with Gerrit Trigger plugin: https://wiki.jenkins-ci.org/display/JENKINS/Gerrit+Trigger on a Jenkins server.
Then you should set up Sonar plugin in Jenkins. Log in as admin, Manage Jenkins - Configure System - Sonar - Advanced… - Additional properties: add and adjust your settings:

```
-DGERRIT_SCHEME=http -DGERRIT_HTTP_PORT=8080 -DGERRIT_HTTP_USERNAME=sonar -DGERRIT_HTTP_PASSWORD=sonar_password
```

Last step is to add Post-Build action - SonarQube™ to every Jenkin's job you want to.

Configure Gerrit
----------------

You need to be able to connect to Gerrit with a valid username and password through HTTP or HTTPS. User can be part of the Non-Interactive Users group.
Plugin vote on Code-Review label. To vote on another label, add a new label on Gerrit and change the plugin settings

```
[label "Quality-Check"]
    function = MaxWithBlock
    value = -1 Issues to be corrected
    value =  0 No score
    value = +1 Code is clean
```

How does it work?
-----------------

As SonarQube™ analysis starts, plugin connects to Gerrit and asks what files were changed in a patchset. Sonar iterates on every file and if file is contained in a patchset, plugin collects it's violations and alerts.
When analysis is finished, plugin connects to Gerrit again and publish violations and alerts as patchset comments.
Depending on the threshold, plugin votes -1/+1 so submit can be blocked based on your Gerrit settings.

License
-------

This project is licenced under Apache License.

