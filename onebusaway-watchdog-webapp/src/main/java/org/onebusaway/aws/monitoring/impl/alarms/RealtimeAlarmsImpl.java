package org.onebusaway.aws.monitoring.impl.alarms;

import java.util.List;

import org.onebusaway.aws.monitoring.model.metrics.MetricName;
import org.onebusaway.aws.monitoring.model.metrics.MetricResponse;
import org.onebusaway.aws.monitoring.service.alarms.RealtimeAlarms;
import org.springframework.stereotype.Component;

import com.amazonaws.services.cloudwatch.model.ComparisonOperator;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricAlarmRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;

@Component
public class RealtimeAlarmsImpl extends AlarmsTemplate implements RealtimeAlarms  {

	@Override
	public void createRealtimeLocationsTotalAlarm() {
		PutMetricAlarmRequest putMetricAlarmRequest = getMetricAlarmRequest(MetricName.RealtimeLocationsTotal);
		putMetricAlarmRequest.setThreshold(0d);
		putMetricAlarmRequest.setAlarmActions(getCriticalAction());
		putMetricAlarmRequest.setUnit(StandardUnit.Count);
		putMetricAlarmRequest.setComparisonOperator(ComparisonOperator.LessThanOrEqualToThreshold);
		_cloudWatchService.publishAlarm(putMetricAlarmRequest);	
	}

	@Override
	public void createRealtimeLocationsInvalidAlarm() {
		PutMetricAlarmRequest putMetricAlarmRequest = getMetricAlarmRequest(MetricName.RealtimeLocationsInvalid);
		putMetricAlarmRequest.setThreshold(30d);
		putMetricAlarmRequest.setAlarmActions(getCriticalAction());
		putMetricAlarmRequest.setUnit(StandardUnit.Count);
		_cloudWatchService.publishAlarm(putMetricAlarmRequest);	
		
	}

	@Override
	public void createRealtimeStopsMatchedAlarm() {
		PutMetricAlarmRequest putMetricAlarmRequest = getMetricAlarmRequest(MetricName.RealtimeStopsMatched);
		putMetricAlarmRequest.setThreshold(0d);
		putMetricAlarmRequest.setAlarmActions(getCriticalAction());
		putMetricAlarmRequest.setUnit(StandardUnit.Count);
		putMetricAlarmRequest.setComparisonOperator(ComparisonOperator.LessThanOrEqualToThreshold);
		_cloudWatchService.publishAlarm(putMetricAlarmRequest);	
		
	}

	@Override
	public void createRealtimeStopsUnmatchedAlarm() {
		PutMetricAlarmRequest putMetricAlarmRequest = getMetricAlarmRequest(MetricName.RealtimeStopsUnmatched);
		putMetricAlarmRequest.setThreshold(300d); // about 4-5%
		putMetricAlarmRequest.setAlarmActions(getCriticalAction());
		putMetricAlarmRequest.setUnit(StandardUnit.Count);
		_cloudWatchService.publishAlarm(putMetricAlarmRequest);		
	}

	@Override
	public void createRealtimeTripsTotalAlarm() {
		PutMetricAlarmRequest putMetricAlarmRequest = getMetricAlarmRequest(MetricName.RealtimeTripsTotal);
		putMetricAlarmRequest.setAlarmActions(getCriticalAction());
		putMetricAlarmRequest.setUnit(StandardUnit.Count);
		putMetricAlarmRequest.setComparisonOperator(ComparisonOperator.LessThanOrEqualToThreshold);
		putMetricAlarmRequest.setThreshold(_configService.getConfigurationValueAsDouble("alarm.rtTripsTotal", 0d));
		_cloudWatchService.publishAlarm(putMetricAlarmRequest);	
		
	}

	@Override
	public void createRealtimeTripsMatchedAlarm() {
		PutMetricAlarmRequest putMetricAlarmRequest = getMetricAlarmRequest(MetricName.RealtimeTripsMatched);
		putMetricAlarmRequest.setAlarmActions(getCriticalAction());
		putMetricAlarmRequest.setUnit(StandardUnit.Count);
		putMetricAlarmRequest.setComparisonOperator(ComparisonOperator.LessThanOrEqualToThreshold);
		putMetricAlarmRequest.setThreshold(_configService.getConfigurationValueAsDouble("alarm.rtTripsMatched", 200d));
		_cloudWatchService.publishAlarm(putMetricAlarmRequest);	
		
	}
	
	@Override
	public void createRealtimeTripsUnmatchedAlarm() {
		PutMetricAlarmRequest putMetricAlarmRequest = getMetricAlarmRequest(MetricName.RealtimeTripsUnmatched);
		putMetricAlarmRequest.setAlarmActions(getCriticalAction());
		putMetricAlarmRequest.setUnit(StandardUnit.Count);
		putMetricAlarmRequest.setThreshold(_configService.getConfigurationValueAsDouble("alarm.rtTripsUnmatched", 0d));
		_cloudWatchService.publishAlarm(putMetricAlarmRequest);	
		
	}

