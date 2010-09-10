/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.seam.sidekick.test.project;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.inject.Inject;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.eclipse.core.internal.resources.Project;
import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.seam.sidekick.parser.java.JavaClass;
import org.jboss.seam.sidekick.parser.java.JavaParser;
import org.jboss.seam.sidekick.project.ProjectModelException;
import org.jboss.seam.sidekick.project.model.MavenProject;
import org.jboss.seam.sidekick.project.model.maven.DependencyBuilder;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
@RunWith(Arquillian.class)
public class MavenProjectTest
{
   @Deployment
   public static JavaArchive createTestArchive()
   {
      return ShrinkWrap.create(JavaArchive.class, "test.jar")
               .addPackages(true, Project.class.getPackage())
               .addClass(MavenProject.class)
               .addClass(DependencyBuilder.class)
               .addManifestResource(new ByteArrayAsset("<beans/>".getBytes()), ArchivePaths.create("beans.xml"));
   }

   private static final String PKG = MavenProjectTest.class.getSimpleName().toLowerCase();
   private static File tempFolder;
   private static MavenProject tempProject;

   private final MavenProject thisProject = new MavenProject();

   private final MavenProject testProject = new MavenProject("src/test/resources/test-pom");

   @Inject
   private DependencyBuilder dependencyBuilder;

   @BeforeClass
   public static void before() throws IOException
   {
      tempFolder = File.createTempFile(PKG, null);
      tempFolder.delete();
      tempFolder.mkdirs();
      File pom = new File(tempFolder.getAbsolutePath() + "/pom.xml");
      pom.createNewFile();
      tempProject = new MavenProject(tempFolder, true);
   }

   @AfterClass
   public static void after()
   {
      if (tempFolder.exists())
      {
         assertTrue(tempProject.delete(tempFolder));
      }
   }

   @Test
   public void testCreateDefault() throws Exception
   {
      assertTrue(tempProject.exists());
   }

   @Test
   public void testGetDefaultSourceDir() throws Exception
   {
      assertEquals(new File(tempProject.getProjectRoot() + "/src/main/java/"), tempProject.getSourceFolder());
   }

   @Test
   public void testGetTestSourceDir() throws Exception
   {
      assertEquals(new File(tempProject.getProjectRoot() + "/src/test/java/"), tempProject.getTestSourceFolder());
   }

   @Test
   public void testCreateJavaFile() throws Exception
   {
      String name = "JustCreated";
      JavaClass clazz = JavaParser.createClass().setName(name).setPackage(PKG);
      clazz.applyChanges();
      File file = tempProject.createJavaFile(clazz);
      assertEquals(name + ".java", file.getName());

      JavaClass result = JavaParser.parse(file);
      assertEquals(name, result.getName());
      assertEquals(PKG, result.getPackage());
      assertTrue(tempProject.delete(file));
      assertEquals(clazz, result);
   }

   @Test
   public void testCreatePOM() throws Exception
   {
      Model pom = tempProject.getPOM();
      pom.setGroupId("org.jboss.seam");
      pom.setArtifactId("scaffolding");
      pom.setVersion("X-SNAPSHOT");
      tempProject.setPOM(pom);
      File file = pom.getPomFile();
      assertTrue(file.exists());

      MavenXpp3Reader reader = new MavenXpp3Reader();
      Model result = reader.read(new FileInputStream(file));
      assertEquals(pom.getArtifactId(), result.getArtifactId());
   }

   @Test
   public void testHasDependency() throws Exception
   {
      assertTrue(testProject.hasDependency(dependencyBuilder.setGroupId("com.ocpsoft")
               .setArtifactId("prettyfaces-jsf2").setVersion("3.0.2-SNAPSHOT").build()));
   }

   @Test
   public void testAddDependency() throws Exception
   {
      Dependency dependency = dependencyBuilder.setGroupId("org.jboss")
            .setArtifactId("test-dependency").setVersion("1.0.0.Final").build();

      assertFalse(tempProject.hasDependency(dependency));
      tempProject.addDependency(dependency);
      assertTrue(tempProject.hasDependency(dependency));
   }

   @Test
   public void testRemoveDependency() throws Exception
   {
      Dependency dependency = dependencyBuilder.setGroupId("org.jboss")
            .setArtifactId("test-dependency").setVersion("1.0.1.Final").build();

      assertFalse(tempProject.hasDependency(dependency));
      tempProject.addDependency(dependency);
      assertTrue(tempProject.hasDependency(dependency));
      tempProject.removeDependency(dependency);
      assertFalse(tempProject.hasDependency(dependency));
   }

   @Test
   public void testProjectIsCurrentProject() throws Exception
   {
      Model pom = thisProject.getPOM();
      assertEquals("sidekick-project-model", pom.getArtifactId());
   }

   @Test
   public void testAbsoluteProjectIsResolvedCorrectly() throws Exception
   {
      Model pom = testProject.getPOM();
      assertEquals("socialpm", pom.getArtifactId());
   }

   @Test(expected = ProjectModelException.class)
   public void testAbsoluteUnknownProjectCannotInstantiate() throws Exception
   {
      File temp = File.createTempFile(PKG, null);
      temp.delete();
      temp.mkdirs();
      new MavenProject(temp); // boom
   }

   @Test
   public void testAbsoluteUnknownProjectInstantiatesWithCreateTrue() throws Exception
   {
      File temp = File.createTempFile(PKG, null);
      temp.delete();
      temp.mkdirs();
      new MavenProject(temp, true); // no boom
   }
}