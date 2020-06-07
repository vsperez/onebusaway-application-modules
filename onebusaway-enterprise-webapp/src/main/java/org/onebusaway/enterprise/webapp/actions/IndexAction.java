/**
 * Copyright (C) 2011 Metropolitan Transportation Authority
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
package org.onebusaway.enterprise.webapp.actions;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.omg.CORBA.Object;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.presentation.impl.cities.CityServiceImpl;
import org.onebusaway.presentation.services.realtime.RealtimeService;
import org.onebusaway.presentation.services.routes.RouteListService;
import org.onebusaway.util.services.configuration.ConfigurationService;
import org.onebusaway.transit_data.model.CityBean;
import org.onebusaway.transit_data.model.CityRoutesBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionProxy;

/**
 * Action for home page
 * 
 */
public class IndexAction extends OneBusAwayEnterpriseActionSupport {

  private static final long serialVersionUID = 1L;
  
  @Autowired
  CityServiceImpl _cityService;

  @Autowired
  private ConfigurationService _configurationService;
  
  @Autowired
  private RealtimeService _realtimeService;
  
  
  @Autowired
  private RouteListService _routeListService;
  ObjectMapper mapper=new ObjectMapper();
  public List<CityRoutesBean> getCityRoutes()
  {
	  return _cityService.getCitiesWithRouteList();
//	  try
//	  {
//		  
//		 Map<String,String> cityMap=new HashMap<String,String>();
//		 for(CityBean city:_cityService.getAvailableCities())
//		 {
//		
//			 List<AgencyAndId> listaIdRecorridos = _cityService.getCityRouteList(city.getId());
//			 String list="";
//			 for(AgencyAndId ids:listaIdRecorridos)
//			 {
//				 list+=ids.getId()+";";
//			 }
//			 if(!list.isEmpty() && list.length()>2)
//				 list=list.substring(0,list.length()-2);
//			 cityMap.put(city.getName(),list);
//		//  List<RouteBean> routes = _routeListService.getRoutes();
//		 }
//	  //return mapper.writeValueAsString(cityMap);
//	  }
//	  catch (Exception e) {
//		e.printStackTrace();
//		return "";
//	}
  }

  public String getGoogleMapsClientId() {
    return _configurationService.getConfigurationValueAsString("display.googleMapsClientId", "");    
  }
  
  public String getGoogleMapsChannelId() {
	  return _configurationService.getConfigurationValueAsString("display.googleMapsChannelId", "");    
  }

  public String getGoogleAdClientId() {
	return _configurationService.getConfigurationValueAsString("display.googleAdsClientId", "");    
  }

  public String getGoogleMapsApiKey() {
    return _configurationService.getConfigurationValueAsString("display.googleMapsApiKey", "");
  }

  public List<ServiceAlertBean> getGlobalServiceAlerts() {
    List<ServiceAlertBean> results = _realtimeService.getServiceAlertsGlobal();
    return (results != null && results.size() > 0) ? results : null;
  }

  @Override
  public String execute() throws Exception {
    ActionContext context = ActionContext.getContext();
    ActionInvocation invocation = context.getActionInvocation();
    ActionProxy proxy = invocation.getProxy();

    String name = proxy.getActionName().toLowerCase();
    String namespace = proxy.getNamespace().toLowerCase();

    // FIXME: since Struts doesn't seem to like wildcard namespaces (in wiki/IndexAction) and default
    // actions, we have to have this action check to see if it's being called as a "default" action and
    // return the 404 message if so. There has to be a better way than this? 
    if((name.equals("") || name.equals("index")) && (namespace.equals("") || namespace.equals("/"))) {
    	return SUCCESS;
    }

    return "NotFound";
  }
  public String getMapInstance() {
	    return _configurationService.getConfigurationValueAsString("display.mapInstance", "google");
  }

}