	@Override
	public void createScheduleRealtimeDeltaAlarm() {
		PutMetricAlarmRequest putMetricAlarmRequest = getMetricAlarmRequest(MetricName.ScheduleRealtimeDelta);
		putMetricAlarmRequest.setAlarmActions(getCriticalAction());
		putMetricAlarmRequest.setUnit(StandardUnit.Count);
		putMetricAlarmRequest.setThreshold(_configService.getConfigurationValueAsDouble("alarm.rtDelta", 50d));
		_cloudWatchService.publishAlarm(putMetricAlarmRequest);	
		
	}

	@Override
	public void createRealtimeLocationsTotalPctAlarm() {
		PutMetricAlarmRequest putMetricAlarmRequest = getMetricAlarmRequest(MetricName.RealtimeLocationsTotalPct);
		putMetricAlarmRequest.setAlarmActions(getNonCriticalAction());
		putMetricAlarmRequest.setUnit(StandardUnit.Percent);
		putMetricAlarmRequest.setThreshold(_configService.getConfigurationValueAsDouble("alarm.rtLocationsTotalPct", 10d));
		_cloudWatchService.publishAlarm(putMetricAlarmRequest);	
		
	}

	@Override
	public void createRealtimeInvalidLatLonPctAlarm() {
		PutMetricAlarmRequest putMetricAlarmRequest = getMetricAlarmRequest(MetricName.RealtimeInvalidLatLonPct);
		putMetricAlarmRequest.setAlarmActions(getNonCriticalAction());
		putMetricAlarmRequest.setUnit(StandardUnit.Percent);
		putMetricAlarmRequest.setThreshold(_configService.getConfigurationValueAsDouble("alarm.rtTnvalidLatLonPct", 10d));
		_cloudWatchService.publishAlarm(putMetricAlarmRequest);	
		
	}

	@Override
	public void createRealtimeStopsMatchedPctAlarm() {
		PutMetricAlarmRequest putMetricAlarmRequest = getMetricAlarmRequest(MetricName.RealtimeStopsMatchedPct);
		putMetricAlarmRequest.setAlarmActions(getNonCriticalAction());
		putMetricAlarmRequest.setUnit(StandardUnit.Percent);
		putMetricAlarmRequest.setThreshold(_configService.getConfigurationValueAsDouble("alarm.rtStopsMatchedPct", 10d));
		_cloudWatchService.publishAlarm(putMetricAlarmRequest);	
		
	}

	@Override
	public void createRealtimeStopsUnmatchedPctAlarm() {
		PutMetricAlarmRequest putMetricAlarmRequest = getMetricAlarmRequest(MetricName.RealtimeStopsUnmatchedPct);
		putMetricAlarmRequest.setAlarmActions(getNonCriticalAction());
		putMetricAlarmRequest.setUnit(StandardUnit.Percent);
		putMetricAlarmRequest.setComparisonOperator(ComparisonOperator.LessThanOrEqualToThreshold);
		putMetricAlarmRequest.setThreshold(_configService.getConfigurationValueAsDouble("alarm.rtStopsUnmatchedPct", 0d));
		_cloudWatchService.publishAlarm(putMetricAlarmRequest);	
		
	}

	@Override
	public void createRealtimeTripTotalPctAlarm() {
		PutMetricAlarmRequest putMetricAlarmRequest = getMetricAlarmRequest(MetricName.RealtimeTripTotalPct);
		putMetricAlarmRequest.setAlarmActions(getCriticalAction());
		putMetricAlarmRequest.setUnit(StandardUnit.Percent);
		putMetricAlarmRequest.setThreshold(_configService.getConfigurationValueAsDouble("alarm.rtTripTotalPct", 10d));
		_cloudWatchService.publishAlarm(putMetricAlarmRequest);	
		
	}

	@Override
	public void createRealtimeTripsMatchedAvgAlarm() {
		PutMetricAlarmRequest putMetricAlarmRequest = getMetricAlarmRequest(MetricName.RealtimeTripsMatchedAvg);
		putMetricAlarmRequest.setAlarmActions(getNonCriticalAction());
		putMetricAlarmRequest.setUnit(StandardUnit.Count);
		putMetricAlarmRequest.setThreshold(_configService.getConfigurationValueAsDouble("alarm.rtTripsMatchedAvg", 50d));
		_cloudWatchService.publishAlarm(putMetricAlarmRequest);	
		
	}

	@Override
	public void createRealtimeBusesInServiceAlarm() {
		PutMetricAlarmRequest putMetricAlarmRequest = getMetricAlarmRequest(MetricName.RealtimeBusesInServicePct);
		putMetricAlarmRequest.setAlarmActions(getCriticalAction());
		putMetricAlarmRequest.setUnit(StandardUnit.Percent);
		putMetricAlarmRequest.setComparisonOperator(ComparisonOperator.LessThanOrEqualToThreshold);
		putMetricAlarmRequest.setThreshold(_configService.getConfigurationValueAsDouble("alarm.rtBusesInServicePct", 0d));
		_cloudWatchService.publishAlarm(putMetricAlarmRequest);	
	}

}
