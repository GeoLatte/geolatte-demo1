package org.geolatte.demo1.services;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 * Application class for the flooding app.
 * </p>
 *
 * @author Bert Vanhooff
 * @author <a href="http://www.qmino.com">Qmino bvba</a>
 */
public class FloodingApplication extends Application {
   private Set<Object> singletons = new HashSet<Object>();
   private Set<Class<?>> empty = new HashSet<Class<?>>();

   public FloodingApplication() {
      singletons.add(new FloodingService());
   }

   @Override
   public Set<Class<?>> getClasses() {
      return empty;
   }

   @Override
   public Set<Object> getSingletons() {
      return singletons;
   }
}