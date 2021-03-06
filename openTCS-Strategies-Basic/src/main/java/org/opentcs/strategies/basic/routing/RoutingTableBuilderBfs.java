/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.routing;

import com.google.common.collect.HashBasedTable;
import com.google.inject.BindingAnnotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.opentcs.access.LocalKernel;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.StaticRoute;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.Route;

/**
 * Builds routing tables using a breadth-first-search implementation.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class RoutingTableBuilderBfs
    extends RoutingTableBuilderAbstract
    implements RoutingTableBuilder {

  /**
   * This class's Logger.
   */
  private static final Logger log
      = Logger.getLogger(RoutingTableBuilderBfs.class.getName());
  /**
   * Whether to terminate the BFS early when a cheaper route to a point has
   * already been found.
   */
  private final boolean terminateEarly;
  /**
   * The queue of nodes/points in the model that still need to be visited by the
   * BFS algorithm.
   */
  private final Queue<QueueEntry> queue = new PriorityQueue<>();

  /**
   * Creates a new instance.
   *
   * @param kernel The kernel providing the model data.
   * @param routeEvaluator The evaluator to be used to compute costs for routes.
   * @param terminateEarly Whether to terminate a branch of the search early
   * if a cheaper route to a point has already been found. Setting this can
   * dramatically increase performance, but note that - depending on the route
   * evaluator implementation, the existence of a different, cheaper route to
   * the current point does not necessarily mean that route is cheaper for
   * subsequent points, too.
   */
  @Inject
  RoutingTableBuilderBfs(LocalKernel kernel,
                         RouteEvaluator routeEvaluator,
                         @TerminateEarly boolean terminateEarly) {
    super(kernel, routeEvaluator);
    this.terminateEarly = terminateEarly;
  }

  @Override
  public RoutingTable computeTable(Vehicle vehicle) {
    this.vehicle = requireNonNull(vehicle, "vehicle");

    routingTable = HashBasedTable.create();
    queue.clear();
    long timeStampBefore = System.currentTimeMillis();
    for (Point startPoint : kernel.getTCSObjectsOriginal(Point.class)) {
      updateTableEntry(startPoint.getReference(),
                       startPoint.getReference(),
                       new LinkedList<>(),
                       0);
      queue.add(new QueueEntry(startPoint, new LinkedList<>(), 0));
      while (!queue.isEmpty()) {
        processEntry(startPoint, queue.remove());
      }
    }
    double timePassed = (System.currentTimeMillis() - timeStampBefore) / 1000.0;
    log.log(Level.FINE,
            "Computed routing table for {0} in {1,number,#.##} seconds.",
            new Object[] {vehicle.getName(), timePassed});
    for (StaticRoute staticRoute : kernel.getTCSObjectsOriginal(StaticRoute.class)) {
      integrateStaticRoute(staticRoute);
    }
    return new RoutingTable(routingTable);
  }

  private void processEntry(Point startPoint, QueueEntry entry) {
    checkSuccessorsForward(startPoint, entry);
    checkSuccessorsBackward(startPoint, entry);
  }

  private void checkSuccessorsForward(Point startPoint, QueueEntry entry) {
    // For every successor of the current entry's point, check if we can
    // update the table entry.
    // If we can update the table entry, also add a queue entry for the successor.
    for (TCSObjectReference<Path> outPathRef : entry.point.getOutgoingPaths()) {
      Path outPath = kernel.getTCSObjectOriginal(Path.class, outPathRef);
      Point nextPoint = kernel.getTCSObjectOriginal(Point.class,
                                                    outPath.getDestinationPoint());
      if (!outPath.isNavigableForward()
          || visitedPointOnRoute(nextPoint, entry.steps)) {
        continue;
      }
      LinkedList<Route.Step> steps = new LinkedList<>(entry.steps);
      steps.add(new Route.Step(outPath,
                               nextPoint,
                               Vehicle.Orientation.FORWARD,
                               steps.size()));
      long costs = routeEvaluator.computeCosts(vehicle, startPoint, steps);
      checkForTableAndQueueUpdate(startPoint, nextPoint, steps, costs);
    }
  }

  private void checkSuccessorsBackward(Point startPoint, QueueEntry entry) {
    // For every successor of the current entry's point, check if we can
    // update the table entry.
    // If we can update the table entry, also add a queue entry for the successor.
    for (TCSObjectReference<Path> inPathRef : entry.point.getIncomingPaths()) {
      Path inPath = kernel.getTCSObjectOriginal(Path.class, inPathRef);
      Point nextPoint = kernel.getTCSObjectOriginal(Point.class,
                                                    inPath.getSourcePoint());
      if (!inPath.isNavigableReverse()
          || visitedPointOnRoute(nextPoint, entry.steps)) {
        continue;
      }
      LinkedList<Route.Step> steps = new LinkedList<>(entry.steps);
      steps.add(new Route.Step(inPath,
                               nextPoint,
                               Vehicle.Orientation.BACKWARD,
                               steps.size()));
      long costs = routeEvaluator.computeCosts(vehicle, startPoint, steps);
      checkForTableAndQueueUpdate(startPoint, nextPoint, steps, costs);
    }
  }

  private void checkForTableAndQueueUpdate(Point startPoint,
                                           Point currentPoint,
                                           LinkedList<Route.Step> steps,
                                           long costs) {
    RoutingTable.Entry tableEntry = routingTable.get(startPoint.getReference(),
                                                     currentPoint.getReference());
    // If we found a better route than any known one, update the table entry.
    if (tableEntry == null || costs < tableEntry.getCosts()) {
      updateTableEntry(startPoint.getReference(),
                       currentPoint.getReference(),
                       steps,
                       costs);
    }
    // If the route found is not better than an existing one and we should
    // terminate early, do so.
    // (Not knowing the cost function applied to the route, terminating here
    // might mean that a shorter route to one of the successors will not be
    // found. An exhaustive search might take much longer, however.)
    else if (terminateEarly) {
      return;
    }
    queue.add(new QueueEntry(currentPoint, steps, costs));
  }

  /**
   * Annotation type for injecting whether to do a complete search or not.
   */
  @BindingAnnotation
  @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  public static @interface TerminateEarly {
    // Nothing here.
  }

  private static class QueueEntry
      implements Comparable<QueueEntry> {

    private final Point point;

    private final List<Route.Step> steps;

    private final long routeCosts;

    public QueueEntry(Point point, List<Route.Step> steps, long routeCosts) {
      this.point = requireNonNull(point, "point");
      this.steps = requireNonNull(steps, "steps");
      this.routeCosts = routeCosts;
    }

    @Override
    public int compareTo(QueueEntry other) {
      requireNonNull(other, "other");

      if (routeCosts != other.routeCosts) {
        return routeCosts < other.routeCosts ? -1 : 1;
      }
      else if (!Objects.equals(point, other.point)) {
        return point.getId() < other.point.getId() ? -1 : 1;
      }
      else if (steps.size() != other.steps.size()) {
        return steps.size() < other.steps.size() ? -1 : 1;
      }
      else {
        return 0;
      }
    }
  }
}
