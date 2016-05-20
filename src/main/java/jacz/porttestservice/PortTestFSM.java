package jacz.porttestservice;

import jacz.commengine.channel.ChannelConnectionPoint;
import jacz.commengine.channel.TimedChannelFSMAction;
import jacz.peerengineservice.PeerId;
import jacz.peerengineservice.client.connection.peers.ConnectionEstablishmentServerFSM;
import jacz.peerengineservice.util.ChannelConstants;
import jacz.util.concurrency.execution_control.TrafficControl;

/**
 * FSM for testing a peer connection
 */
public class PortTestFSM implements TimedChannelFSMAction<PortTestFSM.State> {

    public enum State {
        REQUEST_SENT,
        SUCCESS,
        FAIL,
        ERROR
    }

    public static final byte LISTENING_CHANNEL = (byte) 0;

    private final PeerId peerID;

    private final TrafficControl waitUntilFinished;

    private boolean success;

    public PortTestFSM(PeerId peerID) {
        this.peerID = peerID;
        waitUntilFinished = new TrafficControl();
        waitUntilFinished.pause();
        success = false;
    }

    public State processMessage(State currentState, byte channel, Object message, ChannelConnectionPoint ccp) throws IllegalArgumentException {
        if (message instanceof String) {
            PeerId receivedPeerId = new PeerId((String) message);
            if (peerID.equals(receivedPeerId)) {
                return State.SUCCESS;
            } else {
                return State.FAIL;
            }
        } else {
            return State.ERROR;
        }
    }

    public State processMessage(State currentState, byte channel, byte[] data, ChannelConnectionPoint ccp) throws IllegalArgumentException {
        return State.ERROR;
    }

    public State init(ChannelConnectionPoint ccp) {
        ccp.write(ChannelConstants.CONNECTION_ESTABLISHMENT_CHANNEL, new ConnectionEstablishmentServerFSM.PingRequest(LISTENING_CHANNEL));
        return State.REQUEST_SENT;
    }

    public boolean isFinalState(State state, ChannelConnectionPoint ccp) {
        switch (state) {

            case SUCCESS:
                finish(true);
                return true;

            case FAIL:
            case ERROR:
                finish(false);
                return true;

            default:
                return false;
        }
    }

    public void disconnected(ChannelConnectionPoint ccp) {
        finish(false);
    }

    public void timedOut(State state, ChannelConnectionPoint ccp) {
        finish(false);
    }

    public void waitUntilFinished() {
        waitUntilFinished.access();
    }

    private void finish(boolean success) {
        this.success = success;
        waitUntilFinished.resume();
    }

    public boolean isSuccess() {
        return success;
    }
}
