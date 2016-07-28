/*
*    Document   : RestController.java
*    Created on : Jul 18, 2016, 12:02:43 PM
*    Author     : H3387
*/
package iotitude.com;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.core.MediaType;
import javax.servlet.http.*;
import java.net.*;
import javax.ws.rs.core.Response;
import javax.servlet.http.Cookie;
import java.io.IOException;
import java.util.HashMap;


// AdminKamu Imports
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import org.kaaproject.kaa.client.DesktopKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.SimpleKaaClientStateListener;
import org.kaaproject.kaa.client.event.EventFamilyFactory;
import org.kaaproject.kaa.client.event.FindEventListenersCallback;
import org.kaaproject.kaa.client.event.registration.UserAttachCallback;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachResponse;
import org.kaaproject.kaa.schema.sample.event.kamu.ChangeProfile;
import org.kaaproject.kaa.schema.sample.event.kamu.KaMUEventClassFamily;
import org.kaaproject.kaa.schema.sample.event.kamu.RegistrationAnswer;
import org.kaaproject.kaa.schema.sample.event.kamu.RegistrationRequest;
import org.kaaproject.kaa.schema.sample.event.kamu.RestartDevice;
import org.kaaproject.kaa.schema.sample.event.kamu.UpdateDevice;

@Path("controller")
public class RestController {
    
