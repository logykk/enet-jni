package logykk.enet;

/**
 * Represents an ENet event.
 */
public class ENetEvent {
    private int type;           // Event type (one of the ENET_EVENT_TYPE_* constants)
    private long peer;          // Peer that generated the event
    private int channelID;      // Channel on which the event occurred
    private int data;           // User-supplied data
    private long packet;        // Packet associated with the event (if any)
    
    /**
     * Creates a new ENet event.
     */
    public ENetEvent() {
        this.type = ENetLibrary.ENET_EVENT_TYPE_NONE;
        this.peer = 0;
        this.channelID = 0;
        this.data = 0;
        this.packet = 0;
    }
    
    /**
     * Gets the event type.
     * 
     * @return the event type (one of the ENET_EVENT_TYPE_* constants)
     */
    public int getType() {
        return type;
    }
    
    /**
     * Sets the event type.
     * 
     * @param type the event type
     */
    void setType(int type) {
        this.type = type;
    }
    
    /**
     * Gets the peer that generated the event.
     * 
     * @return the peer handle
     */
    public long getPeer() {
        return peer;
    }
    
    /**
     * Sets the peer that generated the event.
     * 
     * @param peer the peer handle
     */
    void setPeer(long peer) {
        this.peer = peer;
    }
    
    /**
     * Gets the channel on which the event occurred.
     * 
     * @return the channel ID
     */
    public int getChannelID() {
        return channelID;
    }
    
    /**
     * Sets the channel on which the event occurred.
     * 
     * @param channelID the channel ID
     */
    void setChannelID(int channelID) {
        this.channelID = channelID;
    }
    
    /**
     * Gets the user-supplied data.
     * 
     * @return the data
     */
    public int getData() {
        return data;
    }
    
    /**
     * Sets the user-supplied data.
     * 
     * @param data the data
     */
    void setData(int data) {
        this.data = data;
    }
    
    /**
     * Gets the packet associated with the event.
     * 
     * @return the packet handle
     */
    public long getPacket() {
        return packet;
    }
    
    /**
     * Sets the packet associated with the event.
     * 
     * @param packet the packet handle
     */
    void setPacket(long packet) {
        this.packet = packet;
    }
    
    /**
     * Gets the data from the packet associated with this event.
     * This is a convenience method that extracts the data from the packet.
     * 
     * @return the packet data or null if there is no packet
     */
    public byte[] getPacketData() {
        if (packet == 0) {
            return null;
        }
        return ENetPacket.getData(packet);
    }
    
    /**
     * Gets the length of the data in the packet associated with this event.
     * 
     * @return the packet data length or 0 if there is no packet
     */
    public int getPacketDataLength() {
        if (packet == 0) {
            return 0;
        }
        return ENetPacket.getDataLength(packet);
    }
}