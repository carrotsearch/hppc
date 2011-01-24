
HPPC, high performance primitive collections
--------------------------------------------

See JavaDoc and http://labs.carrotsearch.com/hppc.html for more info


Development
-----------

mvn clean                Clean the working copy.
mvn install -DskipTests  Quick compile, no tests.
mvn -Pbenchmark          Include a benchmark round in the build.
                         Results in: target/benchmarks
mvn -Prelease            Release mode (javadocs, sources, zip/tgz bundles).
                         Results in: target/*.tgz
mvn -Prelease,sonatype   Release mode, sign and prepare upload release bundle.
                         Results in: target/release-bundle.jar
mvn site                 Generate reports: pmd, cpd, findbugs.
                         Results in: target/site
mvn -Pclover             Code coverage reports [requires -Dclover.license or global setting]
                         Results in: target/clover/

Release
-------

mvn clean deploy         Snapshot deploy to sonatype [requires proper setup in settings.xml]
mvn -Prelease,sonatype clean verify Prepare release bundle for manual staging.