    static int profileID;
    static KaaClient kaaClient = Kaa.newClient(new DesktopKaaPlatformContext(), new SimpleKaaClientStateListener());
    static String target;
    static BaasBoxController bbc = new BaasBoxController();
    static String session = bbc.logIn();
    static Map<String, String> devices = getDevices(session);

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of RestController
     */
    public RestController() {
        try {
            kaaClient.start();
            bbc.updateAdminHash(session, kaaClient.getEndpointKeyHash());
            attachUser();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Retrieves representation of an instance of iotitude.com.RestController
     * @return an instance of java.lang.String
     */
    @GET
    @Produces("text/html")
    public String getHtml() {
        //TODO return proper representation object
        
        return "<html><body><img src=\"../images/8a8.jpg\" alt=\"DAT BOI\" style=\"width:304px;height:228px;\"></body></html>";
    }

    
    @POST
    @Path("sendProfileAll")
    @Produces("text/html")
    public void sendProfileToAll(){
        final EventFamilyFactory eventFamilyFactory = kaaClient.getEventFamilyFactory();
        final KaMUEventClassFamily tecf = eventFamilyFactory.getKaMUEventClassFamily();
        tecf.sendEventToAll(new ChangeProfile(1, 1));
        System.out.println("Change profile request sent");
    }
    
    // POST method for updating KaMU devices
    @POST
    @Path("updateDevice")
    @Produces("text/html")
    public void updateDevice(@Context HttpServletRequest request, @Context HttpServletResponse response) {
        String Version = request.getParameter("versionTag");
        String Date = request.getParameter("datetime");
        String Hash = request.getParameter("hash");
        
        try {
            String responseToClient = "Version: " + Version + " Date: " + Date + " Hash: " + Hash;
            
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(responseToClient);
            response.getWriter().flush();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // POST method for restarting KaMU devices
    @POST
    @Path("restartDevice")
    @Produces("text/html")
    public void restartDevice(@Context HttpServletRequest request, @Context HttpServletResponse response) {
        String Date = request.getParameter("datetime");
        String Hash = request.getParameter("hash");
        
        try {
            String responseToClient = "Date: " + Date + " Hash: " + Hash;
            
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(responseToClient);
            response.getWriter().flush();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // POST method for changing the profile of KaMU devices
    @POST
    @Path("changeProfile")
    @Produces("text/html")
    public void profileChanger(@Context HttpServletRequest request, @Context HttpServletResponse response) {
        //String Target = request.getParameter("target");
        
        String Profile = request.getParameter("profileId");
        String Date = request.getParameter("datetime");
        String Hash = request.getParameter("hash");
        
        System.out.println("asd");
        //createKaaClient();
        sendProfileToSingleTarget(Hash);
        
        try {
            String responseToClient = "Profile: " + Profile + " Date: " + Date + " Hash: " + Hash;
            
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(responseToClient);
            response.getWriter().flush();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    public static void sendProfileToSingleTarget(final String target){
        
        List<String> FQNs = new LinkedList<>();
        FQNs.add(ChangeProfile.class.getName());
        final EventFamilyFactory eventFamilyFactory = kaaClient.getEventFamilyFactory();
        final KaMUEventClassFamily tecf = eventFamilyFactory.getKaMUEventClassFamily();
        kaaClient.findEventListeners(FQNs, new FindEventListenersCallback() {
            @Override
            public void onEventListenersReceived(List<String> eventListeners) {
                ChangeProfile ctc = new ChangeProfile(1, 1);
                // Assume the target variable is one of the received in the findEventListeners method
                tecf.sendEvent(ctc, target);
                System.out.println(target + " target");
            }   
        
            @Override
            public void onRequestFailed() {
                System.out.println("Send profile request failed");
            }
        });
    }
    
    public static void attachUser(){
        kaaClient.attachUser("asd", "asd", new UserAttachCallback() {
            @Override
            public void onAttachResult(UserAttachResponse response) {
                System.out.println("Attach response " + response.getResult());
                
                if (response.getResult() == SyncResponseResultType.SUCCESS){
                    System.out.println("User attached");
                    receiveEvents();
                }
                else{
                    kaaClient.stop();
                    System.out.println("Stopped");
                }
            }
        });
    }
    
    public static void receiveEvents(){
        final EventFamilyFactory eventFamilyFactory = kaaClient.getEventFamilyFactory();
        final KaMUEventClassFamily tecf = eventFamilyFactory.getKaMUEventClassFamily();
        System.out.println("Listening to incoming events");
        tecf.addListener(new KaMUEventClassFamily.Listener() {
            @Override
            public void onEvent(ChangeProfile event, String source) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            } 
            
            @Override
            public void onEvent(RegistrationRequest event, String source) {
                System.out.println("Event received from: " + source);
                String mac = event.getMac();
                String hash = event.getHash();
                System.out.println(mac + " " + hash);
                registerDevice(mac, hash);
            }

            @Override
            public void onEvent(RegistrationAnswer event, String source) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void onEvent(UpdateDevice event, String source) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void onEvent(RestartDevice event, String source) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
                
   }
    
   public static void registerDevice(String mac, String hash){
        if (devices.containsValue(hash)) {
            System.out.println("Device already registered.");
            sendRegistrationAnswer(hash, true);
        }
        else {
            System.out.println("Registering device.");
            boolean status = bbc.updateDeviceHash(session, mac, hash);
            System.out.println(status);
            if (status) {
                sendRegistrationAnswer(hash, status);
            }
            else {
                System.out.println("Error trying to register device.");
                sendRegistrationAnswer(hash, status);
            } 
        }
    }
   
   public static void sendRegistrationAnswer(String target, boolean status) {
        final EventFamilyFactory eventFamilyFactory = kaaClient.getEventFamilyFactory();
        final KaMUEventClassFamily tecf = eventFamilyFactory.getKaMUEventClassFamily();
        RegistrationAnswer ctc = new RegistrationAnswer(status, 50, 1);
        tecf.sendEvent(ctc, target);
        System.out.println("Device registration answer sent");
   }
   
   public static Map<String, String> getDevices(String session) {
        JSONArray data = bbc.getDeviceInfo(session, "Master");
        Map<String, String> devices = new HashMap();
        for (int i = 0; i < data.length(); i++) {
            JSONObject object = data.getJSONObject(i);
            devices.put(object.getString("mac"), object.getString("hash"));
        }
        return devices;
    }
}
