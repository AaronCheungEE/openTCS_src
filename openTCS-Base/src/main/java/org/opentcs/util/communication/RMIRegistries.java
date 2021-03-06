/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.communication;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * This class provides helper methods for working with RMI registries.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public final class RMIRegistries {

  /**
   * This class's Logger.
   */
  private static final Logger log
      = Logger.getLogger(RMIRegistries.class.getName());

  /**
   * Prevents creation of instances.
   */
  private RMIRegistries() {
  }

  /**
   * Checks if a usable registry is available on the given host and port.
   *
   * @param host The host to check.
   * @param port The port to check.
   * @return <code>true</code> if, and only if, a usable registry was found on
   * the given host and port.
   */
  public static boolean registryAvailable(String host, int port) {
    return lookupRegistry(host, port).isPresent();
  }

  /**
   * Returns a reference to a working registry on the given host and port, or
   * <code>null</code>, if that's not possible.
   *
   * @param host The host to check.
   * @param port The port to check.
   * @return A reference to a working registry on the given host and port, or
   * <code>null</code>, if a working one was not found there.
   * @deprecated Scheduled for removal with release 4.0.0. Use
   * lookupRegistry(String, int) instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "4.0.0")
  public static Registry getWorkingRegistry(String host, int port) {
    return lookupRegistry(host, port).orElse(null);
  }

  /**
   * Returns a reference to a working registry on the given host and port, if
   * there is one.
   *
   * @param host The host to check.
   * @param port The port to check.
   * @return A reference to a working registry on the given host and port, if a
   * working one was found there.
   */
  public static Optional<Registry> lookupRegistry(String host, int port) {
    Registry registry;
    log.fine("Checking for working RMI registry on " + host + ":" + port + ".");
    try {
      registry = LocateRegistry.getRegistry(host, port);
      String[] boundNames = registry.list();
    }
    catch (RemoteException exc) {
      log.fine("RMI registry on " + host + ":" + port + "unavailable.");
      return Optional.empty();
    }
    return Optional.of(registry);
  }

  /**
   * Returns a reference to a working local registry, or <code>null</code>, if
   * that's impossible.
   * This method first checks if a local registry at the given port is
   * available and usable. If so, a reference to it is returned, otherwise a new
   * one is created. If that is not possible, either, <code>null</code> is
   * returned.
   *
   * @param port The port at which the registry should be listening.
   * @return A reference to a working local registry, or <code>null</code>, if
   * getting or creating a working one was not possible.
   * @deprecated Scheduled for removal with release 4.0.0. Use
   * lookupOrInstallRegistry(int) instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "4.0.0")
  public static Registry getOrCreateWorkingRegistry(int port) {
    return lookupOrInstallRegistry(port).orElse(null);
  }

  /**
   * Returns a reference to a working local registry, if one already existed or
   * a new one could be installed.
   * This method first checks if a local registry at the given port is
   * available and usable. If so, a reference to it is returned, otherwise a new
   * one is created. If that is not possible, either, no reference is returned.
   *
   * @param port The port at which the registry should be listening.
   * @return A reference to a working local registry, if getting or creating a
   * working one was possible.
   */
  public static Optional<Registry> lookupOrInstallRegistry(int port) {
    Registry registry;
    String[] boundNames;
    log.fine("Checking for local RMI registry...");
    try {
      // Try to get a reference to an operating registry and test it.
      registry = LocateRegistry.getRegistry(port);
      boundNames = registry.list();
    }
    catch (RemoteException exc) {
      // No registry available, yet...
      log.fine("Local RMI registry unavailable, trying to create one...");
      try {
        // Try to create a new local registry and test it.
        registry = LocateRegistry.createRegistry(port);
        boundNames = registry.list();
      }
      catch (RemoteException anotherExc) {
        // Couldn't create a working registry, either - give up.
        log.log(Level.WARNING,
                "Could not get or create a usable registry, giving up.",
                anotherExc);
        registry = null;
      }
    }
    return Optional.ofNullable(registry);
  }

  /**
   * Returns a reference to a working local registry, or <code>null</code>, if
   * that's impossible.
   * This method first checks if a local registry at the default port is
   * available and usable. If so, a reference to it is returned, otherwise a new
   * one is created. If that is not possible, either, <code>null</code> is
   * returned.
   *
   * @return A reference to a working local registry, or <code>null</code>, if
   * getting or creating a working one was not possible.
   * @deprecated Scheduled for removal with release 4.0.0. Use
   * lookupOrInstallRegistry() instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "4.0.0")
  public static Registry getOrCreateWorkingRegistry() {
    return lookupOrInstallRegistry().orElse(null);
  }

  /**
   * Returns a reference to a working local registry, or <code>null</code>, if
   * that's impossible.
   * This method first checks if a local registry at the default port is
   * available and usable. If so, a reference to it is returned, otherwise a new
   * one is created. If that is not possible, either, <code>null</code> is
   * returned.
   *
   * @return A reference to a working local registry, or <code>null</code>, if
   * getting or creating a working one was not possible.
   */
  public static Optional<Registry> lookupOrInstallRegistry() {
    return lookupOrInstallRegistry(Registry.REGISTRY_PORT);
  }
}
