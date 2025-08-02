package logykk.enet;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the ENet JNI bindings.
 */
public class ENetLibraryTest {
    
    /**
     * Test that the library can be initialized and deinitialized.
     */
    @Test
    public void testInitializeAndDeinitialize() {
        int result = ENetLibrary.initialize();
        assertEquals(0, result, "ENet initialization should succeed");
        
        // Deinitialize should not throw any exceptions
        ENetLibrary.deinitialize();
    }
    
    /**
     * Test creating and destroying a host.
     */
    @Test
    public void testHostCreateAndDestroy() {
        ENetLibrary.initialize();
        
        try {
            // Create a client (no bind address)
            long clientHost = ENetLibrary.hostCreate(null, 1, 2, 0, 0);
            assertNotEquals(0, clientHost, "Client host creation should succeed");
            
            // Create a server
            ENetAddress serverAddress = ENetAddress.createIPv4((byte)127, (byte)0, (byte)0, (byte)1, (short)7777);
            long serverHost = ENetLibrary.hostCreate(serverAddress, 32, 2, 0, 0);
            assertNotEquals(0, serverHost, "Server host creation should succeed");
            
            // Clean up
            ENetLibrary.hostDestroy(clientHost);
            ENetLibrary.hostDestroy(serverHost);
        } finally {
            ENetLibrary.deinitialize();
        }
    }
    
    /**
     * Test creating and destroying a packet.
     */
    @Test
    public void testPacketCreateAndDestroy() {
        ENetLibrary.initialize();
        
        try {
            // Create a packet
            byte[] data = "Hello, ENet!".getBytes();
            long packet = ENetLibrary.packetCreate(data, data.length, ENetLibrary.ENET_PACKET_FLAG_RELIABLE);
            assertNotEquals(0, packet, "Packet creation should succeed");
            
            // Clean up
            ENetLibrary.packetDestroy(packet);
        } finally {
            ENetLibrary.deinitialize();
        }
    }
    
    /**
     * Test the ENetAddress class.
     */
    @Test
    public void testENetAddress() {
        // Create an IPv4 address
        ENetAddress ipv4 = ENetAddress.createIPv4((byte)192, (byte)168, (byte)0, (byte)1, (short)8080);
        assertEquals(8080, ipv4.getPort() & 0xFFFF, "Port should be set correctly");
        
        // Check string representation
        String addrStr = ipv4.toString();
        assertTrue(addrStr.startsWith("192.168.0.1:"), "IPv4 address should be formatted correctly");
        
        // Create a custom address
        byte[] host = new byte[16];
        host[15] = 1; // ::1 (localhost)
        ENetAddress ipv6 = new ENetAddress(host, (short)9000, (short)0);
        assertEquals(9000, ipv6.getPort() & 0xFFFF, "Port should be set correctly");
    }
}