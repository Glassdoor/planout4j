package com.glassdoor.planout4j.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;

import static com.google.common.base.Preconditions.*;

/**
 * Manages reading and writing of Planout4j configuration from / to the file system.
 */
public class Planout4jConfigFileBackend implements Planout4jConfigBackend {

   private static final Logger LOG = LoggerFactory.getLogger(Planout4jConfigFileBackend.class);

   private String sourceDirHierarchy;
   private String destDirHierarchy;

   private String inputFileNamesPattern;
   private String outputFileExtension;

   public void configure(final Config config) {
      sourceDirHierarchy = getDir(config, "sourceDirHierarchy");
      destDirHierarchy = getDir(config, "destDirHierarchy");
      if (sourceDirHierarchy != null) {
         inputFileNamesPattern = config.getString("inputFileNamesPattern");
      }
      if (destDirHierarchy != null) {
         outputFileExtension = config.getString("outputFileExtension");
      }
      checkState(sourceDirHierarchy != null || destDirHierarchy != null,
              "At least one of [sourceDirHierarchy, destDirHierarchy] properties must be set");
   }

   private String getDir(final Config config, final String prop) {
      try {
         return config.getString(prop).replace('/', File.separatorChar);
      } catch (ConfigException e) {
         LOG.debug("Missing {} property, {} mode will be disabled", prop, prop.startsWith("source") ? "reading" : "writing");
         return null;
      }
   }

   @Override
   public Map<String, String> loadAll() {
      if (sourceDirHierarchy == null) {
         throw new UnsupportedOperationException("Reading mode is not configured");
      }
      Map<String, String> name2Content = new HashMap<>();
      LOG.info("Looking up folder {} to read all files matching the pattern {}", sourceDirHierarchy, inputFileNamesPattern);
      try (DirectoryStream<Path> ds = Files.newDirectoryStream(Paths.get(sourceDirHierarchy), inputFileNamesPattern)) {
         for (Path p : ds) {
            if (!Files.isDirectory(p)) {
               name2Content.put(
                     com.google.common.io.Files.getNameWithoutExtension(p.getFileName().toString()),
                     new String(Files.readAllBytes(p)));
            } else {
               LOG.debug("Skipping directory {}", p);
            }
         }
      } catch (IOException e) {
         throw new RuntimeException("Failed to read planout4j configuration", e);
      }
      return name2Content;
   }

   @Override
   public void persist(Map<String, String> configData) {
      if (destDirHierarchy == null) {
         throw new UnsupportedOperationException("Writing mode is not configured");
      }
      Path outputFile;
      for (String name : configData.keySet()) {
         try {
            outputFile = Paths.get(destDirHierarchy).resolve(name + outputFileExtension);
            if (!Files.exists(outputFile.getParent())) {
               Files.createDirectories(outputFile.getParent());
            }
            Files.write(outputFile, configData.get(name).getBytes(), StandardOpenOption.CREATE,
                  StandardOpenOption.TRUNCATE_EXISTING);
         } catch (IOException e) {
            throw new RuntimeException("Failed to write planout4j configuration", e);
         }
      }

   }

   @Override
   public String persistenceLayer() {
      return "FILE SYSTEM";
   }

   @Override
   public String persistenceDestination() {
      return String.format("%s: source(%s, %s); destination(%s, %s)", persistenceLayer(),
              sourceDirHierarchy, inputFileNamesPattern, destDirHierarchy, outputFileExtension);
   }

   public String getSourceDirHierarchy() {
      return sourceDirHierarchy;
   }

   public String getDestDirHierarchy() {
      return destDirHierarchy;
   }

   public String getInputFileNamesPattern() {
      return inputFileNamesPattern;
   }

   public String getOutputFileExtension() {
      return outputFileExtension;
   }

   // for unit test
   void setSourceDirHierarchy(final String sourceDirHierarchy) {
      this.sourceDirHierarchy = sourceDirHierarchy;
   }

}
