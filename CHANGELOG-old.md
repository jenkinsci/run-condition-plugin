## Change log

#### GitHub releases

Newer versions are published on GitHub releases

#### 1.0 (10/5/2013)

-   fix [JENKINS-14438](https://issues.jenkins-ci.org/browse/JENKINS-14438)
    Build cause condition fails on matrix projects
-   fix
    [JENKINS-14584](https://issues.jenkins-ci.org/browse/JENKINS-14584)
    add more information about what a condition is evaluating (there
    might be more conditions which need some more logging)
-   implement
    [JENKINS-19300](https://issues.jenkins-ci.org/browse/JENKINS-19300)
    Allow users to run conditional build steps if the build is running
    on a specific node

#### 0.10 (25/5/2012)

-   fix
    [JENKINS-13853](https://issues.jenkins-ci.org/browse/JENKINS-13853)
    Conditional step is not being executed by UserCause when Jenkins \>
    1.427

#### 0.9 (1/5/2012)

-   new condition: Cause Condition
-   new condition: Shell Condition
-   new Condition: WindowsBatch Condition

#### 0.7 (14/01/2012)

-   Fixed
    [JENKINS-12414](https://issues.jenkins-ci.org/browse/JENKINS-12414).
    Now if no result has been set, the "Current status" is SUCCESS
    ("Current build status" run condition)
-   [JENKINS-12411](https://issues.jenkins-ci.org/browse/JENKINS-12411)
    Expand environment and build variables directly wherever tokens can
    be used

#### 0.6 (15/11/2011)

-   Add a Strings match condition to test if two strings are the same
-   Add a Time condition to only run during a particular time of the day
-   Add a Day of week condition to only run on certain days

#### 0.5 (13/11/2011)

-   Links in help files open in a new window

#### 0.4 (10/11/2011)

-   Add File exists and Files match to the core conditions

#### 0.3 (09/11/2011)

-   Add build step runners that enable the user to choose what happens
    if something goes wrong when evaluating a condition  
    Problems conditions may have when evaluationg - a regular expression
    is not valid, token expansion fails, expanded token expected to be a
    number, there is no spoon
-   Add Boolean condition to the core run conditions

#### 0.2 (08/11/2011)

-   Add Numerical comparison to the core run conditions

#### 0.1 (07/11/2011)

-   Initial release

Questions, Comments, Bugs and Feature Requests

Please post questions or comments about this plugin to the [Jenkins User
mailing list](http://jenkins-ci.org/content/mailing-lists).  
To report a bug or request an enhancement to this plugin please [create
a ticket in
JIRA](http://issues.jenkins-ci.org/browse/JENKINS/component/16129).
