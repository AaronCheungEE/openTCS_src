/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers;

import java.io.Serializable;
import static java.util.Objects.requireNonNull;
import org.opentcs.util.eventsystem.TCSEvent;

/**
 * An event emitted by a communication adapter.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class CommunicationAdapterEvent
    extends TCSEvent
    implements Serializable {

  /**
   * The name of the adapter that emitted this event.
   */
  private final String adapterName;
  /**
   * An optional appendix containing additional arbitrary information about the
   * event.
   */
  private final Serializable appendix;

  /**
   * Creates a new instance.
   *
   * @param adapterName The name of the adapter that emitted this event.
   * @param appendix An optional appendix containing additional arbitrary
   * information about the event.
   */
  public CommunicationAdapterEvent(String adapterName, Serializable appendix) {
    this.adapterName = requireNonNull(adapterName, "adapterName");
    this.appendix = appendix;
  }

  /**
   * Creates a new instance without an appendix.
   *
   * @param adapterName The name of the adapter that emitted this event.
   */
  public CommunicationAdapterEvent(String adapterName) {
    this(adapterName, null);
  }
  

  /**
   * Returns the name of the adapter that emitted this event.
   *
   * @return The name of the adapter that emitted this event.
   */
  public String getAdapterName() {
    return adapterName;
  }

  /**
   * Returns the (optional) appendix containing additional arbitrary information
   * about the event.
   *
   * @return The (optional) appendix containing additional arbitrary information
   * about the event.
   */
  public Serializable getAppendix() {
    return appendix;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName()
        + "(adapterName: " + adapterName + ", appendix: " + appendix + ")";
  }
}
