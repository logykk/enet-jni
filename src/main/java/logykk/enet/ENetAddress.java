package logykk.enet;

/**
 * Represents an ENet address (IPv4 or IPv6).
 */
public class ENetAddress {
    private final byte[] host;  // Represents the in6_addr structure (16 bytes)
    private short port;   // Port in host byte order
    private short scopeId; // IPv6 scope ID
    
    /**
     * Creates a new ENet address.
     */
    public ENetAddress() {
        this.host = new byte[16];  // Size of in6_addr
        this.port = 0;
        this.scopeId = 0;
    }
    
    /**
     * Creates a new ENet address with the specified host and port.
     * 
     * @param host IPv4 or IPv6 address bytes
     * @param port port number
     * @param scopeId IPv6 scope ID
     */
    public ENetAddress(byte[] host, short port, short scopeId) {
        if (host.length != 16) {
            throw new IllegalArgumentException("Host address must be 16 bytes (IPv6 format)");
        }
        this.host = host.clone();
        this.port = port;
        this.scopeId = scopeId;
    }
    
    /**
     * Creates an IPv4 address.
     * 
     * @param a first octet
     * @param b second octet
     * @param c third octet
     * @param d fourth octet
     * @param port port number
     * @return a new ENetAddress
     */
    public static ENetAddress createIPv4(byte a, byte b, byte c, byte d, short port) {
        byte[] host = new byte[16];
        // IPv4-mapped IPv6 address format: ::ffff:a.b.c.d
        host[10] = (byte) 0xff;
        host[11] = (byte) 0xff;
        host[12] = a;
        host[13] = b;
        host[14] = c;
        host[15] = d;
        return new ENetAddress(host, port, (short) 0);
    }
    
    /**
     * Gets the host address bytes.
     * 
     * @return the host address
     */
    public byte[] getHost() {
        return host.clone();
    }
    
    /**
     * Sets the host address.
     * 
     * @param host the host address (16 bytes)
     */
    public void setHost(byte[] host) {
        if (host.length != 16) {
            throw new IllegalArgumentException("Host address must be 16 bytes (IPv6 format)");
        }
        System.arraycopy(host, 0, this.host, 0, 16);
    }
    
    /**
     * Gets the port.
     * 
     * @return the port
     */
    public short getPort() {
        return port;
    }
    
    /**
     * Sets the port.
     * 
     * @param port the port
     */
    public void setPort(short port) {
        this.port = port;
    }
    
    /**
     * Gets the IPv6 scope ID.
     * 
     * @return the scope ID
     */
    public short getScopeId() {
        return scopeId;
    }
    
    /**
     * Sets the IPv6 scope ID.
     * 
     * @param scopeId the scope ID
     */
    public void setScopeId(short scopeId) {
        this.scopeId = scopeId;
    }
    
    /**
     * Returns a string representation of this address.
     * 
     * @return a string representation
     */
    @Override
    public String toString() {
        // Check if this is an IPv4-mapped address
        boolean isIPv4Mapped = true;
        for (int i = 0; i < 10; i++) {
            if (host[i] != 0) {
                isIPv4Mapped = false;
                break;
            }
        }
        if (isIPv4Mapped && host[10] == (byte)0xff && host[11] == (byte)0xff) {
            // Format as IPv4
            return String.format("%d.%d.%d.%d:%d", 
                    host[12] & 0xFF, 
                    host[13] & 0xFF, 
                    host[14] & 0xFF, 
                    host[15] & 0xFF, 
                    port & 0xFFFF);
        } else {
            // Format as IPv6
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 16; i += 2) {
                if (i > 0) sb.append(":");
                sb.append(String.format("%02x%02x", host[i], host[i+1]));
            }
            if (scopeId != 0) {
                sb.append("%").append(scopeId);
            }
            sb.append(":").append(port & 0xFFFF);
            return sb.toString();
        }
    }
}