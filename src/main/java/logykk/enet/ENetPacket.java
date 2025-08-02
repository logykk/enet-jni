package logykk.enet;

/**
 * Represents an ENet packet.
 */
public class ENetPacket {
    /**
     * Creates a packet that may be sent to a peer.
     * 
     * @param data initial contents of the packet
     * @param flags packet flags
     * @return a handle to the new packet
     */
    public static long create(byte[] data, int flags) {
        return ENetLibrary.packetCreate(data, data.length, flags);
    }
    
    /**
     * Destroys the packet and deallocates its data.
     * 
     * @param packet the packet handle
     */
    public static void destroy(long packet) {
        ENetLibrary.packetDestroy(packet);
    }
    
    /**
     * Gets the data from a packet.
     * 
     * @param packet the packet handle
     * @return the packet data
     */
    public static native byte[] getData(long packet);
    
    /**
     * Gets the length of the data in a packet.
     * 
     * @param packet the packet handle
     * @return the packet data length
     */
    public static native int getDataLength(long packet);
    
    /**
     * Gets the flags of a packet.
     * 
     * @param packet the packet handle
     * @return the packet flags
     */
    public static native int getFlags(long packet);
}