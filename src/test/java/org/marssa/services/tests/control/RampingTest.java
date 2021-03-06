package org.marssa.services.tests.control;
/**
 * Copyright 2012 MARSEC-XL International Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
//package org.marssa.services.control;
//
//import org.marssa.footprint.datatypes.decimal.MFloat;
//import org.marssa.footprint.datatypes.integer.MInteger;
//import org.marssa.footprint.exceptions.ConfigurationError;
//import org.marssa.footprint.exceptions.NoConnection;
//import org.marssa.footprint.exceptions.OutOfRange;
//import org.marssa.footprint.interfaces.control.IController;
//import org.marssa.services.constants.ServicesTestConstants;
//import org.marssa.services.control.Ramping.RampingType;
//
//import org.restlet.Application;
//import org.restlet.Component;
//import org.restlet.Request;
//import org.restlet.Response;
//import org.restlet.Restlet;
//import org.restlet.data.MediaType;
//import org.restlet.data.Protocol;
//import org.restlet.data.Status;
//import org.restlet.routing.Router;
//
//public class RampingTest {
//
//	static private class TestController implements IController {
//		private Ramping ramping;
//
//		public TestController() {
//			try {
//				ramping = new Ramping(new MInteger(50), new MFloat(1.0f), this, RampingType.ACCELERATED);
//			} catch (ConfigurationError e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (OutOfRange e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (NoConnection e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//
//		public void rampTo(MFloat desiredValue) throws InterruptedException, ConfigurationError, OutOfRange {
//			ramping.rampTo(desiredValue);
//		}
//
//		public void outputValue(MFloat value) throws ConfigurationError,
//				OutOfRange, NoConnection {
//			// TODO Auto-generated method stub
//			System.out.println(value);
//		}
//
//		public void setPolaritySignal(Polarity polarity)
//				throws NoConnection {
//			// TODO Auto-generated method stub
//
//		}
//
//		public MFloat getValue() {
//			// TODO Auto-generated method stub
//			return null;
//		}
//	}
//	private static class RampingTestApplication extends Application {
//	    /**
//	     * Creates a root Restlet that will receive all incoming calls.
//	     */
//
//		TestController controller = new TestController();
//	    @Override
//	    public synchronized Restlet createInboundRoot() {
//	        Router router = new Router(getContext());
//
//	        // Create the navigation lights state handler
//	        Restlet lightState = new Restlet() {
//	        	@Override
//	            public void handle(Request request, Response response) {
//
//					try {
//	        			double value = Float.parseFloat(request.getAttributes().get("desiredValue").toString());
//	        			controller.rampTo(new MFloat(value));
//	        			response.setEntity("Ramping motor speed to " + value + "%", MediaType.TEXT_PLAIN);
//	        		} catch (NumberFormatException e) {
//	        			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "The value of the speed resource has an incorrect format");
//	        		} catch (InterruptedException e) {
//	        			response.setStatus(Status.INFO_PROCESSING, "The ramping algorithm has been interrupted");
//	        			e.printStackTrace();
//					} catch (ConfigurationError e) {
//						response.setStatus(Status.SERVER_ERROR_INTERNAL, "The request has returned a ConfigurationError");
//						e.printStackTrace();
//					} catch (OutOfRange e) {
//						response.setStatus(Status.SERVER_ERROR_INTERNAL, "The specified value is out of range");
//						e.printStackTrace();
//					}
//	            }
//	        };
//
//	        router.attach("/ramping/{desiredValue}", lightState);
//
//	        return router;
//	    }
//	}
//
//	/**
//	 * @param args the args 
//	 */
//	public static void main(java.lang.String[] args) {
//		// Create a new Component
//		Component component = new Component();
//
//	    // Add a new HTTP server listening on the given port
//	    component.getServers().add(Protocol.HTTP, ServicesTestConstants.WEB_SERVICES.HOST.getContents(), ServicesTestConstants.WEB_SERVICES.PORT.getValue());
//
//		// Attach the motion control feedback application
//	    component.getDefaultHost().attach("/testing", new RampingTestApplication());
//
//	    // Start the component
//	    try {
//			component.start();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//}
