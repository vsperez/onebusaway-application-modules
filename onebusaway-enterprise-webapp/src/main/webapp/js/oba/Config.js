/*
 * Copyright (c) 2011 Metropolitan Transportation Authority
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

var _gaq = _gaq || [];
var OBA = window.OBA || {};

// This is mostly for IE7, to make sure AJAX/JSON calls are not cached. 
$.ajaxSetup({ cache: false });

OBA.Config = {
		autocompleteUrl: "api/autocomplete",
		searchUrl: "api/search",
		configUrl: "api/config",
		stopsWithinBoundsUrl: "api/stops-within-bounds",
		stopsOnRouteForDirection: "api/stops-on-route-for-direction",
		stopForId: "api/stop-for-id",
		apiUrl: "/onebusaway-api-webapp/api",
		legacyUrl: "../",
		urlPrefix: "",
    	searchParamsPrefix: "#",
		includeBubbleFooter: true,
		hasSidebar: true,
	
		// siriSMUrl and siriVMUrl now moved to config.jspx
		
		refreshInterval: 15000,
		
		// This variable is overwritten by the configuration service--the JS found at configUrl (above)
		staleTimeout: 120,

		// This method is called by the JS found at configUrl (above) when the configuration has finished loading.
		configurationHasLoaded: function() {
			_gaq.push(['_setAccount', OBA.Config.googleAnalyticsSiteId]);
			_gaq.push(['_setDomainName', 'none']);
			_gaq.push(['_setAllowLinker', true]);
			_gaq.push(['_trackPageview']);
			(function() {var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);})();
		},
		
		// For debugging: set to an ISO time string to make UI request from another time
		time: null,		
		
		// Called by UI functions that want to send events to GA
		analyticsFunction: function(type, value) {
			_gaq.push(['_trackEvent', "Desktop Web", type, value]);
		},
		
		loadLocationIcons: function() {
			if (google===undefined || google.maps === undefined)
			{
				//  shadowUrl: 'leaf-shadow.png',
				// shadowSize:   [50, 64], // size of the shadow
				var locationIcons = [], activeLocationIcons = [];
				var normalLocationIcon = L.icon({
				    iconUrl: "img/location/location.png",
				  

				    iconSize:     [24, 32], // size of the icon
				   
				    iconAnchor:   [0, 0], // point of the icon which will correspond to marker's location
				    shadowAnchor: [4, 62],  // the same for the shadow
				    popupAnchor:  [-3, -76] // point from which the popup should open relative to the iconAnchor
				});
				
				var activeLocationIcon = L.icon({
				    iconUrl: "img/location/location_active.png",
				  

				    iconSize:     [24, 32], // size of the icon
				   
				    iconAnchor:   [0, 0], // point of the icon which will correspond to marker's location
				    shadowAnchor: [4, 62],  // the same for the shadow
				    popupAnchor:  [-3, -76] // point from which the popup should open relative to the iconAnchor
				});
				
				locationIcons[0] = normalLocationIcon;
				activeLocationIcons[0] = activeLocationIcon;
				
				for (var i=1; i < 10; i++) {
					var numberedLocationIcon = L.icon({iconUrl: "img/location/location_" + i + ".png", iconSize:     [24, 32]});
					var activeNumberedLocationIcon = L.icon({iconUrl: "img/location/location_active_" + i + ".png", iconSize:     [24, 32]});
					locationIcons[i] = numberedLocationIcon;
					activeLocationIcons[i] = activeNumberedLocationIcon;
				}
				var shadowImage =L.icon({iconUrl: "img/location/shadow.png",iconSize:     [24, 32]});
					
				return  [locationIcons, activeLocationIcons, shadowImage];
			}
			var locationIcons = [], activeLocationIcons = [];
			var size = new google.maps.Size(24, 32), 
				o_point = new google.maps.Point(0,0), 
				mid_point = new google.maps.Point(12, 32);

			var normalLocationIcon = new google.maps.MarkerImage("img/location/location.png",
		            size, o_point, mid_point);
			var activeLocationIcon = new google.maps.MarkerImage("img/location/location_active.png",
					size, o_point, mid_point);
			
			locationIcons[0] = normalLocationIcon;
			activeLocationIcons[0] = activeLocationIcon;
			
			for (var i=1; i < 10; i++) {
				var numberedLocationIcon = new google.maps.MarkerImage("img/location/location_" + i + ".png",
						size, o_point, mid_point);
				var activeNumberedLocationIcon = new google.maps.MarkerImage("img/location/location_active_" + i + ".png",
						size, o_point, mid_point);
				
				locationIcons[i] = numberedLocationIcon;
				activeLocationIcons[i] = activeNumberedLocationIcon;
			}
			var shadowImage = new google.maps.MarkerImage('img/location/shadow.png',
					size, o_point, mid_point);
			
			return [locationIcons, activeLocationIcons, shadowImage];
		}
};
