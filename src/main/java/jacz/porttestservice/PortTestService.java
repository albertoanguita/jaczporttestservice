package jacz.porttestservice;

import com.google.gson.Gson;
import jacz.commengine.channel.ChannelAction;
import jacz.commengine.channel.ChannelConnectionPoint;
import jacz.commengine.clientserver.client.ClientModule;
import jacz.commengine.communication.CommError;
import jacz.peerengineservice.PeerID;
import jacz.util.network.IP4Port;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Port test service
 */
public class PortTestService extends HttpServlet {

    private static class ChannelActionImpl implements ChannelAction {

        @Override
        public void newMessage(ChannelConnectionPoint ccp, byte channel, Object message) {
            // ignore
        }

        @Override
        public void newMessage(ChannelConnectionPoint ccp, byte channel, byte[] data) {
            // ignore
        }

        @Override
        public void channelFreed(ChannelConnectionPoint ccp, byte channel) {
            // ignore
        }

        @Override
        public void disconnected(ChannelConnectionPoint ccp, boolean expected) {
            // ignore
        }

        @Override
        public void error(ChannelConnectionPoint ccp, CommError e) {
            // ignore
        }
    }

    public static final class Result {

        private final String result;

        public Result(String result) {
            this.result = result;
        }

        public String getResult() {
            return result;
        }
    }

    private final static int CONNECTION_MAX_WAIT = 4000;

    private final static long FSM_MAX_WAIT = 3000;

    private final static String IP = "ip";

    private final static String PORT = "port";

    private final static String PEER_ID = "peerid";

    private final static Result RESULT_OK = new Result("OK");

    private final static Result RESULT_TEST_FAILED = new Result("TEST_FAILED");

    private final static Result RESULT_BAD_REQUEST_FORMAT = new Result("BAD_REQUEST_FORMAT");

    private final static Result RESULT_COULD_NOT_CONNECT = new Result("COULD_NOT_CONNECT");


    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {
        String ip = request.getParameter(IP);
        String portStr = request.getParameter(PORT);
        String peerIDStr = request.getParameter(PEER_ID);
        if (ip == null || portStr == null || peerIDStr == null) {
            System.out.println(RESULT_BAD_REQUEST_FORMAT.result);
            writeResponse(response, RESULT_BAD_REQUEST_FORMAT);
        } else {
            System.out.println("Request received: " + ip + ", " + portStr + ", " + peerIDStr);
            try {
                int port = Integer.parseInt(portStr);
                if (port < 0 || port > 65535) {
                    throw new NumberFormatException();
                }
                PeerID peerID = new PeerID(peerIDStr);
                PortTestFSM portTestFSM = new PortTestFSM(peerID);
                Set<Set<Byte>> concurrentChannels = new HashSet<>();
                Set<Byte> listeningChannel = new HashSet<>();
                listeningChannel.add(PortTestFSM.LISTENING_CHANNEL);
                concurrentChannels.add(listeningChannel);
                ClientModule clientModule = new ClientModule(new IP4Port(ip, port), new ChannelActionImpl(), concurrentChannels);
                ChannelConnectionPoint ccp = clientModule.connect(CONNECTION_MAX_WAIT);
                clientModule.start();
                if (ccp.registerTimedFSM(portTestFSM, FSM_MAX_WAIT, PortTestFSM.LISTENING_CHANNEL) != null) {
                    portTestFSM.waitUntilFinished();
                    if (portTestFSM.isSuccess()) {
                        System.out.println(RESULT_OK.result);
                        writeResponse(response, RESULT_OK);
                    } else {
                        System.out.println(RESULT_TEST_FAILED.result);
                        writeResponse(response, RESULT_TEST_FAILED);
                    }
                } else {
                    // disconnected
                    System.out.println(RESULT_COULD_NOT_CONNECT.result);
                    writeResponse(response, RESULT_COULD_NOT_CONNECT);
                }
                ccp.disconnect();
            } catch (IllegalArgumentException e) {
                System.out.println(RESULT_BAD_REQUEST_FORMAT.result);
                writeResponse(response, RESULT_BAD_REQUEST_FORMAT);
            } catch (IOException e) {
                System.out.println(RESULT_COULD_NOT_CONNECT.result);
                writeResponse(response, RESULT_COULD_NOT_CONNECT);
            }
        }
    }

    private void writeResponse(HttpServletResponse response, Result result) throws IOException {
        Gson gson = new Gson();
        String jsonResponse = gson.toJson(result);
        response.getWriter().write(jsonResponse);
    }
}