package logykk.enet;

/**
 * JNI bindings for the ENet networking library.
 */
public class ENetLibrary {
    static {
        try {
            String osName = System.getProperty("os.name").toLowerCase();
            String osArch = System.getProperty("os.arch").toLowerCase();
            String nativePath;
            String libName;

            boolean isArm64 = osArch.equals("aarch64") || osArch.contains("arm64");
            String archSuffix = isArm64 ? "arm64" : "x86-64";
            
            if (osName.contains("win")) {
                nativePath = "/natives/windows-" + archSuffix + "/";
                libName = "enet-jni.dll";
            } else if (osName.contains("mac") || osName.contains("darwin")) {
                nativePath = "/natives/macos-" + archSuffix + "/";
                libName = "libenet-jni.dylib";
            } else {
                nativePath = "/natives/linux-" + archSuffix + "/";
                libName = "libenet-jni.so";
            }

            try {
                System.loadLibrary("enet-jni");
            } catch (UnsatisfiedLinkError e) {
                loadFromResource(nativePath + libName);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load native library", e);
        }
    }
    
    private static void loadFromResource(String resourcePath) throws Exception {
        java.io.InputStream is = ENetLibrary.class.getResourceAsStream(resourcePath);
        if (is == null) {
            throw new RuntimeException("Native library not found: " + resourcePath);
        }

        String fileName = resourcePath.substring(resourcePath.lastIndexOf('/') + 1);
        java.io.File tempFile = java.io.File.createTempFile("enet-jni-", fileName);
        tempFile.deleteOnExit();

        try (java.io.FileOutputStream os = new java.io.FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int readBytes;
            while ((readBytes = is.read(buffer)) != -1) {
                os.write(buffer, 0, readBytes);
            }
        }

        System.load(tempFile.getAbsolutePath());
    }
    
    // Version constants
    public static final int ENET_VERSION_MAJOR = 2;
    public static final int ENET_VERSION_MINOR = 6;
    public static final int ENET_VERSION_PATCH = 5;
    
    // Packet flag constants
    public static final int ENET_PACKET_FLAG_RELIABLE = (1);
    public static final int ENET_PACKET_FLAG_UNSEQUENCED = (1 << 1);
    public static final int ENET_PACKET_FLAG_NO_ALLOCATE = (1 << 2);
    public static final int ENET_PACKET_FLAG_UNRELIABLE_FRAGMENT = (1 << 3);
    public static final int ENET_PACKET_FLAG_UNTHROTTLED = (1 << 4);
    public static final int ENET_PACKET_FLAG_SENT = (1 << 8);
    
    // Peer state constants
    public static final int ENET_PEER_STATE_DISCONNECTED = 0;
    public static final int ENET_PEER_STATE_CONNECTING = 1;
    public static final int ENET_PEER_STATE_ACKNOWLEDGING_CONNECT = 2;
    public static final int ENET_PEER_STATE_CONNECTION_PENDING = 3;
    public static final int ENET_PEER_STATE_CONNECTION_SUCCEEDED = 4;
    public static final int ENET_PEER_STATE_CONNECTED = 5;
    public static final int ENET_PEER_STATE_DISCONNECT_LATER = 6;
    public static final int ENET_PEER_STATE_DISCONNECTING = 7;
    public static final int ENET_PEER_STATE_ACKNOWLEDGING_DISCONNECT = 8;
    public static final int ENET_PEER_STATE_ZOMBIE = 9;
    
    // Event type constants
    public static final int ENET_EVENT_TYPE_NONE = 0;
    public static final int ENET_EVENT_TYPE_CONNECT = 1;
    public static final int ENET_EVENT_TYPE_DISCONNECT = 2;
    public static final int ENET_EVENT_TYPE_RECEIVE = 3;
    
    /**
     * Initializes ENet.
     * @return 0 on success, < 0 on failure
     */
    public static native int initialize();
    
    /**
     * Deinitializes ENet.
     */
    public static native void deinitialize();
    
    /**
     * Creates a host for communicating with peers.
     * 
     * @param address the address at which other peers may connect to this host or null for no external connections
     * @param peerCount the maximum number of peers that should be allocated for the host
     * @param channelLimit the maximum number of channels allowed
     * @param incomingBandwidth downstream bandwidth in bytes/second; 0 for unlimited
     * @param outgoingBandwidth upstream bandwidth in bytes/second; 0 for unlimited
     * @return a new ENetHost instance or null on failure
     */
    public static native long hostCreate(ENetAddress address, int peerCount, int channelLimit, int incomingBandwidth, int outgoingBandwidth);
    
    /**
     * Destroys the host and all resources associated with it.
     * 
     * @param host the host to destroy
     */
    public static native void hostDestroy(long host);
    
    /**
     * Initiates a connection to a foreign host.
     * 
     * @param host the host that will be connecting
     * @param address destination for the connection
     * @param channelCount number of channels to allocate
     * @param data user data supplied to the receiving host
     * @return a new peer representing the foreign host or null on failure
     */
    public static native long hostConnect(long host, ENetAddress address, int channelCount, int data);
    
    /**
     * Checks for any queued events on the host and dispatches one if available.
     * 
     * @param host the host to check for events
     * @param event an event structure where event details will be placed if available
     * @param timeout optional timeout in milliseconds
     * @return > 0 if an event occurred within the specified time, 0 if no events are available, < 0 on failure
     */
    public static native int hostService(long host, ENetEvent event, int timeout);
    
    /**
     * Sends any queued packets on the host.
     * 
     * @param host the host to flush
     */
    public static native void hostFlush(long host);
    
    /**
     * Creates a packet that may be sent to a peer.
     * 
     * @param data initial contents of the packet
     * @param dataLength length of the data
     * @param flags packet flags
     * @return a new packet or null on failure
     */
    public static native long packetCreate(byte[] data, int dataLength, int flags);
    
    /**
     * Destroys the packet and deallocates its data.
     * 
     * @param packet the packet to destroy
     */
    public static native void packetDestroy(long packet);
    
    /**
     * Queues a packet to be sent to all peers associated with the host.
     * 
     * @param host the host on which to broadcast the packet
     * @param channelID the channel on which to broadcast
     * @param packet the packet to broadcast
     */
    public static native void hostBroadcast(long host, int channelID, long packet);
    
    /**
     * Queues a packet to be sent to a peer.
     * 
     * @param peer destination for the packet
     * @param channelID channel on which to send
     * @param packet the packet to send
     * @return 0 on success, < 0 on failure
     */
    public static native int peerSend(long peer, int channelID, long packet);
    
    /**
     * Forcefully disconnects a peer.
     * 
     * @param peer the peer to disconnect
     * @param data data describing the disconnection
     */
    public static native void peerDisconnect(long peer, int data);
    
    /**
     * Request a disconnection from a peer, but don't notify the foreign host immediately.
     * 
     * @param peer the peer to disconnect later
     * @param data data describing the disconnection
     */
    public static native void peerDisconnectLater(long peer, int data);
    
    /**
     * Forcefully disconnects a peer immediately.
     * 
     * @param peer the peer to disconnect
     * @param data data describing the disconnection
     */
    public static native void peerDisconnectNow(long peer, int data);
    
    /**
     * Gets the IP address and port of a peer.
     * 
     * @param peer the peer to get the address from
     * @return the peer's address
     */
    public static native ENetAddress peerGetAddress(long peer);
    
    /**
     * Gets the round trip time (ping) to a peer.
     * 
     * @param peer the peer to get the RTT from
     * @return the round trip time in milliseconds
     */
    public static native int peerGetRoundTripTime(long peer);
    
    /**
     * Gets the state of a peer.
     * 
     * @param peer the peer to get the state from
     * @return the peer's state (one of the ENET_PEER_STATE_* constants)
     */
    public static native int peerGetState(long peer);
}