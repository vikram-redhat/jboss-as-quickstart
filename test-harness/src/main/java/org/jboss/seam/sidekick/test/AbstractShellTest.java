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

package org.jboss.seam.sidekick.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.jboss.arquillian.api.Deployment;
import org.jboss.seam.sidekick.BasePackageMarker;
import org.jboss.seam.sidekick.project.Project;
import org.jboss.seam.sidekick.project.model.ProjectImpl;
import org.jboss.seam.sidekick.shell.Shell;
import org.jboss.seam.sidekick.shell.plugins.events.Startup;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public abstract class AbstractShellTest
{
   @Deployment
   public static JavaArchive getDeployment()
   {

      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "test.jar")
               .addPackages(true, BasePackageMarker.class.getPackage())
               .addManifestResource(new ByteArrayAsset("<beans/>".getBytes()), ArchivePaths.create("beans.xml"));

      return archive;
   }

   @Inject
   private Shell shell;

   @Inject
   private BeanManager beanManager;

   private Queue<String> inputQueue = new LinkedList<String>();

   private final List<File> tempFolders = new ArrayList<File>();

   private static final String PKG = AbstractShellTest.class.getSimpleName().toLowerCase();

   @Inject
   private Instance<Project> project;

   @BeforeClass
   public static void before() throws IOException
   {
   }

   @AfterClass
   public static void after()
   {
   }

   @Before
   public void beforeTest() throws IOException
   {
      File tempFolder = createTempFolder();

      shell.setVerbose(true);
      shell.setCurrentDirectory(tempFolder.getAbsoluteFile());
      beanManager.fireEvent(new Startup(), new Annotation[] {});

      resetInputQueue();
      shell.setOutputWriter(new PrintWriter(System.out));
   }

   /**
    * @throws IOException
    */
   protected File createTempFolder() throws IOException
   {
      File tempFolder = File.createTempFile(PKG, null);
      tempFolder.delete();
      tempFolder.mkdirs();
      tempFolders.add(tempFolder);
      return tempFolder;
   }

   /**
    * Reset the shell input queue (called automatically before each test.)
    */
   protected void resetInputQueue() throws IOException
   {
      inputQueue = new LinkedList<String>();
      QueuedInputStream is = new QueuedInputStream(inputQueue);
      shell.setInputStream(is);
   }

   @After
   public void afterTest() throws IOException
   {
      for (File file : tempFolders)
      {
         if (file.exists())
         {
            assertTrue(new ProjectImpl(file).delete(file));
         }
      }
   }

   protected void queueInputLines(final String... inputs)
   {
      for (String input : inputs)
      {
         inputQueue.add(input + "\n");
      }
   }

   protected BeanManager getBeanManager()
   {
      return beanManager;
   }

   protected Shell getShell()
   {
      return shell;
   }

   protected Project getProject()
   {
      return project.get();
   }

   protected void initializeJavaProject() throws IOException
   {
      File folder = createTempFolder();
      getShell().execute("cd " + folder.getAbsolutePath());
      queueInputLines("", "com.test", "");
      getShell().execute("new-project test");
   }

}