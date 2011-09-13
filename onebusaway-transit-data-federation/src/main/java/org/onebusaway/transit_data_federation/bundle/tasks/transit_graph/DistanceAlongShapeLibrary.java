/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.transit_data_federation.bundle.tasks.transit_graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.onebusaway.collections.Min;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.model.XYPoint;
import org.onebusaway.geospatial.services.UTMLibrary;
import org.onebusaway.geospatial.services.UTMProjection;
import org.onebusaway.transit_data_federation.impl.shapes.PointAndIndex;
import org.onebusaway.transit_data_federation.impl.shapes.ShapePointsLibrary;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopTimeEntryImpl;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DistanceAlongShapeLibrary {

  private static Logger _log = LoggerFactory.getLogger(DistanceAlongShapeLibrary.class);

  private ShapePointsLibrary _shapePointsLibrary = new ShapePointsLibrary();

  private double _maxDistanceFromStopToShapePoint = 1000;

  public void setLocalMinimumThreshold(double localMinimumThreshold) {
    _shapePointsLibrary.setLocalMinimumThreshold(localMinimumThreshold);
  }

  /**
   * If the closest distance from a stop to a shape is more than
   * maxDistanceFromStopToShapePoint in the
   * {@link #getDistancesAlongShape(ShapePoints, List)}, then a
   * {@link StopIsTooFarFromShapeException} will be thrown.
   * 
   * @param maxDistanceFromStopToShapePoint distance in meters
   */
  public void setMaxDistanceFromStopToShapePoint(
      double maxDistanceFromStopToShapePoint) {
    _maxDistanceFromStopToShapePoint = maxDistanceFromStopToShapePoint;
  }

  public PointAndIndex[] getDistancesAlongShape(ShapePoints shapePoints,
      List<StopTimeEntryImpl> stopTimes)
      throws InvalidStopToShapeMappingException, StopIsTooFarFromShapeException {

    PointAndIndex[] stopTimePoints = new PointAndIndex[stopTimes.size()];

    UTMProjection projection = UTMLibrary.getProjectionForPoint(
        shapePoints.getLats()[0], shapePoints.getLons()[0]);

    List<XYPoint> projectedShapePoints = _shapePointsLibrary.getProjectedShapePoints(
        shapePoints, projection);

    double[] shapePointsDistTraveled = shapePoints.getDistTraveled();

    List<List<PointAndIndex>> possibleAssignments = computePotentialAssignments(
        projection, projectedShapePoints, shapePointsDistTraveled, stopTimes);

    assignmentSanityCheck(shapePoints, stopTimes, possibleAssignments);

    double maxDistanceTraveled = shapePointsDistTraveled[shapePointsDistTraveled.length - 1];

    List<PointAndIndex> bestAssignment = computeBestAssignment(shapePoints,
        stopTimes, possibleAssignments, projection, projectedShapePoints);

    for (int i = 0; i < stopTimePoints.length; i++) {
      PointAndIndex pindex = bestAssignment.get(i);
      if (pindex.distanceAlongShape > maxDistanceTraveled) {
        int index = projectedShapePoints.size() - 1;
        XYPoint point = projectedShapePoints.get(index);
        StopEntryImpl stop = stopTimes.get(i).getStop();
        XYPoint stopPoint = projection.forward(stop.getStopLocation());
        double d = stopPoint.getDistance(point);
        pindex = new PointAndIndex(point, index, d, maxDistanceTraveled);
      }
      stopTimePoints[i] = pindex;
    }
    return stopTimePoints;
  }

  private void assignmentSanityCheck(ShapePoints shapePoints,
      List<StopTimeEntryImpl> stopTimes,
      List<List<PointAndIndex>> possibleAssignments)
      throws StopIsTooFarFromShapeException {

    int stIndex = 0;
    for (List<PointAndIndex> assignments : possibleAssignments) {
      Min<PointAndIndex> m = new Min<PointAndIndex>();
      for (PointAndIndex pindex : assignments)
        m.add(pindex.distanceFromTarget, pindex);
      if (m.getMinValue() > _maxDistanceFromStopToShapePoint) {
        StopTimeEntry stopTime = stopTimes.get(stIndex);
        PointAndIndex pindex = m.getMinElement();
        CoordinatePoint point = shapePoints.getPointForIndex(pindex.index);
        throw new StopIsTooFarFromShapeException(stopTime, pindex, point);
      }
      stIndex++;
    }
  }

  private List<List<PointAndIndex>> computePotentialAssignments(
      UTMProjection projection, List<XYPoint> projectedShapePoints,
      double[] shapePointDistance, List<StopTimeEntryImpl> stopTimes) {

    List<List<PointAndIndex>> possibleAssignments = new ArrayList<List<PointAndIndex>>();

    for (StopTimeEntryImpl stopTime : stopTimes) {

      StopEntryImpl stop = stopTime.getStop();
      XYPoint stopPoint = projection.forward(stop.getStopLocation());

      List<PointAndIndex> assignments = _shapePointsLibrary.computePotentialAssignments(
          projectedShapePoints, shapePointDistance, stopPoint, 0,
          projectedShapePoints.size());

      Collections.sort(assignments);

      possibleAssignments.add(assignments);
    }
    return possibleAssignments;
  }

  private List<PointAndIndex> computeBestAssignment(ShapePoints shapePoints,
      List<StopTimeEntryImpl> stopTimes,
      List<List<PointAndIndex>> possibleAssignments, UTMProjection projection,
      List<XYPoint> projectedShapePoints)
      throws InvalidStopToShapeMappingException {

    checkFirstAndLastStop(stopTimes, possibleAssignments, shapePoints,
        projection, projectedShapePoints);

    List<PointAndIndex> currentAssignment = new ArrayList<PointAndIndex>(
        possibleAssignments.size());

    List<Assignment> allValidAssignments = new ArrayList<Assignment>();

    recursivelyConstructAssignments(possibleAssignments, currentAssignment, 0,
        allValidAssignments);

    if (allValidAssignments.isEmpty()) {
      constructError(shapePoints, stopTimes, possibleAssignments, projection);
    }

    Min<Assignment> bestAssignments = new Min<Assignment>();

    for (Assignment validAssignment : allValidAssignments)
      bestAssignments.add(validAssignment.score, validAssignment);

    Assignment bestAssignment = bestAssignments.getMinElement();

    return bestAssignment.assigment;
  }

  /**
   * Special check for an issue with start points where the first stop isn't all
   * that near the start of the shape (the first stop being more of a layover
   * point). If the shape is working against us, the closest point for the first
   * stop can be a point further along the shape, which causes problems.
   */
  private void checkFirstAndLastStop(List<StopTimeEntryImpl> stopTimes,
      List<List<PointAndIndex>> possibleAssignments, ShapePoints shapePoints,
      UTMProjection projection, List<XYPoint> projectedShapePoints) {

    if (possibleAssignments.size() >= 2) {

      PointAndIndex first = possibleAssignments.get(0).get(0);
      PointAndIndex second = possibleAssignments.get(1).get(0);
      if (first.distanceAlongShape > second.distanceAlongShape) {

        StopTimeEntryImpl firstStopTime = stopTimes.get(0);

        _log.warn("snapping first stop time id=" + firstStopTime.getId()
            + " to start of shape");

        XYPoint point = projectedShapePoints.get(0);

        StopEntryImpl stop = firstStopTime.getStop();
        XYPoint stopPoint = projection.forward(stop.getStopLocation());

        double d = stopPoint.getDistance(point);

        possibleAssignments.get(0).add(new PointAndIndex(point, 0, d, 0.0));
      }

      int n = possibleAssignments.size();
      PointAndIndex prev = possibleAssignments.get(n - 2).get(0);
      PointAndIndex last = possibleAssignments.get(n - 1).get(0);
      if (prev.distanceAlongShape > last.distanceAlongShape) {
      }

    }

    if (possibleAssignments.size() > 0) {

      /**
       * We snap the last stop to the end of the shape and add it to the set of
       * possible assignments. In the worst case, it will be a higher-scoring
       * assignment and ignored, but it can help in cases where the stop was
       * weirdly assigned.
       */
      PointAndIndex lastSnapped = getLastStopSnappedToEndOfShape(stopTimes,
          shapePoints, projection, projectedShapePoints);

      possibleAssignments.get(possibleAssignments.size() - 1).add(lastSnapped);
    }
  }

  private PointAndIndex getLastStopSnappedToEndOfShape(
      List<StopTimeEntryImpl> stopTimes, ShapePoints shapePoints,
      UTMProjection projection, List<XYPoint> projectedShapePoints) {

    int i = stopTimes.size() - 1;
    StopTimeEntryImpl lastStopTime = stopTimes.get(i);

    int lastShapePointIndex = projectedShapePoints.size() - 1;
    XYPoint lastShapePoint = projectedShapePoints.get(lastShapePointIndex);
    XYPoint stopLocation = projection.forward(lastStopTime.getStop().getStopLocation());

    double existingDistanceAlongShape = shapePoints.getDistTraveledForIndex(lastShapePointIndex);
    double extraDistanceAlongShape = lastShapePoint.getDistance(stopLocation);
    double distanceAlongShape = existingDistanceAlongShape
        + extraDistanceAlongShape;

    double d = lastShapePoint.getDistance(stopLocation);

    return new PointAndIndex(lastShapePoint, lastShapePointIndex, d,
        distanceAlongShape);
  }

  private void recursivelyConstructAssignments(
      List<List<PointAndIndex>> possibleAssignments,
      List<PointAndIndex> currentAssignment, int i, List<Assignment> best) {

    /**
     * If we've made it through ALL assignments, we have a valid assignment!
     */
    if (i == possibleAssignments.size()) {

      double score = 0;
      for (PointAndIndex p : currentAssignment)
        score += p.distanceFromTarget;
      currentAssignment = new ArrayList<PointAndIndex>(currentAssignment);
      Assignment result = new Assignment(currentAssignment, score);
      best.add(result);
      return;
    }

    List<PointAndIndex> possibleAssignmentsForIndex = possibleAssignments.get(i);

    List<PointAndIndex> validAssignments = new ArrayList<PointAndIndex>();

    double lastDistanceAlongShape = -1;

    if (i > 0) {
      PointAndIndex prev = currentAssignment.get(i - 1);
      lastDistanceAlongShape = prev.distanceAlongShape;
    }

    for (PointAndIndex possibleAssignmentForIndex : possibleAssignmentsForIndex) {
      if (possibleAssignmentForIndex.distanceAlongShape >= lastDistanceAlongShape)
        validAssignments.add(possibleAssignmentForIndex);
    }

    /**
     * There is no satisfying assignment for this search tree, so we return
     */
    if (validAssignments.isEmpty()) {
      return;
    }

    /**
     * For each valid assignment, pop it onto the current assignment and
     * recursively evaluate
     */
    for (PointAndIndex validAssignment : validAssignments) {

      currentAssignment.add(validAssignment);

      recursivelyConstructAssignments(possibleAssignments, currentAssignment,
          i + 1, best);

      currentAssignment.remove(currentAssignment.size() - 1);
    }
  }

  private void constructError(ShapePoints shapePoints,
      List<StopTimeEntryImpl> stopTimes,
      List<List<PointAndIndex>> possibleAssignments, UTMProjection projection)
      throws InvalidStopToShapeMappingException {

    StopTimeEntryImpl first = stopTimes.get(0);
    StopTimeEntryImpl last = stopTimes.get(stopTimes.size() - 1);

    _log.error("We were attempting to compute the distance along a particular trip for each stop time of that trip by snapping them to the shape for that trip.  However, we could not find an assignment for each stop time where the distance traveled along the shape for each stop time was strictly increasing (aka a stop time seemed to travel backwards)");

    _log.error("error constructing stop-time distances along shape for trip="
        + first.getTrip().getId() + " firstStopTime=" + first.getId()
        + " lastStopTime=" + last.getId());

    StringBuilder b = new StringBuilder();
    int index = 0;

    b.append("# potential assignments:\n");
    b.append("# index stopId stopLat stopLon\n");
    b.append("#   distanceAlongShapeA locationOnShapeLatA locationOnShapeLonA shapePointIndexA\n");
    b.append("#   ...\n");
    
    double prevMaxDistanceAlongShape = Double.NEGATIVE_INFINITY;
    
    for (List<PointAndIndex> possible : possibleAssignments) {
      StopTimeEntryImpl stopTime = stopTimes.get(index);
      StopEntryImpl stop = stopTime.getStop();
      b.append(index);
      b.append(' ');
      b.append(stop.getId());
      b.append(' ');
      b.append(stop.getStopLat());
      b.append(' ');
      b.append(stop.getStopLon());
      b.append('\n');

      double maxDistanceAlongShape = Double.NEGATIVE_INFINITY;
      double minDistanceAlongShape = Double.POSITIVE_INFINITY;
      
      for (PointAndIndex pindex : possible) {
        b.append("  ");
        b.append(pindex.distanceAlongShape);
        b.append(' ');
        b.append(projection.reverse(pindex.point));
        b.append(' ');
        b.append(pindex.index);
        b.append("\n");
        maxDistanceAlongShape = Math.max(maxDistanceAlongShape, pindex.distanceAlongShape);
        minDistanceAlongShape = Math.min(minDistanceAlongShape, pindex.distanceAlongShape);
      }
      
      if( minDistanceAlongShape < prevMaxDistanceAlongShape ) {
        b.append("    ^ potential problem here ^\n");
      }
      
      prevMaxDistanceAlongShape = maxDistanceAlongShape;

      index++;
    }
    _log.error(b.toString());

    b = new StringBuilder();
    index = 0;
    for (int i = 0; i < shapePoints.getSize(); i++) {
      b.append(shapePoints.getLatForIndex(i));
      b.append(' ');
      b.append(shapePoints.getLonForIndex(i));
      b.append(' ');
      b.append(shapePoints.getDistTraveledForIndex(i));
      b.append('\n');
    }

    _log.error("shape points:\n" + b.toString());

    throw new InvalidStopToShapeMappingException(first.getTrip());
  }

  private static class Assignment implements Comparable<Assignment> {
    private final List<PointAndIndex> assigment;
    private final double score;

    public Assignment(List<PointAndIndex> assignment, double score) {
      this.assigment = assignment;
      this.score = score;
    }

    @Override
    public int compareTo(Assignment o) {
      return Double.compare(score, o.score);
    }
  }

  public static class DistanceAlongShapeException extends Exception {

    private static final long serialVersionUID = 1L;

    public DistanceAlongShapeException(String message) {
      super(message);
    }
  }

  public static class StopIsTooFarFromShapeException extends
      DistanceAlongShapeException {

    private static final long serialVersionUID = 1L;
    private final StopTimeEntry _stopTime;
    private final PointAndIndex _pointAndIndex;
    private final CoordinatePoint _point;

    public StopIsTooFarFromShapeException(StopTimeEntry stopTime,
        PointAndIndex pointAndIndex, CoordinatePoint point) {
      super("stopTime=" + stopTime + " pointAndIndex=" + pointAndIndex
          + " point=" + point);
      _stopTime = stopTime;
      _pointAndIndex = pointAndIndex;
      _point = point;
    }

    public StopTimeEntry getStopTime() {
      return _stopTime;
    }

    public PointAndIndex getPointAndIndex() {
      return _pointAndIndex;
    }

    public CoordinatePoint getPoint() {
      return _point;
    }
  }

  public static class InvalidStopToShapeMappingException extends
      DistanceAlongShapeException {

    private static final long serialVersionUID = 1L;

    private final TripEntry _trip;

    public InvalidStopToShapeMappingException(TripEntry trip) {
      super("trip=" + trip.getId());
      _trip = trip;
    }

    public TripEntry getTrip() {
      return _trip;
    }
  }
}
