package org.codehaus.mojo.build.it;

import io.takari.maven.testing.TestResources;
import io.takari.maven.testing.executor.MavenExecution;
import io.takari.maven.testing.executor.MavenExecutionResult;
import io.takari.maven.testing.executor.MavenRuntime;
import io.takari.maven.testing.executor.MavenRuntime.MavenRuntimeBuilder;
import io.takari.maven.testing.executor.MavenVersions;
import io.takari.maven.testing.executor.junit.MavenJUnitTestRunner;

import java.io.File;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

import org.codehaus.plexus.util.FileUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith( MavenJUnitTestRunner.class )
@MavenVersions( { "3.1.1" } )
public class BuildNumberMojoTest
{

    @Rule
    public final TestResources resources = new TestResources();

    public final MavenRuntime maven;

    public BuildNumberMojoTest( MavenRuntimeBuilder builder )
        throws Exception
    {
        this.maven = builder.withCliOptions( "-B" ).build();
    }

    @Test
    public void basicIT()
        throws Exception
    {
        File projDir = resources.getBasedir( "basic-it" );

        MavenExecution mavenExec = maven.forProject( projDir );
        MavenExecutionResult result = mavenExec.execute( "clean" );
        result.assertErrorFreeLog();
        File testDir = result.getBasedir();
        FileUtils.copyDirectory( new File( testDir, "DotSvnDir" ), new File( testDir, ".svn" ) );
        result = mavenExec.execute( "clean", "verify" );
        result.assertLogText( "Storing buildNumber: 19665" );
        result.assertLogText( "Storing buildScmBranch: trunk" );

        File artifact = new File( testDir, "target/buildnumber-maven-plugin-basic-it-1.0-SNAPSHOT.jar" );
        JarFile jarFile = new JarFile( artifact );
        Attributes manifest = jarFile.getManifest().getMainAttributes();
        jarFile.close();
        String scmRev = manifest.getValue( "SCM-Revision" );
        Assert.assertEquals( "19665" , scmRev );


    }

    @Test
    @Ignore
    public void basicItClearcaseScm()
        throws Exception
    {
        File projDir = resources.getBasedir( "basic-it-clearcase-scm" );

        MavenExecution mavenExec = maven.forProject( projDir );
        MavenExecutionResult result = mavenExec.execute( "clean", "verify" );
        File testDir = result.getBasedir();
        result.assertLogText( "Storing buildNumber: foo" );
        result.assertLogText( "Storing buildScmBranch: UNKNOWN_BRANCH" );

        File artifact = new File( testDir, "target/buildnumber-maven-plugin-basic-it-clearcase-scm-1.0-SNAPSHOT.jar" );
        JarFile jarFile = new JarFile( artifact );
        Attributes manifest = jarFile.getManifest().getMainAttributes();
        jarFile.close();
        String scmRev = manifest.getValue( "SCM-Revision" );
        Assert.assertEquals( "foo" , scmRev );


    }
}