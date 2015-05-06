package com.glassdoor.planout4j.tools;

import org.slf4j.LoggerFactory;

import com.glassdoor.planout4j.config.Planout4jConfigShipperImpl;

/**
 * This tool invokes {@link com.glassdoor.planout4j.config.Planout4jConfigShipper} instance.
 */
public class Planout4jConfigShipperTool {

   public static void main(String[] args) {
      ToolsSupport.initLogging("config shipper tool");
      try {
         new Planout4jConfigShipperImpl().ship();
      } catch (Exception e) {
         LoggerFactory.getLogger(Planout4jConfigShipperTool.class).error("Failed to ship configs", e);
      }
   }
   
}
