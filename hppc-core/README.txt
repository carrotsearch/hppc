
HPPC, high performance primitive collections
--------------------------------------------

See JavaDoc and http://labs.carrotsearch.com/hppc.html for more info


Development
-----------

mvn clean                Clean the working copy.
mvn install -DskipTests  Quick compile, no tests.
mvn -Pbenchmark          Include a benchmark round in the build.
                         Results in: target/benchmarks
mvn site                 Generate reports: pmd, cpd, findbugs.
                         Results in: target/site
mvn -Pclover             Code coverage reports [requires -Dclover.license or global setting]
                         Results in: target/clover/

Release
-------

mvn clean deploy         Snapshot deploy to sonatype [requires proper setup in settings.xml]

mvn -Prelease clean deploy
                         Push snapshot or release artefacts to SonaType.

mvn -Psite-labs          Creates a release directory for rsyncing to labs.carrotsearch.com
                         Results in: target/site-labs

                         rsync -azv -e "ssh -p 2222" --chmod=u=rwX,g=rX,o=rX \
                           target/site-labs/     \
                           carrot2@hostgator.carrot2.org:./public_html/com.carrotsearch.labs/download/hppc/


Clover
------

A local license is required for Clover support. Edit your ~/.m2/settings.xml and 
add an active profile definiting these settings:

...
  <profiles>
          <profile>
            <id>clover-license</id>
            <properties>
              <clover.license.path>[...]\clover.license</clover.license.path>
              <maven.clover.licenseLocation>[...]\clover.license</maven.clover.licenseLocation>
            </properties>
          </profile>
  </profiles>

  <activeProfiles>
    <activeProfile>clover-license</activeProfile>
  </activeProfiles>
...
