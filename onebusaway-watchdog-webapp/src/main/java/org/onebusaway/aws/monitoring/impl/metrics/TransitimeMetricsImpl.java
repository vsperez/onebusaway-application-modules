package org.onebusaway.aws.monitoring.impl.metrics;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.onebusaway.aws.monitoring.model.metrics.MetricName;
import org.onebusaway.aws.monitoring.service.metrics.TransitimeMetrics;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Component
public class TransitimeMetricsImpl extends MetricsTemplate implements TransitimeMetrics, ApplicationListener<ContextRefreshedEvent> {

	private String transitime_url = "";
	
	private void reloadConfig(){
		transitime_url = _configurationService.getConfigurationValueAsString("monitoring.transitimeUrl", "http://gtfsrt.dev.wmata.obaweb.org:8080/api/v1/key/4b248c1b/command/");
	}
	
	@Override
	public void publishGtfsRtMetric() {

		double metric = 1;
		try {
			JsonObject agencyList = getJsonObject(transitime_url + "agencies?format=json");
			JsonArray agencies = agencyList.get("agency").getAsJsonArray();
			for(JsonElement agencyElement : agencies){
				JsonObject agency = agencyElement.getAsJsonObject();
				if(agency != null){
					if (agency.get("name").getAsString().equalsIgnoreCase("WMATA")) {
						metric = 0;
						break;
					}
				}
			}
			publishMetric(MetricName.TransitimeApiErrorResponse, StandardUnit.Count, metric);
		} catch (MalformedURLException mue) {
			_log.error(mue.getMessage());
			return;
		} catch (IOException ioe) {
			_log.warn("Error communicating with specified url : "
					+ transitime_url + "agencies?format=json");
			publishMetric(MetricName.TransitimeApiErrorResponse, StandardUnit.Count, metric);
		}
		catch(NullPointerException npe){
			_log.warn("Error reading the agenciesList");
		}
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		reloadConfig();
		
	}

}
